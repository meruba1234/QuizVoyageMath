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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*--- データベースを操作するクラス ---*/
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "quiz_database";
    private static final int DATABASE_VERSION = 1;

    /** 問題データを収めた assets のファイル名 */
    static final String ASSET_FILE_NAME = "initial_data_math.json";

    static final String TABLE_QUIZ = "quiz_questions";

    /** 1問あたりの選択肢の数。テーブルの option1〜option4 と対応する */
    static final int CHOICE_COUNT = 4;

    // JSON のキー名。読み込み側で綴りがずれないよう一箇所にまとめる
    private static final String KEY_TABS = "tabs";
    private static final String KEY_NAME = "name";
    private static final String KEY_BUTTONS = "buttons";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_QUESTIONS = "questions";
    private static final String KEY_QUESTION = "question";
    private static final String KEY_CHOICES = "choices";
    private static final String KEY_CORRECT_ANSWER = "correct_answer";

    private static final String CREATE_QUIZ_TABLE =
            "CREATE TABLE " + TABLE_QUIZ + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "question TEXT NOT NULL," +
            "option1 TEXT NOT NULL," +
            "option2 TEXT NOT NULL," +
            "option3 TEXT NOT NULL," +
            "option4 TEXT NOT NULL," +
            "correct_answer INTEGER)";

    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // Activity を握り続けないよう、アプリケーション側の Context を保持する
        this.context = context.getApplicationContext();
    }

    // JSONファイルを読み込んで、String型のデータに変換している。
    // MainActivityで使われる
    public String loadJSONFromAsset() {
        try (InputStream is = context.getAssets().open(ASSET_FILE_NAME)) {
            // available() は今すぐ読める見込みの件数しか返さないため、最後まで読み切る
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int read;
            while ((read = is.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e(TAG, ASSET_FILE_NAME + " の読み込みに失敗しました。", e);
            return null;
        }
    }

    // String型になったJSONデータをMap<String, Map<String, Data_Button>>としてデータを格納する。
    // 最初のStringはタブ名、2番目のStringはボタンID
    public Map<String, Map<String, Data_Button>> parseQuizQuestions(String jsonData) {
        // 読み込んだデータの順序を保持するために、LinkedHashMapを使用する。
        LinkedHashMap<String, Map<String, Data_Button>> tabQuestions = new LinkedHashMap<>();
        if (jsonData == null) {
            return tabQuestions;
        }

        try {
            JSONArray tabsArray = new JSONObject(jsonData).getJSONArray(KEY_TABS);

            for (int t = 0; t < tabsArray.length(); t++) {
                JSONObject tabObject = tabsArray.getJSONObject(t);
                String tabName = tabObject.getString(KEY_NAME);
                JSONArray buttonsArray = tabObject.getJSONArray(KEY_BUTTONS);

                LinkedHashMap<String, Data_Button> buttonData = new LinkedHashMap<>();

                for (int b = 0; b < buttonsArray.length(); b++) {
                    JSONObject buttonObject = buttonsArray.getJSONObject(b);
                    String buttonId = buttonObject.getString(KEY_ID);
                    String title = buttonObject.getString(KEY_TITLE);
                    JSONArray questionsArray = buttonObject.getJSONArray(KEY_QUESTIONS);

                    List<Data_Quiz> questions = new ArrayList<>();
                    for (int q = 0; q < questionsArray.length(); q++) {
                        Data_Quiz quiz = toQuiz(questionsArray.getJSONObject(q));
                        // 未記入の設問を出題すると、問題文が空欄のまま表示されてしまう
                        if (isUsable(quiz)) {
                            questions.add(quiz);
                        }
                    }

                    buttonData.put(buttonId, new Data_Button(buttonId, title, questions));
                }

                tabQuestions.put(tabName, buttonData);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON解析エラー", e);
        }

        return tabQuestions;
    }

    /**
     * タブ・問題集の階層をたどって、全設問を1つのリストに平坦化する。
     *
     * <p>DB への初期投入とタブ表示用の {@link #parseQuizQuestions} が、
     * 同じファイルを別の形式だと解釈してしまう事故を防ぐため、
     * 設問の読み取りは {@link #toQuiz} に集約している。
     *
     * <p>Android に依存しないため、ユニットテストから直接呼び出せる。
     */
    static List<Data_Quiz> extractAllQuestions(String jsonData) throws JSONException {
        List<Data_Quiz> all = new ArrayList<>();
        JSONArray tabsArray = new JSONObject(jsonData).getJSONArray(KEY_TABS);

        for (int t = 0; t < tabsArray.length(); t++) {
            JSONArray buttonsArray = tabsArray.getJSONObject(t).getJSONArray(KEY_BUTTONS);
            for (int b = 0; b < buttonsArray.length(); b++) {
                JSONArray questionsArray = buttonsArray.getJSONObject(b).getJSONArray(KEY_QUESTIONS);
                for (int q = 0; q < questionsArray.length(); q++) {
                    all.add(toQuiz(questionsArray.getJSONObject(q)));
                }
            }
        }
        return all;
    }

    /**
     * 出題・検索の対象にできる設問かどうかを判定する。
     *
     * <p>同梱データには問題文が未記入のまま残っている設問があり、
     * そのまま出題すると空欄の問題が表示され、DB 側も option 列が
     * NOT NULL のため登録できない。
     */
    static boolean isUsable(Data_Quiz quiz) {
        return !quiz.getQuestion().trim().isEmpty()
                && quiz.getChoices().size() == CHOICE_COUNT;
    }

    /** 設問1問分の JSON を Data_Quiz に変換する */
    private static Data_Quiz toQuiz(JSONObject questionObject) throws JSONException {
        String questionText = questionObject.getString(KEY_QUESTION);
        JSONArray choicesArray = questionObject.getJSONArray(KEY_CHOICES);

        List<String> choices = new ArrayList<>();
        for (int i = 0; i < choicesArray.length(); i++) {
            choices.add(choicesArray.getString(i));
        }
        return new Data_Quiz(questionText, choices, questionObject.getInt(KEY_CORRECT_ANSWER));
    }

    // アプリのインストール時に一回だけ行われるメソッド
    //　もう一度行いたい場合は、アンインストールしてから再度インストールする。
    @Override
    public void onCreate(SQLiteDatabase db) {
        // クイズデータを格納するためのテーブルを作成
        db.execSQL(CREATE_QUIZ_TABLE);

        // JSONファイルから初期データを挿入
        insertInitialData(db);
    }

    // 初期データをデータベースに挿入するメソッド
    private void insertInitialData(SQLiteDatabase db) {
        String jsonData = loadJSONFromAsset();
        if (jsonData == null) {
            return; // 読み込み失敗は loadJSONFromAsset 側でログ出力済み
        }

        List<Data_Quiz> questions;
        try {
            questions = extractAllQuestions(jsonData);
        } catch (JSONException e) {
            Log.e(TAG, "初期データの解析に失敗しました。", e);
            return;
        }

        int inserted = 0;
        int skipped = 0;
        db.beginTransaction();
        try {
            for (Data_Quiz quiz : questions) {
                if (!isUsable(quiz)) {
                    Log.w(TAG, "未完成の設問のため登録を見送りました: 「"
                            + quiz.getQuestion() + "」");
                    skipped++;
                    continue;
                }
                List<String> choices = quiz.getChoices();

                ContentValues values = new ContentValues();
                values.put(KEY_QUESTION, quiz.getQuestion());
                for (int i = 0; i < CHOICE_COUNT; i++) {
                    values.put("option" + (i + 1), choices.get(i));
                }
                values.put(KEY_CORRECT_ANSWER, quiz.getCorrectAnswerIndex());
                db.insert(TABLE_QUIZ, null, values);
                inserted++;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        Log.i(TAG, "初期データを登録しました。登録 " + inserted + "件 / 見送り " + skipped + "件");
    }

    // 検索クエリに基づいてデータベースから質問を検索するメソッド
    // 問題文に基づいてデータを検索
    public List<Data_Quiz> searchQuizQuestions(String query) {
        List<Data_Quiz> matchedQuestions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT * FROM " + TABLE_QUIZ + " WHERE " + KEY_QUESTION + " LIKE ?";
        try (Cursor cursor = db.rawQuery(sql, new String[]{"%" + query + "%"})) {
            int questionColumnIndex = cursor.getColumnIndex(KEY_QUESTION);
            int correctAnswerColumnIndex = cursor.getColumnIndex(KEY_CORRECT_ANSWER);
            if (questionColumnIndex == -1 || correctAnswerColumnIndex == -1) {
                Log.e(TAG, "想定した列が見つかりませんでした。");
                return matchedQuestions;
            }

            while (cursor.moveToNext()) {
                List<String> choices = new ArrayList<>();
                for (int i = 1; i <= CHOICE_COUNT; i++) {
                    int choiceColumnIndex = cursor.getColumnIndex("option" + i);
                    if (choiceColumnIndex != -1) {
                        choices.add(cursor.getString(choiceColumnIndex));
                    }
                }
                matchedQuestions.add(new Data_Quiz(
                        cursor.getString(questionColumnIndex),
                        choices,
                        cursor.getInt(correctAnswerColumnIndex)));
            }
        }
        return matchedQuestions;
    }

    // データベースをバージョンアップしたときに、テーブルを再作成
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // DROP しただけでは次回以降のクエリがテーブル不在で失敗するため、
        // 作り直しと初期データの再投入まで行う
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZ);
        onCreate(db);
    }
}
