package dev.ae2guide.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;

/** A linear course and searchable reference; each row opens exactly one storyboard. */
public final class GuideScreen extends Screen {
    private static final int LIST_X = 128;
    private static final int LIST_Y = 84;
    private static final int ROW_HEIGHT = 36;
    private final Screen parent;
    private final List<Button> rows = new ArrayList<>();
    private final Map<String, Button> chapterButtons = new LinkedHashMap<>();
    private List<GuideBook.Lesson> filtered = List.of();
    private EditBox search;
    private Button previous;
    private Button next;
    private String query = "";
    private String chapter = "";
    private int page;
    private int pageSize;

    public GuideScreen(Screen parent) {
        super(Component.translatable("ae2ponder.title"));
        this.parent = parent;
    }

    private static Component localized(GuideBook.Localized text) {
        return Component.literal(Minecraft.getInstance().getLanguageManager().getSelected().startsWith("ja") ? text.ja() : text.en());
    }

    @Override protected void init() {
        rows.clear();
        chapterButtons.clear();
        pageSize = Math.max(1, (height - LIST_Y - 44) / ROW_HEIGHT);
        search = new EditBox(font, LIST_X, 44, width - LIST_X - 16, 20, Component.translatable("ae2ponder.search"));
        search.setHint(Component.translatable("ae2ponder.search"));
        search.setMaxLength(100);
        search.setValue(query);
        search.setResponder(value -> { query = value; page = 0; refresh(); });
        addRenderableWidget(search);
        addChapter("", Component.translatable("ae2ponder.chapter.all"), 76);
        int y = 98;
        for (var value : GuideChapter.values()) {
            addChapter(value.id(), value.label(), y);
            y += 22;
        }
        previous = addRenderableWidget(Button.builder(Component.literal("<"), button -> { page--; refresh(); })
                .bounds(LIST_X, height - 31, 28, 20).build());
        next = addRenderableWidget(Button.builder(Component.literal(">"), button -> { page++; refresh(); })
                .bounds(LIST_X + 32, height - 31, 28, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> onClose())
                .bounds(width - 86, height - 31, 70, 20).build());
        refresh();
    }

    private void addChapter(String id, Component label, int y) {
        chapterButtons.put(id, addRenderableWidget(Button.builder(label, button -> { chapter = id; page = 0; refresh(); })
                .bounds(12, y, 104, 20).build()));
    }

    private void refresh() {
        if (search == null) return;
        chapterButtons.forEach((id, button) -> button.active = !id.equals(chapter));
        rows.forEach(this::removeWidget);
        rows.clear();
        String term = query.strip().toLowerCase(Locale.ROOT);
        filtered = GuideBook.lessons().stream().filter(lesson -> chapter.isEmpty() || chapter.equals(lesson.chapter()))
                .filter(lesson -> matches(lesson, term)).toList();
        page = Math.max(0, Math.min(page, Math.max(0, (filtered.size() - 1) / pageSize)));
        for (int i = page * pageSize; i < Math.min(filtered.size(), (page + 1) * pageSize); i++) {
            var lesson = filtered.get(i);
            int y = LIST_Y + (i % pageSize) * ROW_HEIGHT;
            var row = Button.builder(Component.empty(), button -> minecraft.setScreen(new GuidePonderScreen(this, lesson)))
                    .bounds(LIST_X, y, width - LIST_X - 16, ROW_HEIGHT - 3)
                    .tooltip(Tooltip.create(localized(lesson.summary()))).build();
            rows.add(addRenderableWidget(row));
        }
        previous.active = page > 0;
        next.active = (page + 1) * pageSize < filtered.size();
    }

    private static boolean matches(GuideBook.Lesson lesson, String term) {
        if (term.isEmpty()) return true;
        var text = lesson.title().ja() + " " + lesson.title().en() + " " + lesson.summary().ja() + " "
                + lesson.summary().en() + " " + lesson.id() + " " + String.join(" ", lesson.components());
        text += " " + lesson.steps().stream().map(step -> step.text().ja() + " " + step.text().en())
                .collect(java.util.stream.Collectors.joining(" "));
        return text.toLowerCase(Locale.ROOT).contains(term);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(font, title, 14, 14, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("ae2ponder.subtitle"), 14, 29, 0xA7BDC9);
        long completed = GuideBook.lessons().stream().filter(lesson -> GuideProgress.completed(lesson.id())).count();
        graphics.drawString(font, Component.translatable("ae2ponder.progress", completed, GuideBook.lessons().size()), 14, 54, 0x71DEB3);
        graphics.drawString(font, Component.translatable("ae2ponder.results", filtered.size()), LIST_X, 70, 0xD5E4EC);
        renderRows(graphics);
        if (filtered.isEmpty()) graphics.drawString(font, Component.translatable("ae2ponder.no_results"), LIST_X + 8, LIST_Y + 12, 0xD5E4EC);
        graphics.drawString(font, Component.literal((page + 1) + " / " + Math.max(1, (filtered.size() + pageSize - 1) / pageSize)),
                LIST_X + 70, height - 25, 0xD5E4EC);
    }

    private void renderRows(GuiGraphics graphics) {
        for (int row = 0; row < rows.size(); row++) {
            var lesson = filtered.get(page * pageSize + row);
            int y = LIST_Y + row * ROW_HEIGHT;
            graphics.renderItem(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(lesson.icon()))), LIST_X + 7, y + 8);
            int number = GuideBook.lessons().indexOf(lesson) + 1;
            String prefix = (GuideProgress.completed(lesson.id()) ? "✓ " : "") + String.format(Locale.ROOT, "%02d  ", number);
            String name = prefix + localized(lesson.title()).getString();
            graphics.drawString(font, font.plainSubstrByWidth(name, width - LIST_X - 56), LIST_X + 30, y + 5, 0xFFFFFF);
            graphics.drawString(font, font.plainSubstrByWidth(localized(lesson.summary()).getString(), width - LIST_X - 56),
                    LIST_X + 30, y + 19, 0xBCD0DA);
        }
    }

    @Override public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (x >= LIST_X && scrollY != 0) {
            page += scrollY < 0 ? 1 : -1;
            refresh();
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override public void onClose() { minecraft.setScreen(parent); }
}
