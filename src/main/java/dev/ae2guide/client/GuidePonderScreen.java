package dev.ae2guide.client;

import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;

final class GuidePonderScreen extends PonderUI {
    private final GuideScreen catalogue;
    private final GuideBook.Lesson lesson;

    GuidePonderScreen(GuideScreen catalogue, GuideBook.Lesson lesson) {
        super(PonderIndex.getSceneAccess().compile(List.of(GuidePlugin.entry(lesson.id()))));
        this.catalogue = catalogue;
        this.lesson = lesson;
    }

    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("ae2ponder.button.catalogue"), button -> onClose())
                .bounds(width - 108, 5, 102, 20).build());
        var lessons = GuideBook.lessons();
        int next = lessons.indexOf(lesson) + 1;
        if (next < lessons.size()) addRenderableWidget(Button.builder(Component.translatable("ae2ponder.button.next"),
                button -> minecraft.setScreen(new GuidePonderScreen(catalogue, lessons.get(next))))
                .bounds(width - 108, 29, 102, 20).build());
    }

    @Override public void onClose() { minecraft.setScreen(catalogue); }
}
