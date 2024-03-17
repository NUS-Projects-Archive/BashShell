package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

class SortApplicationPublicTest {

    private static final String TEMP = "temp-sort";
    private static final Path TEMP_PATH = Paths.get(TEMP);
    private static final Deque<Path> FILES = new ArrayDeque<>();
    private static final String TEST_FILE = "file.txt";
    private static final String[] IN_NO_FLAG = {"a", "c", "b"};
    private static final String[] IN_FIST_NUM = {"10 b", "5 c", "1 a"};
    private static final String[] IN_REV_ORDER = {"a", "c", "b"};
    private static final String[] IN_CASE_IND = {"A", "C", "b"};
    private static final String[] OUT_NO_FLAG = {"a", "b", "c"};
    private static final String[] OUT_FIST_NUM = {"1 a", "5 c", "10 b"};
    private static final String[] OUT_REV_ORDER = {"c", "b", "a"};
    private static final String[] OUT_CASE_IND = {"A", "b", "C"};

    private SortApplication sortApplication;
    private String initialDir;

    private String joinStringsByLineSeparator(String... strs) {
        return String.join(TestStringUtils.STRING_NEWLINE, strs);
    }

    private InputStream generateInputStreamFromStrings(String... strs) {
        return new ByteArrayInputStream(joinStringsByLineSeparator(strs).getBytes(StandardCharsets.UTF_8));
    }

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException, IOException {
        sortApplication = new SortApplication();
        initialDir = TestEnvironmentUtil.getCurrentDirectory();
        TestEnvironmentUtil.setCurrentDirectory(TEMP_PATH.toString());
        Files.createDirectory(TEMP_PATH);
    }

    @AfterEach
    void tearDown() throws IOException, NoSuchFieldException, IllegalAccessException {
        Files.walk(TEMP_PATH)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        TestEnvironmentUtil.setCurrentDirectory(initialDir);
        for (Path file : FILES) {
            Files.deleteIfExists(file);
        }
    }

    private void createFile(String content) throws IOException {
        Path path = TEMP_PATH.resolve(SortApplicationPublicTest.TEST_FILE);
        Files.createFile(path);
        Files.write(path, content.getBytes());
        FILES.push(path);
    }

    @Test
    void sortFromStdin_NoFlags_ReturnsSortedList() {
        String actual = assertDoesNotThrow(() -> {
            InputStream stdin = generateInputStreamFromStrings(IN_NO_FLAG);
            return sortApplication.sortFromStdin(false, false, false, stdin);
        });
        String expected = joinStringsByLineSeparator(OUT_NO_FLAG);
        assertEquals(expected, actual);
    }

    @Test
    void sortFromStdin_IsFirstWordNumber_ReturnsSortedList() {
        String actual = assertDoesNotThrow(() -> {
            InputStream stdin = generateInputStreamFromStrings(IN_FIST_NUM);
            return sortApplication.sortFromStdin(true, false, false, stdin);
        });
        String expected = joinStringsByLineSeparator(OUT_FIST_NUM);
        assertEquals(expected, actual);
    }

    @Test
    void sortFromStdin_ReverseOrder_ReverseSortedList() {
        String actual = assertDoesNotThrow(() -> {
            InputStream stdin = generateInputStreamFromStrings(IN_REV_ORDER);
            return sortApplication.sortFromStdin(false, true, false, stdin);
        });
        String expected = joinStringsByLineSeparator(OUT_REV_ORDER);
        assertEquals(expected, actual);
    }

    @Test
    void sortFromStdin_CaseIndependent_CaseIndependentSortedList() {
        String actual = assertDoesNotThrow(() -> {
            InputStream stdin = generateInputStreamFromStrings(IN_CASE_IND);
            return sortApplication.sortFromStdin(false, false, true, stdin);
        });
        String expected = joinStringsByLineSeparator(OUT_CASE_IND);
        assertEquals(expected, actual);
    }

    @Test
    void sortFromFiles_NoFlags_ReturnsSortedList() {
        assertDoesNotThrow(() -> createFile(joinStringsByLineSeparator(IN_NO_FLAG)));
        String actual = assertDoesNotThrow(() -> sortApplication.sortFromFiles(false, false, false, TEST_FILE));
        String expected = joinStringsByLineSeparator(OUT_NO_FLAG);
        assertEquals(expected, actual);
    }

    @Test
    void sortFromFiles_IsFirstWordNumber_ReturnsSortedList() {
        assertDoesNotThrow(() -> createFile(joinStringsByLineSeparator(IN_FIST_NUM)));
        String actual = assertDoesNotThrow(() -> sortApplication.sortFromFiles(true, false, false, TEST_FILE));
        String expected = joinStringsByLineSeparator(OUT_FIST_NUM);
        assertEquals(expected, actual);
    }

    @Test
    void sortFromFiles_ReverseOrder_ReverseSortedList() {
        assertDoesNotThrow(() -> createFile(joinStringsByLineSeparator(IN_REV_ORDER)));
        String actual = assertDoesNotThrow(() -> sortApplication.sortFromFiles(false, true, false, TEST_FILE));
        String expected = joinStringsByLineSeparator(OUT_REV_ORDER);
        assertEquals(expected, actual);
    }

    @Test
    void sortFromFiles_CaseIndependent_CaseIndependentSortedList() {
        assertDoesNotThrow(() -> createFile(joinStringsByLineSeparator(IN_CASE_IND)));
        String actual = assertDoesNotThrow(() -> sortApplication.sortFromFiles(false, false, true, TEST_FILE));
        String expected = joinStringsByLineSeparator(OUT_CASE_IND);
        assertEquals(expected, actual);
    }
}
