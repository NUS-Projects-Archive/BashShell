package sg.edu.nus.comp.cs4218.impl.app.helper;

import sg.edu.nus.comp.cs4218.exception.LsException;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;

// To give a meaningful variable name
@SuppressWarnings("PMD.LongVariable")
class LsApplicationHelperTest {
    // Main temporary folder
    @TempDir
    private static Path cwdPath;
    private static final String[] CWD_NON_FOLDERS = { "a.z", "z.a", "z" };
    private static final String FOLDER_A_NAME = "folderA";
    private static final String[] CWD_FOLDERS = { FOLDER_A_NAME };
    private static final String UNSORTED_CWD_CONTENTS = joinStringsBySystemLineSeparator(getCwdContents());
    private static final String UNSORTED_CWD_CONTENTS_WITH_HEADER = joinStringsBySystemLineSeparator(".:",
            UNSORTED_CWD_CONTENTS);
    private final String SORTED_CWD_CONTENTS_STRING = joinStringsBySystemLineSeparator(
            joinStringsBySystemLineSeparator("folderA", "z", "z.a", "a.z"));
    private final String SORTED_CWD_CONTENTS_STRING_WITH_HEADER = joinStringsBySystemLineSeparator(".:",
            SORTED_CWD_CONTENTS_STRING);

    // Temporary folder A in main temporary folder
    private static Path folderAPath;
    private static final String[] FOLDER_A_NON_FOLDERS = { "0" };
    private static final String UNSORTED_FOLDER_A_CONTENTS_WITH_HEADER = joinStringsBySystemLineSeparator(
            String.format(".%s%s:", CHAR_FILE_SEP, FOLDER_A_NAME), getFolderAContents());
    private static final String SORTED_FOLDER_A_CONTENTS_WITH_HEADER = UNSORTED_FOLDER_A_CONTENTS_WITH_HEADER;

    private static final String TWO_LINE_SEPARATOR = System.lineSeparator() + System.lineSeparator();

    private static String getCwdContents() {
        List<String> fileList = Stream.concat(Arrays.stream(CWD_NON_FOLDERS), Arrays.stream(CWD_FOLDERS))
                .sorted()
                .collect(Collectors.toList());
        return String.join(System.lineSeparator(), fileList);
    }

    private static String getFolderAContents() {
        return String.join(System.lineSeparator(), FOLDER_A_NON_FOLDERS) + System.lineSeparator();
    }

    private static String joinStringsBySystemLineSeparator(String... strings) {
        return String.join(System.lineSeparator(), strings);
    }

    @BeforeAll
    static void setUpEnvironment() throws IOException {
        for (String file : CWD_NON_FOLDERS) {
            cwdPath.resolve(file).toFile().createNewFile();
        }
        for (String folder : CWD_FOLDERS) {
            Files.createDirectory(cwdPath.resolve(folder));
        }
        for (String file : FOLDER_A_NON_FOLDERS) {
            cwdPath.resolve(CWD_FOLDERS[0]).resolve(file).toFile().createNewFile();
        }

        String cwdPathName = cwdPath.toString();
        folderAPath = Paths.get(cwdPathName, FOLDER_A_NAME);
        // Set current working directory to cwdPath
        System.setProperty("user.dir", cwdPathName);
    }

    // isRecursive = false and isSortByExt = false handled in
    // LsApplicationTest.run_EmptyArgs_ReturnsCwdContents(...)
    @Test
    void buildResult_IsRecursiveIsTrue_ReturnsAllFiles() {
        // Given
        String expected = String.format("%s%s%s%s", UNSORTED_CWD_CONTENTS_WITH_HEADER, TWO_LINE_SEPARATOR,
                UNSORTED_FOLDER_A_CONTENTS_WITH_HEADER, System.lineSeparator());

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
                SORTED_FOLDER_A_CONTENTS_WITH_HEADER, System.lineSeparator());

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
     * @throws LsException Error is thrown in listCwdContent.
     */
    void testListCwdContent(String expected, boolean isSortByExt) throws LsException {
        // When
        String actual = LsApplicationHelper.listCwdContent(isSortByExt);

        // Then
        assertEquals(expected, actual);
    }

    /**
     * Tests if listCwdContent returns sorted contents when isSortByExt is true.
     *
     * @throws LsException Error is thrown in listCwdContent.
     */
    @Test
    void listCwdContent_IsSortByExtIsTrue_ReturnsSortedCwdContents() throws LsException {
        // Given
        String expected = String.join(System.lineSeparator(), SORTED_CWD_CONTENTS_STRING);

        testListCwdContent(expected, true);
    }

    /**
     * Tests if listCwdContent returns unsorted contents when isSortByExt is false.
     *
     * @throws LsException Error is thrown in listCwdContent.
     */
    @Test
    void listCwdContent_IsSortByExtIsFalse_ReturnsUnsortedCwdContents() throws LsException {
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
        List<Path> expected = List.of(folderAPath);

        // When
        List<Path> actual = LsApplicationHelper.resolvePaths(FOLDER_A_NAME);

        // Then
        assertEquals(expected, actual);
    }
}
