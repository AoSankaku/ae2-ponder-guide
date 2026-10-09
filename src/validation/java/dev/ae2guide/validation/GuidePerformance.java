package dev.ae2guide.validation;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Opt-in normal-world profiling; only opens a world copied into this isolated run directory. */
@EventBusSubscriber(modid = "ae2ponder", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GuidePerformance {
    private static boolean opening;
    private static long openingStarted;
    private static long started;
    private static long previousFrame;
    private static long renderStarted;
    private static Recording recording;
    private static final List<Double> FRAMES = new ArrayList<>();
    private static final List<Double> RENDERS = new ArrayList<>();
    private static final List<Double> TICKS = new ArrayList<>();

    private GuidePerformance() {}

    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        if (!Boolean.getBoolean("ae2ponder.profile")) return;
        event.enqueueWork(() -> {
            var client = Minecraft.getInstance();
            GLFW.glfwHideWindow(client.getWindow().getWindow());
            try { Files.deleteIfExists(client.gameDirectory.toPath().resolve("performance-result.json")); }
            catch (Exception exception) { throw new IllegalStateException("Cannot clear old performance result", exception); }
            NeoForge.EVENT_BUS.addListener(GuidePerformance::tick);
            NeoForge.EVENT_BUS.addListener(GuidePerformance::beforeFrame);
            NeoForge.EVENT_BUS.addListener(GuidePerformance::afterFrame);
        });
    }

    private static void tick(ClientTickEvent.Post event) {
        var client = Minecraft.getInstance();
        if (!opening && client.screen instanceof TitleScreen && client.getOverlay() == null) {
            opening = true;
            openingStarted = System.nanoTime();
            client.options.pauseOnLostFocus = false;
            client.createWorldOpenFlows().openWorld("profile-world", client::stop);
        }
        if (opening && started == 0 && System.nanoTime() - openingStarted > 150_000_000_000L) {
            LogUtils.getLogger().error("AE2_GUIDE_PROFILE_FAIL: copied world did not open within 150 seconds");
            client.stop();
            return;
        }
        if (client.level == null || client.player == null || client.screen != null || client.getOverlay() != null) return;
        try {
            if (started == 0) {
                started = System.nanoTime();
                recording = new Recording(Configuration.getConfiguration("profile"));
                recording.start();
                LogUtils.getLogger().info("AE2_GUIDE_PROFILE_START: render distance {}, simulation distance {}, resolution {}x{}",
                        client.options.renderDistance().get(), client.options.simulationDistance().get(),
                        client.getWindow().getWidth(), client.getWindow().getHeight());
            }
            double seconds = (System.nanoTime() - started) / 1e9;
            // Sweep the camera to exercise terrain rendering, without editing or moving through the world.
            client.player.setYRot((float) (seconds * 18));
            if (seconds >= 15) TICKS.add(client.getSingleplayerServer().getAverageTickTimeNanos() / 1e6);
            if (seconds >= 45) {
                recording.stop();
                recording.dump(client.gameDirectory.toPath().resolve("performance.jfr"));
                recording.close();
                var result = new LinkedHashMap<String, Object>();
                result.put("frames", statistics(FRAMES));
                result.put("render", statistics(RENDERS));
                result.put("serverTick", statistics(TICKS));
                result.put("renderDistance", client.options.renderDistance().get());
                result.put("simulationDistance", client.options.simulationDistance().get());
                result.put("resolution", client.getWindow().getWidth() + "x" + client.getWindow().getHeight());
                result.put("javaVersion", System.getProperty("java.version"));
                result.put("javaVendor", System.getProperty("java.vendor"));
                result.put("vsync", client.options.enableVsync().get());
                result.put("fpsLimit", client.options.framerateLimit().get());
                Files.writeString(client.gameDirectory.toPath().resolve("performance-result.json"),
                        new GsonBuilder().setPrettyPrinting().create().toJson(result));
                LogUtils.getLogger().info("AE2_GUIDE_PROFILE_PASS: {}", result);
                client.stop();
            }
        } catch (Exception exception) {
            LogUtils.getLogger().error("AE2_GUIDE_PROFILE_FAIL", exception);
            client.stop();
        }
    }

    private static void beforeFrame(RenderFrameEvent.Pre event) {
        long now = System.nanoTime();
        if (sampling() && previousFrame != 0) FRAMES.add((now - previousFrame) / 1e6);
        previousFrame = now;
        renderStarted = now;
    }

    private static void afterFrame(RenderFrameEvent.Post event) {
        if (sampling()) RENDERS.add((System.nanoTime() - renderStarted) / 1e6);
    }

    private static boolean sampling() {
        return started != 0 && System.nanoTime() - started >= 15_000_000_000L;
    }

    private static Map<String, Object> statistics(List<Double> samples) {
        var sorted = samples.stream().sorted().toList();
        if (sorted.isEmpty()) return Map.of("count", 0);
        return Map.of("count", sorted.size(), "meanMs", sorted.stream().mapToDouble(Double::doubleValue).average().orElseThrow(),
                "medianMs", sorted.get(sorted.size() / 2), "p95Ms", sorted.get((int) ((sorted.size() - 1) * .95)),
                "maxMs", sorted.getLast());
    }
}
