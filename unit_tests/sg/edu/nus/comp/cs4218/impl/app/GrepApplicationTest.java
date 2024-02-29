package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.GrepException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_REGEX;

class GrepApplicationTest {
    private GrepApplication grepApplication;
    private InputStream stdin;
    private Path file;
    private String fileAbsPath;

    private static final String FILE_NAME = "file";
    private static final String FILE_CONTENTS = "tset";

    private static final String STDIN_STRING = "(standard input)";
    private static final String[] STDIN_CONTENTS = new String[]{"test", "tEst"};

    private static final String PATTERN_SMALL_E = "e";
    private static final String PATTERN_BIG_E = "E";

    String joinStringsByLineSeparator(String... strings) {
        StringBuilder joinedStr = new StringBuilder();
        for (String str: strings) {
            joinedStr.append(str).append(System.lineSeparator());
        }
        return joinedStr.toString();
    }

    @BeforeEach
    void setUp() {
        grepApplication = new GrepApplication();
        stdin = new ByteArrayInputStream(String.format("%s\n%s", STDIN_CONTENTS[0], STDIN_CONTENTS[1]).getBytes());

        // Create a temporary file
        try {
            file = Files.createTempFile(FILE_NAME, "");
            fileAbsPath = file.toString();
        } catch (IOException e) {
            fail("Unable to create temporary file during setup");
        }
        try {
            Files.write(file, FILE_CONTENTS.getBytes());
        } catch (IOException e) {
            fail("Unable to write to temporary file during setup");
        }
    }

    @AfterEach
    void tearDown() {
        try {
            // Delete the temporary file
            Files.deleteIfExists(file);
        } catch (IOException e) {
            fail("Unable to delete temporary file during tear down");
        }
    }

    /**
     * Test case where -i -c -H are specified.
     */
    @Test
    void grepFromFile_AllFlagsSpecifiedForOneFile_ReturnsCorrectOutput() {
        String expected = "1" + System.lineSeparator();
        String result = assertDoesNotThrow(() -> grepApplication.grepFromFiles(PATTERN_BIG_E,
                true, true, true, fileAbsPath));
        assertEquals(expected, result);
    }

    /**
     * Test case where -i is specified only.
     */
    @Test
    void grepFromFile_isCaseSensitiveIsTrueForOneFile_ReturnsCorrectOutput() {
        String expected = FILE_CONTENTS + System.lineSeparator();
        String result = assertDoesNotThrow(() -> grepApplication.grepFromFiles(PATTERN_BIG_E,
                true, false, true, fileAbsPath));
        assertEquals(expected, result);
    }

    /**
     * Test case where -c is specified only.
     */
    @Test
    void grepFromFile_isCountLinesIsTrueForOneFile_ReturnsCorrectOutput() {
        String expected = "0" + System.lineSeparator();
        String result = assertDoesNotThrow(() -> grepApplication.grepFromFiles(PATTERN_BIG_E,
                false, true, false, fileAbsPath));
        assertEquals(expected, result);
    }

    /**
     * Test case where -H is specified and filename is to be specified in output.
     * Disabled because this feature is not implemented yet.
     */
    @Test
    @Disabled
    void grepFromFile_isPrefixFileNameIsTrueForOneFile_ReturnsCorrectOutput() {
        String expected = FILE_NAME + ": " + FILE_CONTENTS + System.lineSeparator();
        String result = assertDoesNotThrow(() -> grepApplication.grepFromFiles(PATTERN_BIG_E,
                false, false, true, fileAbsPath));
        assertEquals(expected, result);
    }

    /**
     * Test case where no flags are specified but multiple valid files are specified.
     */
    @Test
    void grepFromFile_NoFlagsSpecifiedForMultipleFiles_ReturnsCorrectOutput() {
        // Create a temporary file
        Path secFile = null;
        String secFileName = "secFile";
        String secFileContents = "tseet";

        try {
            secFile = Files.createTempFile(secFileName, "");
        } catch (IOException e) {
            fail("Unable to create second temporary file");
        }

        try {
            Files.write(secFile, secFileContents.getBytes());
        } catch (IOException e) {
            fail("Unable to write to second temporary file");
        }

        String secFileAbsPath = secFile.toString();

        String expected = joinStringsByLineSeparator(fileAbsPath + ": " + FILE_CONTENTS, secFileAbsPath + ": " + secFileContents);
        String result = assertDoesNotThrow(() -> grepApplication.grepFromFiles(PATTERN_SMALL_E,
                false, false, false, fileAbsPath, secFileAbsPath));
        assertEquals(expected, result);

        try {
            // Delete the temporary file
            Files.deleteIfExists(secFile);
        } catch (IOException e) {
            fail("Unable to delete second temporary file");
        }
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
        String expected = "2" + System.lineSeparator();
        String result = assertDoesNotThrow(() -> grepApplication.grepFromStdin(PATTERN_SMALL_E, true, true, true, stdin));
        assertEquals(expected, result);
    }

