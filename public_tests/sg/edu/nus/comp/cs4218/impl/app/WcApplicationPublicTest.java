package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

public class WcApplicationPublicTest {
    private static final String TEMP = "temp-wc";
    private static final Path TEMP_PATH = Paths.get(TEMP);
    private static final String LABEL_TOTAL = "total";
    private static final String NUMBER_FORMAT = " %7d";
    private static final String MULTI_LINE_TEXT = "This is a test.\nThis is still a test.";
    private static final String STDIN_FILENAME = "-";
    private static final String SINGLE_LINE_TEXT = "This is a test.";
    private static final long BYTECOUNT_SINGLE = SINGLE_LINE_TEXT.getBytes().length;
    private static final long BYTECOUNT_MULTI = MULTI_LINE_TEXT.getBytes().length;
    private static final long BYTESUM_SINGLE = BYTECOUNT_SINGLE + BYTECOUNT_MULTI;
    private static final Deque<Path> FILES = new ArrayDeque<>();
    private static String initialDir;
    private WcApplication wcApplication;

    private static String formatCounts(int lineCount, int wordCount, long byteCount, String lastLine) {
        StringBuilder stringBuilder = new StringBuilder();
        if (lineCount > -1) {
            stringBuilder.append(String.format(NUMBER_FORMAT, lineCount));
        }
        if (wordCount > -1) {
            stringBuilder.append(String.format(NUMBER_FORMAT, wordCount));
        }
        if (byteCount > -1) {
            stringBuilder.append(String.format(NUMBER_FORMAT, byteCount));
        }
        if (!"".equals(lastLine)) {
            stringBuilder.append(' ').append(lastLine);
        }
        return stringBuilder.toString();
    }

    @BeforeEach
    void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        wcApplication = new WcApplication();
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

    private void createFile(String name, String content) throws IOException {
        Path path = TEMP_PATH.resolve(name);
        Files.createFile(path);
        Files.write(path, content.getBytes());
        FILES.push(path);
    }

    @Test
    void countFromFiles_NullFile_ThrowsException() {
        assertThrows(WcException.class, () -> wcApplication.countFromFiles(
                true, true, true, null));
    }

    @Test
    void countFromFiles_EmptyFile_ReturnsAllZeros() throws Exception {
        String[] fileNames = new String[]{"file1.txt"};
        String testFileContent = "";
        createFile("file1.txt", testFileContent);
        String expected = formatCounts(0, 0, 0, "file1.txt");

        String result = wcApplication.countFromFiles(true, true, true, fileNames);

        assertEquals(expected, result);
    }

    @Test
    void countFromFiles_SingleFileWithSingleLineText_ReturnsLinesWordsBytesCount() throws Exception {
        String[] fileNames = new String[]{"file2.txt"};
        createFile("file2.txt", SINGLE_LINE_TEXT);
        String expected = formatCounts(0, 4, BYTECOUNT_SINGLE, "file2.txt");

        String result = wcApplication.countFromFiles(true, true, true, fileNames);

        assertEquals(expected, result);
    }

    @Test
    void countFromFiles_SingleFileWithSingleLineTextBytesOption_ReturnBytesCount() throws Exception {
        String[] fileNames = new String[]{"file3.txt"};
        createFile("file3.txt", SINGLE_LINE_TEXT);
        String expected = formatCounts(-1, -1, BYTECOUNT_SINGLE, "file3.txt");

        String result = wcApplication.countFromFiles(true, false, false, fileNames);

        assertEquals(expected, result);
    }

    @Test
    void countFromFiles_SingleFileWithSingleLineTextLinesOption_ReturnLinesCount() throws Exception {
        String[] fileNames = new String[]{"file4.txt"};
        createFile("file4.txt", SINGLE_LINE_TEXT);
        String expected = formatCounts(0, -1, -1, "file4.txt");

        String result = wcApplication.countFromFiles(false, true, false, fileNames);

        assertEquals(expected, result);
    }

    @Test
    void countFromFiles_SingleFileWithSingleLineTextWordsOption_ReturnWordsCount() throws Exception {
        String[] fileNames = new String[]{"file5.txt"};
        createFile("file5.txt", SINGLE_LINE_TEXT);
        String expected = formatCounts(-1, 4, -1, "file5.txt");

        String result = wcApplication.countFromFiles(false, false, true, fileNames);

        assertEquals(expected, result);
    }

    @Test
    void countFromStdin_EmptyFile_ReturnsAllZeros() throws Exception {
        String testFileContent = "";
        InputStream stdin = new ByteArrayInputStream(testFileContent.getBytes());
        String expected = formatCounts(0, 0, 0, "");

        String result = wcApplication.countFromStdin(true, true, true, stdin);

        assertEquals(expected, result);
    }

    @Test
    void countFromStdin_SingleFileWithSingleLineText_ReturnsLinesWordsBytesCount() throws Exception {

        InputStream stdin = new ByteArrayInputStream(SINGLE_LINE_TEXT.getBytes());
        String expected = formatCounts(0, 4, BYTECOUNT_SINGLE, "");

        String result = wcApplication.countFromStdin(true, true, true, stdin);

        assertEquals(expected, result);
    }

    @Test
    void countFromStdin_SingleFileWithSingleLineTextBytesOption_ReturnBytesCount() throws Exception {

        InputStream stdin = new ByteArrayInputStream(SINGLE_LINE_TEXT.getBytes());
        String expected = formatCounts(-1, -1, BYTECOUNT_SINGLE, "");

        String result = wcApplication.countFromStdin(true, false, false, stdin);

        assertEquals(expected, result);
    }

