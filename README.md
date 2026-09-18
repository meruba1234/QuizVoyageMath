# QuizVoyageMath

四択クイズ形式の学習用 Android アプリです。
[QuizVoyage](https://github.com/meruba1234/QuizVoyage) から派生させ、
数式を LaTeX で表示できるようにしたものです。

**未完成の学習成果物です。** 作りかけの機能が残っています。詳細は
[未完成・既知の問題](#未完成既知の問題) を参照してください。

---

## QuizVoyage との違い

クラス構成はほぼ同じで、実質的な差は次の2点だけです。

| 箇所 | QuizVoyage | QuizVoyageMath |
|------|-----------|----------------|
| 問題データ | `assets/initial_data.json` | `assets/initial_data_math.json` |
| 問題文の表示 | `TextView#setText` | `JLatexMathView#setLatex`（[jlatexmath-android](https://github.com/noties/jlatexmath-android) 0.2.0） |

問題文・選択肢のどちらも、内容に応じて表示方法を切り替えています
（[数式の表示方針](#数式の表示方針) を参照）。

---

## 画面と機能

| 画面 | クラス | 内容 |
|------|--------|------|
| ホーム | `MainActivity` | タブ（分野）とカルーセルで問題集を選ぶ |
| 出題 | `QuizQuestionActivity` | 四択問題を1問ずつ出題し、正誤を判定する |
| 結果 | `QuizResultsActivity` | 成績を集計してグラフで表示する |
| 検索 | `SearchActivity` | 問題文を部分一致で検索する |

問題データは分野（タブ）→ 問題集（ボタン）→ 設問、の3階層です。
11分野 / 34問題集 / 51問を同梱しています。

```json
{
  "tabs": [
    {
      "name": "複素数平面",
      "buttons": [
        {
          "id": "button1",
          "title": "複素数の計算",
          "questions": [
            {
              "question": "...",
              "choices": ["...", "...", "...", "..."],
              "correct_answer": 0
            }
          ]
        }
      ]
    }
  ]
}
```

---

## 技術構成

- Java / Android（`minSdk 26`, `targetSdk 34`, `compileSdk 34`）
- SQLite（`SQLiteOpenHelper` を直接利用。ORM なし）
- ViewPager2 + TabLayout + RecyclerView
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) v3.1.0（成績グラフ）
- [jlatexmath-android](https://github.com/noties/jlatexmath-android) 0.2.0（数式表示）
- Android Gradle Plugin 8.0.2 / Gradle 8.0

---

## ビルド方法

### Android Studio

`File > Open` でこのディレクトリを開き、Gradle 同期後に Run すればビルドできます。

### コマンドライン

JDK 17 と Android SDK（platform 34 / build-tools 34.0.0）が必要です。
Gradle 8.0 は JDK 21 以降では動作しないため、JDK 17 を使ってください。

```bash
# SDK の場所を指定する（このファイルは .gitignore 済み。環境ごとに作成する）
echo "sdk.dir=/path/to/android-sdk" > local.properties

export JAVA_HOME=/path/to/jdk-17
./gradlew assembleDebug
```

成果物は `app/build/outputs/apk/debug/app-debug.apk` に出力されます。

---

## 未完成・既知の問題

派生元の [QuizVoyage](https://github.com/meruba1234/QuizVoyage) と同じ不具合を
引き継いでいたため、同じ修正を適用しています。

### 修正済み

| 内容 | 影響 |
|------|------|
| `DatabaseHelper.insertInitialData()` が `initial_data_math.json` を JSON 配列として読もうとしていた（実際のファイルはトップレベルが `{"tabs": [...]}` のオブジェクト）。例外はログ出力だけで握りつぶされていた | `quiz_questions` テーブルが空のままになり、**検索が常に0件を返していた** |
| `DatabaseHelper.onUpgrade()` がテーブルを DROP した後に再作成していなかった | DB バージョンを上げた時点でテーブルが失われる |
| 問題文が未記入の設問がそのまま出題・登録対象になっていた | 空欄の問題が表示される |
| 日本語を含む問題文まで JLaTeXMath に流していた。JLaTeXMath は日本語のグリフを持たないため、文字が描画されず消えていた | **日本語だけの問題文が画面に表示されない**（同梱データでは大半が該当）。「終わり！」の表示も同様 |
| 選択肢ボタンが数式表示に未対応だった | 数式を含む選択肢が LaTeX のソースのまま表示される |
| `SearchActivity.performSearch()` が同じ検索を2回実行していた。検索履歴（`searchHistory`）は宣言だけで一度も追加されず、`itemsToShow` も画面に反映されない死んだコードだった | 無駄なクエリ。検索履歴が機能していなかった |

### 残っている問題

| 内容 | 影響 |
|------|------|
| 「油脂・セッケンのまとめ2」に、問題文が未記入の設問が2問ある | この問題集は出題対象が0問になる（開くとメッセージを表示して戻る） |

---

## 数式の表示方針

JLaTeXMath は日本語のグリフを持たないため、日本語を数式として描画すると
文字が消えてしまいます（文字数を増やしても描画幅が変わらないことを
`LatexRenderingTest` で確認しています）。

そのため `MathText#isRenderableMath` で次のように振り分けています。

| 内容 | 表示方法 |
|------|----------|
| 日本語を含まない純粋な数式（例: `$\frac{3}{25} - \frac{4}{25}i$`） | `JLatexMathView` / ボタンの画像として描画 |
| 上記以外（日本語を含む文章、数式記法のない文字列） | 通常の `TextView` / ボタンの文字 |

同梱データでは、問題文と選択肢あわせて245要素のうち45要素が数式として描画され、
残りは通常のテキストとして表示されます。

---

## テスト

端末やエミュレータなしで実行できます。

```bash
./gradlew testDebugUnitTest
```

- `QuizDataParsingTest` — 同梱データをアプリの読み取りロジックで解釈できるか
- `DatabaseHelperTest` — Robolectric で実際の SQLite を動かし、初期データの投入と検索を検証
- `SearchHistoryTest` — 検索履歴の並び・重複排除・件数制限・永続化を検証
- `MathTextTest` — 数式として描画すべきかの判定を検証
- `LatexRenderingTest` — 日本語が描画されない前提の確認と、同梱データの振り分け

上の「検索が常に0件」の不具合は、修正前のコードに対して `DatabaseHelperTest` を実行すると
検索結果0件で失敗することを確認しています（修正後は49件）。

---

## リポジトリについて

このリポジトリは、もともと `QuizVoyageMath.zip` を1ファイル置いただけの状態でした。
GitHub 上でコードを読めず、差分も追えなかったため、ソースツリーとして展開し直しています。
元の zip は参照用にそのまま残しています。
