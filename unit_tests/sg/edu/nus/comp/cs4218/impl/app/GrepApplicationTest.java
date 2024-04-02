package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_EMPTY_PATTERN;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_REGEX;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFile;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFileInDir;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.deleteFileOrDirectory;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.joinStringsByNewline;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.GrepException;

class GrepApplicationTest {

    private static final String FILE_CONTENTS = "test";

    private static final String GREP_STRING = "grep: ";
    private static final String STDIN_STRING = "(standard input)";
    private static final String[] STDIN_CONTENTS = new String[]{"test", "tEst"};

    private static final String PATTERN_SMALL_E = "e";
    private static final String PATTERN_BIG_E = "E";

    private static final String SEMICOLON_SPACE = ": ";

    private GrepApplication app;
    private InputStream stdin;

    @TempDir
    private Path tempDir;
    private Path file;
    private String fileAbsPath;
    private String fileName;

    @BeforeEach
    void setUp() {
        app = new GrepApplication();
        stdin = new ByteArrayInputStream(String.format("%s\n%s", STDIN_CONTENTS[0], STDIN_CONTENTS[1]).getBytes());
        Environment.currentDirectory = tempDir.toFile().getAbsolutePath();
        file = createNewFileInDir(tempDir, "file name", FILE_CONTENTS);
        fileAbsPath = file.toString();
        fileName = file.getFileName().toString();
    }

    @AfterEach
    void tearDown() {
        deleteFileOrDirectory(file);
    }

    /**
     * Test to check if grepFromFiles throws GrepException when no files are specified.
     */
    @Test
    void grepFromFile_NullFile_ThrowsGrepException() {
        GrepException result = assertThrowsExactly(GrepException.class, () ->
                app.grepFromFiles(PATTERN_BIG_E, false, false, false)
        );
        String expected = GREP_STRING + ERR_NULL_STREAMS;
        assertEquals(expected, result.getMessage());
    }

    /**
     * Test to check if grepFromFiles throws GrepException when files are empty.
     */
    @Test
    void grepFromFile_EmptyFile_ThrowsGrepException() {
        GrepException result = assertThrowsExactly(GrepException.class, () ->
                app.grepFromFiles(PATTERN_BIG_E, false, false, false, new String[0])
        );
        String expected = GREP_STRING + ERR_NULL_STREAMS;
        assertEquals(expected, result.getMessage());
    }

    /**
     * Test to check if grepFromFiles throws GrepException when no pattern is specified.
     */
    @Test
    void grepFromFile_NullPattern_ThrowsGrepException() {
        GrepException result = assertThrowsExactly(GrepException.class, () ->
                app.grepFromFiles(null, false, false, false, fileAbsPath)
        );
        String expected = GREP_STRING + ERR_NULL_STREAMS;
        assertEquals(expected, result.getMessage());
    }

    /**
     * Test to check if grepFromFiles throws GrepException when pattern is empty.
     */
    @Test
    void grepFromFile_EmptyPattern_ThrowsGrepException() {
        GrepException result = assertThrowsExactly(GrepException.class, () ->
                app.grepFromFiles("", false, false, false, fileAbsPath)
        );
        String expected = GREP_STRING + ERR_EMPTY_PATTERN;
        assertEquals(expected, result.getMessage());
    }

