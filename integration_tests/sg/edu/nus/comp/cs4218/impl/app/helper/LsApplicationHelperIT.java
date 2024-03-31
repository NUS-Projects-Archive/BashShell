package sg.edu.nus.comp.cs4218.impl.app.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.app.helper.LsApplicationHelper.buildResult;
import static sg.edu.nus.comp.cs4218.impl.app.helper.LsApplicationHelper.listCwdContent;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewDirectory;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.deleteFileOrDirectory;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.joinStringsByNewline;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.LsException;

// To give a meaningful variable name
@SuppressWarnings({"PMD.LongVariable", "PMD.ClassNamingConventions"})
class LsApplicationHelperIT {

    private static final String DIR_A_NAME = "dirA";
    private static final String STRING_AZ = "a.z";
    private static final String STRING_ZA = "z.a";
    private static final String STRING_Z = "z";
    private static final String TWO_LINE_SEPARATOR = STRING_NEWLINE + STRING_NEWLINE;

    private static final String[] CWD_NON_DIRS = {STRING_AZ, STRING_ZA, STRING_Z};
    private static final String[] CWD_DIRS = {DIR_A_NAME};
    private static final String UNSORTED_CWD_CONTENTS = joinStringsByNewline(getCwdContents());
    private static final String UNSORTED_CWD_CONTENTS_WITH_HEADER = joinStringsByNewline(".:",
            UNSORTED_CWD_CONTENTS);
    private static final String[] DIR_A_NON_DIRS = {"0"};
    private static final String UNSORTED_DIR_A_CONTENTS_WITH_HEADER = joinStringsByNewline(
            String.format(".%s%s:", CHAR_FILE_SEP, DIR_A_NAME), getDirAContents());
    private static final String SORTED_DIR_A_CONTENTS_WITH_HEADER = UNSORTED_DIR_A_CONTENTS_WITH_HEADER;
    private static final String SORTED_CWD_CONTENTS_STRING = joinStringsByNewline("dirA", STRING_Z,
            STRING_ZA, STRING_AZ);
    private static final String SORTED_CWD_CONTENTS_STRING_WITH_HEADER = joinStringsByNewline(".:",
            SORTED_CWD_CONTENTS_STRING);
    // Main temporary dir
    @TempDir
    private Path cwdPath;

    private static String getCwdContents() {
        String[] fileList = Stream.concat(Arrays.stream(CWD_NON_DIRS), Arrays.stream(CWD_DIRS))
                .sorted()
                .toArray(String[]::new);
        return joinStringsByNewline(fileList);
    }

    private static String getDirAContents() {
        return joinStringsByNewline(DIR_A_NON_DIRS) + STRING_NEWLINE;
    }

    /**
     * Asserts that the output of listCwdContent matches the expected output, when given isSortByExt.
     *
     * @param expected    The expected output of listCwdContent
     * @param isSortByExt A boolean to indicate whether the output should be sorted by extension
     */
    private void assertEqualsListCwdContent(String expected, boolean isSortByExt) {
        String result = assertDoesNotThrow(() -> listCwdContent(isSortByExt));
        assertEquals(expected, result);
    }

    /**
     * Sets up the current working directory environment.
     * Current working directory contains of 3 files and 1 directory (dir A). Dir A contains 1 file.
     */
    @BeforeEach
    void setUp() {
        for (String file : CWD_NON_DIRS) {
            assertDoesNotThrow(() -> cwdPath.resolve(file).toFile().createNewFile());
        }
        for (String dir : CWD_DIRS) {
            createNewDirectory(cwdPath, dir);
        }
        for (String file : DIR_A_NON_DIRS) {
            assertDoesNotThrow(() -> cwdPath.resolve(CWD_DIRS[0]).resolve(file).toFile().createNewFile());
        }

        // Set current working directory to cwdPath
        Environment.currentDirectory = cwdPath.toString();
    }

    @AfterEach
    void tearDown() {
        Environment.currentDirectory = System.getProperty("user.dir");
    }

