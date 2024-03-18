package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.CutException;

public class CutApplicationPublicTest {

    private static final int[] RANGE_1_TO_3 = {1, 3};
    private static final String[] HELLO_WORLD_ARRAY = {"hello", "world"};
    private static final String HELLO_WORLD = "hello world";
    private static final String HEL = "hel";
    private static final String HEL_WOR = "hel" + STRING_NEWLINE + "wor";
    private CutApplication cutApplication;

    private String joinStringsByLineSeparator(String... strs) {
        return String.join(STRING_NEWLINE, strs);
    }

    private InputStream generateInputStreamFromStrings(String... strs) {
        return new ByteArrayInputStream(joinStringsByLineSeparator(strs).getBytes(StandardCharsets.UTF_8));
    }

    @BeforeEach
    public void setUp() {
        cutApplication = new CutApplication();
    }

    @Test
    void cutFromStdin_NullContent_ThrowsCutException() {
        int[] ranges = new int[]{1, 2};
        assertThrows(CutException.class, () -> cutApplication.cutFromStdin(false, true, List.of(ranges), null));
    }

    @Test
    void cutFromStdin_SingleLineByCharRange_ReturnCutByLine() {
        String actual = assertDoesNotThrow(() -> {
            InputStream stdin = generateInputStreamFromStrings(HELLO_WORLD);
            return cutApplication.cutFromStdin(true, false, List.of(RANGE_1_TO_3), stdin);
        });
        assertEquals(HEL, actual);
    }

    @Test
    void cutFromStdin_SingleLineByByteRange_ReturnCutByByte() {
        String actual = assertDoesNotThrow(() -> {
            InputStream stdin = generateInputStreamFromStrings(HELLO_WORLD);
            return cutApplication.cutFromStdin(false, true, List.of(RANGE_1_TO_3), stdin);
        });
        assertEquals(HEL, actual);
    }

    @Test
    void cutFromStdin_MultipleLinesByCharRange_ReturnCutContentAtEachLineByByte() {
        String actual = assertDoesNotThrow(() -> {
            InputStream stdin = generateInputStreamFromStrings(HELLO_WORLD_ARRAY);
            return cutApplication.cutFromStdin(true, false, List.of(RANGE_1_TO_3), stdin);
        });
        assertEquals(HEL_WOR, actual);
    }

    @Test
    void cutFromStdin_MultipleLinesByByteRange_ReturnCutContentAtEachLineByByte() {
        String actual = assertDoesNotThrow(() -> {
            InputStream stdin = generateInputStreamFromStrings(HELLO_WORLD_ARRAY);
            return cutApplication.cutFromStdin(false, true, List.of(RANGE_1_TO_3), stdin);
        });
        assertEquals(HEL_WOR, actual);
    }


    @Test
    void cutFromFile_InvalidFile_ThrowsCutException() {
        assertThrows(CutException.class, () ->
                cutApplication.cutFromFiles(false, true, List.of(RANGE_1_TO_3), "invalidFile")
        );
    }
}
