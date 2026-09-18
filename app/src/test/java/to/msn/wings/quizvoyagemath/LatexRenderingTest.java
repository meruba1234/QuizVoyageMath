package to.msn.wings.quizvoyagemath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import ru.noties.jlatexmath.JLatexMathAndroid;
import ru.noties.jlatexmath.JLatexMathDrawable;

/**
 * 数式の描画と、数式／通常テキストの振り分けを検証する。
 *
 * <p>JLaTeXMath が日本語を描画できないという前提の上に
 * {@link MathText} の判定が成り立っているため、その前提自体もここで固定する。
 */
@RunWith(RobolectricTestRunner.class)
public class LatexRenderingTest {

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        JLatexMathAndroid.init(context);
    }

    private int width(String latex) {
        return JLatexMathDrawable.builder(latex).textSize(40).build().getIntrinsicWidth();
    }

    private List<Data_Quiz> questions() throws IOException, JSONException {
        String json = new String(Files.readAllBytes(
                Paths.get("src/main/assets", DatabaseHelper.ASSET_FILE_NAME)), StandardCharsets.UTF_8);
        List<Data_Quiz> usable = new ArrayList<>();
        for (Data_Quiz quiz : DatabaseHelper.extractAllQuestions(json)) {
            if (DatabaseHelper.isUsable(quiz)) {
                usable.add(quiz);
            }
        }
        return usable;
    }

    /**
     * JLaTeXMath は日本語を描画しない。
     *
     * <p>文字を増やしても幅が変わらないことで確認する。この前提が崩れた
     * （ライブラリが日本語に対応した）場合はここが落ちるので、
     * MathText の振り分けを見直すきっかけになる。
     */
    @Test
    public void 日本語は数式として描画されない() {
        assertEquals("日本語は文字数を増やしても幅が変わらない",
                width("あ"), width("あいうえお"));
        assertTrue("ASCII は文字数に応じて幅が増える", width("abcde") > width("a"));
    }

    /** $ や \\( \\[ は区切り記号として解釈され、描画には含まれない */
    @Test
    public void 数式の区切り記号は描画に含まれない() {
        assertEquals(width("5"), width("$5$"));
        assertEquals(width("\\sin(x)"), width("\\(\\sin(x)\\)"));
        assertEquals(width("x^2"), width("\\[x^2\\]"));
    }

    /** 同梱データのすべての問題文・選択肢が、例外なく描画または表示できること */
    @Test
    public void 同梱データをすべて表示できる() throws IOException, JSONException {
        int asMath = 0;
        int asText = 0;
        for (Data_Quiz quiz : questions()) {
            List<String> texts = new ArrayList<>(quiz.getChoices());
            texts.add(quiz.getQuestion());
            for (String text : texts) {
                if (MathText.isRenderableMath(text)) {
                    // 数式として描画しても落ちないこと
                    JLatexMathDrawable.builder(text).textSize(40).build();
                    asMath++;
                } else {
                    asText++;
                }
            }
        }
        System.out.println("数式として描画: " + asMath + " 件 / 通常テキスト: " + asText + " 件");
        assertTrue("数式として描画される要素が1つもない", asMath > 0);
        assertTrue("通常テキストとして表示される要素が1つもない", asText > 0);
        assertEquals("問題文と選択肢の総数", 49 * 5, asMath + asText);
    }

    /** 日本語を含む問題文は通常テキスト側に回ること（回さないと画面から消える） */
    @Test
    public void 日本語を含む問題文は通常テキストで表示する() throws IOException, JSONException {
        for (Data_Quiz quiz : questions()) {
            String question = quiz.getQuestion();
            if (question.matches(".*[^\\x00-\\x7F].*")) {
                assertTrue("日本語を含むのに数式として描画されようとしている: " + question,
                        !MathText.isRenderableMath(question));
            }
        }
    }
}
