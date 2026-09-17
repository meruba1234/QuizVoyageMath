package to.msn.wings.quizvoyagemath;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*--- ユーザーの学習情報統計 ---*/
public class StatisticsDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Statistics.db";// データベース名
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_NAME = "statistics"; //テーブル名
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TIME_TAKEN = "timeTaken"; //経過時間
    private static final String COLUMN_CORRECT_ANSWERS = "correctAnswers"; //正答数
    private static final String COLUMN_TAB_NAME = "tabName"; //選択されたタブ名
    private static final String COLUMN_BUTTON_ID = "buttonId"; //選択されたボタンID
    private static final String COLUMN_TIMESTAMP = "timestamps"; //各レコードがいつ追加されたかを記録するためのカラム(列)

    //コンストラクタ
    public StatisticsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    //　statisticsテーブルの作成
    public void onCreate(SQLiteDatabase db) {
        String CREATE_STATISTICS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TIME_TAKEN + " INTEGER,"
                + COLUMN_CORRECT_ANSWERS + " INTEGER,"
                + COLUMN_BUTTON_ID + " TEXT,"
                + COLUMN_TAB_NAME + " TEXT,"
                + COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_STATISTICS_TABLE);
    }

    // データベースの更新
    // バージョンが変わった場合、既存のテーブルを削除後、再度テーブルを作成
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    //クイズ完了時に統計データを挿入
    public void addStatistics(int timeTaken, int correctAnswers, String buttonId, String tabName) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 既存のレコード数をチェック
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        // レコード数が10を超えていれば、最も古いレコードを削除
        if (count >= 10) {
            db.execSQL("DELETE FROM " + TABLE_NAME + " " +
                    "WHERE " + COLUMN_ID +
                    " IN (SELECT " + COLUMN_ID +
                        " FROM " + TABLE_NAME +
                        " ORDER BY " + COLUMN_TIMESTAMP +
                        " ASC LIMIT " + (count - 9) + ")");
        }

        // 新しい統計データを挿入
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIME_TAKEN, timeTaken); // 経過時間
        values.put(COLUMN_CORRECT_ANSWERS, correctAnswers); // 正答数
        values.put(COLUMN_BUTTON_ID, buttonId); // 選択されたボタンID
        values.put(COLUMN_TAB_NAME, tabName); // 選択されたタブ名

        try {
            db.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            // 例外が発生した場合の処理
            Log.e("Database", "Error while adding statistics", e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // tabNameとbuttonIdに基づいて統計データを取得するメソッド
    public Cursor getStatisticsForButtonAndTab(String buttonId, String tabName, int limit) {
        SQLiteDatabase db = this.getReadableDatabase();

        // SQLクエリを直接記述
        String rawQuery = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + COLUMN_BUTTON_ID + " = ? AND " + COLUMN_TAB_NAME + " = ?" +
                // COLUMN_BUTTON_IDと COLUMN_TAB_NAMEという2つのカラムに基づいて、フィルターをかけるための条件を設定
                // ?はプレースホルダーと呼ぶ。
                " ORDER BY " + COLUMN_TIMESTAMP + " ASC" +
                " LIMIT " + limit; // 最新のデータから順にlimit件分取得する

        // selectionArgs配列を用意して、SQLインジェクションを防ぐ
        String[] selectionArgs = new String[] { buttonId, tabName };

        // rawQueryメソッドを使用してクエリを実行し、Cursorを返す
        return db.rawQuery(rawQuery, selectionArgs);
    }

    public static String getColumnTimeTaken() {
        return COLUMN_TIME_TAKEN;
    }

    public static String getColumnCorrectAnswers() {
        return COLUMN_CORRECT_ANSWERS;
    }

    // 使っていないゲッター
    /*public static String getColumnTimestamp() {
        return COLUMN_TIMESTAMP;
    }

    public static String getTableName() {
        return TABLE_NAME;
    }

    public static String getColumnId() {
        return COLUMN_ID;
    }*/
}
