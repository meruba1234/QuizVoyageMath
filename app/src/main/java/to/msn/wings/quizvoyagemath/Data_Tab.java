package to.msn.wings.quizvoyagemath;

import java.util.List;
import java.util.Map;

public class Data_Tab {
    private String tabTitle; // タブのタイトル
    private List<String> buttonTitles; // タブごとに異なる、ボタンのタイトルリスト
    private Map<String, Data_Button> buttonDataMap; // 各ボタンIDに対応するQuizDataのマップ
    private String point; // タブに関連するポイント

    // コンストラクタ
    public Data_Tab(String tabTitle, List<String> buttonTitles, Map<String, Data_Button> buttonDataMap, String point) {
        this.tabTitle = tabTitle;
        this.buttonTitles = buttonTitles;
        this.buttonDataMap = buttonDataMap;
        this.point = point;
    }

    // ゲッター
    public String getTabTitle() {
        return tabTitle;
    }

    public List<String> getButtonTitles() {
        return buttonTitles;
    }

    public Map<String, Data_Button> getButtonDataMap() {
        return buttonDataMap;
    }

    public String getPoint() {
        return point;
    }
}
