package dev.ae2guide.validation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.ae2guide.client.GuideBook;
import dev.ae2guide.client.GuidePonderScreen;
import dev.ae2guide.client.GuideScreen;
import guideme.Guides;
import guideme.GuidesCommon;
import guideme.PageAnchor;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Verifies all installed locale resources; samples full page/font reloads in one hidden client. */
final class GuideLanguageValidation {
    private static final ResourceLocation GUIDE = ResourceLocation.parse("ae2:guide");
    private static final ResourceLocation PAGE = ResourceLocation.parse("ae2ponder:ponder.md");
    private static final List<String> SAMPLES = List.of("hi_in", "zh_hk", "pt_pt");
    private static int checkedLanguages;
    private static String originalLanguage;
    private static int sample;
    private static int frame;
    private static CompletableFuture<Void> reload;

    private GuideLanguageValidation() {}

    static int checkedLanguages() { return checkedLanguages; }

    static boolean tick() throws Exception {
        var client = Minecraft.getInstance();
        if (originalLanguage == null) {
            originalLanguage = client.getLanguageManager().getSelected();
            verifyAllLanguages();
            beginReload(SAMPLES.getFirst());
            return false;
        }
        if (reload != null) {
            if (!reload.isDone()) return false;
            reload.join();
            reload = null;
            if (sample == SAMPLES.size()) return true;
            if (!Guides.getById(GUIDE).getParsedPage(PAGE).getLanguage().equals(SAMPLES.get(sample)))
                throw new IllegalStateException("GuideME did not select its translated page: " + SAMPLES.get(sample));
            GuidesCommon.openGuide(client.player, GUIDE, PageAnchor.page(PAGE));
            frame = 0;
        }
        frame++;
        String locale = SAMPLES.get(sample);
        if (frame == 4) {
            screenshot("guide-" + locale + ".png");
            GuideUiValidation.checkCompiledGuideContents(client.screen);
            client.setScreen(new GuideScreen(client.screen));
        }
        if (frame == 7) {
            screenshot("catalogue-" + locale + ".png");
            GuidePonderScreen.open(GuideBook.lessons().stream().filter(lesson -> lesson.id().equals("first_network")).findFirst().orElseThrow());
        }
        if (frame == 9) {
            GuideUiValidation.requireLesson("first_network");
            ((PonderUI) client.screen).seekToTime(45);
        }
        if (frame == 30) screenshot("ponder-" + locale + ".png");
        if (frame == 31) {
            sample++;
            beginReload(sample == SAMPLES.size() ? originalLanguage : SAMPLES.get(sample));
        }
        return false;
    }

    private static void beginReload(String locale) {
        var client = Minecraft.getInstance();
        client.getLanguageManager().setSelected(locale);
        client.options.languageCode = locale;
        reload = client.reloadResourcePacks();
    }

    private static void verifyAllLanguages() throws Exception {
        var client = Minecraft.getInstance();
        var languages = read(ResourceLocation.parse("ae2ponder:guide/languages.json"));
        if (languages.size() != 15) throw new IllegalStateException("Missing supported languages in the release");
        var field = GuideScreen.class.getDeclaredField("filtered");
        field.setAccessible(true);
        for (String locale : languages.keySet()) {
            if (client.getLanguageManager().getLanguage(locale) == null)
                throw new IllegalStateException("Unknown Minecraft locale: " + locale);
            client.getLanguageManager().setSelected(locale);
            client.options.languageCode = locale;
            // Cheap language-only reload; GuideME/font reloads are exercised for three samples below.
            client.getLanguageManager().onResourceManagerReload(client.getResourceManager());
            var own = read(ResourceLocation.fromNamespaceAndPath("ae2ponder", "lang/" + locale + ".json"));
            var nativeText = read(ResourceLocation.fromNamespaceAndPath("ponder", "lang/" + locale + ".json"));
            var guideMe = read(ResourceLocation.fromNamespaceAndPath("guideme", "lang/" + locale + ".json"));
            for (var translations : List.of(own, nativeText, guideMe)) {
                for (var entry : translations.entrySet()) {
                    if (!Language.getInstance().getOrDefault(entry.getKey()).equals(entry.getValue()))
                        throw new IllegalStateException("Runtime translation fell back: " + locale + "/" + entry.getKey());
                }
            }
            if (!Ponder.lang().translate("ui.next").string().equals(nativeText.get("ponder.ui.next")))
                throw new IllegalStateException("Ponder controls did not use the selected language: " + locale);
            var catalogue = new GuideScreen(null);
            client.setScreen(catalogue);
            var search = catalogue.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).findFirst().orElseThrow();
            search.setValue(own.get("ae2ponder.ponder.meteorite.header"));
            @SuppressWarnings("unchecked") var found = (List<GuideBook.Lesson>) field.get(catalogue);
            if (found.size() != 1 || !found.getFirst().id().equals("meteorite"))
                throw new IllegalStateException("Catalogue cannot search localized lesson names: " + locale);
            var addonLesson = GuideBook.lessons().stream()
                    .filter(lesson -> lesson.chapter().equals("addons")).findFirst();
            if (addonLesson.isPresent()) {
                String addonTitle = own.get("ae2ponder.ponder." + addonLesson.get().id() + ".header");
                if (addonTitle == null || addonTitle.isBlank())
                    throw new IllegalStateException("Localized add-on lesson title is missing: " + locale);
                search.setValue(addonTitle);
                @SuppressWarnings("unchecked") var addonResults = (List<GuideBook.Lesson>) field.get(catalogue);
                if (addonResults.stream().noneMatch(lesson -> lesson.id().equals(addonLesson.get().id())))
                    throw new IllegalStateException("Catalogue cannot search localized add-on titles: " + locale);
            }
            GuideUiValidation.checkTagPages();
            String prefix = locale.equals("en_us") ? "" : "_" + locale + "/";
            readPage(ResourceLocation.fromNamespaceAndPath("ae2ponder", "ae2guide/" + prefix + "ponder.md"));
            checkedLanguages++;
        }
    }

    private static Map<String, String> read(ResourceLocation id) throws Exception {
        var resources = Minecraft.getInstance().getResourceManager();
        try (var reader = new InputStreamReader(resources.getResourceOrThrow(id).open(), StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
        }
    }

    private static void readPage(ResourceLocation id) throws Exception {
        var resources = Minecraft.getInstance().getResourceManager();
        try (var input = resources.getResourceOrThrow(id).open()) {
            String page = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            for (var lesson : GuideBook.allLessons()) {
                if (!page.contains("command=\"/ae2ponder lesson " + lesson.id() + "\""))
                    throw new IllegalStateException("Localized ME Guide missed a lesson link: " + id + "/" + lesson.id());
            }
        }
    }

    private static void screenshot(String name) {
        var client = Minecraft.getInstance();
        Screenshot.grab(client.gameDirectory, name, client.getMainRenderTarget(), message -> {});
    }
}
