package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_REGEX;
import static sg.edu.nus.comp.cs4218.impl.util.FileUtils.createNewFile;
import static sg.edu.nus.comp.cs4218.impl.util.FileUtils.deleteFileOrDirectory;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.GrepException;

class GrepApplicationTest {
    private static final String FILE_NAME = "file";
    private static final String FILE_CONTENTS = "tset";

    private static final String STDIN_STRING = "(standard input)";
    private static final String[] STDIN_CONTENTS = new String[]{"test", "tEst"};

    private static final String PATTERN_SMALL_E = "e";
    private static final String PATTERN_BIG_E = "E";

    private static final String SEMICOLON_SPACE = ": ";

    private GrepApplication grepApplication;
    private InputStream stdin;
    private Path file;
    private String fileAbsPath;

    @BeforeEach
    void setUp() {
        grepApplication = new GrepApplication();
        stdin = new ByteArrayInputStream(String.format("%s\n%s", STDIN_CONTENTS[0], STDIN_CONTENTS[1]).getBytes());
        file = createNewFile(FILE_NAME, FILE_CONTENTS);
        fileAbsPath = file.toString();
    }

    @AfterEach
    void tearDown() {
        deleteFileOrDirectory(file);
    }

    /**
     * Test case where -i -c -H are specified.
     */
    @Test
    void grepFromFile_AllFlagsSpecifiedForOneFile_ReturnsCorrectNumberOfMatchingLinesWithFileName() {
        String expected = "1" + STRING_NEWLINE;
        String result = assertDoesNotThrow(() ->
                grepApplication.grepFromFiles(PATTERN_BIG_E, true, true, true, fileAbsPath));
        assertEquals(expected, result);
    }

    /**
     * Test case where -i is specified only.
     */
    @Test
    void grepFromFile_isCaseSensitiveIsTrueForOneFile_ReturnsCorrectMatchingLines() {
        String expected = FILE_CONTENTS + STRING_NEWLINE;
        String result = assertDoesNotThrow(() ->
                grepApplication.grepFromFiles(PATTERN_BIG_E, true, false, true, fileAbsPath));
        assertEquals(expected, result);
    }

    /**
     * Test case where -c is specified.
     */
    @Test
    void grepFromFile_isCountLinesIsTrueForOneFile_ReturnsCorrectNumberOfMatchingLines() {
        String expected = "0" + STRING_NEWLINE;
        String result = assertDoesNotThrow(() ->
                grepApplication.grepFromFiles(PATTERN_BIG_E, false, true, false, fileAbsPath));
        assertEquals(expected, result);
    }

    /**
     * Test case where -H is specified.
     */
    @Test
    void grepFromFile_isPrefixFileNameIsTrueForOneFile_ReturnsCorrectMatchingLinesWithFileName() {
        String expected = FILE_NAME + SEMICOLON_SPACE + FILE_CONTENTS + STRING_NEWLINE;
        String result = assertDoesNotThrow(() ->
                grepApplication.grepFromFiles(PATTERN_SMALL_E, false, false, true, fileAbsPath));
        assertEquals(expected, result);
    }

    /**
     * Test case where no flags are specified but multiple valid files are specified.
     */
    @Test
    void grepFromFile_NoFlagsSpecifiedForMultipleFiles_ReturnsCorrectOutput() {
        // Create a temporary file
        String secFileName = "secFile";
        String secFileContents = "tseet";

        Path secFile = createNewFile(secFileName, secFileContents);

        String secFileAbsPath = secFile.toString();

        String expected = String.join(STRING_NEWLINE,
                fileAbsPath + SEMICOLON_SPACE + FILE_CONTENTS,
                secFileAbsPath + SEMICOLON_SPACE + secFileContents) + STRING_NEWLINE;
        String result = assertDoesNotThrow(() ->
                grepApplication.grepFromFiles(PATTERN_SMALL_E, false, false, false, fileAbsPath, secFileAbsPath));
        assertEquals(expected, result);

        deleteFileOrDirectory(secFile);
    }

    /**
     * Test to check if grepFromFiles throws GrepException when no files are specified.
     */
    @Test
    void grepFromFile_NoFilesSpecified_ThrowsGrepException() {
        GrepException result = assertThrows(GrepException.class,
                () -> grepApplication.grepFromFiles("", false, false, false, null));
        assertEquals("grep: " + NULL_POINTER, result.getMessage());
    }

    /**
     * Test to check if grepFromFiles throws GrepException when invalid pattern is specified.
     */
    @Test
    void grepFromStdin_InvalidPattern_ThrowsGrepException() {
        GrepException result = assertThrows(GrepException.class,
                () -> grepApplication.grepFromStdin("*", false, false, false, stdin));
        assertEquals("grep: " + ERR_INVALID_REGEX, result.getMessage());
    }

