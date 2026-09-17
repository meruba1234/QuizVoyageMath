package to.msn.wings.quizvoyagemath;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*--- データベースを操作するクラス ---*/
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "quiz_database";
    private static final int DATABASE_VERSION = 1;
    private Context context; // Contextへの参照を追加

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context; // Contextを保存
    }

    // JSONファイルを読み込んで、String型のデータに変換している。
    // MainActivityで使われる
    public  String loadJSONFromAsset() {
        String json = null;
        try {
            // 1.JSONファイルを開く
            InputStream is = context.getAssets().open("initial_data_math.json");
            // 2.ファイルの内容を読み込む
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            //3.バイト配列をUTF-8エンコーディングの文字列に変換
            json = new String(buffer, "UTF-8");
            //4.ファイルストリームを閉じる
            is.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    // String型になったJSONデータをMap<String, Map<String, List<QuizQuestion>>>としてデータを格納する。
    // 最初のStringはタブ名、2番目のStringはボタンID、List<QuizQuestion>はそのボタンに割り当てられる問題リスト
    public Map<String, Map<String, Data_Button>> parseQuizQuestions(String jsonData) {
        // 読み込んだデータの順序を保持するために、LinkedHashMapを使用する。
        LinkedHashMap<String, Map<String, Data_Button>> tabQuestions = new LinkedHashMap<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray tabsArray = jsonObject.getJSONArray("tabs");

            for (int t = 0; t < tabsArray.length(); t++) {
                JSONObject tabObject = tabsArray.getJSONObject(t);
                String tabName = tabObject.getString("name");
                JSONArray buttonsArray = tabObject.getJSONArray("buttons");

                LinkedHashMap<String, Data_Button> buttonData = new LinkedHashMap<>();

                for (int b = 0; b < buttonsArray.length(); b++) {
                    JSONObject buttonObject = buttonsArray.getJSONObject(b);
                    String buttonId = buttonObject.getString("id");
                    String title = buttonObject.getString("title");
                    JSONArray questionsArray = buttonObject.getJSONArray("questions");

                    List<Data_Quiz> questions = new ArrayList<>();
                    for (int q = 0; q < questionsArray.length(); q++) {
                        JSONObject questionObject = questionsArray.getJSONObject(q);
                        String questionText = questionObject.getString("question");
                        JSONArray choicesArray = questionObject.getJSONArray("choices");
                        int correctAnswer = questionObject.getInt("correct_answer");

                        List<String> choices = new ArrayList<>();
                        for (int j = 0; j < choicesArray.length(); j++) {
                            choices.add(choicesArray.getString(j));
                        }
                        questions.add(new Data_Quiz(questionText, choices, correctAnswer));
                    }

                    Data_Button quizData = new Data_Button(buttonId, title, questions);
                    buttonData.put(buttonId, quizData);
                }

                tabQuestions.put(tabName, buttonData);
            }
        } catch (JSONException e) {
            Log.e("DatabaseHelper", "JSON解析エラー", e);
        }

        return tabQuestions;
    }

    // アプリのインストール時に一回だけ行われるメソッド
    //　もう一度行いたい場合は、アンインストールしてから再度インストールする。
    @Override
    public void onCreate(SQLiteDatabase db) {
        // クイズデータを格納するためのテーブルを作成
        String CREATE_QUIZ_TABLE =
                "CREATE TABLE quiz_questions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "question TEXT NOT NULL," +
                "option1 TEXT NOT NULL," +
                "option2 TEXT NOT NULL," +
                "option3 TEXT NOT NULL," +
                "option4 TEXT NOT NULL," +
                "correct_answer INTEGER)";
        db.execSQL(CREATE_QUIZ_TABLE);

        // JSONファイルから初期データを挿入
        insertInitialData(db);
    }

    // 初期データをデータベースに挿入するメソッド
    private void insertInitialData(SQLiteDatabase db) {
        try {
            // JSONファイルを読み込む
            InputStream is = context.getAssets().open("initial_data_math.json");
            // BufferReaderを使って、ファイル内容を文字列として読み込む
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            // 読み込んだ文字列をJSONArrayインスタンスに変換して、
            // JSON配列の各要素にアクセスできるようにする。
            JSONArray jsonArray = new JSONArray(sb.toString());
            // 問題文と選択肢、正解の要素番号を取得する。
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String question = jsonObject.getString("question");
                JSONArray choices = jsonObject.getJSONArray("choices");
                int correctAnswer = jsonObject.getInt("correct_answer");

                // クイズの初期データを挿入
                ContentValues values = new ContentValues();
                values.put("question", question);
                for (int j = 0; j < choices.length(); j++) {
                    values.put("option" + (j + 1), choices.getString(j));
                }
                values.put("correct_answer", correctAnswer);
                db.insert("quiz_questions", null, values);
            }
        } catch (
                IOException |
                JSONException e) {
            Log.e("MyApp", "insertInitialDataでエラーが発生しました。");
            e.printStackTrace();
        }
    }

    // 検索クエリに基づいてデータベースから質問を検索するメソッド
    // 問題文に基づいてデータを検索
    public List<Data_Quiz> searchQuizQuestions(String query) {
        List<Data_Quiz> matchedQuestions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM quiz_questions WHERE question LIKE ?", new String[]{"%" + query + "%"});

        if (cursor.moveToFirst()) {
            do {
                int questionColumnIndex = cursor.getColumnIndex("question");
                if (questionColumnIndex != -1) {
                    String questionText = cursor.getString(questionColumnIndex);
                    List<String> choices = new ArrayList<>();
                    for (int i = 1; i <= 4; i++) {
                        int choiceColumnIndex = cursor.getColumnIndex("option" + i);
                        if (choiceColumnIndex != -1) {
                            choices.add(cursor.getString(choiceColumnIndex));
                        }
                    }
                    int correctAnswerColumnIndex = cursor.getColumnIndex("correct_answer");
                    if (correctAnswerColumnIndex != -1) {
                        int correctAnswer = cursor.getInt(correctAnswerColumnIndex);
                        matchedQuestions.add(new Data_Quiz(questionText, choices, correctAnswer));
                    }
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return matchedQuestions;
    }

    // データベースをバージョンアップしたときに、テーブルを再作成
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (db != null) {
            db.execSQL("DROP TABLE IF EXISTS quiz_questions");
            super.onOpen(db);
        }
    }
}
