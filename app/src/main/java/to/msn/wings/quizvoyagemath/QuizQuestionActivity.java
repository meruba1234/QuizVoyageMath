package to.msn.wings.quizvoyagemath;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

import ru.noties.jlatexmath.JLatexMathDrawable;
import ru.noties.jlatexmath.JLatexMathView;

// 4択問題画面に遷移後のアクティビティを管理するクラス
public class QuizQuestionActivity extends AppCompatActivity {
    private String buttonId;
    private String tabName;
    private String selectedButtonTitle;

    private List<Data_Quiz> questions; // 問題のリスト
    private int totalQuestions; // 問題リスト内の問題数
    private int currentQuestionIndex = 0; // 現在の問題のインデックス
    private boolean readyForNextQuestion = false; // 次の問題に進む準備ができているか
    private int[] choiceButtonId; // 選択肢ボタンのID配列

    private long startTime; // すべての問題が解き終わるまでのタイマー用変数
    private int correctAnswers = 0; // 正解数をカウントする変数
    private boolean isFirstAnswer = true;// 問題が最初に回答されたかどうかを追跡

    private TextView questionNumberTextView; // 「現在の問題インデックス / 全問題数」を表示するためのTextView
    private TextView tvCorrectAnswersCount; // 「正解数」を表示するTextView
    private Button endQuizButton; // 結果画面への遷移Button

