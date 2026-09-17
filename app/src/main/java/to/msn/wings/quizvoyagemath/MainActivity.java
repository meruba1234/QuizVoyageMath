package to.msn.wings.quizvoyagemath;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private Executor executor;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ExecutorとHandlerの初期化
        // バックグラウンドスレッドを使って非同期処理をする準備
        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());

        // バックグラウンドスレッドで、データベース操作を非同期で実行
        executor.execute(() -> {// ラムダ式でrunメソッドの省略
            try {
                // ヘルパーを準備
                DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);

                // 下記2行でJSON形式ファイルを読み込み、Mapに変換
                String jsonData = dbHelper.loadJSONFromAsset();// JSON形式ファイルを読み込み、String型に変換
                Map<String, Map<String, Data_Button>> data = dbHelper.parseQuizQuestions(jsonData);// String型になったJSONをMapに変換

                // UIスレッドでのビュー更新(バックグラウンドでの処理のあとに、UIスレッドで結果を反映)
                // UIスレッド　=　メインスレッド
                handler.post(()-> { // ラムダ式でrunメソッドの省略
                    setUpViewPager(data);
                });
            } catch (Exception e) {
                // 例外処理
                Log.e("MyApp", "MainActivityのOnCreateでの例外です。");
                e.printStackTrace();
            }
        });
    }

    // ViewPagerの設定やタブのレイアウトを更新するためのメソッド
    private void setUpViewPager(Map<String, Map<String, Data_Button>> tabData) {
        // ページで利用するデータをリスト化する
        ArrayList<Data_Tab> data = new ArrayList<>();
        // 拡張for文で、引数tabDataの各entryタブ名とそのタブのボタンIDに関連する問題のマップ）を反復処理
        for (Map.Entry<String, Map<String, Data_Button>> entry : tabData.entrySet()) {
            //Map.EntryはJavaのMapインターフェース内のインターフェースで、Mapに格納されているキーと値のペアを表す。
            //Mapはキーと値のペアの集合で、それぞれのペアは Map.Entry オブジェクトとして扱われる。
            //これにより、キーと値を一緒に処理することができる。
            String tabTitle = entry.getKey();// entryの内のkeyを取得(keyはタブの名前)
            Map<String, Data_Button> buttonQuestionsMap = entry.getValue();// entryの内のvalueを取得(valueは該当タブの問題リスト)

            List<String> buttonTitles = new ArrayList<>();// buttonTitleを格納するためのリストを作成
            for (String buttonId : buttonQuestionsMap.keySet()) { // 拡張for文
                Data_Button buttonData = buttonQuestionsMap.get(buttonId);
                buttonTitles.add(buttonData.getButtonTitle()); // ここでボタンIDをタイトルリストに追加
            }

            // ここで、タブ名と対応するボタンIDの問題リストをListItemに追加
            // 新しいData_Tabオブジェクトを作成し、
            // タブ名(tabTitle)、ボタンIDのリスト（buttonTitles）、
            // そのボタンIDに関連する問題リストのマップ（buttonQuestionsMap）、および「正解ポイント」という表示テキストを渡す。
            // 最後に、Data_tabオブジェクトをdatリストに追加する。
            data.add(new Data_Tab(tabTitle, buttonTitles, buttonQuestionsMap, "正解ポイント"));
        }

        /*TabLayoutとViewPager2の設定*/
        // TabLayoutのインスタンスを作成し、ViewPager2にリンクさせる。
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager2 = findViewById(R.id.viewPager2);

        viewPager2.setAdapter(new MyListAdapter(data, viewPager2));// アダプター設定
        // ページ間の余白を調整
        viewPager2.setOffscreenPageLimit(1); // 隣接ページを1ページのみ保持する
        viewPager2.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER); // オーバースクロールエフェクトを無効化

        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        // tabTitleとbuttonTitleの紐づけ
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, ((tab, position) -> {
            //タブの設定を行う。
            tab.setText(data.get(position).getTabTitle());
            tab.setContentDescription("Page" + (position + 1));
        }));
        tabLayoutMediator.attach();


        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer((@NonNull View page, float position) -> { //ラムダ式でtransformPageメソッドの記述を省略
            //カルーセルを作成する処理
            float offset = position * (dpFormat(2) * dpFormat(10) + dpFormat(10));
            page.setTranslationX(-offset);

        });
        //scrollした際のAnimationを設定する
        viewPager2.setPageTransformer(compositePageTransformer);

    }

    //pxをdpに変換する
    private int dpFormat(int dp) {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

}