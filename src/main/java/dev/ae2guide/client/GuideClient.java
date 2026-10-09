package dev.ae2guide.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ae2guide.AE2PonderGuide;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = AE2PonderGuide.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GuideClient {
    private static final KeyMapping OPEN_GUIDE = new KeyMapping("ae2ponder.key.open",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, "ae2ponder.key.category");

    private GuideClient() {}

    @SubscribeEvent public static void registerKeys(RegisterKeyMappingsEvent event) { event.register(OPEN_GUIDE); }

    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new GuidePlugin());
        event.enqueueWork(() -> {
            NeoForge.EVENT_BUS.addListener(GuideClient::tick);
            NeoForge.EVENT_BUS.addListener(GuideClient::screenButtons);
        });
    }

    private static void tick(ClientTickEvent.Post event) {
        var client = Minecraft.getInstance();
        while (OPEN_GUIDE.consumeClick()) {
            if (client.level != null && client.screen == null) client.setScreen(new GuideScreen(null));
        }
        if (client.screen instanceof PonderUI ui && ui.getActiveScene().isFinished()
                && ui.getActiveScene().getId().getNamespace().equals(AE2PonderGuide.MOD_ID)) {
            GuideProgress.complete(ui.getActiveScene().getId().getPath());
        }
    }

    private static void screenButtons(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> || event.getScreen() instanceof PauseScreen) {
            var parent = event.getScreen();
            event.addListener(Button.builder(Component.translatable("ae2ponder.button.guide"),
                    button -> Minecraft.getInstance().setScreen(new GuideScreen(parent)))
                    .bounds(parent.width - 108, 5, 102, 20).build());
        }
    }
}
