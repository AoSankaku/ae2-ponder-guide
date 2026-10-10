package dev.ae2guide.validation;

import dev.ae2guide.client.GuideBook;
import dev.ae2guide.client.GuideChapter;
import dev.ae2guide.client.GuidePonderScreen;
import guideme.Guides;
import guideme.GuidesCommon;
import guideme.PageAnchor;
import guideme.document.LytPoint;
import guideme.document.block.LytDocument;
import guideme.document.block.LytVisitor;
import guideme.document.flow.LytFlowContainer;
import guideme.document.flow.LytFlowContent;
import guideme.document.flow.LytFlowLink;
import guideme.internal.screen.DocumentScreen;
import guideme.internal.screen.IndepentScaleScreen;
import guideme.ui.GuideUiHost;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderButton;
import net.createmod.ponder.foundation.ui.PonderTagScreen;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Tests actual widgets and GuideME links in the isolated, hidden client. */
final class GuideUiValidation {
    private static final ResourceLocation GUIDE = ResourceLocation.parse("ae2:guide");
    private static final ResourceLocation PAGE = ResourceLocation.parse("ae2ponder:ponder.md");
    private static int layouts;
    private static Screen meGuide;

    private GuideUiValidation() {}

    static int layouts() { return layouts; }

    static void tick(int frame) throws Exception {
        frame -= 40;
        var client = Minecraft.getInstance();
        if (frame == 22) checkTagPages();
        if (frame == 23) GLFW.glfwSetWindowSize(client.getWindow().getWindow(), 640, 480);
        if (frame == 26) client.setScreen(new PonderTagScreen(ResourceLocation.parse("ae2ponder:advanced")));
        if (frame == 28) {
            requireInside(client.screen.children().stream().filter(AbstractWidget.class::isInstance)
                    .map(AbstractWidget.class::cast).toList(), client.screen.width, client.screen.height);
            screenshot("tags-small-first.png");
        }
        if (frame == 29) pageButton((PonderTagScreen) client.screen, ">").onPress();
        if (frame == 32) screenshot("tags-small-next.png");
        if (frame == 33) GLFW.glfwSetWindowSize(client.getWindow().getWindow(), 960, 640);
        if (frame == 36) client.setScreen(new PonderTagScreen(ResourceLocation.parse("ae2ponder:advanced")));
        if (frame == 38) screenshot("tags-advanced.png");
        if (frame == 40) {
            var guide = Guides.getById(GUIDE);
            if (guide == null || !guide.pageExists(PAGE) || !"ja_jp".equals(guide.getParsedPage(PAGE).getLanguage()))
                throw new IllegalStateException("Japanese Ponder page missing from ME Guide");
            GuidesCommon.openGuide(client.player, GUIDE, PageAnchor.page(PAGE));
            // The hidden window starts with its pointer at the sidebar's auto-open hotspot.
            // Update Minecraft's own mouse state without moving the user's desktop pointer.
            var window = client.getWindow();
            var onMove = net.minecraft.client.MouseHandler.class.getDeclaredMethod("onMove", long.class, double.class, double.class);
            onMove.setAccessible(true);
            onMove.invoke(client.mouseHandler, window.getWindow(), window.getScreenWidth() * 0.8, window.getScreenHeight() * 0.5);
        }
        if (frame == 44) {
            if (!(client.screen instanceof GuideUiHost)) throw new IllegalStateException("ME Guide did not open");
            meGuide = client.screen;
            checkCompiledGuideContents(meGuide);
            screenshot("me-guide-ponder.png");
        }
        if (frame == 45) clickFirstGuideLink();
        if (frame == 47) {
            requireLesson(GuideBook.lessons().getFirst().id());
            checkCourseOrder((PonderUI) client.screen);
        }
        if (frame == 70) clickArrow(true);
        if (frame == 72) requireLesson(GuideBook.lessons().get(1).id());
        if (frame == 77) clickArrow(false);
        if (frame == 78) requireLesson(GuideBook.lessons().getFirst().id());
        if (frame == 82) clickBack();
        if (frame == 84 && client.screen != meGuide)
            throw new IllegalStateException("Native back button did not restore ME Guide");
        if (frame == 85) client.player.connection.sendCommand("ae2ponder lesson first_network");
        if (frame == 87) requireLesson("first_network");
        if (frame == 110) clickArrow(false);
        if (frame == 111) {
            int index = GuideBook.lessons().stream().map(GuideBook.Lesson::id).toList().indexOf("first_network");
            requireLesson(GuideBook.lessons().get(index - 1).id());
        }
        if (frame == 116) clickArrow(true);
        if (frame == 117) requireLesson("first_network");
        if (frame == 118) {
            var ui = (PonderUI) client.screen;
            ui.seekToTime(45);
        }
        if (frame == 121) screenshot("native-course-navigation.png");
        if (frame == 124) clickBack();
        if (frame == 126) {
            if (client.screen != meGuide) throw new IllegalStateException("Selected lesson did not return to ME Guide");
            client.player.connection.sendCommand("ae2ponder lesson " + GuideBook.lessons().getLast().id());
        }
        if (frame == 128) requireLesson(GuideBook.lessons().getLast().id());
        if (frame == 151) {
            requireLesson(GuideBook.lessons().getLast().id());
            clickArrow(true);
            requireLesson(GuideBook.lessons().getLast().id());
        }
        if (frame == 152) clickBack();
        if (frame == 154 && client.screen != meGuide)
            throw new IllegalStateException("Last lesson lost the GuideME return screen");
    }

