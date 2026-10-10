package dev.ae2guide.validation;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import dev.ae2guide.client.GuideBook;
import dev.ae2guide.client.GuideChapter;
import dev.ae2guide.client.GuidePlugin;
import dev.ae2guide.client.GuideScreen;
import dev.ae2guide.client.GuideScenes;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.element.InputWindowElement;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.WorldDataConfiguration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Opt-in integration harness, excluded from the released mod JAR. */
@EventBusSubscriber(modid = "ae2ponder", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GuideValidation {
    private static boolean opening;
    private static boolean validating;
    private static int frame;
    private static final int SCENE_START = 220;
    private static final List<PonderScene> SCENES = new ArrayList<>();
    private static final List<List<Integer>> CALLOUT_TIMES = new ArrayList<>();
    private static final List<String> CALLOUT_SCREENSHOTS = List.of(
            "first_network", "charged_fluix", "processors", "p2p_channels", "quantum_bridge", "cell_io",
            "omni_cells", "omni_crafting", "bigger_singularity", "bigger_crafting", "things_disks", "mega_fluid_disks");
    private static int verifiedCallouts;

    private GuideValidation() {}

    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        if (!Boolean.getBoolean("ae2ponder.validate")) return;
        event.enqueueWork(() -> {
            var client = Minecraft.getInstance();
            GLFW.glfwHideWindow(client.getWindow().getWindow());
            try {
                Files.deleteIfExists(client.gameDirectory.toPath().resolve("validation-result.json"));
                Files.deleteIfExists(client.gameDirectory.toPath().resolve("validation-failure.txt"));
            }
            catch (Exception exception) { throw new IllegalStateException("Cannot clear old validation result", exception); }
            NeoForge.EVENT_BUS.addListener(GuideValidation::tick);
        });
    }

    private static void tick(ClientTickEvent.Post event) {
        var client = Minecraft.getInstance();
        if (!opening && client.screen instanceof TitleScreen && client.getOverlay() == null) {
            opening = true;
            var rules = new GameRules();
            rules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
            rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
            rules.getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0, null);
            client.createWorldOpenFlows().createFreshLevel("guide-validation-" + System.currentTimeMillis(),
                    new LevelSettings("AE2 Guide Validation", GameType.CREATIVE, false,
                            Difficulty.PEACEFUL, true, rules, WorldDataConfiguration.DEFAULT),
                    new WorldOptions(1L, false, false), registries -> registries.registryOrThrow(Registries.WORLD_PRESET)
                            .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), client.screen);
        }
        if (!opening || client.level == null || client.getOverlay() != null) return;
        if (!validating && (client.screen != null || client.player == null)) return;
        try {
            if (!validating) {
                validating = true;
                validateScenes();
                client.setScreen(new OptionsScreen(null, client.options));
                frame = 0;
            }
            frame++;
            if (frame == 6) screenshot("options-guide-button.png");
            if (frame == 7) {
                if (!(client.screen instanceof OptionsScreen)) throw new IllegalStateException("Options did not open");
                client.screen.mouseClicked(18, client.screen.height - 18, 0);
                if (!(client.screen instanceof GuideScreen)) throw new IllegalStateException("Options guide button did not open catalogue");
            }
            if (frame == 8) screenshot("catalogue.png");
            if (frame == 10) {
                // Exercise the actual catalogue row rather than bypassing its selection handler.
                if (!(client.screen instanceof GuideScreen)) throw new IllegalStateException("Catalogue did not open");
                client.screen.mouseClicked(160, 95, 0);
            }
            if (frame == 11) {
                if (!(client.screen instanceof PonderUI ui)) throw new IllegalStateException("Catalogue row did not open Ponder");
                GuideUiValidation.requireLesson(GuideBook.lessons().getFirst().id());
                ui.seekToTime(Math.min(280, ui.getActiveScene().getTotalTime() - 1));
            }
            // Native controls are hidden during Ponder's opening zoom; wait for that transition.
            if (frame == 31) GuideUiValidation.clickArrow(true);
            if (frame == 32) GuideUiValidation.requireLesson(GuideBook.lessons().get(1).id());
            if (frame == 41) GuideUiValidation.clickArrow(false);
            if (frame == 42) GuideUiValidation.requireLesson(GuideBook.lessons().getFirst().id());
            if (frame == 47) screenshot("first-lesson.png");
            if (frame == 50) GuideUiValidation.clickBack();
            if (frame == 52 && !(client.screen instanceof GuideScreen)) throw new IllegalStateException("Native back did not restore catalogue");
            GuideUiValidation.tick(frame);
            if (frame >= SCENE_START && frame < SCENE_START + SCENES.size() * 12 && (frame - SCENE_START) % 12 == 0) {
                int index = (frame - SCENE_START) / 12;
                client.setScreen(new ValidationPonderScreen(SCENES.get(index), CALLOUT_TIMES.get(index).getFirst()));
            }
            if (frame >= SCENE_START && frame < SCENE_START + SCENES.size() * 12 && client.screen instanceof PonderUI ui) {
                int phase = (frame - SCENE_START) % 12;
                if (phase == 4) {
                    var times = CALLOUT_TIMES.get((frame - SCENE_START) / 12);
                    int target = times.get(times.size() / 2);
                    if (target > ui.getActiveScene().getCurrentTime()) ui.seekToTime(target);
                }
                if (phase == 8) ui.seekToTime(ui.getActiveScene().getTotalTime());
                boolean captureCallouts = SCENES.size() <= 6 || CALLOUT_SCREENSHOTS.contains(ui.getActiveScene().getId().getPath());
                if (phase == 3 && (captureCallouts || ((frame - SCENE_START) / 12) % 6 == 0))
                    screenshot("lesson-" + ui.getActiveScene().getId().getPath() + ".png");
                if (phase == 6 && captureCallouts)
                    screenshot("callout-" + ui.getActiveScene().getId().getPath() + ".png");
            }
            if (frame >= SCENE_START + SCENES.size() * 12) {
                if (Boolean.getBoolean("ae2ponder.validateLanguages") && !GuideLanguageValidation.tick()) return;
                var result = client.gameDirectory.toPath().resolve("validation-result.json");
                Files.writeString(result, new Gson().toJson(java.util.Map.of("passed", true,
                        "lessons", SCENES.size(), "rendered", SCENES.size(), "callouts", verifiedCallouts,
                        "tagLayouts", GuideUiValidation.layouts(), "nativeNavigation", true, "guideMeLink", true,
                        "languages", GuideLanguageValidation.checkedLanguages(), "createInstalled", false,
                        "loadedAddons", List.of("ae2omnicells", "bigger_ae2", "ae2things", "ae2_mega_things")
                                .stream().filter(ModList.get()::isLoaded).toList())));
                LogUtils.getLogger().info("AE2_GUIDE_VALIDATION_PASS: {} storyboards compiled, replayed and rendered; {} item callouts verified",
                        SCENES.size(), verifiedCallouts);
                client.stop();
            }
        } catch (Throwable exception) {
            LogUtils.getLogger().error("AE2_GUIDE_VALIDATION_FAIL", exception);
            try { Files.writeString(client.gameDirectory.toPath().resolve("validation-failure.txt"), exception.toString()); }
            catch (Exception writeFailure) { LogUtils.getLogger().error("Cannot write validation failure", writeFailure); }
            client.stop();
        }
    }

    private static void validateScenes() {
        if (net.neoforged.fml.ModList.get().isLoaded("create")) throw new IllegalStateException("Create must be absent during validation");
        validateOptionalLessons();
        String selection = System.getProperty("ae2ponder.validationLessons", "");
        var selectedIds = selection.isBlank() ? List.<String>of() : List.of(selection.split(","));
        var lessons = GuideBook.lessons().stream().filter(lesson -> selectedIds.isEmpty() || selectedIds.contains(lesson.id())).toList();
        if (lessons.isEmpty() || (!selectedIds.isEmpty() && lessons.size() != selectedIds.size()))
            throw new IllegalArgumentException("Unknown validation lesson selection: " + selection);
        for (var lesson : lessons) {
            requireItem(lesson.icon());
            lesson.components().forEach(GuideValidation::requireItem);
            for (var component : lesson.components()) {
                if (!PonderIndex.getSceneAccess().doScenesExistForId(ResourceLocation.parse(component)))
                    throw new IllegalStateException("Item has no tooltip Ponder entry: " + component);
            }
            lesson.steps().stream().filter(step -> step.flow() != null).forEach(step -> requireItem(step.flow().item()));
            lesson.steps().stream().flatMap(step -> step.callouts().stream()).forEach(callout -> requireItem(callout.item()));
            var scene = PonderIndex.getSceneAccess().compile(List.of(GuidePlugin.entry(lesson.id()))).getFirst();
            if (scene.getBounds().maxY() != 4 || scene.getBounds().maxX() != 8) throw new IllegalStateException("Incorrect stage bounds");
            scene.seekToTime(scene.getTotalTime());
            checkBlocks(lesson, scene);
            scene.begin();
            CALLOUT_TIMES.add(checkCallouts(lesson, scene));
            scene.seekToTime(scene.getTotalTime());
            checkBlocks(lesson, scene);
            SCENES.add(scene);
            LogUtils.getLogger().info("AE2_GUIDE_SCENE_PASS {} ({} nodes, {} steps)", lesson.id(), lesson.nodes().size(), lesson.steps().size());
        }
    }

    private static void validateOptionalLessons() {
        var supportedAddons = List.of("ae2omnicells", "bigger_ae2", "ae2things", "ae2_mega_things");
        var allLessons = GuideBook.allLessons();
        for (var lesson : allLessons) {
            if (lesson.chapter().equals(GuideChapter.ADDONS.id()) && lesson.requiresMods().isEmpty())
                throw new IllegalStateException("Add-on lesson has no optional mod requirement: " + lesson.id());
            if (!supportedAddons.containsAll(lesson.requiresMods()))
                throw new IllegalStateException("Lesson requires an untested add-on: " + lesson.id());
        }

        for (int mask = 0; mask < 1 << supportedAddons.size(); mask++) {
            var loaded = new java.util.HashSet<String>();
            for (int bit = 0; bit < supportedAddons.size(); bit++)
                if ((mask & (1 << bit)) != 0) loaded.add(supportedAddons.get(bit));
            var expected = allLessons.stream().filter(lesson -> loaded.containsAll(lesson.requiresMods()))
                    .map(GuideBook.Lesson::id).toList();
            var actual = GuideBook.lessonsForMods(loaded).stream().map(GuideBook.Lesson::id).toList();
            if (!actual.equals(expected)) throw new IllegalStateException("Optional lesson filter failed for add-on mask " + mask);
        }

        var loadedAddons = supportedAddons.stream().filter(ModList.get()::isLoaded).collect(java.util.stream.Collectors.toSet());
        boolean expectAddons = Boolean.getBoolean("ae2ponder.expectAddons");
        if (expectAddons && !loadedAddons.equals(Set.copyOf(supportedAddons)))
            throw new IllegalStateException("All four optional add-ons were expected, found " + loadedAddons);
        if (!expectAddons && !loadedAddons.isEmpty())
            throw new IllegalStateException("The no-add-on test configuration unexpectedly loaded " + loadedAddons);

        var expectedAvailable = GuideBook.lessonsForMods(loadedAddons);
        if (!GuideBook.lessons().stream().map(GuideBook.Lesson::id).toList()
                .equals(expectedAvailable.stream().map(GuideBook.Lesson::id).toList()))
            throw new IllegalStateException("Runtime lesson availability disagrees with loaded add-ons");
        var activeChapters = GuideChapter.active(GuideBook.lessons());
        if (activeChapters.contains(GuideChapter.ADDONS) != GuideBook.lessons().stream()
                .anyMatch(lesson -> lesson.chapter().equals(GuideChapter.ADDONS.id())))
            throw new IllegalStateException("The add-on chapter visibility is incorrect");

        var addonLessons = expectedAvailable.stream()
                .filter(lesson -> lesson.chapter().equals(GuideChapter.ADDONS.id())).toList();
        var addonTagId = ResourceLocation.fromNamespaceAndPath("ae2ponder", GuideChapter.ADDONS.id());
        var addonTag = PonderIndex.getTagAccess().getRegisteredTag(addonTagId);
        if (addonLessons.isEmpty()) {
            // Ponder returns its not_registered sentinel for an unknown tag, rather than null.
            if (addonTag.getId().equals(addonTagId)
                    || PonderIndex.getTagAccess().getListedTags().stream().anyMatch(tag -> tag.getId().equals(addonTagId))
                    || !PonderIndex.getTagAccess().getItems(addonTagId).isEmpty())
                throw new IllegalStateException("The add-on chapter is registered without an installed add-on");
        } else {
            if (!addonTag.getId().equals(addonTagId))
                throw new IllegalStateException("The installed add-on chapter was not registered");
            var expectedItems = new java.util.HashSet<ResourceLocation>();
            expectedItems.add(ResourceLocation.parse(GuideChapter.ADDONS.icon()));
            addonLessons.forEach(lesson -> {
                expectedItems.add(ResourceLocation.parse(lesson.icon()));
                lesson.components().stream().map(ResourceLocation::parse).forEach(expectedItems::add);
            });
            var actualItems = new java.util.HashSet<>(PonderIndex.getTagAccess().getItems(addonTag));
            if (!addonTag.getMainItem().isEmpty())
                actualItems.add(BuiltInRegistries.ITEM.getKey(addonTag.getMainItem().getItem()));
            if (!actualItems.equals(expectedItems))
                throw new IllegalStateException("The add-on chapter exposes missing or unavailable related items");
        }

        for (var lesson : expectedAvailable) {
            requireItem(lesson.icon());
            lesson.components().forEach(GuideValidation::requireItem);
            GuidePlugin.entry(lesson.id());
        }
        for (var lesson : allLessons) {
            if (expectedAvailable.contains(lesson)) continue;
            try {
                GuidePlugin.entry(lesson.id());
                throw new IllegalStateException("Unavailable add-on lesson was registered: " + lesson.id());
            } catch (IllegalStateException expected) {
                if (!expected.getMessage().startsWith("Ponder lesson has not been registered:")) throw expected;
            }
        }

        var compiledCourse = PonderIndex.getSceneAccess().compile(GuidePlugin.course()).stream()
                .map(scene -> scene.getId().getPath()).toList();
        if (!compiledCourse.equals(expectedAvailable.stream().map(GuideBook.Lesson::id).toList()))
            throw new IllegalStateException("Native Ponder course includes hidden lessons or has the wrong order");
        LogUtils.getLogger().info("AE2_GUIDE_OPTIONAL_PASS: {} loaded; {} available lessons; 16 mod combinations checked",
                loadedAddons, expectedAvailable.size());
    }

    private static List<Integer> checkCallouts(GuideBook.Lesson lesson, PonderScene scene) {
        try {
            var itemField = InputWindowElement.class.getDeclaredField("item");
            itemField.setAccessible(true);
            var times = new ArrayList<Integer>();
            if (scene.getKeyframeCount() != lesson.steps().size()) throw new IllegalStateException("Missing step keyframes");
            for (int stepIndex = 0; stepIndex < lesson.steps().size(); stepIndex++) {
                var step = lesson.steps().get(stepIndex);
                if (step.callouts().isEmpty()) continue;
                int slotDuration = GuideScenes.captionDuration(lesson, stepIndex) / step.callouts().size();
                for (int slot = 0; slot < step.callouts().size(); slot++) {
                    int time = scene.getKeyframeTime(stepIndex) + 12 + slot * slotDuration + 15;
                    scene.seekToTime(time);
                    var visible = scene.getElements().stream().filter(InputWindowElement.class::isInstance)
                            .map(InputWindowElement.class::cast).filter(InputWindowElement::isVisible).toList();
                    if (visible.size() != 1) throw new IllegalStateException("Missing or overlapping callouts: " + lesson.id() + "/" + stepIndex);
                    var bubble = visible.getFirst();
                    var item = (ItemStack) itemField.get(bubble);
                    if (bubble.getFade(1) < .99f || !BuiltInRegistries.ITEM.getKey(item.getItem()).toString().equals(step.callouts().get(slot).item()))
                        throw new IllegalStateException("Wrong or invisible callout item: " + lesson.id() + "/" + stepIndex + "/" + slot);
                    times.add(time);
                    verifiedCallouts++;
                }
            }
            if (times.isEmpty()) throw new IllegalStateException("No item callouts in " + lesson.id());
            return List.copyOf(times);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot inspect Ponder item callouts", exception);
        }
    }

    private static void checkBlocks(GuideBook.Lesson lesson, PonderScene scene) {
        for (var node : lesson.nodes()) {
            var expected = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(node.block()));
            if (!scene.getWorld().getBlockState(node.position()).is(expected))
                throw new IllegalStateException("Missing stage model: " + lesson.id() + "/" + node.id());
            if (!node.nbt().equals("{}") && scene.getWorld().getBlockEntity(node.position()) == null)
                throw new IllegalStateException("Missing stage block entity: " + lesson.id() + "/" + node.id());
            if (node.block().equals("ae2:cable_bus")) {
                var blockEntity = scene.getWorld().getBlockEntity(node.position());
                var expectedNbt = GuideScenes.parseNbt(node.nbt());
                lesson.steps().forEach(step -> {
                    if (step.nbt().containsKey(node.id())) expectedNbt.merge(GuideScenes.parseNbt(step.nbt().get(node.id())));
                });
                var actual = blockEntity.saveWithoutMetadata(scene.getWorld().registryAccess());
                for (var slot : List.of("cable", "up", "down", "north", "south", "east", "west")) {
                    String id = expectedNbt.getCompound(slot).getString("id");
                    if (!id.isEmpty() && !actual.getCompound(slot).getString("id").equals(id))
                        throw new IllegalStateException("Missing AE2 part: " + lesson.id() + "/" + node.id() + "/" + slot + ": " + id);
                }
                var state = scene.getWorld().getBlockState(node.position());
                var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
                var data = model.getModelData(scene.getWorld(), node.position(), state, scene.getWorld().getModelData(node.position()));
                int quads = 0;
                for (var layer : RenderType.chunkBufferLayers()) {
                    quads += model.getQuads(state, null, RandomSource.create(1L), data, layer).size();
                    for (var direction : Direction.values()) quads += model.getQuads(state, direction, RandomSource.create(1L), data, layer).size();
                }
                if (quads == 0) throw new IllegalStateException("Invisible AE2 cable model: " + lesson.id() + "/" + node.id());
            }
        }
    }

    private static void requireItem(String id) {
        if (!BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(id))) throw new IllegalStateException("Unknown guide item: " + id);
    }

    private static void screenshot(String name) {
        var client = Minecraft.getInstance();
        Screenshot.grab(client.gameDirectory, name, client.getMainRenderTarget(), message -> LogUtils.getLogger().info("{}", message.getString()));
    }

    private static final class ValidationPonderScreen extends PonderUI {
        private final int initialTime;
        private ValidationPonderScreen(PonderScene scene, int initialTime) {
            super(List.of(scene));
            this.initialTime = initialTime;
        }
        @Override protected void init() {
            super.init();
            seekToTime(initialTime);
        }
    }
}
