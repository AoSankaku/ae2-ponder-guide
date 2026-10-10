package dev.ae2guide.client;

import dev.ae2guide.mixin.PonderUIAccessor;
import net.createmod.catnip.gui.NavigatableSimiScreen;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;

/** All lessons use Ponder's own arrows, next-up prompt and history navigation. */
public final class GuidePonderScreen extends PonderUI {
    public GuidePonderScreen(GuideBook.Lesson lesson) {
        super(PonderIndex.getSceneAccess().compile(GuidePlugin.course()));
        int index = GuideBook.lessons().indexOf(lesson);
        if (index < 0) throw new IllegalArgumentException("Unknown guide lesson: " + lesson.id());
        var access = (PonderUIAccessor) (Object) this;
        access.ae2ponder$setIndex(index);
        access.ae2ponder$getLazyIndex().startWithValue(index);
    }

    public static void open(GuideBook.Lesson lesson) {
        var screen = new GuidePonderScreen(lesson);
        // Catnip's transition framebuffer is intended for its own navigatable screens.
        // Ordinary catalogue/GuideME screens use the same history without that animation.
        if (Minecraft.getInstance().screen instanceof NavigatableSimiScreen)
            ScreenOpener.transitionTo(screen);
        else
            ScreenOpener.open(screen);
    }
}