    static void checkTagPages() {
        var client = Minecraft.getInstance();
        for (var size : List.of(new int[]{320, 240}, new int[]{427, 260}, new int[]{640, 360})) {
            for (var chapter : GuideChapter.active(GuideBook.lessons())) {
                var tag = PonderIndex.getTagAccess().getRegisteredTag(ResourceLocation.fromNamespaceAndPath("ae2ponder", chapter.id()));
                var screen = new PonderTagScreen(tag);
                client.setScreen(screen);
                screen.init(client, size[0], size[1]);
                Set<ResourceLocation> expected = new HashSet<>(PonderIndex.getTagAccess().getItems(tag));
                if (!tag.getMainItem().isEmpty()) expected.add(BuiltInRegistries.ITEM.getKey(tag.getMainItem().getItem()));
                Set<ResourceLocation> seen = new HashSet<>();
                int pages = 0;
                do {
                    requireInside(screen.children().stream().filter(AbstractWidget.class::isInstance)
                            .map(AbstractWidget.class::cast).toList(), size[0], size[1]);
                    for (var child : screen.children()) {
                        if (child instanceof PonderButton button && button.getItem() != null && !button.getItem().isEmpty()) {
                            if (!seen.add(BuiltInRegistries.ITEM.getKey(button.getItem().getItem())))
                                throw new IllegalStateException("Duplicate item across tag pages: " + chapter.id());
                        }
                    }
                    if (!pageButton(screen, ">").active) break;
                    pageButton(screen, ">").onPress();
                    if (++pages > expected.size()) throw new IllegalStateException("Tag pagination does not terminate");
                } while (true);
                if (!seen.equals(expected)) throw new IllegalStateException("Missing related items: " + chapter.id());
                // Resize while on the last page; the page index must be clamped to its new range.
                screen.init(client, 640, 480);
                requireInside(screen.children().stream().filter(AbstractWidget.class::isInstance)
                        .map(AbstractWidget.class::cast).toList(), 640, 480);
                if (expected.size() > 0 && screen.children().stream().noneMatch(child -> child instanceof PonderButton))
                    throw new IllegalStateException("Resize stranded an empty tag page");
                layouts++;
            }
        }
    }

    private static void requireInside(List<AbstractWidget> widgets, int width, int height) {
        for (var widget : widgets) {
            if (widget.getX() < 0 || widget.getY() < 0 || widget.getX() + widget.getWidth() > width
                    || widget.getY() + widget.getHeight() > height)
                throw new IllegalStateException("Widget outside " + width + "x" + height + ": " + widget.getMessage().getString());
        }
    }

    private static Button pageButton(PonderTagScreen screen, String label) {
        return screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                .filter(button -> button.getMessage().getString().equals(label)).findFirst().orElseThrow();
    }

    static void requireLesson(String id) {
        var screen = Minecraft.getInstance().screen;
        if (!(screen instanceof GuidePonderScreen ui) || !ui.getActiveScene().getId().getPath().equals(id))
            throw new IllegalStateException("Wrong initial or navigated Ponder lesson: " + id + "; screen=" + screen.getClass().getSimpleName()
                    + (screen instanceof PonderUI ui ? "; actual=" + ui.getActiveScene().getId() : ""));
        if (screen.children().stream().anyMatch(Button.class::isInstance))
            throw new IllegalStateException("Custom lesson navigation button remains");
    }

