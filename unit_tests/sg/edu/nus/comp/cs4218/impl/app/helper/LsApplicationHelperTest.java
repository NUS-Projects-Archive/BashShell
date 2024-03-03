package sg.edu.nus.comp.cs4218.impl.app.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.FileUtils.createNewDirectory;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.Environment;

// To give a meaningful variable name
@SuppressWarnings("PMD.LongVariable")
class LsApplicationHelperTest {

    private static final String DIR_A_NAME = "dirA";
    private static final String STRING_AZ = "a.z";
    private static final String STRING_ZA = "z.a";
    private static final String STRING_Z = "z";
    private static final String[] CWD_NON_DIRS = {STRING_AZ, STRING_ZA, STRING_Z};
    private static final String[] CWD_DIRS = {DIR_A_NAME};
    private static final String UNSORTED_CWD_CONTENTS = String.join(STRING_NEWLINE, getCwdContents());
    private static final String UNSORTED_CWD_CONTENTS_WITH_HEADER = String.join(STRING_NEWLINE, ".:",
            UNSORTED_CWD_CONTENTS);
    private static final String[] DIR_A_NON_DIRS = {"0"};
    private static final String UNSORTED_DIR_A_CONTENTS_WITH_HEADER = String.join(STRING_NEWLINE,
            String.format(".%s%s:", CHAR_FILE_SEP, DIR_A_NAME), getDirAContents());
    private static final String SORTED_DIR_A_CONTENTS_WITH_HEADER = UNSORTED_DIR_A_CONTENTS_WITH_HEADER;
    private static final String TWO_LINE_SEPARATOR = STRING_NEWLINE + STRING_NEWLINE;
    private static final String SORTED_CWD_CONTENTS_STRING = String.join(STRING_NEWLINE, "dirA", STRING_Z, STRING_ZA, STRING_AZ);
    private static final String SORTED_CWD_CONTENTS_STRING_WITH_HEADER = String.join(STRING_NEWLINE, ".:",
            SORTED_CWD_CONTENTS_STRING);

    // Main temporary dir
    @TempDir
    private Path cwdPath;
    // Temporary dir A is in main temporary dir
    private Path dirAPath;

    private static String getCwdContents() {
        List<String> fileList = Stream.concat(Arrays.stream(CWD_NON_DIRS), Arrays.stream(CWD_DIRS))
                .sorted()
                .collect(Collectors.toList());
        return String.join(STRING_NEWLINE, fileList);
    }

    private static String getDirAContents() {
        return String.join(STRING_NEWLINE, DIR_A_NON_DIRS) + STRING_NEWLINE;
    }

    /**
     * Tests if listCwdContent returns expected output given isSortByExt.
     *
     * @param expected    Expected output.
     * @param isSortByExt Boolean to indicate if output should be sorted by
     *                    extension.
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

        String cwdPathName = cwdPath.toString();
        dirAPath = Paths.get(cwdPathName, DIR_A_NAME);
        // Set current working directory to cwdPath
        Environment.currentDirectory = cwdPathName;
    }

    /**
     * Tests if listCwdContent returns unsorted contents when isSortByExt is false.
     */
    @Test
    void listCwdContent_IsSortByExtIsFalse_ReturnsUnsortedCwdContents() {
        // Given
        String expected = String.join(STRING_NEWLINE, UNSORTED_CWD_CONTENTS);

        testListCwdContent(expected, false);
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

    /**
     * Tests if listCwdContent returns sorted contents when isSortByExt is true.
     */
    @Test
    void listCwdContent_IsSortByExtIsTrue_ReturnsSortedCwdContents() {
        // Given
        String expected = String.join(STRING_NEWLINE, SORTED_CWD_CONTENTS_STRING);

        testListCwdContent(expected, true);
    }

    @Test
    void formatContents_IsSortByExtIsTrue_ReturnsSortedFormattedContents() {
        // Given
        String expected = String.join(STRING_NEWLINE, STRING_Z, STRING_ZA, STRING_AZ);
        List<Path> contents = Arrays.stream(CWD_NON_DIRS)
                .map(Paths::get)
                .collect(Collectors.toList());
        // When
        String actual = LsApplicationHelper.formatContents(contents, true);

        // Then
        assertEquals(expected, actual);
    }

    @Test
    void formatContents_IsSortByExtIsFalse_ReturnsFormattedContents() {
        // Given
        String expected = String.join(STRING_NEWLINE, STRING_AZ, STRING_Z, STRING_ZA);
        List<Path> contents = Arrays.stream(CWD_NON_DIRS)
                .map(Paths::get)
                .collect(Collectors.toList());
        // When
        String actual = LsApplicationHelper.formatContents(contents, false);

        // Then
        assertEquals(expected, actual);
    }

    /***
     * Tests if resolvePaths returns a list of paths when given a valid directory.
     */
    @Test
    void resolvePaths_ValidDirectory_ReturnsValidListOfPath() {
        // Given
        List<Path> expected = List.of(dirAPath);

        // When
        List<Path> actual = assertDoesNotThrow(() -> LsApplicationHelper.resolvePaths(DIR_A_NAME));

        // Then
        assertEquals(expected, actual);
    }
}
