package to.msn.wings.quizvoyagemath;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class Data_Quiz implements Serializable {
    private String question; // 問題文
    private List<String> choices; // 選択肢
    private int correctAnswerIndex; // 正解のインデックス

    // コンストラクタ
    public Data_Quiz(String question, List<String> choices, int correctAnswerIndex) {
        this.question = question;
        this.choices = choices;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    // ゲッター
    public String getQuestion() {
        return question;
    }

    public List<String> getChoices() {
        return choices;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    // 選択肢をランダムにシャッフルし、正解のインデックスを更新するメソッド
    public void shuffleChoices(){
        // 正解の選択肢のテキストを保存
        String correctAnswer = choices.get(correctAnswerIndex);

        // 選択肢をシャッフル
        Collections.shuffle(choices);

        // 正解の新しいインデックスを見つけて更新
        correctAnswerIndex = choices.indexOf(correctAnswer);
    }
}
