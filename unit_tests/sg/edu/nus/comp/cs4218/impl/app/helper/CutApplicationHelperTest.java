package sg.edu.nus.comp.cs4218.impl.app.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.impl.app.helper.CutApplicationHelper.cutSelectedPortions;
import static sg.edu.nus.comp.cs4218.testutils.AssertUtils.assertSameList;
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
    private static final Integer CONTENT_LENGTH = 10;
    private InputStream stdin;

    @BeforeEach
    void setUp() {
        stdin = new ByteArrayInputStream(STDIN_CONTENT.getBytes());
    }

    @Test
    void cutSelectedPortions_NoCutOptionsSelected_ThrowsIllegalArgException() {
        IllegalArgumentException result = assertThrowsExactly(IllegalArgumentException.class, () ->
                cutSelectedPortions(false, false, RANGE_ONE_TO_FIVE, stdin)
        );
        String expected = "You must specify either cut by character or byte";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void cutSelectedPortions_EmptyStdin_ReturnsEmptyList() {
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        List<String> result = assertDoesNotThrow(() ->
                cutSelectedPortions(true, false, RANGE_ONE_TO_FIVE, stdin)
        );
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void cutSelectedPortions_RangeStartValueLessThanOne_ReturnsEmptyList() {
        List<int[]> range = List.of(new int[]{-1, 5});
        List<String> result = assertDoesNotThrow(() ->
                cutSelectedPortions(true, false, range, stdin)
        );
        List<String> expected = List.of("", ""); // 0-based index
        assertSameList(expected, result);
    }

    @Test
    void cutSelectedPortions_RangeStartValueMoreThanFileLineLength_ReturnsEmptyList() {
        List<int[]> range = List.of(new int[]{Integer.MAX_VALUE, 5});
        List<String> result = assertDoesNotThrow(() ->
                cutSelectedPortions(true, false, range, stdin)
        );
        List<String> expected = List.of("", ""); // 0-based index
        assertSameList(expected, result);
    }

    @Test
    void cutSelectedPortions_RangeStartValueEqualToFileLineLength_ReturnsEmptyList() {
        List<int[]> range = List.of(new int[]{CONTENT_LENGTH + 1, 5}); // 0-based index
        List<String> result = assertDoesNotThrow(() ->
                cutSelectedPortions(true, false, range, stdin)
        );
        List<String> expected = List.of("", ""); // 0-based index
        assertSameList(expected, result);
    }

    @Test
    void cutSelectedPortions_RangeEndValueSmallerThanStartValue_ThrowsStringIndexOutOfBoundException() {
        List<int[]> range = List.of(new int[]{5, 0});
        assertThrowsExactly(StringIndexOutOfBoundsException.class, () ->
                cutSelectedPortions(true, false, range, stdin)
        );
    }

    @Test
    void cutSelectedPortions_RangeEndValueLargerThanStdinLength_ReturnsUntilEndOfStdin() {
        List<int[]> range = List.of(new int[]{5, Integer.MAX_VALUE});
        List<String> result = assertDoesNotThrow(() ->
                cutSelectedPortions(true, false, range, stdin)
        );
        List<String> expected = List.of("567890", "654321"); // 0-based index
        assertSameList(expected, result);
    }

    @Test
    void cutSelectedPortions_CutByChar_ReturnsCutStringList() {
        List<String> result = assertDoesNotThrow(() ->
                cutSelectedPortions(true, false, RANGE_ONE_TO_FIVE, stdin)
        );
        List<String> expected = List.of("12345", "09876");
        assertSameList(expected, result);
    }

    @Test
    void cutSelectedPortions_CutByByte_ReturnsCutStringList() {
        List<String> result = assertDoesNotThrow(() ->
                cutSelectedPortions(false, true, RANGE_ONE_TO_FIVE, stdin)
        );
        List<String> expected = List.of("12345", "09876");
        assertSameList(expected, result);
    }
}
