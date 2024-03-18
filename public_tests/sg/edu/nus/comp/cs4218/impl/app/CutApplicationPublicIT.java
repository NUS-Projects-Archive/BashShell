package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.ClassNamingConventions")
public class CutApplicationPublicIT {

    private static final String[] HELLO_WORLD_ARRAY = {"hello", "world"};
    private static final String HELLO_WORLD = "hello world";
    private static final String HEL = "hel";
    private static final String HEL_WOR = "hel" + STRING_NEWLINE + "wor";
    private static final String TEST_RANGE = "1-3";
    private static final String CHAR_FLAG = "-c";
    private static final String BYTE_FLAG = "-b";
    private CutApplication cutApplication;
    private ByteArrayOutputStream output;

    private String joinStringsByLineSeparator(String... strs) {
        return String.join(STRING_NEWLINE, strs);
    }

    private InputStream generateInputStreamFromStrings(String... strs) {
        return new ByteArrayInputStream(joinStringsByLineSeparator(strs).getBytes(StandardCharsets.UTF_8));
    }

    @BeforeEach
    public void setUp() {
        cutApplication = new CutApplication();
        output = new ByteArrayOutputStream();
    }


    @Test
    void run_SingleLineByCharRange_ReturnCutByLine() {
        String[] argList = new String[]{CHAR_FLAG, TEST_RANGE};
        assertDoesNotThrow(() -> {
            InputStream stdin = generateInputStreamFromStrings(HELLO_WORLD);
            cutApplication.run(argList, stdin, output);
        });
        assertEquals(HEL + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleLineByByteRange_ReturnCutByByte() {
        String[] argList = new String[]{BYTE_FLAG, TEST_RANGE};
        assertDoesNotThrow(() -> {
            InputStream stdin = generateInputStreamFromStrings(HELLO_WORLD);
            cutApplication.run(argList, stdin, output);
        });
        assertEquals(HEL + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleLinesByCharRange_ReturnCutContentAtEachLineByByte() {
        String[] argList = new String[]{CHAR_FLAG, TEST_RANGE};
        assertDoesNotThrow(() -> {
            InputStream stdin = generateInputStreamFromStrings(HELLO_WORLD_ARRAY);
            cutApplication.run(argList, stdin, output);
        });
        assertEquals(HEL_WOR + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleLinesByByteRange_ReturnCutContentAtEachLineByByte() {
        String[] argList = new String[]{BYTE_FLAG, TEST_RANGE};
        assertDoesNotThrow(() -> {
            InputStream stdin = generateInputStreamFromStrings(HELLO_WORLD_ARRAY);
            cutApplication.run(argList, stdin, output);
        });
        assertEquals(HEL_WOR + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_InvalidFile_WritesErrorMessageToStdout() throws Exception {
        String[] argList = new String[]{BYTE_FLAG, TEST_RANGE, "invalidFile"};
        cutApplication.run(argList, System.in, output);
        String expected = "cut: 'invalidFile': No such file or directory" + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }
}
