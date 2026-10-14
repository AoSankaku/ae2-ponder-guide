package dev.ae2guide.client;

import dev.ae2guide.GuideConstants;
import dev.ae2guide.GuideResources;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class GuideClient {
    private GuideClient() {}

    static void setup() {
        PonderIndex.addPlugin(new GuidePlugin());
    }

    static void tick() {
        var client = Minecraft.getInstance();
        if (client.screen instanceof PonderUI ui && ui.getActiveScene().isFinished()
                && ui.getActiveScene().getId().getNamespace().equals(GuideConstants.MOD_ID)) {
            GuideProgress.complete(ui.getActiveScene().getId().getPath());
        }
    }

    static Button guideButton(Screen screen) {
        return GuideClientPlatform.isOptionsScreen(screen) ? new GuideButton(screen) : null;
    }

    private static final class GuideButton extends Button {
        private final ItemStack icon = new ItemStack(BuiltInRegistries.ITEM.get(GuideResources.parse("ae2:fluix_crystal")));

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
