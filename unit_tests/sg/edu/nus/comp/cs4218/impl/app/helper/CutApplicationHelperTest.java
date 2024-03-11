package sg.edu.nus.comp.cs4218.impl.app.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CutApplicationHelperTest {

    private static final String STDIN_CONTENT = "1234567890" + STRING_NEWLINE + "0987654321";
    private static final List<int[]> RANGE_ONE_TO_FIVE = List.of(new int[]{1, 5});
    private InputStream stdin;

    @BeforeEach
    void setUp() {
        stdin = new ByteArrayInputStream(STDIN_CONTENT.getBytes());
    }

    @Test
    void cutSelectedPortions_EmptyStdin_ReturnsEmptyList() {
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        List<String> result = assertDoesNotThrow(() ->
                CutApplicationHelper.cutSelectedPortions(true, false, RANGE_ONE_TO_FIVE, stdin)
        );
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void cutSelectedPortions_RangeStartValueLessThanOne_ReturnsEmptyList() {
        List<int[]> range = List.of(new int[]{0, 5});
        List<String> result = assertDoesNotThrow(() ->
                CutApplicationHelper.cutSelectedPortions(true, false, range, stdin)
        );
        List<String> expected = List.of("", ""); // 0-based index
        for (int i = 0; i < result.size(); i++) {
            assertEquals(expected.get(i), result.get(i));
        }
    }

    @Test
    void cutSelectedPortions_RangeEndValueLargerThanStdinLength_ReturnsUntilEndOfStdin() {
        List<int[]> range = List.of(new int[]{5, 100});
        List<String> result = assertDoesNotThrow(() ->
                CutApplicationHelper.cutSelectedPortions(true, false, range, stdin)
        );
        List<String> expected = List.of("567890", "654321"); // 0-based index
        for (int i = 0; i < result.size(); i++) {
            assertEquals(expected.get(i), result.get(i));
        }
    }

    @Test
    void cutSelectedPortions_CutByChar_ReturnsCutStringList() {
        List<String> result = assertDoesNotThrow(() ->
                CutApplicationHelper.cutSelectedPortions(true, false, RANGE_ONE_TO_FIVE, stdin)
        );
        List<String> expected = List.of("12345", "09876");
        for (int i = 0; i < result.size(); i++) {
            assertEquals(expected.get(i), result.get(i));
        }
    }

    @Test
    void cutSelectedPortions_CutByByte_ReturnsCutStringList() {
        List<String> result = assertDoesNotThrow(() ->
                CutApplicationHelper.cutSelectedPortions(false, true, RANGE_ONE_TO_FIVE, stdin)
        );
        List<String> expected = List.of("12345", "09876");
        for (int i = 0; i < result.size(); i++) {
            assertEquals(expected.get(i), result.get(i));
        }
    }
}
