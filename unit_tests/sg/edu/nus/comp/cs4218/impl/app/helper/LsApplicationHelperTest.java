package sg.edu.nus.comp.cs4218.impl.app.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;

// To give a meaningful variable name
@SuppressWarnings("PMD.LongVariable")
class LsApplicationHelperTest {
    // Main temporary dir
    @TempDir
    private static Path cwdPath;
    private static final String[] CWD_NON_DIRS = { "a.z", "z.a", "z" };
    private static final String DIR_A_NAME = "dirA";
    private static final String[] CWD_DIRS = { DIR_A_NAME };
    private static final String UNSORTED_CWD_CONTENTS = joinStringsBySystemLineSeparator(getCwdContents());
    private static final String UNSORTED_CWD_CONTENTS_WITH_HEADER = joinStringsBySystemLineSeparator(".:",
            UNSORTED_CWD_CONTENTS);
    private final String SORTED_CWD_CONTENTS_STRING = joinStringsBySystemLineSeparator(
            joinStringsBySystemLineSeparator("dirA", "z", "z.a", "a.z"));
    private final String SORTED_CWD_CONTENTS_STRING_WITH_HEADER = joinStringsBySystemLineSeparator(".:",
            SORTED_CWD_CONTENTS_STRING);

    // Temporary dir A in main temporary dir
    private static Path dirAPath;
    private static final String[] DIR_A_NON_DIRS = { "0" };
    private static final String UNSORTED_DIR_A_CONTENTS_WITH_HEADER = joinStringsBySystemLineSeparator(
            String.format(".%s%s:", CHAR_FILE_SEP, DIR_A_NAME), getDirAContents());
    private static final String SORTED_DIR_A_CONTENTS_WITH_HEADER = UNSORTED_DIR_A_CONTENTS_WITH_HEADER;

    private static final String TWO_LINE_SEPARATOR = System.lineSeparator() + System.lineSeparator();

    private static String getCwdContents() {
        List<String> fileList = Stream.concat(Arrays.stream(CWD_NON_DIRS), Arrays.stream(CWD_DIRS))
                .sorted()
                .collect(Collectors.toList());
        return String.join(System.lineSeparator(), fileList);
    }

    private static String getDirAContents() {
        return String.join(System.lineSeparator(), DIR_A_NON_DIRS) + System.lineSeparator();
    }

    private static String joinStringsBySystemLineSeparator(String... strings) {
        return String.join(System.lineSeparator(), strings);
    }

    @BeforeAll
    static void setUpEnvironment() throws IOException {
        for (String file : CWD_NON_DIRS) {
            cwdPath.resolve(file).toFile().createNewFile();
        }
        for (String dir : CWD_DIRS) {
            Files.createDirectory(cwdPath.resolve(dir));
        }
        for (String file : DIR_A_NON_DIRS) {
            cwdPath.resolve(CWD_DIRS[0]).resolve(file).toFile().createNewFile();
        }

        String cwdPathName = cwdPath.toString();
        dirAPath = Paths.get(cwdPathName, DIR_A_NAME);
        // Set current working directory to cwdPath
        System.setProperty("user.dir", cwdPathName);
    }

    @Test
    void buildResult_IsRecursiveIsTrue_ReturnsAllFiles() {
        // Given
        String expected = String.format("%s%s%s%s", UNSORTED_CWD_CONTENTS_WITH_HEADER, TWO_LINE_SEPARATOR,
                UNSORTED_DIR_A_CONTENTS_WITH_HEADER, System.lineSeparator());

        // When
        String actual = LsApplicationHelper.buildResult(List.of(cwdPath), true, false, false);

        // Then
        assertEquals(expected, actual);
    }

    /**
     * Tests if buildResult returns expected output given isSortByExt is true.
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
     * Tests if buildResult returns expected output given isRecursive and
     * isSortByExt are true.
     */
    @Test
    void buildResult_IsRecursiveIsTrueAndIsSortByExtIsTrue_ReturnsAllFilesSortedByExt() {
        // Given
        String expected = String.format("%s%s%s%s", SORTED_CWD_CONTENTS_STRING_WITH_HEADER, TWO_LINE_SEPARATOR,
                SORTED_DIR_A_CONTENTS_WITH_HEADER, System.lineSeparator());

        // When
        String actual = LsApplicationHelper.buildResult(List.of(cwdPath), true, true,
                false);

        // Then
        assertEquals(expected, actual);
    }

    /**
     * Tests if listCwdContent returns expected output given isSortByExt.
     *
     * @param expected    Expected output.
     * @param isSortByExt Boolean to indicate if output should be sorted by
     *                    extension.
     */
    void testListCwdContent(String expected, boolean isSortByExt) {
        // When
        String actual = assertDoesNotThrow(() -> LsApplicationHelper.listCwdContent(isSortByExt));

        // Then
        assertEquals(expected, actual);
    }

    /**
     * Tests if listCwdContent returns sorted contents when isSortByExt is true.
     */
    @Test
    void listCwdContent_IsSortByExtIsTrue_ReturnsSortedCwdContents() {
        // Given
        String expected = String.join(System.lineSeparator(), SORTED_CWD_CONTENTS_STRING);

        testListCwdContent(expected, true);
    }

    /**
     * Tests if listCwdContent returns unsorted contents when isSortByExt is false.
     */
    @Test
    void listCwdContent_IsSortByExtIsFalse_ReturnsUnsortedCwdContents() {
        // Given
        String expected = String.join(System.lineSeparator(), UNSORTED_CWD_CONTENTS);

        testListCwdContent(expected, false);
    }

    /***
     * Tests if resolvePaths returns a list of paths when given a valid directory.
     */
    @Test
    void resolvePaths_ValidDirectory_ReturnsValidListOfPath() {
        // Given
        List<Path> expected = List.of(dirAPath);

        // When
        List<Path> actual = LsApplicationHelper.resolvePaths(DIR_A_NAME);

        // Then
        assertEquals(expected, actual);
    }
}