    @Test
    void listCwdContent_CurrentDirectoryDoNotExist_ThrowsLsException() {
        Environment.currentDirectory = "doNotExistCwdPath";
        assertThrowsExactly(LsException.class, () -> listCwdContent(false));
    }

    @Test
    void listCwdContent_IsSortByExtIsFalse_ReturnsUnsortedCwdContents() {
        String expected = joinStringsByNewline(UNSORTED_CWD_CONTENTS);
        assertEqualsListCwdContent(expected, false);
    }

    @Test
    void listCwdContent_IsSortByExtIsTrue_ReturnsSortedCwdContents() {
        String expected = joinStringsByNewline(SORTED_CWD_CONTENTS_STRING);
        assertEqualsListCwdContent(expected, true);
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void listCwdContent_CurrentDirectoryNoPermissionToRead_ThrowsLsException() {
        // Create a directory that is not readable and change current directory to that
        Path unreadableDir = assertDoesNotThrow(() -> Files.createTempDirectory("unreadable"));
        boolean isSetReadable = unreadableDir.toFile().setReadable(false);
        assertTrue(isSetReadable, "Failed to set read permission to false for test source file");
        Environment.currentDirectory = unreadableDir.toString();

        LsException result = assertThrowsExactly(LsException.class, () -> listCwdContent(false));
        String expected = "ls: cannot open directory '.': Permission denied";
        assertEquals(expected, result.getMessage());
        deleteFileOrDirectory(unreadableDir);
    }

    @Test
    void buildResult_PathDoNotExist_ReturnsNoPermissionError() {
        Path doNotExistPath = Paths.get(cwdPath + "/doNotExistPath");
        String result = buildResult(List.of(doNotExistPath), false, false, false);
        String expected = "ls: cannot access 'doNotExistPath': No such file or directory" + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void buildResult_PathNoPermissionToRead_ReturnsNoPermissionError() {
        boolean isSetReadable = cwdPath.toFile().setReadable(false);
        assertTrue(isSetReadable, "Failed to set read permission to false for test source file");
        String result = buildResult(List.of(cwdPath), false, false, false);
        String expected = "ls: cannot open directory '.': Permission denied" + TWO_LINE_SEPARATOR;
        assertEquals(expected, result);
    }

    @Test
    void buildResult_NoFlags_ReturnsAllFiles() {
        String result = buildResult(List.of(cwdPath), false, false, false);
        String expected = String.format("%s%s", UNSORTED_CWD_CONTENTS_WITH_HEADER, TWO_LINE_SEPARATOR);
        assertEquals(expected, result);
    }

    @Test
    void buildResult_IsRecursive_ReturnsAllFiles() {
        String result = buildResult(List.of(cwdPath), true, false, false);
        String expected = String.format("%s%s%s%s", UNSORTED_CWD_CONTENTS_WITH_HEADER, TWO_LINE_SEPARATOR,
                UNSORTED_DIR_A_CONTENTS_WITH_HEADER, STRING_NEWLINE);
        assertEquals(expected, result);
    }

    @Test
    void buildResult_IsSortByExt_ReturnsTempDirFilesSortedByExt() {
        String result = buildResult(List.of(cwdPath), false, true, false);
        String expected = String.format("%s%s", SORTED_CWD_CONTENTS_STRING_WITH_HEADER, TWO_LINE_SEPARATOR);
        assertEquals(expected, result);
    }

    @Test
    void buildResult_IsRecursiveAndIsSortByExt_ReturnsAllFilesSortedByExt() {
        String result = buildResult(List.of(cwdPath), true, true, false);
        String expected = String.format("%s%s%s%s", SORTED_CWD_CONTENTS_STRING_WITH_HEADER, TWO_LINE_SEPARATOR,
                SORTED_DIR_A_CONTENTS_WITH_HEADER, STRING_NEWLINE);
        assertEquals(expected, result);
    }
}
