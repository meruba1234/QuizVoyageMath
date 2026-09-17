package to.msn.wings.quizvoyagemath;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchView;

    private DatabaseHelper databaseHelper;
    private SearchHistory searchHistory;
    private SearchResultsAdapter resultsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        databaseHelper = new DatabaseHelper(this);
        searchHistory = new SearchHistory(this);

        // SearchViewとRecyclerViewの参照を取得
        searchView = findViewById(R.id.search_view);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_search_results);

        // 履歴の行をタップしたら、その語で検索し直す
        resultsAdapter = new SearchResultsAdapter(this::searchAgain);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(resultsAdapter);

        // 入力前は検索履歴を出しておく
        showHistory();

        // SearchViewのリスナーを設定
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 検索ボタンが押されたときだけ履歴に残す
                searchHistory.add(query);
                showResults(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText == null || newText.isEmpty()) {
                    // 検索文字列が空になったら検索履歴を表示する
                    showHistory();
                } else {
                    showResults(newText); // リアルタイム検索
                }
                return true;
            }
        });
    }

    /** 検索して結果を表示する */
    private void showResults(String query) {
        resultsAdapter.showResults(databaseHelper.searchQuizQuestions(query));
    }

    /** 検索履歴を表示する */
    private void showHistory() {
        resultsAdapter.showHistory(searchHistory.getAll());
    }

    /** 履歴から選ばれた語で検索し直す（submit 扱いなので履歴の先頭に移動する） */
    private void searchAgain(String query) {
        searchView.setQuery(query, true);
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}
