package to.msn.wings.quizvoyagemath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

/**
 * DatabaseHelper を実際の SQLite 上で動かして検証する。
 *
 * <p>初期データの投入が失敗しても例外が握りつぶされていたため、
 * 問題テーブルが空のまま検索が常に0件を返していた。
 * 同じことが起きたら気づけるようにする。
 */
@RunWith(RobolectricTestRunner.class)
public class DatabaseHelperTest {

    private DatabaseHelper helper;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        helper = new DatabaseHelper(context);
    }

    @After
    public void tearDown() {
        helper.close();
    }

    /** 初期データが実際に DB へ入り、検索がヒットすること */
    @Test
    public void 検索が結果を返す() {
        List<Data_Quiz> results = helper.searchQuizQuestions("の");

        assertFalse("検索結果が0件。初期データが投入されていない可能性がある", results.isEmpty());
        for (Data_Quiz quiz : results) {
            assertTrue("問題文に検索語が含まれていない: " + quiz.getQuestion(),
                    quiz.getQuestion().contains("の"));
            assertEquals(DatabaseHelper.CHOICE_COUNT, quiz.getChoices().size());
        }
    }

    /** 空文字で検索すると、登録済みの全設問が返ること（LIKE '%%' のため） */
    @Test
    public void 全件が登録されている() {
        // 未記入の2問は登録対象から外れる
        assertEquals(51 - 2, helper.searchQuizQuestions("").size());
    }

    /** 一致しない語では0件になること */
    @Test
    public void 一致しない語では0件になる() {
        assertTrue(helper.searchQuizQuestions("該当しない文字列ZZZ").isEmpty());
    }

    /** 未記入の設問は検索結果に現れないこと */
    @Test
    public void 未記入の設問は検索に出てこない() {
        for (Data_Quiz quiz : helper.searchQuizQuestions("")) {
            assertFalse("問題文が空の設問が登録されている", quiz.getQuestion().trim().isEmpty());
        }
    }
}
