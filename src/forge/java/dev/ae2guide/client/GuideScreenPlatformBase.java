package dev.ae2guide.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Adapts the Minecraft 1.20 scroll callback to the shared catalogue screen. */
abstract class GuideScreenPlatformBase extends Screen {
    protected GuideScreenPlatformBase(Component title) { super(title); }

    protected boolean onGuideMouseScrolled(double x, double y, double scrollY) { return false; }

    @Override public boolean mouseScrolled(double x, double y, double delta) {
        return onGuideMouseScrolled(x, y, delta) || super.mouseScrolled(x, y, delta);
    }
}
