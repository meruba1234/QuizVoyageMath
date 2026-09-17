package to.msn.wings.quizvoyagemath;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class QuizResultsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_results_activity);

        // QuizQuestionActivityからのIntentを取得
        long timeTaken = getIntent().getLongExtra("TIME_TAKEN", 0);
        int correctAnswers = getIntent().getIntExtra("CORRECT_ANSWERS", 0);
        int totalQuestions = getIntent().getIntExtra("TOTAL_QUESTIONS", 0);
        String buttonId = getIntent().getStringExtra("BUTTON_ID");
        String tabName = getIntent().getStringExtra("TAB_NAME");
        String selectedButtonTitle = getIntent().getStringExtra("SELECTED_BUTTON_TITLE");

        // 経過時間を分と秒に変換
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeTaken);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeTaken) - TimeUnit.MINUTES.toSeconds(minutes);

        // レイアウトへの参照値を取得
        TextView tvButtonNameTitle = findViewById(R.id.tvButtonNameTitle);
        TextView tvTimeTaken = findViewById(R.id.tvTimeTaken);
        TextView tvCorrectAnswers = findViewById(R.id.tvCorrectAnswers);
        Button buttonToMain = findViewById(R.id.buttonToMain);

        tvButtonNameTitle.setText(selectedButtonTitle + "の成績表"); // ボタン名を含めたテキストを設定
        tvTimeTaken.setText(String.format(Locale.getDefault(), "時間: %02d:%02d", minutes, seconds));
        tvCorrectAnswers.setText(String.format(Locale.getDefault(), "正解数: %d/%d", correctAnswers, totalQuestions));

        long time = minutes*60 + seconds;

        // 結果画面が作成された際に統計データを保存
        saveStatistics((int) time, correctAnswers, buttonId, tabName);

        // 統計データの表示
        showStatisticsGraphForButton(buttonId, tabName, totalQuestions);

        // メイン画面へ戻るボタンクリック時の処理
        buttonToMain.setOnClickListener(v -> {
            // MainActivityへのインテントを作成
            Intent intent = new Intent(QuizResultsActivity.this, MainActivity.class);
            // MainActivityを開始
            startActivity(intent);
            // 結果画面を終了（オプション）
            finish();
        });
    }

    // 統計データの保存
    public void saveStatistics(int timeTaken, int correctAnswers, String buttonId, String tabName) {
        StatisticsDbHelper dbHelper = new StatisticsDbHelper(this);
        dbHelper.addStatistics(timeTaken, correctAnswers, buttonId, tabName);
    }

    // 統計データの取得とグラフの作成
    public void showStatisticsGraphForButton(String buttonId, String tabName, int totalQuestions) {
        // StatisticsDbHelperを使用して、特定のボタンとタブに対する統計データをデータベースから取得
        StatisticsDbHelper dbHelper = new StatisticsDbHelper(this);
        // 最新の10件のデータを取得するためのクエリを実行
        Cursor cursor = dbHelper.getStatisticsForButtonAndTab(buttonId, tabName, 10);

        // 経過時間と正答数を格納するためのリストを初期化
        List<Entry> timeEntries = new ArrayList<>(); // 経過時間用のリスト
        List<BarEntry> correctAnswersEntries = new ArrayList<>(); // 正答数用のリスト
        int i = 1; // グラフに表示するためのインデックス変数

        // カラムインデックスを取得
        int timeTakenIndex = cursor.getColumnIndex(StatisticsDbHelper.getColumnTimeTaken());
        int correctAnswersIndex = cursor.getColumnIndex(StatisticsDbHelper.getColumnCorrectAnswers());

        // カラムインデックスのチェック
        if (timeTakenIndex != -1 && correctAnswersIndex != -1) {
            while (cursor.moveToNext()) {
                int timeTaken = cursor.getInt(timeTakenIndex);
                int correctAnswers = cursor.getInt(correctAnswersIndex);

                // グラフに表示するデータを追加
                timeEntries.add(new Entry(i, timeTaken));// iをインデックスとして使用
                correctAnswersEntries.add(new BarEntry(i, correctAnswers));
                i++; // インデックスをインクリメント
            }
        } else {
            // エラーハンドリング: カラムが存在しない場合の処理
            Log.e("Database", "Required column not found in the database");
        }
        cursor.close();

        // 経過時間用のLineDataSetを作成し、スタイルを設定
        LineDataSet elapsedTimeDataSet = new LineDataSet(timeEntries, "経過時間");
        elapsedTimeDataSet.setColor(Color.RED);  // 線の色を設定
        elapsedTimeDataSet.setLineWidth(2f);      // 線の太さを設定
        elapsedTimeDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);// 右側のY軸を使用
        elapsedTimeDataSet.setDrawValues(true);  // データセットの各点に値を表示
        // 「分:秒」形式で点の値を表示させるカスタムValueFormatter
        elapsedTimeDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                // entry.getY()で取得した値は秒単位の想定
                int totalSeconds = (int) entry.getY();
                int minutes = totalSeconds / 60;
                int seconds = totalSeconds % 60;
                return String.format("%d:%02d", minutes, seconds);
            }
        });

        // 正答数用のBarDataSetを作成し、スタイルを設定
        BarDataSet correctAnswersDataSet = new BarDataSet(correctAnswersEntries, "正答数");
        correctAnswersDataSet.setColor(ContextCompat.getColor(getApplicationContext(), R.color.light_green));// 棒の色を設定
        correctAnswersDataSet .setAxisDependency(YAxis.AxisDependency.LEFT);

        // LineDataとBarDataを作成
        LineData lineData = new LineData(elapsedTimeDataSet);
        BarData barData = new BarData(correctAnswersDataSet);
        lineData.setValueTextSize(12);
        barData.setValueTextSize(12);

        // LineDataとBarDataを組み合わせたCombinedDataを作成
        CombinedData combinedData = new CombinedData();
        combinedData.setData(lineData);
        combinedData.setData(barData);

        // CombinedChartにCombinedDataをセットし、グラフを表示
        CombinedChart combinedChart = findViewById(R.id.combinedChart);
        combinedChart.setData(combinedData);
        combinedChart.getDescription().setEnabled(false); // 説明表示なし
        combinedChart.setVisibleXRangeMaximum(10);
        combinedChart.setDoubleTapToZoomEnabled(false); // ダブルタップのズーム無効
        combinedChart.setScaleEnabled(false); // Zoomしない

        // X軸の設定
        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X軸の値を棒グラフの真下に表示
        xAxis.setAxisMinimum(0); // X軸の最小値を設定
        xAxis.setAxisMaximum(11); // X軸の最大値を設定（10個のデータを表示）
        xAxis.setGranularity(1); // X軸の間隔を設定

        xAxis.setLabelCount(11, false); // ラベルを0から10まで1ごとに表示
        xAxis.setDrawGridLines(false); // グリッドラインを非表示にする

        // 右側のY軸(経過時間用)の設定
        YAxis rightAxis = combinedChart.getAxisRight();
        rightAxis.setEnabled(false);

        // 左側のY軸（正答数用）の設定
        YAxis leftAxis = combinedChart.getAxisLeft();
        leftAxis.setAxisMinimum(0); // Y軸の最小値を0に設定
        leftAxis.setAxisMaximum(totalQuestions); // 左側のY軸の最大値を、問題数に設定
        leftAxis.setEnabled(true);

        combinedChart.invalidate(); // グラフを更新
    }
}
