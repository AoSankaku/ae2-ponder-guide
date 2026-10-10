# AE2 Ponder Guide

Minecraft 1.21.1 / NeoForge向けの、Applied Energistics 2を立体アニメーションで学ぶガイドです。
独立版Ponderを使用するため、**Create本体は不要**です。15言語に対応しています。
AE2本体は6章・40レッスン・200ステップで、121種類のAE2アイテムから関連場面を開けます。
対応アドオンには計15レッスン・75ステップを追加し、導入済みのものを表示します。
MEガイドのアイテムからは、利用可能な全レッスンのコースを開けます。

## 使い方

1. インベントリで対応するAE2アイテムにカーソルを合わせ、**Wキーを長押し**するとPonderが開きます。
2. Ponderの再生、一時停止、タイムライン、キーフレーム、ブロック識別を利用できます。
3. 全レッスンの一覧は、ワールド内の「設定…」画面左下にある小さなフルーシュクリスタルのボタンから開けます。
4. ME Guideのサイドバー「Ponder」にも、導入済みのレッスンへのリンクがあります。
5. 一覧やME Guideから選んだ場面の前後へ、Ponder標準の左右ボタンで移動できます。
   左下の戻るボタンで一覧やME Guideへ戻れます。

章の関連アイテム一覧は画面幅と高さに合わせて配置し、収まらない分はページに分けます。

インベントリやポーズ画面への追加ボタン、専用のPキー割り当てはありません。
タイトル画面から開いた設定では、ワールドに入るまで一覧ボタンは無効です。
レッスンを最後まで再生すると学習済みになり、`config/ae2ponder-progress.json` に保存されます。

## 内容

素材の入手、結晶成長、プロセッサ製造、電力、最小ネットワーク、チャネル、コントローラ、
ケーブル、端末、ストレージセル、収納バス、搬入・搬出、液体、無線、セル設定、
自動クラフト、加工パターン、クラフトCPU、サブネット、P2P、量子リンク、
空間ストレージ、レベルエミッタ、プレーン、凝縮器、トラブルシューティングを扱います。

場面は動作を説明する模型です。表示用の通電状態・チャネル数・アイテム移動は演出であり、
プレイヤーのワールドを操作しません。電力源や他MODの加工機は導入環境によって異なります。
レシピ変更やチャネル設定変更があるMODパックでは、その設定を優先してください。

収納セル、投入素材、回路、アップグレードカード、パターンなどは、対象の装置に向けた
アイテム画像付きの吹き出しでも示します。複数の素材は順番に表示し、字幕と一緒に確認できます。
実際にアイテムを持って操作する場面には、右クリックやスニークの印も表示します。

## 対応言語と更新

日本語、英語、ヒンディー語、繁体字中国語（香港・台湾）、簡体字中国語（中国）、
ロシア語、ポルトガル語（ブラジル・ポルトガル）、スペイン語、ドイツ語、
チェコ語、ポーランド語、マレー語、ルーマニア語に対応しています。
字幕、全レッスンのタイトルと要約、一覧と検索、章名、Ponder・ME Guideの操作表示、
ME GuideのページがMinecraftで選択した言語に切り替わります。

今後の追加・変更も全15言語を同時に更新します。欠落したキー、空欄、
不正なプレースホルダ、原文が変わった後に未確認の翻訳がある場合はビルドが失敗します。
日本語の説明はCreateと同じ「です・ます」調に揃え、ほかの言語もCreate/Ponderの呼びかけ方に合わせます。
見出しや短い操作ラベルは自然な形を保ちます。対応する翻訳がない場合のフォールバックは英語です。
原文の確認が済むまで翻訳ファイルの `_source_hash` を更新しないでください。
日本語・英語にも `source-review.json` で確認を記録します。
詳細は [AGENTS.md](AGENTS.md) を参照してください。

```powershell
python tools/localization.py source
python tools/localization.py check
python -m unittest discover -s tools -p test_localization.py
```

ME Guideのページは翻訳データから生成するため、ページごとの手作業の更新は不要です。

## 必要なMOD

| MOD | 検証対象バージョン |
| --- | --- |
| NeoForge | 21.1.256（最低21.1.206） |
| Applied Energistics 2 | 19.2.18 |
| GuideME | 21.1.19（AE2の必須依存） |
| Ponder | 1.0.87+mc1.21.1 |
| Flywheel | 1.0.4 |
| AE2 Ponder Guide | 1.0.6 |

AE2が要求するGuideMEは必要ですが、今回のガイドはPonderから利用します。
Createが既にPonder/Flywheelを同梱している環境では、同じMODを重複導入しないでください。

## 対応アドオン（任意）

| MOD | 検証対象バージョン | ガイド内容 |
| --- | --- | --- |
| AE2 OMNI Cells | 1.1.6 | 混合ストレージ、材料、携帯セル、資源型フィルター、クラフトCPU |
| Bigger AE2 | 1.21.1-1.4.9 | Quantumアイテム・液体セル、Digital Singularityセル、アクセラレーター |
| AE2 Things | 1.4.2-beta | DISKの作成、パーティション、退避と再利用 |
| AE2 MEGA Things | 2.0.4 | MEGAアイテムDISK、液体DISK、設定と再利用 |

