package to.msn.wings.quizvoyagemath;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 検索履歴の保持と永続化を担当する。
 *
 * <p>画面側から切り離してあるため、端末なしでもテストできる。
 * 新しい順に最大 {@link #MAX_SIZE} 件を保持し、同じ語を再検索したときは
 * 重複させずに先頭へ移動させる。
 */
public class SearchHistory {

    /** 保持する履歴の上限 */
    public static final int MAX_SIZE = 10;

    private static final String PREF_NAME = "search_history";
    private static final String KEY_ITEMS = "items";
    /** 検索語に含まれない文字を区切りに使う */
    private static final String SEPARATOR = "\n";

    private final SharedPreferences preferences;
    private final List<String> items = new ArrayList<>();

    public SearchHistory(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        load();
    }

    private void load() {
        items.clear();
        String stored = preferences.getString(KEY_ITEMS, "");
        if (stored == null || stored.isEmpty()) {
            return;
        }
        for (String item : Arrays.asList(stored.split(SEPARATOR, -1))) {
            if (!item.isEmpty()) {
                items.add(item);
            }
        }
    }

    private void save() {
        preferences.edit()
                .putString(KEY_ITEMS, String.join(SEPARATOR, items))
                .apply();
    }

    /**
     * 検索語を履歴に加える。空白のみの語は無視する。
     * 既にある語は重複させず、先頭へ移動させる。
     */
    public void add(String query) {
        if (query == null) {
            return;
        }
        String trimmed = query.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        items.remove(trimmed);
        items.add(0, trimmed);
        while (items.size() > MAX_SIZE) {
            items.remove(items.size() - 1);
        }
        save();
    }

    /** 新しい順の履歴を返す */
    public List<String> getAll() {
        return new ArrayList<>(items);
    }

    public void clear() {
        items.clear();
        save();
    }
}