    /**
     * Test case where -i -c -H are specified.
     */
    @Test
    void grepFromFile_AllFlagsSpecifiedForOneFile_ReturnsCorrectNumberOfMatchingLinesWithFileName() {
        String result = assertDoesNotThrow(() ->
                app.grepFromFiles(PATTERN_BIG_E, true, true, true, fileAbsPath)
        );
        String expected = fileAbsPath + ": 1" + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    /**
     * Test case where -i is specified only.
     */
    @Test
    void grepFromFile_IsCaseSensitiveIsTrueForOneFile_ReturnsCorrectMatchingLines() {
        String result = assertDoesNotThrow(() ->
                app.grepFromFiles(PATTERN_BIG_E, true, false, false, fileName)
        );
        String expected = FILE_CONTENTS + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    /**
     * Test case where -c is specified.
     */
    @Test
    void grepFromFile_IsCountLinesIsTrueForOneFile_ReturnsCorrectNumberOfMatchingLines() {
        String result = assertDoesNotThrow(() ->
                app.grepFromFiles(PATTERN_BIG_E, false, true, false, fileAbsPath)
        );
        String expected = "0" + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    /**
     * Test case where -H is specified.
     */
    @Test
    void grepFromFile_IsPrefixFileNameIsTrueForOneFile_ReturnsCorrectMatchingLinesWithFileName() {
        String result = assertDoesNotThrow(() ->
                app.grepFromFiles(PATTERN_SMALL_E, false, false, true, fileName)
        );
        String expected = fileName + SEMICOLON_SPACE + FILE_CONTENTS + STRING_NEWLINE;
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

        String result = assertDoesNotThrow(() ->
                app.grepFromFiles(PATTERN_SMALL_E, false, false, false, fileAbsPath, secFileAbsPath)
        );
        String expected = joinStringsByNewline(fileAbsPath + SEMICOLON_SPACE + FILE_CONTENTS, secFileAbsPath +
                SEMICOLON_SPACE + secFileContents) + STRING_NEWLINE;
        assertEquals(expected, result);

        deleteFileOrDirectory(secFile);
    }

    /**
     * Test to check if grepFromStdin throws GrepException when no pattern is specified.
     */
    @Test
    void grepFromStdin_NullPattern_ThrowsGrepException() {
        GrepException result = assertThrowsExactly(GrepException.class, () ->
                app.grepFromStdin(null, false, false, false, stdin)
        );
        String expected = GREP_STRING + ERR_NULL_STREAMS;
        assertEquals(expected, result.getMessage());
    }

    /**
     * Test to check if grepFromStdin throws GrepException when pattern is empty.
     */
    @Test
    void grepFromStdin_EmptyPattern_ThrowsGrepException() {
        GrepException result = assertThrowsExactly(GrepException.class, () ->
                app.grepFromStdin("", false, false, false, stdin)
        );
        String expected = GREP_STRING + ERR_EMPTY_PATTERN;
        assertEquals(expected, result.getMessage());
    }

    /**
     * Test to check if grepFromStdin throws GrepException when invalid pattern are specified.
     */
    @Test
    void grepFromStdin_InvalidPattern_ThrowsGrepException() {
        GrepException result = assertThrowsExactly(GrepException.class, () ->
                app.grepFromStdin("*", false, false, false, stdin)
        );
        String expected = GREP_STRING + ERR_INVALID_REGEX;
        assertEquals(expected, result.getMessage());
    }

    /**
     * Test case where -i -c -H are specified.
     * Returns count of lines that match the pattern.
     */
    @Test
    void grepFromStdin_AllFlagsSpecified_ReturnsCountOfLinesMatchingPattern() {
        String result = assertDoesNotThrow(() ->
                app.grepFromStdin(PATTERN_SMALL_E, true, true, true, stdin)
        );
        String expected = STDIN_STRING + ": 2" + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    /**
     * Test case where -i is specified only.
     */
    @Test
    void grepFromStdin_isCaseInsensitiveIsTrue_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() ->
                app.grepFromStdin(PATTERN_SMALL_E, true, false, false, stdin)
        );
        String expected = joinStringsByNewline(STDIN_CONTENTS) + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    /**
     * Test case where -c is specified only.
     */
    @Test
    void grepFromStdin_isCountLinesIsTrue_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() ->
                app.grepFromStdin(PATTERN_SMALL_E, false, true, false, stdin)
        );
        String expected = "1" + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    /**
     * Test case where -H is specified.
     */
    @Test
    void grepFromStdin_isPrefixFileNameIsTrue_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() ->
                app.grepFromStdin(PATTERN_SMALL_E, false, false, true, stdin)
        );
        String expected = STDIN_STRING + SEMICOLON_SPACE + STDIN_CONTENTS[0] + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    /**
     * Test case where no flags are specified.
     */
    @Test
    void grepFromStdin_NoFlagsSpecified_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() ->
                app.grepFromStdin(PATTERN_SMALL_E, false, false, false, stdin)
        );
        String expected = STDIN_CONTENTS[0] + STRING_NEWLINE;
        assertEquals(expected, result);
    }
}
