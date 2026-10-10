---
navigation:
  title: Ponder
  icon: ae2:fluix_crystal
  position: 15
---

# Ponderで学ぶAE2

動く立体模型で、AE2の使い方を40場面に分けて学べます。最初から順番に見ることも、下の項目から知りたい場面へ進むこともできます。どの項目も、同じコースの選んだ場面から再生します。

<CommandLink command="/ae2ponder">最初から見る</CommandLink>

## 見方

ワールドに入った状態でガイドを開き、見たい項目をクリックしてください。Ponderの左右のナビゲーションボタンで前後の場面へ移動できます。左下の戻る矢印を押すと、このガイドページへ戻ります。

接続、品物の流れ、間違えやすい点を順に説明します。途中で一時停止して、配置を確かめながら進められます。

## 1. はじめの準備

素材を集め、結晶を育て、プロセッサを作る。

- <CommandLink command="/ae2ponder lesson meteorite">隕石からはじめよう</CommandLink> — 必要な結晶と4枚のプレスを集める。
- <CommandLink command="/ae2ponder lesson certus_growth">結晶の育ち方と修復</CommandLink> — 芽・結晶・品質の違いを見分ける。
- <CommandLink command="/ae2ponder lesson growth_power">成長加速器に電力を届ける</CommandLink> — 加速器は芽生えたブロックに隣接させる。
- <CommandLink command="/ae2ponder lesson charged_fluix">帯電した結晶とフルーシュ</CommandLink> — 充電器と水中反応で基本素材を用意する。
- <CommandLink command="/ae2ponder lesson processors">3種類のプロセッサを作る</CommandLink> — 回路とシリコンを印刷してから組み立てる。
- <CommandLink command="/ae2ponder lesson inscriber_automation">インスクライバーの自動化</CommandLink> — 投入面と自動搬出の設定を理解する。

## 2. MEネットワーク

電力・チャネル・配線のつながりを理解する。

- <CommandLink command="/ae2ponder lesson first_network">最初のME倉庫を組む</CommandLink> — 電力・ドライブ・セル・端末をひとつにつなぐ。
- <CommandLink command="/ae2ponder lesson energy_buffer">停電と瞬間的な電力不足</CommandLink> — 発電量だけでなく蓄電量も確保する。
- <CommandLink command="/ae2ponder lesson network_connections">どこまでが同じネットワーク？</CommandLink> — フルブロックを通る接続にも注意する。
- <CommandLink command="/ae2ponder lesson eight_channels">8チャネルの壁</CommandLink> — コントローラなしの9台目で何が起こるか。
- <CommandLink command="/ae2ponder lesson controller">コントローラで拡張する</CommandLink> — 面ごと32チャネル、構造はひとつにつなげる。
- <CommandLink command="/ae2ponder lesson dense_branching">32本の幹と8本の枝</CommandLink> — 高密度ケーブルから通常ケーブルへ枝分かれする。
- <CommandLink command="/ae2ponder lesson cable_colors">色とアンカーで配線を整理する</CommandLink> — 色は接続を制限し、チャネル数を増やさない。

## 3. 収納

セルの容量、フィルタ、外部収納を使い分ける。

- <CommandLink command="/ae2ponder lesson bytes_types">容量と63種類は別の上限</CommandLink> — 大きなセルも品目数は増えない。
- <CommandLink command="/ae2ponder lesson drive_status">ドライブのランプを読む</CommandLink> — 満杯の理由をランプで見分ける。
- <CommandLink command="/ae2ponder lesson partition_priority">専用セルと優先度を決める</CommandLink> — 大量品を専用の置き場へ集める。
- <CommandLink command="/ae2ponder lesson external_storage">チェストをME倉庫に参加させる</CommandLink> — 収納バスは中身を移さず見せる。
- <CommandLink command="/ae2ponder lesson fluids">液体も同じネットワークへ</CommandLink> — 液体セルと液体フィルタを使う。
- <CommandLink command="/ae2ponder lesson cell_io">セルの引っ越しと再利用</CommandLink> — IOポートで内容を移してから整理する。
- <CommandLink command="/ae2ponder lesson portable_wireless">持ち歩く倉庫と無線端末</CommandLink> — 持ち歩くセルと遠隔アクセスは別の道具。

