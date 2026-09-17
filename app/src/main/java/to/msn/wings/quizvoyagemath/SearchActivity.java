package to.msn.wings.quizvoyagemath;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerView;

    private DatabaseHelper databaseHelper;
    private SearchResultsAdapter resultsAdapter;
    private List<Data_Quiz> searchResults;// 検索結果となる、すべての問題データ
    private List<String> searchHistory = new ArrayList<>(); // 検索履歴を保存するリスト
    private List<Object> itemsToShow = new ArrayList<>(); // 検索結果と検索履歴を保持するためのリストを用意


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // データベースヘルパーのインスタンスを初期化
        databaseHelper = new DatabaseHelper(this);

        // SearchViewとRecyclerViewの参照を取得
        searchView = findViewById(R.id.search_view);
        recyclerView = findViewById(R.id.recycler_view_search_results);

        // 検索結果と検索履歴のリストを初期化
        searchResults = new ArrayList<>();
        searchHistory = new ArrayList<>();

        // アダプターを初期化してRecyclerViewに設定
        resultsAdapter = new SearchResultsAdapter(searchResults);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(resultsAdapter);

        // SearchViewのリスナーを設定
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 検索ボタンが押された時の処理
                performSearch(query); // 検索処理
                updateRecyclerView(); // RecyclerViewを更新して結果を表示
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    searchResults.clear(); // 検索文字列が空になったら検索結果をクリア
                    updateRecyclerView(); // 検索履歴を表示
                } else {
                    performSearch(newText); // リアルタイム検索
                }
                return true;
            }
        });
    }

    private void performSearch(String query) {
        // 検索処理を実行し、結果を取得
        searchResults = databaseHelper.searchQuizQuestions(query);

        // 検索結果の取得（仮実装、実際にはDatabaseHelperから検索結果を取得する）
        List<Data_Quiz> results = databaseHelper.searchQuizQuestions(query); // このメソッドはData_Quizのリストを返すように実装する

        // 検索結果をitemsToShowに追加
        itemsToShow.clear(); // 既存のアイテムをクリア
        itemsToShow.addAll(results); // 検索結果を追加
        updateRecyclerView();

    }

    private void updateRecyclerView() {
        // 検索結果が空かどうかによってロジックを分岐
        if (itemsToShow.isEmpty()) {
            // 検索履歴を表示
            itemsToShow.clear();
            itemsToShow.addAll(searchHistory);
        }
        // Adapterにデータセットを更新するメソッドを呼び出す（Adapterの実装を修正する必要がある）
        resultsAdapter.setDataSet(searchResults);
        resultsAdapter.notifyDataSetChanged();
    }
}
