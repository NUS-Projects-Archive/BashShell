package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

@SuppressWarnings("PMD.ClassNamingConventions")
class SortApplicationPublicIT {

    private static final String TEMP = "temp-sort";
    private static final Path TEMP_PATH = Paths.get(TEMP);
    private static final Deque<Path> FILES = new ArrayDeque<>();
    private static final String TEST_FILE = "file.txt";
    private static final String NUMBER_FLAG = "-n";
    private static final String REVERSE_FLAG = "-r";
    private static final String CASE_IND_FLAG = "-f";
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
        Files.createDirectory(TEMP_PATH);
        TestEnvironmentUtil.setCurrentDirectory(TEMP_PATH.toString());
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
        Path path = TEMP_PATH.resolve(SortApplicationPublicIT.TEST_FILE);
        Files.createFile(path);
        Files.write(path, content.getBytes());
        FILES.push(path);
    }

    @Test
    void sortFromStdin_NoFlags_ReturnsSortedList() {
        String[] argList = new String[0];
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            InputStream stdin = generateInputStreamFromStrings(IN_NO_FLAG);
            sortApplication.run(argList, stdin, output);
        });
        String expected = joinStringsByLineSeparator(OUT_NO_FLAG) + STRING_NEWLINE;
        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void sortFromStdin_IsFirstWordNumber_ReturnsSortedList() {
        String[] argList = new String[]{NUMBER_FLAG};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            InputStream stdin = generateInputStreamFromStrings(IN_FIST_NUM);
            sortApplication.run(argList, stdin, output);
        });
        String expected = joinStringsByLineSeparator(OUT_FIST_NUM) + STRING_NEWLINE;
        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void sortFromStdin_ReverseOrder_ReverseSortedList() {
        String[] argList = new String[]{REVERSE_FLAG};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            InputStream stdin = generateInputStreamFromStrings(IN_REV_ORDER);
            sortApplication.run(argList, stdin, output);
        });
        String expected = joinStringsByLineSeparator(OUT_REV_ORDER) + STRING_NEWLINE;
        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void sortFromStdin_CaseIndependent_CaseIndependentSortedList() {
        String[] argList = new String[]{CASE_IND_FLAG};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            InputStream stdin = generateInputStreamFromStrings(IN_CASE_IND);
            sortApplication.run(argList, stdin, output);
        });
        String expected = joinStringsByLineSeparator(OUT_CASE_IND) + STRING_NEWLINE;
        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void sortFromFiles_NoFlags_ReturnsSortedList() {
        assertDoesNotThrow(() -> createFile(joinStringsByLineSeparator(IN_NO_FLAG)));
        String[] argList = new String[]{TEST_FILE};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> sortApplication.run(argList, System.in, output));
        String expected = joinStringsByLineSeparator(OUT_NO_FLAG) + STRING_NEWLINE;
        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void sortFromFiles_IsFirstWordNumber_ReturnsSortedList() {
        assertDoesNotThrow(() -> createFile(joinStringsByLineSeparator(IN_FIST_NUM)));
        String[] argList = new String[]{NUMBER_FLAG, TEST_FILE};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> sortApplication.run(argList, System.in, output));
        String expected = joinStringsByLineSeparator(OUT_FIST_NUM) + STRING_NEWLINE;
        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void sortFromFiles_ReverseOrder_ReverseSortedList() {
        assertDoesNotThrow(() -> createFile(joinStringsByLineSeparator(IN_REV_ORDER)));
        String[] argList = new String[]{REVERSE_FLAG, TEST_FILE};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> sortApplication.run(argList, System.in, output));
        String expected = joinStringsByLineSeparator(OUT_REV_ORDER) + STRING_NEWLINE;
        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void sortFromFiles_CaseIndependent_CaseIndependentSortedList() {
        assertDoesNotThrow(() -> createFile(joinStringsByLineSeparator(IN_CASE_IND)));
        String[] argList = new String[]{CASE_IND_FLAG, TEST_FILE};
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> sortApplication.run(argList, System.in, output));
        String expected = joinStringsByLineSeparator(OUT_CASE_IND) + STRING_NEWLINE;
        assertEquals(expected, output.toString(StandardCharsets.UTF_8));
    }
}