    @Test
    void countFromStdin_SingleFileWithSingleLineTextLinesOption_ReturnLinesCount() throws Exception {

        InputStream stdin = new ByteArrayInputStream(SINGLE_LINE_TEXT.getBytes());
        String expected = formatCounts(0, -1, -1, "");

        String result = wcApplication.countFromStdin(false, true, false, stdin);

        assertEquals(expected, result);
    }

    @Test
    void countFromStdin_SingleFileWithSingleLineTextWordsOption_ReturnWordsCount() throws Exception {

        InputStream stdin = new ByteArrayInputStream(SINGLE_LINE_TEXT.getBytes());
        String expected = formatCounts(-1, 4, -1, "");

        String result = wcApplication.countFromStdin(false, false, true, stdin);

        assertEquals(expected, result);
    }

    @Test
    void countFromFileAndStdin_SingleFileWithSingleLineTextAndStdin_ReturnsLinesWordsBytesTotalCount() throws Exception {

        createFile("file13.txt", SINGLE_LINE_TEXT);
        InputStream stdin = new ByteArrayInputStream(MULTI_LINE_TEXT.getBytes());
        String[] fileNames = new String[]{"file13.txt", STDIN_FILENAME};
        List<String> expectedList = new ArrayList<>();
        expectedList.add(formatCounts(0, 4, BYTECOUNT_SINGLE, "file13.txt"));
        expectedList.add(formatCounts(1, 9, BYTECOUNT_MULTI, STDIN_FILENAME));
        expectedList.add(formatCounts(1, 13, BYTESUM_SINGLE, LABEL_TOTAL));
        String expected = String.join(STRING_NEWLINE, expectedList);
        String result = wcApplication.countFromFileAndStdin(true, true, true, stdin, fileNames);

        assertEquals(expected, result);
    }

    @Test
    void countFromFileAndStdin_StdinAndSingleFileWithSingleLineText_ReturnsLinesWordsBytesTotalCount() throws Exception {

        createFile("file14.txt", SINGLE_LINE_TEXT);
        InputStream stdin = new ByteArrayInputStream(MULTI_LINE_TEXT.getBytes());
        String[] fileNames = new String[]{STDIN_FILENAME, "file14.txt"};
        List<String> expectedList = new ArrayList<>();
        expectedList.add(formatCounts(1, 9, BYTECOUNT_MULTI, STDIN_FILENAME));
        expectedList.add(formatCounts(0, 4, BYTECOUNT_SINGLE, "file14.txt"));
        expectedList.add(formatCounts(1, 13, BYTESUM_SINGLE, LABEL_TOTAL));
        String expected = String.join(STRING_NEWLINE, expectedList);
        String result = wcApplication.countFromFileAndStdin(true, true, true, stdin, fileNames);

        assertEquals(expected, result);
    }

    @Test
    void countFromFileAndStdin_SingleFileWithSingleLineTextBytesOptionAndStdin_ReturnBytesTotalCount() throws Exception {

        createFile("file15.txt", SINGLE_LINE_TEXT);
        InputStream stdin = new ByteArrayInputStream(MULTI_LINE_TEXT.getBytes());
        String[] fileNames = new String[]{"file15.txt", STDIN_FILENAME};
        List<String> expectedList = new ArrayList<>();
        expectedList.add(formatCounts(-1, -1, BYTECOUNT_SINGLE, "file15.txt"));
        expectedList.add(formatCounts(-1, -1, BYTECOUNT_MULTI, STDIN_FILENAME));
        expectedList.add(formatCounts(-1, -1, BYTESUM_SINGLE, LABEL_TOTAL));
        String expected = String.join(STRING_NEWLINE, expectedList);
        String result = wcApplication.countFromFileAndStdin(true, false, false, stdin, fileNames);

        assertEquals(expected, result);
    }

    @Test
    void countFromFileAndStdin_SingleFileWithSingleLineTextLinesOptionAndStdin_ReturnLinesTotalCount() throws Exception {

        createFile("file16.txt", SINGLE_LINE_TEXT);
        InputStream stdin = new ByteArrayInputStream(MULTI_LINE_TEXT.getBytes());
        String[] fileNames = new String[]{"file16.txt", STDIN_FILENAME};
        List<String> expectedList = new ArrayList<>();
        expectedList.add(formatCounts(0, -1, -1, "file16.txt"));
        expectedList.add(formatCounts(1, -1, -1, STDIN_FILENAME));
        expectedList.add(formatCounts(1, -1, -1, LABEL_TOTAL));
        String expected = String.join(STRING_NEWLINE, expectedList);
        String result = wcApplication.countFromFileAndStdin(false, true, false, stdin, fileNames);

        assertEquals(expected, result);
    }

    @Test
    void countFromFileAndStdin_SingleFileWithSingleLineTextWordsOption_ReturnWordsTotalCount() throws Exception {

        createFile("file17.txt", SINGLE_LINE_TEXT);
        InputStream stdin = new ByteArrayInputStream(MULTI_LINE_TEXT.getBytes());
        String[] fileNames = new String[]{"file17.txt", STDIN_FILENAME};
        List<String> expectedList = new ArrayList<>();
        expectedList.add(formatCounts(-1, 4, -1, "file17.txt"));
        expectedList.add(formatCounts(-1, 9, -1, STDIN_FILENAME));
        expectedList.add(formatCounts(-1, 13, -1, LABEL_TOTAL));
        String expected = String.join(STRING_NEWLINE, expectedList);
        String result = wcApplication.countFromFileAndStdin(false, false, true, stdin, fileNames);
        assertEquals(expected, result);
    }
}
