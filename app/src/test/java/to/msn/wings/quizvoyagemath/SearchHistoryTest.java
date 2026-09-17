package to.msn.wings.quizvoyagemath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

/** 検索履歴の並び・重複排除・件数制限・永続化を検証する */
@RunWith(RobolectricTestRunner.class)
public class SearchHistoryTest {

    private Context context;
    private SearchHistory history;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        history = new SearchHistory(context);
        history.clear();
    }

    @Test
    public void 新しい順に並ぶ() {
        history.add("細胞");
        history.add("酵素");

        assertEquals(Arrays.asList("酵素", "細胞"), history.getAll());
    }

    @Test
    public void 同じ語は重複せず先頭に移動する() {
        history.add("細胞");
        history.add("酵素");
        history.add("細胞");

        assertEquals(Arrays.asList("細胞", "酵素"), history.getAll());
    }

    @Test
    public void 上限を超えたら古いものから消える() {
        for (int i = 1; i <= SearchHistory.MAX_SIZE + 5; i++) {
            history.add("語" + i);
        }

        List<String> all = history.getAll();
        assertEquals(SearchHistory.MAX_SIZE, all.size());
        assertEquals("語" + (SearchHistory.MAX_SIZE + 5), all.get(0));
        assertTrue("最も古い語が残っている", !all.contains("語1"));
    }

    @Test
    public void 空白だけの語は記録しない() {
        history.add("   ");
        history.add("");
        history.add(null);

        assertTrue(history.getAll().isEmpty());
    }

    @Test
    public void 前後の空白は取り除いて記録する() {
        history.add("  細胞  ");

        assertEquals(Arrays.asList("細胞"), history.getAll());
    }

    @Test
    public void 作り直しても履歴が残る() {
        history.add("細胞");
        history.add("酵素");

        SearchHistory reopened = new SearchHistory(context);

        assertEquals(Arrays.asList("酵素", "細胞"), reopened.getAll());
    }

    @Test
    public void クリアすると空になる() {
        history.add("細胞");
        history.clear();

        assertTrue(history.getAll().isEmpty());
        assertTrue(new SearchHistory(context).getAll().isEmpty());
    }
}
