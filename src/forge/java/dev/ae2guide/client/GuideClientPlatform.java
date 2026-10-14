package dev.ae2guide.client;

import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;

/** Client API differences hidden from the shared guide UI. */
final class GuideClientPlatform {
    private GuideClientPlatform() {}

    static boolean isOptionsScreen(Screen screen) { return screen instanceof OptionsScreen; }
}
