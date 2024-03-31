package sg.edu.nus.comp.cs4218.impl.app.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.IS_DIRECTORY;
import static sg.edu.nus.comp.cs4218.impl.app.helper.GrepApplicationHelper.grepResultsFromFiles;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_REGEX;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFileInDir;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.deleteFileOrDirectory;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.joinStringsByNewline;

import java.nio.file.Path;
import java.util.StringJoiner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.GrepException;

@SuppressWarnings("PMD.ClassNamingConventions")
class GrepApplicationHelperIT {

    private static final String VALID_PAT_SMALL = "ab";
    private static final String VALID_PAT_BIG = "AB";
    private static final String INVALID_PAT_BIG = "(AB";
    private static final String GREP_STRING = "grep: ";
    private static final String INPUT_CONTENTS = joinStringsByNewline("aabb", "x", "ab");
    private static final String[] OUTPUT_CONTENTS = {"aabb", "ab"};
    private static final String NON_EXISTENT_FILE = "nonExistentFile";

    @TempDir
    private Path tempDir;
    private Path fileOne;
    private String fileOneName;
    private StringJoiner lineResults;
    private StringJoiner countResults;

    @BeforeEach
    void setUp() {
        Environment.currentDirectory = tempDir.toFile().getAbsolutePath();

        fileOne = createNewFileInDir(tempDir, "tempFile1", INPUT_CONTENTS);
        fileOneName = fileOne.getFileName().toString();

        lineResults = new StringJoiner("");
        countResults = new StringJoiner("");
    }

    @AfterEach
    void tearDown() {
        deleteFileOrDirectory(fileOne);
    }

    @Test
    void grepResultsFromFiles_FileDoNotExist_LineResultsReturnsFileNotFoundError() {
        assertDoesNotThrow(() ->
                grepResultsFromFiles(VALID_PAT_SMALL, false, lineResults, countResults, false, NON_EXISTENT_FILE)
        );
        assertEquals(GREP_STRING + NON_EXISTENT_FILE + ": " + ERR_FILE_NOT_FOUND, lineResults.toString());
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void grepResultsFromFiles_NoReadPermissionFile_LineResultsReturnsNoPermissionError() {
        boolean isSetReadable = fileOne.toFile().setReadable(false);
        assertTrue(isSetReadable, "Failed to set read permission to false for test");
        assertDoesNotThrow(() ->
                grepResultsFromFiles(VALID_PAT_SMALL, false, lineResults, countResults, false, fileOneName)
        );
        assertEquals(GREP_STRING + fileOneName + ": " + ERR_NO_PERM, lineResults.toString());
    }

    @Test
    void grepResultsFromFiles_FileGivenAsDirectory_LineResultsReturnsDirectoryError() {
        assertDoesNotThrow(() ->
                grepResultsFromFiles(VALID_PAT_SMALL, false, lineResults, countResults, false, tempDir.toString())
        );
        assertEquals(GREP_STRING + tempDir + ": " + IS_DIRECTORY, lineResults.toString());
    }

    @Test
    void grepResultsFromFile_FileAsHomeDirectory_LineResultsReturnsDirectoryError() {
        String home = System.getProperty("user.home").trim();
        assertDoesNotThrow(() ->
                grepResultsFromFiles(VALID_PAT_SMALL, false, lineResults, countResults, false, home)
        );
        assertEquals(GREP_STRING + home + ": " + IS_DIRECTORY, lineResults.toString());
    }

    @Test
    void grepResultsFromFile_FileEndsWithFileSeparator_LineResultsReturnsExpectedOutput() {
        String expectedLR = String.join("", OUTPUT_CONTENTS);
        assertDoesNotThrow(() ->
                grepResultsFromFiles(VALID_PAT_SMALL, false, lineResults, countResults, false,
                        fileOneName + CHAR_FILE_SEP)
        );
        assertEquals(expectedLR, lineResults.toString());
    }

    @Test
    void grepResultsFromFile_InvalidPattern_ThrowsGrepException() {
        GrepException result = assertThrowsExactly(GrepException.class, () ->
                grepResultsFromFiles(INVALID_PAT_BIG, false, lineResults, countResults, false, fileOneName)
        );
        assertEquals(GREP_STRING + ERR_INVALID_REGEX, result.getMessage());
    }

    @Test
    void grepResultsFromFiles_FileExistWithValidPatternAndNoFlag_LineResultsAndCountResultsReturnsExpectedOutput() {
        String expectedLR = String.join("", OUTPUT_CONTENTS);
        assertDoesNotThrow(() ->
                grepResultsFromFiles(VALID_PAT_SMALL, false, lineResults, countResults, false, fileOneName)
        );
        assertEquals(expectedLR, lineResults.toString());
        assertEquals("2", countResults.toString());
    }

    @Test
    void grepResultsFromFile_FileExistWithValidPatternAndBothFlagsSet_LineResultsAndCountResultsReturnsExpectedOutput() {
        String expectedLR = fileOneName + ": " + OUTPUT_CONTENTS[0] + fileOneName + ": " + OUTPUT_CONTENTS[1];
        String expectedCR = fileOneName + ": " + OUTPUT_CONTENTS.length;
        assertDoesNotThrow(() ->
                grepResultsFromFiles(VALID_PAT_BIG, true, lineResults, countResults, true, fileOneName)
        );
        assertEquals(expectedLR, lineResults.toString());
        assertEquals(expectedCR, countResults.toString());
    }
}
