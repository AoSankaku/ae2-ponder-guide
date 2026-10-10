"""Export translation sources and validate reviewed language bundles (stdlib only)."""
import argparse
import hashlib
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TRANSLATIONS = ROOT / "src/main/translations"
SUPPORTED = {"ja_jp", "en_us", "hi_in", "zh_hk", "zh_tw", "zh_cn", "ru_ru", "pt_br",
             "pt_pt", "es_es", "de_de", "cs_cz", "pl_pl", "ms_my", "ro_ro"}


def read(path):
    def unique(pairs):
        result = {}
        for key, value in pairs:
            if key in result:
                raise ValueError(f"Duplicate key {key} in {path}")
            result[key] = value
        return result
    return json.loads(path.read_text(encoding="utf-8"), object_pairs_hook=unique)


def sources():
    lessons = read(ROOT / "src/main/resources/assets/ae2ponder/guide/lessons.json")
    lessons += read(ROOT / "src/main/resources/assets/ae2ponder/guide/addons.json")
    result = {}
    for locale, field in [("en_us", "en"), ("ja_jp", "ja")]:
        strings = read(ROOT / f"src/main/templates/lang/{locale}.json")
        for lesson in lessons:
            strings[f"ae2ponder.ponder.{lesson['id']}.header"] = lesson["title"][field]
            strings[f"ae2ponder.lesson.{lesson['id']}.summary"] = lesson["summary"][field]
            for index, step in enumerate(lesson["steps"]):
                strings[f"ae2ponder.ponder.shared.{lesson['id']}_{index}"] = step["text"][field]
        result[locale] = strings
    assert result["en_us"].keys() == result["ja_jp"].keys(), "English/Japanese key mismatch"
    return result


def source_hash(source):
    # Identical to gradle/localization.gradle; a source edit invalidates every bundle.
    text = "".join(locale + "\0" + "".join(key + "\0" + strings[key] + "\0"
                  for key in sorted(strings)) for locale, strings in sorted(source.items()))
    return hashlib.sha256(text.encode("utf-8")).hexdigest()


def placeholders(text):
    tokens = re.findall(r"%(?:(\d+)\$)?([sd])", text)
    result = []
    implicit = 0
    for position, kind in tokens:
        if not position:
            implicit += 1
        result.append((int(position) if position else implicit, kind))
    return sorted(result)


def check():
    source = sources()
    digest = source_hash(source)
    expected = source["en_us"]
    languages = read(TRANSLATIONS / "languages.json")
    assert languages.keys() == SUPPORTED, "All 15 supported languages are required"
    reviews = read(TRANSLATIONS / "source-review.json")
    assert reviews.keys() == source.keys(), "English/Japanese source review records are required"
    for locale in source:
        assert reviews[locale] == digest, f"Stale source review: {locale}"
    for locale in languages:
        if locale in source:
            strings = source[locale]
        else:
            bundle = read(TRANSLATIONS / f"{locale}.json")
            assert bundle.pop("_source_hash", None) == digest, f"Stale translation: {locale}; review all source edits"
            strings = bundle
        assert strings.keys() == expected.keys(), f"Missing/extra keys: {locale}: {strings.keys() ^ expected.keys()}"
        for key, text in strings.items():
            assert isinstance(text, str) and text.strip(), f"Empty translation: {locale}/{key}"
            assert placeholders(text) == placeholders(expected[key]), f"Wrong placeholders: {locale}/{key}"
    print(f"PASS: {len(languages)} languages, {len(expected)} strings each; source freshness and placeholders")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("action", choices=["source", "check"])
    args = parser.parse_args()
    if args.action == "source":
        source = sources()
        target = ROOT / ".local/localization/source.json"
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(json.dumps({"_source_hash": source_hash(source), **source["en_us"]},
                                   ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(f"Exported {len(source['en_us'])} source strings to {target}")
    else:
        check()