この4MODはガイドの必須依存ではありません。未導入のレッスンは一覧、Wキーの候補、
Ponderコース、ME Guideのページと検索から除外します。1つ以上導入すると「アドオン」章が現れます。
AE2 MEGA Thingsを導入する場合は、その必須依存のAE2 Things、MEGA Cells、Kotlin for Forgeも必要です。
検証ではMEGA Cells 4.11.0、Kotlin for Forge 5.12.0を使用します。

開発・テストの標準起動構成には4MODと必要な依存をすべて含めています。
アドオンなしの動作確認は `-PwithoutAddons` を付けて起動してください。
配布JARにアドオン本体は含めません。

## ビルド

Java 21を使用します。

```powershell
.\gradlew.bat build
```

出力は `build/libs/ae2-ponder-guide-1.0.6.jar` です。
`generateGuideResources` が全場面のノード参照・座標・表示順・吹き出し・多言語テキストを検証し、
Ponder用の翻訳ファイルを生成します。構造NBTを変更する場合は `python tools/generate_structure.py` を使います。

統合検証は `./gradlew -PvalidateGuide runValidationClient` で実行できます。
検証用コードは配布JARに含まれません。別の開発ワールドを自動作成し、ウィンドウを隠して、
Createなしで全場面のコンパイル・再生・巻き戻し・部品復元・吹き出しのアイテムと表示順・描画、
全16通りのアドオン導入条件、設定の小ボタンから一覧への移動、関連アイテムのページ送りと画面内配置、
ME Guideのリンクと復帰、Ponder標準の前後移動も確認します。
`-PvalidateLanguages` を付けると15言語の読み込み・検索・操作表示を確認し、
ヒンディー語・香港繁体字・ポルトガル語のガイドページと字幕を描画します。
結果は `build/validation-run/validation-result.json`、画像は同ディレクトリの `screenshots` に保存します。

通常ワールドの性能調査には `./gradlew -PvalidateGuide runPerformanceClient` を利用できます。
調査対象のワールドの**コピー**を `build/performance-run/saves/profile-world` に配置し、
対象の `options.txt` と必要な設定を `build/performance-run` にコピーしてから実行してください。
ガイドを開かずに45秒間カメラを回転させ、最初の15秒を除いた描画間隔・描画時間・サーバー処理時間を
`performance-result.json`、Java Flight Recorderの記録を `performance.jfr` に保存して自動終了します。
描画解像度は1280×720です。メモリ割り当ては `-PprofileMinHeap=1G -PprofileMaxHeap=4G` で変更できます。
この測定用コードも配布JARには含まれません。

## 設計

- `src/main/resources/assets/ae2ponder/guide/lessons.json`: 章・場面・字幕・模型・演出
- `src/main/resources/assets/ae2ponder/guide/addons.json`: 導入条件付きアドオンの場面・字幕・模型
- `GuideScenes`: 共通のPonder再生処理。AE2公式のvisual NBTでケーブルと部品を表示
- `GuidePlugin`: AE2アイテムとカテゴリへの場面登録
- `GuideScreen`: 検索可能な学習順一覧
- `GuidePonderScreen`: 選んだ場面から全コースをPonder標準のナビゲーションで再生
- `GuideCommands` / `assets/ae2ponder/ae2guide`: ME Guideから各場面へのクライアント側リンク
- `PonderTagScreenMixin`: AE2の関連アイテム画面を、画面サイズに応じてページ分割
- `OptionalModTagCompiler` / `GuideDefaultExtensionsMixin`: ME Guideのアドオン節と検索を導入条件で切り替え
- `PonderLevelModelDataMixin`: 仮想ワールドのブロックエンティティからNeoForgeの描画データを渡す互換処理
- `src/main/templates/lang`: 日本語・英語のUI、章、Ponder操作、ガイド導入文
- `src/main/translations`: その他13言語の全テキストと原文確認のハッシュ
- `gradle/localization.gradle`: 15言語の翻訳リソースとME Guideページを生成・検証

サーバー側ではガイドクライアントのクラスをロードしません。

## 一次資料

- [独立版Ponder](https://github.com/Creators-of-Create/Ponder)
- [Ponderの公式Maven](https://maven.createmod.net/net/createmod/ponder/ponder-neoforge/)
- [AE2のPonder用visual NBT](https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/1.21.1/ponderjs.md)
- [AE2公式ガイド](https://guide.appliedenergistics.org/1.21.1/)
- [AE2 OMNI Cells](https://github.com/Frostbite-time/AE2OmniCells)
- [Bigger AE2](https://github.com/GregTech-Exploring-Pioneer/BiggerAE2)
- [AE2 Things](https://github.com/Technici4n/AE2Things-Forge)
- [AE2 MEGA Things](https://github.com/Lapis256/AE2MEGAThings)

このアドオンのライセンスはMITです。各依存MODのライセンスはそれぞれの配布物に従います。
