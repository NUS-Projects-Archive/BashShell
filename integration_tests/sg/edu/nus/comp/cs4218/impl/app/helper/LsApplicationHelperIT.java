package sg.edu.nus.comp.cs4218.impl.app.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.test.FileUtils.createNewDirectory;
import static sg.edu.nus.comp.cs4218.test.FileUtils.deleteFileOrDirectory;

import java.nio.file.Files;
import java.nio.file.Path;
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
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

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
    private static final String[] DIR_A_NON_DIRS = {"0"};

    private static final String UNSORTED_CWD_CONTENTS = StringUtils.joinStringsByNewline(getCwdContents());
    private static final String UNSORTED_CWD_CONTENTS_WITH_HEADER = StringUtils.joinStringsByNewline(".:",
            UNSORTED_CWD_CONTENTS);
    private static final String UNSORTED_DIR_A_CONTENTS_WITH_HEADER = StringUtils.joinStringsByNewline(
            String.format(".%s%s:", CHAR_FILE_SEP, DIR_A_NAME), getDirAContents());
    private static final String SORTED_CWD_CONTENTS_STRING = StringUtils.joinStringsByNewline("dirA", STRING_Z,
            STRING_ZA, STRING_AZ);
    private static final String SORTED_CWD_CONTENTS_STRING_WITH_HEADER = StringUtils.joinStringsByNewline(".:",
            SORTED_CWD_CONTENTS_STRING);
    private static final String SORTED_DIR_A_CONTENTS_WITH_HEADER = UNSORTED_DIR_A_CONTENTS_WITH_HEADER;

    // Main temporary dir
    @TempDir
    private Path cwdPath;

    private static String getCwdContents() {
        String[] fileList = Stream.concat(Arrays.stream(CWD_NON_DIRS), Arrays.stream(CWD_DIRS))
                .sorted()
                .toArray(String[]::new);
        return StringUtils.joinStringsByNewline(fileList);
    }

    private static String getDirAContents() {
        return StringUtils.joinStringsByNewline(DIR_A_NON_DIRS) + STRING_NEWLINE;
    }

    /**
     * Tests if listCwdContent returns expected output given isSortByExt.
     *
     * @param expected    Expected output
     * @param isSortByExt Boolean to indicate if output should be sorted by
     *                    extension
     */
    private void testListCwdContent(String expected, boolean isSortByExt) {
        // When
        String actual = assertDoesNotThrow(() -> LsApplicationHelper.listCwdContent(isSortByExt));

        // Then
        assertEquals(expected, actual);
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

    /**
     * Tests if listCwdContent returns unsorted contents when isSortByExt is false.
     */
    @Test
    void listCwdContent_IsSortByExtIsFalse_ReturnsUnsortedCwdContents() {
        // Given
        String expected = StringUtils.joinStringsByNewline(UNSORTED_CWD_CONTENTS);

        testListCwdContent(expected, false);
    }

    /**
     * Tests if listCwdContent returns sorted contents when isSortByExt is true.
     */
    @Test
    void listCwdContent_IsSortByExtIsTrue_ReturnsSortedCwdContents() {
        // Given
        String expected = StringUtils.joinStringsByNewline(SORTED_CWD_CONTENTS_STRING);
        testListCwdContent(expected, true);
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void listCwdContent_CurrentDirectoryIsNotReadable_ThrowsLsException() {
        // Create a directory that is not readable and change current directory to that
        Path unreadableDir = assertDoesNotThrow(() -> Files.createTempDirectory("unreadable"));
        unreadableDir.toFile().setReadable(false);
        Environment.currentDirectory = unreadableDir.toString();

        // Given
        String expected = "ls: cannot open directory '.': Permission denied";

        // When
        String actual = assertThrowsExactly(LsException.class, () -> LsApplicationHelper.listCwdContent(false)).getMessage();

        // Then
        assertEquals(expected, actual);

        // Clean up
        deleteFileOrDirectory(unreadableDir);
    }

    /**
     * Tests -R flag and checks it returns all the files recursively.
     */
    @Test
    void buildResult_IsRecursiveIsTrue_ReturnsAllFiles() {
        // Given
        String expected = String.format("%s%s%s%s", UNSORTED_CWD_CONTENTS_WITH_HEADER, TWO_LINE_SEPARATOR,
                UNSORTED_DIR_A_CONTENTS_WITH_HEADER, STRING_NEWLINE);

        // When
        String actual = LsApplicationHelper.buildResult(List.of(cwdPath), true, false, false);

        // Then
        assertEquals(expected, actual);
    }

    /**
     * Tests if buildResult returns expected output given isSortByExt -X is true.
     */
    @Test
    void buildResult_IsSortByExtIsTrue_ReturnsTempDirFilesSortedByExt() {
        // Given
        String expected = String.format("%s%s", SORTED_CWD_CONTENTS_STRING_WITH_HEADER, TWO_LINE_SEPARATOR);

        // When
        String actual = LsApplicationHelper.buildResult(List.of(cwdPath), false, true,
                false);

        // Then
        assertEquals(expected, actual);
    }

    /**
     * Tests if buildResult returns expected output given isRecursive -R and isSortByExt -X are true.
     */
    @Test
    void buildResult_IsRecursiveIsTrueAndIsSortByExtIsTrue_ReturnsAllFilesSortedByExt() {
        // Given
        String expected = String.format("%s%s%s%s", SORTED_CWD_CONTENTS_STRING_WITH_HEADER, TWO_LINE_SEPARATOR,
                SORTED_DIR_A_CONTENTS_WITH_HEADER, STRING_NEWLINE);

        // When
        String actual = LsApplicationHelper.buildResult(List.of(cwdPath), true, true,
                false);

        // Then
        assertEquals(expected, actual);
    }
}