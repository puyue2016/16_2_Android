# 16_2_Android_offline
Offline Version for MTI

実行環境
Android Studio 2.0.0以上
Android端末(API level > 21)
子供端末で取得したcsvファイル
本プログラムはネットワークを利用しない

実行に必要なファイル
１．子供端末の取得Bluetoothデータ
２．子供端末の取得加速度データ
３．友達リスト
/(ID).csv
に解析に必要な子供端末のBluetoothデータを保存する．
/(ID)_acc.csv
に解析に必要な子供端末の加速度データを保存する．
/friendlist.csv
のファイルに全部友達のBluetoothIDを保存する．

実行データの参照
１．ハードウェア端末からファイル
(ID).csv　
(ID)_acc.csv
は
/（androidデバイスのメモリのルートディレクトリ）
に保存され，それらのファイルを参照し解析を行う．
２．全部友達のBluetoothIDを保存したファイル
friendlist.csv　
は
/（androidデバイスのメモリのルートディレクトリ）
に保存され，そのファイルを参照し解析を行う．
** サンプルのために,github「tsukuba-pbl/16_2_Android_offline」のコードのルートディレクトリで「friendlist.csv」用意しました。その中で3つBluetoothID「aaaaaaaaaa」、「bbbbbbbbbbbb」、「cccccccccccc」を友達3つとして保存しました。実行の時直接androidデバイスのメモリのルートディレクトリにコピーペーストしてください。

実行
1.MainActivityのname引数で子供端末と対応したIDを設定する(23行目)
2.Android Studioでコンパイルし、Android端末にインストール
3.子供端末で取得た(id).csvと(id)_acc.csvをandroidデバイスのメモリのルートディレクトリに移動
4.friendlist.csvをサンプルような作ってandroidデバイスのメモリのルートディレクトリに移動
5.アプリの左上のNavigationより「データを処理」画面に移動し、当日の日付けボタンを押し、データを解析。
6.アプリの左上のNavigationより「パッと見グラフ」画面に移動してから日付を選択し、解析した結果を円グラフで確認できる
7.アプリの左上のNavigationより「詳しく見る」画面に移動してから日付を選択し、解析した結果を折り線で確認できる

解析結果について
解析結果はandroidデバイスののメモリ
/MTI/ (ID) / (日付) /　
に保存される．
(ID)_alonerange.csv
(ID)_change.csv
(ID)_hourmark.csv
(ID)_minmark.csv
(ID)_sum.csv
の5つのcsvファイルを結果として抽出する．
_alonerangeは子供が一人でいた時間帯
_changeは周囲の子供の変化人数を単位時間でまとめたもの
_hourmarkは「パッと見グラフ」で一時間単位で色分けをするためのもの
_minmarkは「パッと見グラフ」で5分間時間単位で色分けをするもの
_sumは周囲の子供の人数を単位時間でまとめたもの
