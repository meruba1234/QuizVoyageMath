package to.msn.wings.quizvoyagemath;

import java.util.List;

/*
Data_Buttonクラスには、ボタンのID、タイトル、および関連する問題のリストが含まれているべき。
これは、ユーザーがUI上のボタンを押すとき、対応する問題が表示されるようにするため。
*/

/*--- 各ボタンに対して異なるクイズセットを持たせるためのクラス ---*/
public class Data_Button {
    private String buttonId; // ボタンID(ボタンの識別子)
    private String buttonTitle; // ボタンのタイトル
    private List<Data_Quiz> questions; // 問題のリスト(各ボタンがクリックされたときに表示される問題のリスト)

    // コンストラクタ
    public Data_Button(String buttonId, String title, List<Data_Quiz> questions) {
        this.buttonId = buttonId;
        this.buttonTitle = title;
        this.questions = questions;
    }

    // ゲッターメソッド
    public String getButtonId() {
        return buttonId;
    }

    public String getButtonTitle() {
        return buttonTitle;
    }

    public List<Data_Quiz> getQuestions() {
        return questions;
    }
}