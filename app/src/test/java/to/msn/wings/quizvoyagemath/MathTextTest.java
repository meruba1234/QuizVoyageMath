package to.msn.wings.quizvoyagemath;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** 数式として描画してよいかの判定を検証する */
public class MathTextTest {

    @Test
    public void 純粋な数式は数式として描画する() {
        assertTrue(MathText.isRenderableMath("$\\frac{3}{25} - \\frac{4}{25}i$"));
        assertTrue(MathText.isRenderableMath("\\(\\sin^{-1}(x) + C\\)"));
        assertTrue(MathText.isRenderableMath("\\[x^2\\]"));
    }

    @Test
    public void 日本語を含むものは数式として描画しない() {
        // JLaTeXMath は日本語を描画できず、文字が消えてしまう
        assertFalse(MathText.isRenderableMath("複素数 $z = 3 + 4i$ の絶対値を求めよ。"));
        assertFalse(MathText.isRenderableMath("次の極限値を求めなさい。"));
    }

    @Test
    public void 数式記法がないものは数式として描画しない() {
        assertFalse(MathText.isRenderableMath("Hooke"));
        assertFalse(MathText.isRenderableMath("25"));
    }

    @Test
    public void 空文字やnullは数式として描画しない() {
        assertFalse(MathText.isRenderableMath(""));
        assertFalse(MathText.isRenderableMath("   "));
        assertFalse(MathText.isRenderableMath(null));
    }
}
