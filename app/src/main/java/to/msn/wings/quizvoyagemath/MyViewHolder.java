package to.msn.wings.quizvoyagemath;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MyViewHolder extends RecyclerView.ViewHolder {

    // フィールド変数
    public TextView point;
    public ImageButton settingButton;
    public ImageButton searchButton;
    private List<Button> buttons;
    private Context context;

    // コンストラクタ
    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        context = itemView.getContext();
        settingButton = itemView.findViewById(R.id.settingButton);
        point = itemView.findViewById(R.id.point);
        searchButton = itemView.findViewById(R.id.searchButton);

        // 10個のbuttonIdでリストを作成
        buttons = new ArrayList<>();
        int[] buttonId = {R.id.button1,R.id.button2,R.id.button3,R.id.button4,R.id.button5
                ,R.id.button6,R.id.button7,R.id.button8,R.id.button9,R.id.button10};
        for(int i = 0; i < buttonId.length; i++){
            buttons.add((Button) itemView.findViewById(buttonId[i]));
        }

        settingButton.setOnClickListener(v -> {
            // 設定画面への遷移やアクション
            Toast.makeText(this.getContext(),"設定画面への遷移。ここまではOKです。", Toast.LENGTH_SHORT).show();
            // 追加機能を実装する予定
        });


        searchButton.setOnClickListener(v -> {
            Toast.makeText(this.getContext(),"検索画面への遷移。ここまではOKです。", Toast.LENGTH_SHORT).show();
            // SearchActivityを起動するIntentを作成
            Intent intent = new Intent(itemView.getContext(), SearchActivity.class);
            // Intentを使ってSearchActivityを起動
            itemView.getContext().startActivity(intent);
            // 問題データのなかの文字列を検索して、該当の問題を解けるようにする機能を実装予定
        });
    }

    // メイン画面のボタンが押されたときに、4択問題のデータをQuizQuestionActivityに渡す
    public void bind(Data_Tab listItem) {
        String tabName = listItem.getTabTitle();
        Map<String, Data_Button> buttonDataMap = listItem.getButtonDataMap();

        for (int i = 0; i < buttons.size(); i++){//
            Button button = buttons.get(i);
            String buttonId = "button" + (i + 1); // ボタンIDを生成（例: button1, button2）

            // ボタンデータマップから対応するデータを取得
            Data_Button quizData = buttonDataMap.get(buttonId);
            if (quizData != null) {
                button.setVisibility(View.VISIBLE);
                button.setText(quizData.getButtonTitle()); // ボタンにタイトルを設定

                // QuizDataから問題リストを取得
                List<Data_Quiz> questions = quizData.getQuestions();

                // ボタンにリスナーを設定
                button.setOnClickListener(v -> {
                    if (0 < questions.size()) {
                        // メイン画面のボタンが押されたら、questionsリスト内のData_Quizオブジェクトの順序をランダムに並べ替える
                        Collections.shuffle(questions);
                        // 問題リストとボタンタイトルを渡して、アクティビティを開始する。
                        openQuizActivity(questions, quizData.getButtonTitle(), tabName, buttonId);

                    } else {
                        // 問題リストが空だったら、トースト表示する。
                        Toast.makeText(context, R.string.no_questions_available, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // このタブに対応する問題集がないボタン。
                // 後始末をしないと、レイアウトの仮の文字（"22" など）が残り、
                // ページが使い回されたときには別のタブのタイトルとリスナーが
                // そのまま残って、無関係な問題集が開いてしまう。
                // 位置がずれないよう、GONE ではなく INVISIBLE で隠す。
                button.setText("");
                button.setOnClickListener(null);
                button.setVisibility(View.INVISIBLE);
            }
        }
    }

    // QuizQuestionActivityにIntentを使って問題データを渡す
    private void openQuizActivity(List<Data_Quiz> questions, String buttonTitle, String tabName, String buttonId) {
        Intent intent = new Intent(context, QuizQuestionActivity.class);
        // 問題リストをIntentに添付
        intent.putExtra("QUESTION_DATA", (Serializable) questions);
        // 選択されたボタンのタイトルを追加
        intent.putExtra("SELECTED_BUTTON_TITLE", buttonTitle);
        // タブ名とボタンIDを追加
        intent.putExtra("TAB_NAME", tabName);
        intent.putExtra("BUTTON_ID", buttonId);
        context.startActivity(intent);
    }

    public List<Button> getButtons() {
        return buttons;
    }

    public Context getContext() {
        return context;
    }

    public TextView getPoint() {
        return point;
    }

    public ImageButton getsSettingButton() {
        return settingButton;
    }

    public ImageButton getSearchButton() {
        return searchButton;
    }


}