    /**
     * Test case where -i -c -H are specified.
     * Returns count of lines that match the pattern.
     */
    @Test
    void grepFromStdin_AllFlagsSpecified_ReturnsCountOfLinesMatchingPattern() {
        String expected = "2" + STRING_NEWLINE;
        String result = assertDoesNotThrow(() ->
                grepApplication.grepFromStdin(PATTERN_SMALL_E, true, true, true, stdin));
        assertEquals(expected, result);
    }

    /**
     * Test case where -i is specified only.
     */
    @Test
    void grepFromStdin_isCaseInsensitiveIsTrue_ReturnsCorrectOutput() {
        String expected = String.join(STRING_NEWLINE, STDIN_CONTENTS) + STRING_NEWLINE;
        String result = assertDoesNotThrow(() ->
                grepApplication.grepFromStdin(PATTERN_SMALL_E, true, false, false, stdin));
        assertEquals(expected, result);
    }

    /**
     * Test case where -c is specified only.
     */
    @Test
    void grepFromStdin_isCountLinesIsTrue_ReturnsCorrectOutput() {
        String expected = "1" + STRING_NEWLINE;
        String result = assertDoesNotThrow(() ->
                grepApplication.grepFromStdin(PATTERN_SMALL_E, false, true, false, stdin));
        assertEquals(expected, result);
    }

    /**
     * Test case where -H is specified.
     */
    @Test
    void grepFromStdin_isPrefixFileNameIsTrue_ReturnsCorrectOutput() {
        String expected = STDIN_STRING + SEMICOLON_SPACE + STDIN_CONTENTS[0] + STRING_NEWLINE;
        String result = assertDoesNotThrow(() ->
                grepApplication.grepFromStdin(PATTERN_SMALL_E, false, false, true, stdin));
        assertEquals(expected, result);
    }

    /**
     * Test case where no flags are specified.
     */
    @Test
    void grepFromStdin_NoFlagsSpecified_ReturnsCorrectOutput() {
        String expected = STDIN_CONTENTS[0] + STRING_NEWLINE;
        String result = assertDoesNotThrow(() ->
                grepApplication.grepFromStdin(PATTERN_SMALL_E, false, false, false, stdin));
        assertEquals(expected, result);
    }

    /**
     * Test case where no flags are specified.
     */
    @Test
    void grepFromFileAndStdin_NoFlagsSpecified_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() ->
                grepApplication.grepFromFileAndStdin(PATTERN_SMALL_E, false, false, false, stdin, FILE_NAME));
        assertEquals(String.join(STRING_NEWLINE,
                        FILE_NAME + SEMICOLON_SPACE + FILE_CONTENTS,
                        STDIN_STRING + SEMICOLON_SPACE + STDIN_CONTENTS[0]),
                result);
    }

    /**
     * Test case where -i is specified only.
     */
    @Test
    void grepFromFileAndStdin_isCaseSensitiveIsFalse_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() ->
                grepApplication.grepFromFileAndStdin(PATTERN_SMALL_E, true, false, false, stdin, FILE_NAME));
        assertEquals(String.join(STRING_NEWLINE,
                        FILE_NAME + SEMICOLON_SPACE + FILE_CONTENTS,
                        STDIN_STRING + SEMICOLON_SPACE + STDIN_CONTENTS[0],
                        STDIN_STRING + SEMICOLON_SPACE + STDIN_CONTENTS[1]),
                result);
    }

    /**
     * Test case where -c is specified only.
     */
    @Test
    void grepFromFileAndStdin_isCountLinesIsTrue_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() ->
                grepApplication.grepFromFileAndStdin(PATTERN_SMALL_E, false, true, false, stdin, FILE_NAME));
        assertEquals(String.join(STRING_NEWLINE, FILE_NAME + ": 1", STDIN_STRING + ": 1"), result);
    }

    /**
     * Test case where -H is specified and filename is to be specified in output.
     */
    @Test
    void grepFromFileStdin_isPrefixFileNameIsTrue_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() ->
                grepApplication.grepFromFileAndStdin(PATTERN_SMALL_E, false, false, true, stdin, FILE_NAME));
        assertEquals(String.join(STRING_NEWLINE,
                        FILE_NAME + SEMICOLON_SPACE + FILE_CONTENTS,
                        STDIN_STRING + SEMICOLON_SPACE + STDIN_CONTENTS[0]),
                result);
    }

    /**
     * Test case where -i -c -H are specified.
     */
    @Test
    void grepFromFileAndStdin_AllFlagsSpecified_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() ->
                grepApplication.grepFromFileAndStdin(PATTERN_SMALL_E, true, true, true, stdin, FILE_NAME));
        assertEquals(String.join(STRING_NEWLINE, FILE_NAME + ": 1", STDIN_STRING + ": 2"), result);
    }
}