    /**
     * Test case where -i is specified only.
     */
    @Test
    void grepFromStdin_isCaseInsensitiveIsTrue_ReturnsCorrectOutput() {
        String expected = joinStringsByLineSeparator(STDIN_CONTENTS);
        String result = assertDoesNotThrow(() -> grepApplication.grepFromStdin(PATTERN_SMALL_E, true, false, false, stdin));
        assertEquals(expected, result);
    }

    /**
     * Test case where -c is specified only.
     */
    @Test
    void grepFromStdin_isCountLinesIsTrue_ReturnsCorrectOutput() {
        String expected = "1" + System.lineSeparator();
        String result = assertDoesNotThrow(() -> grepApplication.grepFromStdin(PATTERN_SMALL_E, false, true, false, stdin));
        assertEquals(expected, result);
    }

    /**
     * Test case where -H is specified.
     * Disabled because this feature is not implemented yet.
     */
    @Test
    @Disabled
    void grepFromStdin_isPrefixFileNameIsTrue_ReturnsCorrectOutput() {
        String expected = STDIN_STRING + ": " + STDIN_CONTENTS[0] + System.lineSeparator();
        String result = assertDoesNotThrow(() -> grepApplication.grepFromStdin(PATTERN_SMALL_E, false, false, true, stdin));
        assertEquals(expected, result);
    }

    /**
     * Test case where no flags are specified.
     */
    @Test
    void grepFromStdin_NoFlagsSpecified_ReturnsCorrectOutput() {
        String expected = STDIN_CONTENTS[0] + System.lineSeparator();
        String result = assertDoesNotThrow(() -> grepApplication.grepFromStdin(PATTERN_SMALL_E, false, false, false, stdin));
        assertEquals(expected, result);
    }

    /**
     * Test case where no flags are specified.
     * Disabled because this feature is not implemented yet.
     */
    @Test
    @Disabled
    void grepFromFileAndStdin_NoFlagsSpecified_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() -> grepApplication.grepFromFileAndStdin(PATTERN_SMALL_E,
                false, false, false, stdin, FILE_NAME));
        assertEquals(joinStringsByLineSeparator(FILE_NAME + ": " + FILE_CONTENTS, STDIN_STRING + ": " + STDIN_CONTENTS[0]),
                result);
    }

    /**
     * Test case where -i is specified only.
     * Disabled because this feature is not implemented yet.
     */
    @Test
    @Disabled
    void grepFromFileAndStdin_isCaseSensitiveIsFalse_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() -> grepApplication.grepFromFileAndStdin(PATTERN_SMALL_E,
                true, false, false, stdin, FILE_NAME));
        assertEquals(joinStringsByLineSeparator(FILE_NAME + ": " + FILE_CONTENTS, STDIN_STRING + ": " + STDIN_CONTENTS[0], STDIN_STRING + ": " + STDIN_CONTENTS[1]),
                result);
    }

    /**
     * Test case where -c is specified only.
     * Disabled because this feature is not implemented yet.
     */
    @Test
    @Disabled
    void grepFromFileAndStdin_isCountLinesIsTrue_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() -> grepApplication.grepFromFileAndStdin(PATTERN_SMALL_E,
                false, true, false, stdin, FILE_NAME));
        assertEquals(joinStringsByLineSeparator(FILE_NAME + ": 1", STDIN_STRING + ": 1"),
                result);
    }

    /**
     * Test case where -H is specified and filename is to be specified in output.
     * Disabled because this feature is not implemented yet.
     */
    @Test
    @Disabled
    void grepFromFileStdin_isPrefixFileNameIsTrue_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() -> grepApplication.grepFromFileAndStdin(PATTERN_SMALL_E,
                false, false, true, stdin, FILE_NAME));
        assertEquals(joinStringsByLineSeparator(FILE_NAME + ": " + FILE_CONTENTS, STDIN_STRING + ": " + STDIN_CONTENTS[0]),
                result);
    }

    /**
     * Test case where -i -c -H are specified.
     * Disabled because this feature is not implemented yet.
     */
    @Test
    @Disabled
    void grepFromFileAndStdin_AllFlagsSpecified_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() -> grepApplication.grepFromFileAndStdin(PATTERN_SMALL_E,
                true, true, true, stdin, FILE_NAME));
        assertEquals(joinStringsByLineSeparator(FILE_NAME + ": 1", STDIN_STRING + ": 2"),
                result);
    }
}
