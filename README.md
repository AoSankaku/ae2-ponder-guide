# AE2 Ponder Guide

Minecraft 1.21.1 / NeoForge向けの、Applied Energistics 2を立体アニメーションで学ぶガイドです。
独立版Ponderを使用するため、**Create本体は不要**です。日本語と英語に対応しています。
6章・40レッスン・200ステップで、121種類のAE2アイテムから関連場面を開けます。

## 使い方

1. ワールドに入って **Pキー** を押すと、ガイド一覧が開きます。キーは操作設定で変更できます。
2. インベントリやポーズ画面の右上にある **AE2ガイド** ボタンでも開けます。
3. 「すべて・学習順」で最初から学ぶか、章別・検索で必要な場面を選びます。
4. Ponderの再生、一時停止、タイムライン、キーフレーム、ブロック識別を利用できます。
5. 「次のレッスン」で学習を続け、「ガイド一覧へ」またはEscで一覧に戻れます。

対応するAE2アイテムのツールチップからも、Ponder標準の操作で関連場面を開けます。
レッスンを最後まで再生すると学習済みになり、`config/ae2ponder-progress.json` に保存されます。

## 内容

素材の入手、結晶成長、プロセッサ製造、電力、最小ネットワーク、チャネル、コントローラ、
ケーブル、端末、ストレージセル、収納バス、搬入・搬出、液体、無線、セル設定、
自動クラフト、加工パターン、クラフトCPU、サブネット、P2P、量子リンク、
空間ストレージ、レベルエミッタ、プレーン、凝縮器、トラブルシューティングを扱います。

場面は動作を説明する模型です。表示用の通電状態・チャネル数・アイテム移動は演出であり、
プレイヤーのワールドを操作しません。電力源や他MODの加工機は導入環境によって異なります。
レシピ変更やチャネル設定変更があるMODパックでは、その設定を優先してください。

## 必要なMOD

| MOD | 検証対象バージョン |
| --- | --- |
| NeoForge | 21.1.256（最低21.1.206） |
| Applied Energistics 2 | 19.2.18 |
| GuideME | 21.1.19（AE2の必須依存） |
| Ponder | 1.0.87+mc1.21.1 |
| Flywheel | 1.0.4 |
| AE2 Ponder Guide | 1.0.0 |

AE2が要求するGuideMEは必要ですが、今回のガイドはPonderから利用します。
Createが既にPonder/Flywheelを同梱している環境では、同じMODを重複導入しないでください。

## ビルド

Java 21を使用します。

```powershell
.\gradlew.bat build
```

出力は `build/libs/ae2-ponder-guide-1.0.0.jar` です。
`generateGuideResources` が全場面のノード参照・座標・表示順・多言語テキストを検証し、
Ponder用の翻訳ファイルを生成します。構造NBTを変更する場合は `python tools/generate_structure.py` を使います。

統合検証は `./gradlew -PvalidateGuide runValidationClient` で実行できます。
検証用コードは配布JARに含まれません。別の開発ワールドを自動作成し、ウィンドウを隠して、
Createなしで全場面のコンパイル・再生・巻き戻し・部品復元・描画と一覧からの選択を確認します。
結果は `build/validation-run/validation-result.json`、画像は同ディレクトリの `screenshots` に保存します。

## 設計

- `src/main/resources/assets/ae2ponder/guide/lessons.json`: 章・場面・字幕・模型・演出
- `GuideScenes`: 共通のPonder再生処理。AE2公式のvisual NBTでケーブルと部品を表示
- `GuidePlugin`: AE2アイテムとカテゴリへの場面登録
- `GuideScreen`: 検索可能な学習順一覧
- `GuidePonderScreen`: 1レッスンの再生と次のレッスンへの移動
- `PonderLevelModelDataMixin`: 仮想ワールドのブロックエンティティからNeoForgeの描画データを渡す互換処理
- `src/main/templates/lang`: UIの翻訳。場面字幕はJSONから生成

サーバー側ではガイドクライアントのクラスをロードしません。

## 一次資料

- [独立版Ponder](https://github.com/Creators-of-Create/Ponder)
- [Ponderの公式Maven](https://maven.createmod.net/net/createmod/ponder/ponder-neoforge/)
- [AE2のPonder用visual NBT](https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/1.21.1/ponderjs.md)
- [AE2公式ガイド](https://guide.appliedenergistics.org/1.21.1/)

このアドオンのライセンスはMITです。各依存MODのライセンスはそれぞれの配布物に従います。