    private static void checkCourseOrder(PonderUI ui) throws ReflectiveOperationException {
        var field = PonderUI.class.getDeclaredField("scenes");
        field.setAccessible(true);
        @SuppressWarnings("unchecked") var scenes = (List<PonderScene>) field.get(ui);
        if (!scenes.stream().map(scene -> scene.getId().getPath()).toList()
                .equals(GuideBook.lessons().stream().map(GuideBook.Lesson::id).toList()))
            throw new IllegalStateException("Native course omitted or reordered lessons");
    }

    static void clickArrow(boolean forward) {
        var screen = Minecraft.getInstance().screen;
        screen.mouseClicked(screen.width / 2 + (forward ? 28 : -28), screen.height - 41, 0);
    }

    static void clickBack() {
        var screen = Minecraft.getInstance().screen;
        screen.mouseClicked(41, screen.height - 41, 0);
    }

    static void checkCompiledGuideContents(Screen screen) throws ReflectiveOperationException {
        if (!(screen instanceof DocumentScreen)) throw new IllegalStateException("Expected a compiled ME Guide page");
        var getter = DocumentScreen.class.getDeclaredMethod("getDocumentWithLayout");
        getter.setAccessible(true);
        var document = (LytDocument) getter.invoke(screen);
        int[] links = {0};
        var text = new StringBuilder();
        document.visit(new LytVisitor() {
            @Override public Result beforeFlowContent(LytFlowContent content) {
                if (content instanceof LytFlowLink) links[0]++;
                if (content instanceof guideme.document.flow.LytFlowText flowText)
                    text.append(flowText.getText()).append(' ');
                return Result.CONTINUE;
            }
        });
        int expectedLinks = GuideBook.lessons().size() + 1; // One start link and one per available lesson.
        if (links[0] != expectedLinks)
            throw new IllegalStateException("Compiled ME Guide has " + links[0] + " links; expected " + expectedLinks);

        var availableIds = GuideBook.lessons().stream().map(GuideBook.Lesson::id).collect(java.util.stream.Collectors.toSet());
        for (var lesson : GuideBook.allLessons()) {
            if (availableIds.contains(lesson.id())) continue;
            for (String key : List.of("ae2ponder.ponder." + lesson.id() + ".header",
                    "ae2ponder.lesson." + lesson.id() + ".summary")) {
                String hiddenText = Component.translatable(key).getString();
                if (!hiddenText.isBlank() && text.indexOf(hiddenText) >= 0)
                    throw new IllegalStateException("Unavailable add-on text leaked into compiled ME Guide: " + lesson.id());
            }
        }
    }

    private static void clickFirstGuideLink() throws ReflectiveOperationException {
        var screen = Minecraft.getInstance().screen;
        var getter = DocumentScreen.class.getDeclaredMethod("getDocumentWithLayout");
        getter.setAccessible(true);
        var document = (LytDocument) getter.invoke(screen);
        LytFlowLink[] first = {null};
        document.visit(new LytVisitor() {
            @Override public Result beforeFlowContent(LytFlowContent content) {
                if (first[0] == null && content instanceof LytFlowLink link) first[0] = link;
                return Result.CONTINUE;
            }
        });
        if (first[0] == null) throw new IllegalStateException("ME Guide has no Ponder link");
        var container = (LytFlowContainer) first[0].getParent();
        var rect = first[0].getChildren().stream().flatMap(child -> container.enumerateContentBounds(child))
                .filter(bounds -> !bounds.isEmpty()).findFirst().orElseThrow();
        var point = ((GuideUiHost) screen).getScreenPoint(new LytPoint(rect.x() + rect.width() / 2f, rect.y() + rect.height() / 2f));
        double scale = ((IndepentScaleScreen) screen).getEffectiveScale();
        var child = screen.getChildAt(point.x() * scale, point.y() * scale);
        if (child.isPresent() && child.get().getClass().getSimpleName().equals("GuideNavBar"))
            throw new IllegalStateException("ME Guide sidebar intercepted Ponder link at " + point);
        if (!screen.mouseClicked(point.x() * scale, point.y() * scale, 0))
            throw new IllegalStateException("ME Guide Ponder link did not handle its click");
    }

    private static void screenshot(String name) {
        var client = Minecraft.getInstance();
        Screenshot.grab(client.gameDirectory, name, client.getMainRenderTarget(), message -> {});
    }
}
