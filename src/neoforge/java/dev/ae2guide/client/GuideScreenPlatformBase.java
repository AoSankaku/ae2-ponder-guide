package dev.ae2guide.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Adapts the Minecraft 1.21 scroll callback to the shared catalogue screen. */
abstract class GuideScreenPlatformBase extends Screen {
    protected GuideScreenPlatformBase(Component title) { super(title); }

    protected boolean onGuideMouseScrolled(double x, double y, double scrollY) { return false; }

    @Override public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        return onGuideMouseScrolled(x, y, scrollY) || super.mouseScrolled(x, y, scrollX, scrollY);
    }
}
