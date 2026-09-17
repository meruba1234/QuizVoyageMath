package to.msn.wings.quizvoyagemath;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 検索結果と検索履歴の両方を表示するアダプター。
 *
 * <p>検索語が空のときは履歴を、入力があるときは検索結果を並べる。
 * 行のレイアウトは共通で、履歴の行だけタップで再検索できる。
 */
public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    /** 履歴の行がタップされたときの通知先 */
    public interface OnHistoryClickListener {
        void onHistoryClick(String query);
    }

    /** 表示中の要素。Data_Quiz なら検索結果、String なら検索履歴 */
    private final List<Object> items = new ArrayList<>();
    private final OnHistoryClickListener historyClickListener;

    public SearchResultsAdapter(OnHistoryClickListener historyClickListener) {
        this.historyClickListener = historyClickListener;
    }

    /** 検索結果を表示する */
    public void showResults(List<Data_Quiz> results) {
        items.clear();
        items.addAll(results);
        notifyDataSetChanged();
    }

    /** 検索履歴を表示する */
    public void showHistory(List<String> history) {
        items.clear();
        items.addAll(history);
        notifyDataSetChanged();
    }

    // ビューホルダーを生成
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_result_item, parent, false);
        return new ViewHolder(view);
    }

    // ビューにデータを割り当て
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object item = items.get(position);

        if (item instanceof Data_Quiz) {
            holder.textViewQuestion.setText(((Data_Quiz) item).getQuestion());
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);
        } else {
            String query = (String) item;
            holder.textViewQuestion.setText(query);
            holder.itemView.setOnClickListener(v -> {
                if (historyClickListener != null) {
                    historyClickListener.onHistoryClick(query);
                }
            });
        }
    }

    // アイテムの総数を返す
    @Override
    public int getItemCount() {
        return items.size();
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
}