    //カウントダウン用の変数
    private TextView countdownTimerTextView;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quizquestion);

        // IntentからBUTTON_IDとTAB_NAMEとSELECTED_BUTTON_TITLEを取得してセット
        buttonId = getIntent().getStringExtra("BUTTON_ID");
        tabName = getIntent().getStringExtra("TAB_NAME");
        selectedButtonTitle = getIntent().getStringExtra("SELECTED_BUTTON_TITLE");

        // 「現在の問題インデックス / 全問題数」を表示するためのレイアウトへの参照値を取得
        questionNumberTextView = findViewById(R.id.questionNumberTextView);
        // 「正解数」を表示するためのレイアウトへの参照値を取得
        tvCorrectAnswersCount = findViewById(R.id.tvCorrectAnswersCount);

        // textViewを設定して、カウントダウンを始める(4択問題画面に表示するカウントダウン)
        countdownTimerTextView = findViewById(R.id.countdownTimerTextView);
        startCountdownTimer();

        // 解答途中で結果画面に移れるボタンの参照値を取得し、リスナーを設定
        endQuizButton = findViewById(R.id.endQuizButton);
        endQuizButton.setOnClickListener(v -> {
            // タイマーをキャンセルし、結果画面に遷移
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            displayEndMessage();
        });


        // タイマーの開始(全問解き終わるまでの経過時間)
        startTime = System.currentTimeMillis();

        // 選択肢ボタンのID配列を初期化
        choiceButtonId = new int[] {
                R.id.choice1Button,R.id.choice2Button,
                R.id.choice3Button,R.id.choice4Button
        };

        // 選択されたボタンのタイトルをTextViewに設定
        TextView buttonNameTextView = findViewById(R.id.buttonNameTextView);
        if (selectedButtonTitle != null) {
            buttonNameTextView.setText(selectedButtonTitle);
        }

        // Intentから問題リストのデータを取得
        questions = (List<Data_Quiz>) getIntent().getSerializableExtra("QUESTION_DATA");
        // 問題を表示
        if (questions != null && !questions.isEmpty()) {
            displayQuestion(currentQuestionIndex);
        } else {
            // 問題が1問もない問題集を開くと、何も起きない画面で止まってしまうため、
            // 理由を伝えて前の画面に戻す
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
            Toast.makeText(this, R.string.no_questions_available, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 問題文を表示する。
     *
     * <p>JLaTeXMath は日本語を描画できないため、日本語を含む文章は通常の
     * TextView に、日本語を含まない純粋な数式だけ JLatexMathView に流す。
     */
    private void showQuestionText(String text) {
        JLatexMathView questionMathView = findViewById(R.id.questionMathView);
        TextView questionTextView = findViewById(R.id.questionTextView);

        if (MathText.isRenderableMath(text)) {
            questionMathView.setLatex(text);
            questionMathView.setVisibility(View.VISIBLE);
            questionTextView.setVisibility(View.GONE);
        } else {
            questionTextView.setText(text);
            questionTextView.setVisibility(View.VISIBLE);
            questionMathView.setVisibility(View.GONE);
        }
    }

    /**
     * 選択肢をボタンに表示する。
     *
     * <p>純粋な数式のときは描画した数式をボタンの画像として設定する。
     * それ以外は従来どおり文字として表示する。
     */
    private void showChoiceText(Button button, String text) {
        if (MathText.isRenderableMath(text)) {
            JLatexMathDrawable drawable = JLatexMathDrawable.builder(text)
                    .textSize(button.getTextSize())
                    .build();
            button.setText("");
            button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        } else {
            button.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            button.setText(text);
        }
    }

    // 特定の問題を表示するメソッド
    private void displayQuestion(int index) {
        Data_Quiz currentQuestion = questions.get(index);

        // 問題の選択肢をランダムに並び替える処理
        currentQuestion.shuffleChoices();

        // 問題のデータを画面に設定
        showQuestionText(currentQuestion.getQuestion());

        for (int i = 0; i < choiceButtonId.length; i++) {
            Button choiceButton = findViewById(choiceButtonId[i]);
            showChoiceText(choiceButton, currentQuestion.getChoices().get(i));
            changeButtonColor(choiceButton, true);// ボタンの背景色をリセット

            int finalI = i;
            // 4択ボタンを押したときの処理
            choiceButton.setOnClickListener(v -> {
                if (readyForNextQuestion) {//
                    moveToNextQuestion();
                } else {
                    checkAnswer(finalI, currentQuestion.getCorrectAnswerIndex());// ユーザーの回答を処理
                }
            });
        }
        // 次の問題に進むために二つのフラグをリセット
        readyForNextQuestion = false;
        isFirstAnswer = true;

        totalQuestions = questions.size();// 全問題数(questions.size())を取得して代入。

        // 残りの問題数を表示する
        updateQuestionNumberText(index + 1, totalQuestions);

        // カウントダウンを開始する
        startCountdownTimer();
    }



    // ユーザーの回答をチェックする処理
    private void checkAnswer(int userSelection, int correctAnswerIndex) {
        ImageView feedbackImage = findViewById(R.id.feedbackImage);
        Button selectedButton = findViewById(choiceButtonId[userSelection]);// ユーザーが選択したボタン
        Button correctButton = findViewById(choiceButtonId[correctAnswerIndex]);// 正答のボタン
        changeButtonColor(selectedButton, false);

        // 最初の回答時に、
        if (isFirstAnswer) {
            if (countDownTimer != null) {// 例外防止のために条件分岐
                countDownTimer.cancel();
                countDownTimer = null; // カウントダウンタイマーをリセット
            }
            isFirstAnswer = false; // 最初の回答が終わったことを示す
            // 正解の場合
            if (userSelection == correctAnswerIndex) {
                // 正解数のカウント
                correctAnswers++;
                // 正解数を表示
                tvCorrectAnswersCount.setText("正解数：" + correctAnswers);
                // 最初の回答フラグをオフにする
                isFirstAnswer = false;

                handleCorrectAnswer(correctButton);// 正解時の共通処理メソッドへ移動
                // 不正解の場合
            } else {
                // 青いバツ印を表示
                feedbackImage.setImageResource(R.drawable.blue_x);
                feedbackImage.setVisibility(View.VISIBLE);
                // トーストを画面中央に表示
                Toast toast = Toast.makeText(this, "不正解！", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, -200);
                toast.show();
            }
            // 二回目以降の回答時
        } else {
            // 二回目以降に正解した場合
            if (userSelection == correctAnswerIndex) {
                handleCorrectAnswer(correctButton);// 正解時の共通処理メソッドへ移動

                // 二回目以降に不正解した場合
            } else {

                // 青いバツ印を表示
                feedbackImage.setImageResource(R.drawable.blue_x);
                feedbackImage.setVisibility(View.VISIBLE);
                // トーストを画面中央に表示
                Toast toast = Toast.makeText(this, "不正解！", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, -200);
                toast.show();
            }

        }
    }

    // 正解時の共通処理を行うメソッド
    private void handleCorrectAnswer(Button correctButton) {
        ImageView feedbackImage = findViewById(R.id.feedbackImage);

        // 赤い丸印を表示
        feedbackImage.setImageResource(R.drawable.red_circle);
        feedbackImage.setVisibility(View.VISIBLE);

        // 正解のボタンのテキストを赤く変化させる
        correctButton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.correct_answer_text));

        // トーストを画面中央に表示
        Toast toast = Toast.makeText(this, "正解！", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, -200); // 選択肢に重ならないように少し上に寄せる
        toast.show();

        // 次の問題に進む準備ができたので、trueに設定
        readyForNextQuestion = true;
    }

    // 残りの問題数を表示するメソッド
    // 現在の問題のインデックス (currentQuestionIndex)が実引数
    private void updateQuestionNumberText(int currentQuestionNumber, int totalQuestions) {
        String questionNumberText = currentQuestionNumber + "/" + totalQuestions;
        questionNumberTextView.setText(questionNumberText);
    }

    // カウントダウンタイマーを開始するメソッド
    private void startCountdownTimer() {
        // 既存のタイマーがあればキャンセルする(エラー防止のため)
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        // 次の問題に移って15秒カウントダウンスタート
        countDownTimer = new CountDownTimer(15000, 1000) {
            public void onTick(long millisUntilFinished) {
                countdownTimerTextView.setText("残り" + String.valueOf(millisUntilFinished / 1000) + "秒");
            }
            // カウントダウンが終了したときの処理
            public void onFinish() {
                displayEndMessage();
            }
        }.start();
    }

    // 次の問題に移動するメソッド
    // 次の問題に移るので、currentQuestionIndexを増やす。
    private void moveToNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            displayQuestion(currentQuestionIndex);
            ImageView feedbackImage = findViewById(R.id.feedbackImage);
            feedbackImage.setVisibility(View.GONE);
        } else {
            // すべての問題を終了した場合の処理
            displayEndMessage();
        }
    }

    // 結果画面へ移るときの処理
    private void displayEndMessage() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        showQuestionText("終わり！");
        // タイマーの終了と結果画面への遷移
        long timeTaken = System.currentTimeMillis() - startTime;
        showResults(timeTaken, correctAnswers, totalQuestions);
    }



    // タイマーの終了と結果画面への遷移
    private void showResults(long timeTaken, int correctAnswers, int totalQuestions ) {
        Intent intent = new Intent(this, QuizResultsActivity.class);

        intent.putExtra("TIME_TAKEN", timeTaken);
        intent.putExtra("CORRECT_ANSWERS", correctAnswers);
        intent.putExtra("TOTAL_QUESTIONS", totalQuestions);
        intent.putExtra("BUTTON_ID", buttonId);
        intent.putExtra("TAB_NAME", tabName);
        intent.putExtra("SELECTED_BUTTON_TITLE", selectedButtonTitle);

        startActivity(intent);
        finish();
    }

    // オーバーライドして、アクティビティが破棄された際にタイマーをキャンセル
    /* アクティビティが破棄された後もバックグラウンドでタイマーが動き続け、
       リソースの無駄遣いや意図しない動作を引き起こす可能性があるから。*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    // ボタンが押されたときに背景色とテキストカラーを変更する
    // ここでStyleが上書きされている。
    private void changeButtonColor(Button button, boolean reset) {
        if (reset) {
            //ボタンの背景に設定
            button.setBackgroundResource(R.drawable.button_rounded);
            button.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.button_text_default));
        } else {
            // 押された色に変更する
            // ボタンの背景を設定
            button.setBackgroundResource(R.drawable.button_rounded);
            button.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.button_text_pressed));
        }
    }
}
