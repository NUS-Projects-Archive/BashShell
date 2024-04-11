import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser;

@SuppressWarnings("PMD.NoPackage")
public class CutArgsParserTest {

    private static final String FLAG_CUT_BY_CHAR = "-c";
    private static final String TOO_LARGE_RANGE = "9223372036854775808"; // 1 value larger than max of Long
    private static final String FILE_ONE = "file1";
    private CutArgsParser parser;

    @BeforeEach
    void setUp() {
        this.parser = new CutArgsParser();
    }

    @Test
    void parse_CutValueTooLarge_ThrowsInvalidArgsException() {
        InvalidArgsException result = assertThrowsExactly(InvalidArgsException.class, () ->
                parser.parse(FLAG_CUT_BY_CHAR, TOO_LARGE_RANGE, FILE_ONE)
        );
        String expected = String.format("byte/character offset '%s' is too large", TOO_LARGE_RANGE);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void getRangeList_CutValueLargerThanMaxInteger_ReturnsMaxInteger() {
        assertDoesNotThrow(() -> parser.parse(FLAG_CUT_BY_CHAR, "5294967296", FILE_ONE));
        List<int[]> actualList = assertDoesNotThrow(() -> parser.getRangeList());
        List<int[]> expected = List.of(new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE});
        for (int i = 0; i < actualList.size(); i++) {
            assertArrayEquals(expected.get(i), actualList.get(i));
        }
    }
}
