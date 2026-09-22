# AGENTS.md — このリポジトリで作業するときの前提

四択クイズ形式の学習用 Android アプリ（Java）。`meruba1234/QuizVoyage` の派生で、
数式を LaTeX 表示できるようにしたもの。**未完成の学習成果物**として公開している。

**派生元とはクラス構成がほぼ同じ。** 片方を直したらもう片方にも同じ修正が
必要か検討すること（これまでの不具合はすべて両方に存在した）。

**JLaTeXMath は日本語を描画できない。** 日本語を数式として渡すと文字が消える。
`MathText#isRenderableMath` で数式と通常テキストを振り分けている。
詳細は `README.md` の「数式の表示方針」を参照。

---

## ビルドとテスト

**JDK 17 が必要。** Gradle 8.0 は JDK 21 以降では動かない
（`Unsupported class file major version 65` で失敗する）。

Android SDK は platform 34 / build-tools 34.0.0。

```bash
# 環境ごとに作る。.gitignore 済み
echo "sdk.dir=/path/to/android-sdk" > local.properties

export JAVA_HOME=/path/to/jdk-17
./gradlew testDebugUnitTest assembleDebug
```

テストは**端末やエミュレータなしで実行できる**。Robolectric を入れてあり、
実際の SQLite を JVM 上で動かして検証している。

現在のテスト数: **34件 / 失敗 0件**

| テストクラス | 検証内容 |
|---|---|
| `QuizDataParsingTest` | 同梱データをアプリの読み取りロジックで解釈できるか |
| `DatabaseHelperTest` | 実 SQLite で初期データの投入と検索を検証 |
| `SearchHistoryTest` | 検索履歴の並び・重複排除・件数制限・永続化 |
| `StatisticsDbHelperTest` | 成績の保存・取り出し・並び順・問題集ごとの保持件数 |
| `MathTextTest` | 数式として描画すべきかの判定 |
| `LatexRenderingTest` | 日本語が描画されない前提の確認と、同梱データの振り分け |

---

## 変更するときに守ること

| ルール | 理由 |
|---|---|
| **ファイルを削除しない** | 所有者の明示的な指示。`QuizVoyageMath.zip` も残したままにする |
| **改行コードを変えない** | 大半のファイルが CRLF。LF に変換すると全行が差分になり、レビューできなくなる。実際に一度やらかしている |
| **テストを消して通さない** | 失敗したら原因を直す |
| main へ直接 push しない | 作業はブランチで行い、マージは所有者が判断する |

### 修正したら、まず「修正前に落ちること」を確認する

このリポジトリの不具合修正は、すべて次の手順で検証してある。同じやり方を推奨する。

```bash
# 1. テストを書く
# 2. 修正前のコードに対して実行し、落ちることを確認する
git stash && ./gradlew testDebugUnitTest   # 落ちるはず
git stash pop
# 3. 修正後に通ることを確認する
./gradlew testDebugUnitTest
```

これをやらないと、直っていないのに直ったつもりになる。

---

## このコードベースで踏んだ落とし穴

同じ轍を踏まないための記録。

- **例外の握りつぶし。** `catch` してログを出すだけの箇所があり、初期データの
  投入が失敗し続けていたのに誰も気づけなかった。例外は `Log.e(TAG, msg, e)` の
  形で必ず中身を残すこと
- **同じファイルを2箇所が別形式だと思って読んでいた。** 読み取りは
  `DatabaseHelper#toQuiz` に集約してある。増やさないこと
- **`SQLiteOpenHelper` から取った `SQLiteDatabase` を個別メソッドで閉じない。**
  同じヘルパーを使う他の `Cursor` が無効になりうる。閉じるのは `Activity` の
  `onDestroy` でヘルパーごと
- **`CURRENT_TIMESTAMP` は秒単位。** 同じ秒に入ったレコードの順序は定まらない。
  並び順には `id` を使うこと
- **RecyclerView（ViewPager2 の中身）はビューを使い回す。** バインド時に
  「値を設定する」だけでなく「使わない要素を元に戻す」ことまでやらないと、
  前のページの内容が残る

---

## 残っている作業

`TASKS.md` を参照。
