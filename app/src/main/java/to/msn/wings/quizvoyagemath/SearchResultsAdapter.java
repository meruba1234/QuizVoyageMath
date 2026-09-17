package to.msn.wings.quizvoyagemath;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    private List<Data_Quiz> searchResults;

    // コンストラクタ
    public SearchResultsAdapter(List<Data_Quiz> searchResults) {
        this.searchResults = searchResults;
    }

    // ビューホルダーを生成
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_result_item, parent, false);
        return new ViewHolder(view);
    }

    // ビューにデータを割り当て
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // データをビューホルダーのUIコンポーネントにバインド
        Data_Quiz quiz = searchResults.get(position);
        holder.textViewQuestion.setText(quiz.getQuestion());
        // 他の情報も同様にセットする
    }

    // アイテムの総数を返す
    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    // ViewHolderクラス
    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewQuestion;

        public ViewHolder(View view) {
            super(view);
            // UIコンポーネントの参照を取得
            textViewQuestion = view.findViewById(R.id.textViewQuestion);
        }
    }

    // データセットを更新するためのメソッド（修正）
    public void setDataSet(List<Data_Quiz> dataSet) {
        this.searchResults = dataSet;
        notifyDataSetChanged();
    }

}

