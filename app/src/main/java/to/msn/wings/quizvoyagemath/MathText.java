package to.msn.wings.quizvoyagemath;

import java.util.regex.Pattern;

/**
 * 文字列を数式として描画すべきか、通常のテキストとして描画すべきかを判定する。
 *
 * <p>同梱している JLaTeXMath は日本語のグリフを持たないため、日本語を含む文字列を
 * 数式として描画すると、その文字が丸ごと消えてしまう（幅が増えないことを
 * LatexDelimiterProbeTest で確認済み）。
 * そのため、日本語を含まない純粋な数式のときだけ数式として描画し、
 * それ以外は通常のテキストとして表示する。
 */
public final class MathText {

    private MathText() {
    }

    /** LaTeX のコマンドや区切り記号らしさ */
    private static final Pattern MATH_MARKERS =
            Pattern.compile("\\\\[a-zA-Z]+|\\$|\\\\\\(|\\\\\\[|\\^\\{|_\\{");

    /** JLaTeXMath が描画できない文字（日本語など ASCII 外） */
    private static final Pattern NON_ASCII = Pattern.compile("[^\\x00-\\x7F]");

    /**
     * 数式として描画してよいかを返す。
     *
     * <p>数式の記法を含み、かつ日本語などの非 ASCII 文字を含まない場合だけ true。
     */
    public static boolean isRenderableMath(String text) {
        if (text == null) {
            return false;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        return MATH_MARKERS.matcher(trimmed).find()
                && !NON_ASCII.matcher(trimmed).find();
    }
}
