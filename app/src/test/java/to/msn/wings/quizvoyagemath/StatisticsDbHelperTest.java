package to.msn.wings.quizvoyagemath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.Cursor;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

/** 成績の保存と取り出しを、実際の SQLite 上で検証する */
@RunWith(RobolectricTestRunner.class)
public class StatisticsDbHelperTest {

    private static final String TAB = "複素数平面";
    private static final String BUTTON = "button1";

    private StatisticsDbHelper helper;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        helper = new StatisticsDbHelper(context);
    }

    /** 保存した成績を取り出せること（正答数の並び） */
    private List<Integer> correctAnswers(int limit) {
        List<Integer> values = new ArrayList<>();
        try (Cursor cursor = helper.getStatisticsForButtonAndTab(BUTTON, TAB, limit)) {
            int index = cursor.getColumnIndex(StatisticsDbHelper.getColumnCorrectAnswers());
            while (cursor.moveToNext()) {
                values.add(cursor.getInt(index));
            }
        }
        return values;
    }

    @Test
    public void 保存した成績を取り出せる() {
        helper.addStatistics(60, 3, BUTTON, TAB);

        assertEquals(1, correctAnswers(10).size());
        assertEquals(Integer.valueOf(3), correctAnswers(10).get(0));
    }

    /** 同じヘルパーを使い続けても動くこと（addStatistics 内で DB を閉じている） */
    @Test
    public void 同じヘルパーで保存と取得を繰り返せる() {
        for (int i = 1; i <= 5; i++) {
            helper.addStatistics(i * 10, i, BUTTON, TAB);
            assertEquals("i=" + i + " で件数が合わない", i, correctAnswers(10).size());
        }
    }

    /** 別の問題集の成績が混ざらないこと */
    @Test
    public void 別の問題集の成績は混ざらない() {
        helper.addStatistics(10, 1, BUTTON, TAB);
        helper.addStatistics(20, 2, "button2", TAB);

        assertEquals(1, correctAnswers(10).size());
        assertEquals(Integer.valueOf(1), correctAnswers(10).get(0));
    }

    /** 古い順に並ぶこと（グラフは左から古い順に並べる想定） */
    @Test
    public void 古い順に並ぶ() {
        for (int i = 1; i <= 5; i++) {
            helper.addStatistics(i * 10, i, BUTTON, TAB);
        }

        assertEquals(java.util.Arrays.asList(1, 2, 3, 4, 5), correctAnswers(10));
    }

    /** 10件を超えて保存したとき、何件残るか（保持方針の確認） */
    @Test
    public void 保持件数の上限を確かめる() {
        for (int i = 1; i <= 15; i++) {
            helper.addStatistics(i * 10, i, BUTTON, TAB);
        }
        List<Integer> kept = correctAnswers(100);
        System.out.println("15件保存したあとに残った件数: " + kept.size() + " -> " + kept);
        assertTrue("上限を超えて際限なく増えている", kept.size() <= 10);
    }

    /**
     * 他の問題集を解くと、この問題集の履歴が消えてしまうか。
     *
     * <p>古いレコードの削除がテーブル全体を対象にしているため、
     * 別の問題集を続けて解くと、この問題集の成績が押し出される。
     */
    @Test
    public void 他の問題集を解いたときに履歴が残るか() {
        // この問題集を3回解く
        for (int i = 1; i <= 3; i++) {
            helper.addStatistics(i * 10, i, BUTTON, TAB);
        }
        assertEquals(3, correctAnswers(100).size());

        // 別の問題集を10回解く
        for (int i = 1; i <= 10; i++) {
            helper.addStatistics(i * 10, i, "button2", TAB);
        }

        List<Integer> remaining = correctAnswers(100);
        System.out.println("別の問題集を10回解いたあと、元の問題集に残った履歴: " + remaining);
        assertEquals("他の問題集を解いても、この問題集の履歴は残るべき", 3, remaining.size());
    }

    /** limit より多い件数は返さないこと */
    @Test
    public void limitを超える件数は返さない() {
        for (int i = 1; i <= 8; i++) {
            helper.addStatistics(i * 10, i, BUTTON, TAB);
        }

        assertEquals(3, correctAnswers(3).size());
    }
}