## 4. 自動化

入出力、在庫の制御、自動クラフトを組み立てる。

- <CommandLink command="/ae2ponder lesson import_export">搬入と搬出を間違えない</CommandLink> — ネットへ入れるのがインポート、外へ出すのがエクスポート。
- <CommandLink command="/ae2ponder lesson interface_stock">インターフェースを受け渡し口にする</CommandLink> — 外から入れる・必要数を置いておく。
- <CommandLink command="/ae2ponder lesson upgrade_cards">アップグレードカードを使い分ける</CommandLink> — 速度・フィルタ・制御には別々のカードを使う。
- <CommandLink command="/ae2ponder lesson level_control">必要な量で止まる設備</CommandLink> — レベルエミッターの信号で入出力を制御する。
- <CommandLink command="/ae2ponder lesson crafting_basics">はじめての自動クラフト</CommandLink> — パターン・プロバイダー・CPU・組立機をそろえる。
- <CommandLink command="/ae2ponder lesson crafting_cpu">クラフトCPUの組み方</CommandLink> — 直方体、最低1ストレージ、1CPUにつき1ジョブ。
- <CommandLink command="/ae2ponder lesson processing_patterns">機械加工をパターンにする</CommandLink> — 入力と出力を正確に宣言し、結果をMEへ戻す。
- <CommandLink command="/ae2ponder lesson crafting_parallel">自動クラフトを速くする</CommandLink> — 組立機・プロバイダー・CPUの分担を考える。
- <CommandLink command="/ae2ponder lesson crafting_control">材料の混線と作りすぎを防ぐ</CommandLink> — ブロッキング・結果待ち・在庫要求を使い分ける。

## 5. 応用システム

サブネット・P2P・ブロックの回収と設置・量子ブリッジ・空間収納へ進む。

- <CommandLink command="/ae2ponder lesson subnets">小さなサブネットを作る</CommandLink> — 収納を分けた小さなネットで処理する。
- <CommandLink command="/ae2ponder lesson storage_subnet">サブネットの倉庫を見せる</CommandLink> — 主ネットからだけ収納を参照する。
- <CommandLink command="/ae2ponder lesson p2p_channels">P2Pでチャネルを運ぶ</CommandLink> — 32本の通り道を別ネットに載せる。
- <CommandLink command="/ae2ponder lesson p2p_resources">品物・液体・電力のP2P</CommandLink> — 面を遠くにつなぐ仕組みを使い分ける。
- <CommandLink command="/ae2ponder lesson world_planes">ブロックを回収・設置する面</CommandLink> — 世界のブロックも収納の入出力として扱う。
- <CommandLink command="/ae2ponder lesson certus_factory">育ちきった結晶だけを収穫する</CommandLink> — 粉を受け入れないサブネットで成長を待つ。
- <CommandLink command="/ae2ponder lesson condenser_overflow">余り物と凝縮器を扱う</CommandLink> — 消去する経路を専用の品物に限定する。
- <CommandLink command="/ae2ponder lesson quantum_bridge">量子ブリッジで遠くへつなぐ</CommandLink> — 両端の構造・特異点ペア・読み込みをそろえる。
- <CommandLink command="/ae2ponder lesson spatial_io">部屋をそのまま空間収納する</CommandLink> — パイロンで囲った範囲を丸ごと交換する。
- <CommandLink command="/ae2ponder lesson spatial_safety">空間セルの寸法と安全な操作</CommandLink> — 寸法は初回で固定。対象範囲に入らない。

## 6. トラブル解決

動かない原因を順番に確かめる。

- <CommandLink command="/ae2ponder lesson diagnostics">動かないときはここを見る</CommandLink> — 電力→チャネル→収納→入力面→完成品の順で調べる。
