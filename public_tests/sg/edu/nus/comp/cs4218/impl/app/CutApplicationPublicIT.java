package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CutApplicationPublicIT {
    public static final String CHAR_FLAG = "-c";
    public static final String BYTE_FLAG = "-b";
    public static final String TEST_RANGE = "1-3";
    CutApplication cutApplication;

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
    void run_SingleLineByCharRange_ReturnCutByLine() throws Exception {
        String[] argList = new String[]{CHAR_FLAG, TEST_RANGE};
        InputStream stdin = generateInputStreamFromStrings("hello world");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        cutApplication.run(argList, stdin, output);
        assertEquals("hel" + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleLineByByteRange_ReturnCutByByte() throws Exception {
        String[] argList = new String[]{BYTE_FLAG, TEST_RANGE};
        InputStream stdin = generateInputStreamFromStrings("hello world");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        cutApplication.run(argList, stdin, output);
        assertEquals("hel" + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleLinesByCharRange_ReturnCutContentAtEachLineByByte() throws Exception {
        String[] argList = new String[]{CHAR_FLAG, TEST_RANGE};
        InputStream stdin = generateInputStreamFromStrings("hello", "world");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        cutApplication.run(argList, stdin, output);
        assertEquals("hel" + STRING_NEWLINE + "wor" + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleLinesByByteRange_ReturnCutContentAtEachLineByByte() throws Exception {
        String[] argList = new String[]{BYTE_FLAG, TEST_RANGE};
        InputStream stdin = generateInputStreamFromStrings("hello", "world");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        cutApplication.run(argList, stdin, output);
        assertEquals("hel" + STRING_NEWLINE + "wor" + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_InvalidFile_WritesErrorMessageToStdout() throws Exception {
        String[] argList = new String[]{BYTE_FLAG, TEST_RANGE, "invalidFile"};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        cutApplication.run(argList, System.in, output);
        String expected = "cut: 'invalidFile': No such file or directory" + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }
}
