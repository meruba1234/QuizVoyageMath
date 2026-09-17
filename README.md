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

選択肢ボタンは LaTeX 表示に対応しておらず、`TextView` のままです
（コード中にも「選択ボタンにもLatexで表示されるように改変中」というコメントが残っています）。

同梱データ51問のうち、LaTeX 記法を含むのは6問です。

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

派生元の QuizVoyage と同じ不具合をそのまま引き継いでいます。

| # | 内容 | 影響 |
|---|------|------|
| 1 | `DatabaseHelper.insertInitialData()` が `initial_data_math.json` を JSON 配列として読もうとしているが、実際のファイルはトップレベルが `{"tabs": [...]}` のオブジェクト。例外が発生してもログ出力だけで握りつぶしている | `quiz_questions` テーブルが空のままになり、**検索が常に0件を返す** |
| 2 | `DatabaseHelper.onUpgrade()` がテーブルを DROP した後に再作成していない | DB バージョンを上げた時点でテーブルが失われる |
| 3 | 「油脂・セッケンのまとめ2」に、問題文が未記入の設問が2問ある | 空欄の問題が出題される |
| 4 | 選択肢ボタンが LaTeX 表示に未対応 | 数式を含む選択肢がそのままの文字列で表示される |
| 5 | `SearchActivity.performSearch()` が同じ検索を2回実行している。検索履歴（`searchHistory`）は宣言だけで一度も追加されない | 無駄なクエリ。検索履歴機能は未実装 |

---

## リポジトリについて

このリポジトリは、もともと `QuizVoyageMath.zip` を1ファイル置いただけの状態でした。
GitHub 上でコードを読めず、差分も追えなかったため、ソースツリーとして展開し直しています。
元の zip は参照用にそのまま残しています。
