package dev.ae2guide.client;

import dev.ae2guide.AE2PonderGuide;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = AE2PonderGuide.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GuideClient {
    private GuideClient() {}

    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new GuidePlugin());
        event.enqueueWork(() -> {
            NeoForge.EVENT_BUS.addListener(GuideClient::tick);
            NeoForge.EVENT_BUS.addListener(GuideClient::screenButtons);
        });
    }

    private static void tick(ClientTickEvent.Post event) {
        var client = Minecraft.getInstance();
        if (client.screen instanceof PonderUI ui && ui.getActiveScene().isFinished()
                && ui.getActiveScene().getId().getNamespace().equals(AE2PonderGuide.MOD_ID)) {
            GuideProgress.complete(ui.getActiveScene().getId().getPath());
        }
    }

    private static void screenButtons(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof OptionsScreen) event.addListener(new GuideButton(event.getScreen()));
    }

    private static final class GuideButton extends Button {
        private final ItemStack icon = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("ae2:fluix_crystal")));

        private GuideButton(Screen parent) {
            super(8, parent.height - 28, 20, 20, Component.empty(),
                    button -> Minecraft.getInstance().setScreen(new GuideScreen(parent)),
                    message -> Component.translatable("ae2ponder.button.guide"));
            // Ponder's virtual world requires the current world's registry access.
            active = Minecraft.getInstance().level != null;
            setTooltip(Tooltip.create(Component.translatable(active
                    ? "ae2ponder.button.guide" : "ae2ponder.button.requires_world")));
        }

        @Override protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            graphics.renderItem(icon, getX() + 2, getY() + 2);
        }
    }
}
