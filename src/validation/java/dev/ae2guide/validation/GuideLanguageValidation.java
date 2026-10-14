package dev.ae2guide.validation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.ae2guide.GuideResources;
import dev.ae2guide.client.GuideBook;
import dev.ae2guide.client.GuidePonderScreen;
import dev.ae2guide.client.GuideScreen;
import dev.ae2guide.client.RightToLeftText;
import guideme.Guides;
import guideme.GuidesCommon;
import guideme.PageAnchor;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Verifies all installed locale resources; samples full page/font reloads in one hidden client. */
final class GuideLanguageValidation {
    private static final ResourceLocation GUIDE = GuideResources.parse("ae2:guide");
    private static final ResourceLocation PAGE = GuideResources.parse("ae2ponder:ponder.md");
    private static final List<String> SAMPLES = List.of("hi_in", "zh_hk", "pt_pt", "ar_sa", "ko_kr", "vi_vn", "tr_tr");
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
            beginReload(SAMPLES.get(0));
            return false;
        }
        if (reload != null) {
            if (!reload.isDone()) return false;
            reload.join();
            reload = null;
            if (sample == SAMPLES.size()) return true;
            if (!Guides.getById(GUIDE).getParsedPage(PAGE).getLanguage().equals(SAMPLES.get(sample)))
                throw new IllegalStateException("GuideME did not select its translated page: " + SAMPLES.get(sample));
            if (Language.getInstance().isDefaultRightToLeft()) {
                verifyRightToLeftWrapping();
            }
            GuidesCommon.openGuide(client.player, GUIDE, PageAnchor.page(PAGE));
            frame = 0;
        }
        frame++;
        String locale = SAMPLES.get(sample);
        if (frame == 4) {
            screenshot("guide-" + locale + ".png");
            GuideUiValidation.checkCompiledGuideContents(client.screen);
            if (Language.getInstance().isDefaultRightToLeft()) verifyArabicSearchText();
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
        var languages = read(GuideResources.parse("ae2ponder:guide/languages.json"));
        if (languages.size() != 22) throw new IllegalStateException("Missing supported languages in the release");
        var field = GuideScreen.class.getDeclaredField("filtered");
        field.setAccessible(true);
        for (String locale : languages.keySet()) {
            if (client.getLanguageManager().getLanguage(locale) == null)
                throw new IllegalStateException("Unknown Minecraft locale: " + locale);
            client.getLanguageManager().setSelected(locale);
            client.options.languageCode = locale;
            // Cheap language-only reload; GuideME/font reloads are exercised for representative scripts below.
            client.getLanguageManager().onResourceManagerReload(client.getResourceManager());
            var own = read(GuideResources.of("ae2ponder", "lang/" + locale + ".json"));
            var nativeText = read(GuideResources.of("ponder", "lang/" + locale + ".json"));
            var guideMe = read(GuideResources.of("guideme", "lang/" + locale + ".json"));
            for (var translations : List.of(own, nativeText, guideMe)) {
                for (var entry : translations.entrySet()) {
                    if (!Language.getInstance().getOrDefault(entry.getKey()).equals(entry.getValue()))
                        throw new IllegalStateException("Runtime translation fell back: " + locale + "/" + entry.getKey());
                }
            }
            String expectedNext = nativeText.get("ponder.ui.next");
            String ponderNext = Ponder.lang().translate("ui.next").string();
            String componentNext = Component.translatable("ponder.ui.next").getString();
            if (expectedNext == null || !ponderNext.equals(expectedNext))
                throw new IllegalStateException("Ponder controls did not use the selected language: " + locale
                        + ", key=ponder.ui.next, merged locale key present=" + nativeText.containsKey("ponder.ui.next")
                        + ", expected=\"" + expectedNext + "\", merged locale stack=\"" + nativeText.get("ponder.ui.next")
                        + "\", Ponder.lang=\"" + ponderNext
                        + "\", Component.translatable=\"" + componentNext + "\"");
            var catalogue = new GuideScreen(null);
            client.setScreen(catalogue);
            var search = catalogue.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).findFirst().orElseThrow();
            search.setValue(own.get("ae2ponder.ponder.meteorite.header"));
            @SuppressWarnings("unchecked") var found = (List<GuideBook.Lesson>) field.get(catalogue);
            if (found.size() != 1 || !found.get(0).id().equals("meteorite"))
                throw new IllegalStateException("Catalogue cannot search localized lesson names: " + locale);
            Locale caseRules = Locale.forLanguageTag(locale.replace('_', '-'));
            for (String lessonId : List.of("first_network", "import_export", "inscriber_automation")) {
                String title = own.get("ae2ponder.ponder." + lessonId + ".header");
                for (String query : List.of(title.toLowerCase(caseRules), title.toUpperCase(caseRules))) {
                    search.setValue(query);
                    @SuppressWarnings("unchecked") var results = (List<GuideBook.Lesson>) field.get(catalogue);
                    if (results.stream().noneMatch(lesson -> lesson.id().equals(lessonId)))
                        throw new IllegalStateException("Catalogue cannot search localized case variants: " + locale + "/" + lessonId);
                }
            }
            search.setValue("IO");
            @SuppressWarnings("unchecked") var englishResults = (List<GuideBook.Lesson>) field.get(catalogue);
            if (englishResults.stream().noneMatch(lesson -> lesson.id().equals("cell_io")))
                throw new IllegalStateException("Catalogue cannot search English/registry terms: " + locale);
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
            readPage(GuideResources.of("ae2ponder", "ae2guide/" + prefix + "ponder.md"));
            checkedLanguages++;
        }
    }

    private static Map<String, String> read(ResourceLocation id) throws Exception {
        var resources = Minecraft.getInstance().getResourceManager();
        var stack = resources.getResourceStack(id);
        if (stack.isEmpty()) throw new IllegalStateException("Missing validation resource: " + id);
        var translations = new HashMap<String, String>();
        var gson = new Gson();
        var type = new TypeToken<Map<String, String>>() {}.getType();
        // Vanilla loads the resource stack from lower to higher priority, so later packs override earlier values.
        for (var resource : stack) {
            try (var reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                var entries = gson.<Map<String, String>>fromJson(reader, type);
                if (entries != null) translations.putAll(entries);
            }
        }
        return translations;
    }

    private static void verifyRightToLeftWrapping() throws Exception {
        var font = Minecraft.getInstance().font;
        var strings = read(GuideResources.parse("ae2ponder:lang/ar_sa.json"));
        for (var entry : strings.entrySet()) {
            if (!entry.getKey().startsWith("ae2ponder.ponder.shared.")) continue;
            for (int limit : List.of(120, 180)) {
                var lines = RightToLeftText.wrap(font, entry.getValue(), limit, Style.EMPTY);
                if (lines.stream().anyMatch(line -> RightToLeftText.width(font, line.getString()) > limit))
                    throw new IllegalStateException("Shaped subtitle exceeds its window: " + entry.getKey());
                String joined = lines.stream().map(line -> line.getString().replaceAll("\\s", ""))
                        .collect(java.util.stream.Collectors.joining());
                if (!joined.equals(entry.getValue().replaceAll("\\s", "")))
                    throw new IllegalStateException("Shaped subtitle lost text: " + entry.getKey());
            }
        }
    }

    private static void verifyArabicSearchText() throws Exception {
        var guide = Guides.getById(GUIDE);
        var parsed = guide.getParsedPage(PAGE);
        var indexText = guideme.internal.search.GuideSearch.class.getDeclaredMethod("getSearchableText",
                guideme.Guide.class, guideme.compiler.ParsedGuidePage.class);
        indexText.setAccessible(true);
        String searchable = (String) indexText.invoke(null, guide, parsed);
        for (var lesson : GuideBook.lessons()) {
            String title = Language.getInstance().getOrDefault("ae2ponder.ponder." + lesson.id() + ".header");
            if (!searchable.contains(title))
                throw new IllegalStateException("GuideME search lost logical Arabic text: " + lesson.id());
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
