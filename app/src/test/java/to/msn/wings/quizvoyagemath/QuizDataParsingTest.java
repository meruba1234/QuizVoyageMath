package to.msn.wings.quizvoyagemath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 同梱している問題データ（assets/initial_data.json）を、
 * アプリの読み取りロジックが実際に解釈できるかを検証する。
 *
 * <p>かつて、DB への初期投入だけが同じファイルを JSON 配列として読もうとしており、
 * 例外がログ出力だけで握りつぶされていたため、問題テーブルが空のまま
 * 検索が常に0件を返していた。その種の取り違えを検出するためのテスト。
 */
public class QuizDataParsingTest {

    /** ユニットテストの実行ディレクトリは app モジュール直下になる */
    private static final Path ASSET_PATH =
            Paths.get("src/main/assets", DatabaseHelper.ASSET_FILE_NAME);

    private String readAsset() throws IOException {
        return new String(Files.readAllBytes(ASSET_PATH), StandardCharsets.UTF_8);
    }

    /** 出題・検索に使える設問だけを取り出す */
    private List<Data_Quiz> usableQuestions() throws IOException, JSONException {
        List<Data_Quiz> usable = new ArrayList<>();
        for (Data_Quiz quiz : DatabaseHelper.extractAllQuestions(readAsset())) {
            if (DatabaseHelper.isUsable(quiz)) {
                usable.add(quiz);
            }
        }
        return usable;
    }

    /** 同梱データから設問を1問以上取り出せること（空なら検索機能が成立しない） */
    @Test
    public void 同梱データから設問を取り出せる() throws IOException, JSONException {
        List<Data_Quiz> questions = DatabaseHelper.extractAllQuestions(readAsset());

        assertFalse("同梱データから設問が1件も取れていない", questions.isEmpty());
        assertEquals(51, questions.size());
    }

    /**
     * 同梱データには問題文が未記入の設問が2問だけ残っている
     * （「油脂・セッケンのまとめ2」の2問）。出題対象から除外されること。
     *
     * <p>中身を補ったらこの件数は変わるので、そのときはこのテストも更新する。
     */
    @Test
    public void 未記入の設問は出題対象から外れる() throws IOException, JSONException {
        assertEquals(51 - 2, usableQuestions().size());
    }

    /** 出題対象の設問が DB のテーブル定義（option1〜option4 が NOT NULL）を満たすこと */
    @Test
    public void 出題対象の設問は選択肢を4つ持つ() throws IOException, JSONException {
        for (Data_Quiz quiz : usableQuestions()) {
            assertEquals("選択肢の数が想定と異なる: " + quiz.getQuestion(),
                    DatabaseHelper.CHOICE_COUNT, quiz.getChoices().size());
        }
    }

    /** 正解のインデックスが選択肢の範囲に収まっていること */
    @Test
    public void 正解インデックスが選択肢の範囲内にある() throws IOException, JSONException {
        for (Data_Quiz quiz : DatabaseHelper.extractAllQuestions(readAsset())) {
            int index = quiz.getCorrectAnswerIndex();
            assertTrue("正解インデックスが範囲外: " + quiz.getQuestion(),
                    index >= 0 && index < quiz.getChoices().size());
        }
    }

    /** 出題対象の設問は問題文が空でないこと（question は NOT NULL） */
    @Test
    public void 出題対象の設問は問題文が空でない() throws IOException, JSONException {
        for (Data_Quiz quiz : usableQuestions()) {
            assertFalse("問題文が空", quiz.getQuestion().trim().isEmpty());
        }
    }

    /** トップレベルが配列の JSON は解釈できない。旧実装が踏んでいた形式の取り違えを固定する */
    @Test(expected = JSONException.class)
    public void トップレベルが配列のJSONは解釈できない() throws JSONException {
        DatabaseHelper.extractAllQuestions("[{\"question\":\"x\"}]");
    }

    /** 選択肢をシャッフルしても、正解のインデックスが正解の選択肢を指し続けること */
    @Test
    public void シャッフルしても正解が保たれる() throws IOException, JSONException {
        for (Data_Quiz quiz : DatabaseHelper.extractAllQuestions(readAsset())) {
            String expected = quiz.getChoices().get(quiz.getCorrectAnswerIndex());
            for (int i = 0; i < 20; i++) {
                quiz.shuffleChoices();
                assertEquals("シャッフル後に正解がずれた: " + quiz.getQuestion(),
                        expected, quiz.getChoices().get(quiz.getCorrectAnswerIndex()));
            }
        }
    }
}
