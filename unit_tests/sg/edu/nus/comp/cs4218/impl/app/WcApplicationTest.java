package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

public class WcApplicationTest {

    private static final String FILE_NAME_A = "A.txt";
    private static final String FILE_NAME_B = "B.txt";
    private static final String NON_EXISTENT_FILE = "nonExistentFile.txt";
    private static final String WC_EXCEPTION_MSG = "wc: ";
    private static final String NUMBER_FORMAT = " %7d";
    private static final String STRING_FORMAT = " %s";
    private WcApplication app;

    @TempDir
    private Path wcTestDir;
    private String filePathA;
    private String filePathB;

    private static String appendString(int lineCount, int wordCount, int byteCount, String lastLine) {
        StringBuilder sb = new StringBuilder(); //NOPMD
        if (lineCount > -1) {
            sb.append(String.format(NUMBER_FORMAT, lineCount));
        }
        if (wordCount > -1) {
            sb.append(String.format(NUMBER_FORMAT, wordCount));
        }
        if (byteCount > -1) {
            sb.append(String.format(NUMBER_FORMAT, byteCount));
        }
        sb.append(lastLine);
        return sb.toString();
    }

    @BeforeEach
    void setUp() throws IOException {
        app = new WcApplication();

        Path pathA = wcTestDir.resolve(FILE_NAME_A);
        Path pathB = wcTestDir.resolve(FILE_NAME_B);

        filePathA = pathA.toString();
        filePathB = pathB.toString();

        String contentFileA = "This is a sample text\nTo test Wc Application\n For CS4218\n";
        String contentFileB = "Lorem Ipsum is simply\ndummy text of the printing\nand typesetting industry.\n";
        Files.write(pathA, contentFileA.getBytes(StandardCharsets.UTF_8));
        Files.write(pathB, contentFileB.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void countFromFiles_NonExistentFile_ReturnsFileNotFoundError() {
        assertDoesNotThrow(() -> {
            String result = app.countFromFiles(true, true, true, NON_EXISTENT_FILE);
            assertEquals(WC_EXCEPTION_MSG + ERR_FILE_NOT_FOUND, result);
        });
    }

    @Test
    void countFromFiles_NonExistentFileAndValidFile_ReturnsValidFileCountsAndError() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(WC_EXCEPTION_MSG + ERR_FILE_NOT_FOUND);
        expectedList.add(appendString(3, 11, 57, String.format(STRING_FORMAT, filePathA)));
        expectedList.add(appendString(3, 11, 57, " total"));
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertDoesNotThrow(() -> {
            String result = app.countFromFiles(true, true, true, NON_EXISTENT_FILE, filePathA);
            assertEquals(expected, result);
        });
    }

    @Test
    void countFromFiles_CountFromFilesWithAllFlags_ReturnsAllCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, 57, String.format(STRING_FORMAT, filePathA)));
        expectedList.add(appendString(3, 12, 75, String.format(STRING_FORMAT, filePathB)));
        expectedList.add(appendString(6, 23, 132, " total"));
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertDoesNotThrow(() -> {
            String result = app.countFromFiles(true, true, true, filePathA, filePathB);
            assertEquals(expected, result);
        });
    }

    @Test
    void countFromFiles_CountFromFilesWithBytesAndLinesFlags_ReturnsBytesAndLinesCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, -1, String.format(STRING_FORMAT, filePathA)));
        expectedList.add(appendString(3, 12, -1, String.format(STRING_FORMAT, filePathB)));
        expectedList.add(appendString(6, 23, -1, " total"));
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertDoesNotThrow(() -> {
            String result = app.countFromFiles(false, true, true, filePathA, filePathB);
            assertEquals(expected, result);
        });
    }

    @Test
    void countFromStdin_NullStdin_ThrowsException() {
        assertThrows(WcException.class, () -> {
            String result = app.countFromStdin(true, true, true, null);
            assertEquals(WC_EXCEPTION_MSG + ERR_NULL_STREAMS, result);
        });
    }

    @Test
    void countFromStdin_EmptyStdin_ReturnsCorrectCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(0, 0, 0, ""));
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertDoesNotThrow(() -> {
            String result = app.countFromStdin(true, true, true, new ByteArrayInputStream("".getBytes()));
            assertEquals(expected, result);
        });
    }

    @Test
    void countFromStdin_CountFromStdinWithAllFlags_ReturnsAllCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, 57, ""));
        String expected = String.join(STRING_NEWLINE, expectedList);
        try (InputStream inputStream = IOUtils.openInputStream(filePathA)) {
            assertDoesNotThrow(() -> {
                String result = app.countFromStdin(true, true, true, inputStream);
                assertEquals(expected, result);
            });
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void countFromStdin_CountFromStdinWithLinesAndWordsFlags_ReturnsLinesAndWordsCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, -1, ""));
        String expected = String.join(STRING_NEWLINE, expectedList);
        try (InputStream inputStream = IOUtils.openInputStream(filePathA)) {
            assertDoesNotThrow(() -> {
                String result = app.countFromStdin(false, true, true, inputStream);
                assertEquals(expected, result);
            });
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void countFromFileAndStdin_CountFromFileAndStdinWithAllFlags_ReturnsAllCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, 57, " -"));
        expectedList.add(appendString(3, 12, 75, String.format(STRING_FORMAT, filePathB)));
        expectedList.add(appendString(6, 23, 132, " total"));
        String expected = String.join(STRING_NEWLINE, expectedList);
        try (InputStream inputStream = IOUtils.openInputStream(filePathA)) {
            assertDoesNotThrow(() -> {
                String result = app.countFromFileAndStdin(true, true, true, inputStream, "-", filePathB);
                assertEquals(expected, result);
            });
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void countFromFileAndStdin_CountFromFileAndStdinWithBytesAndLinesFlags_ReturnsBytesAndLinesCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, -1, 57, " -"));
        expectedList.add(appendString(3, -1, 75, String.format(STRING_FORMAT, filePathB)));
        expectedList.add(appendString(6, -1, 132, " total"));
        String expected = String.join(STRING_NEWLINE, expectedList);
        try (InputStream inputStream = IOUtils.openInputStream(filePathA)) {
            assertDoesNotThrow(() -> {
                String result = app.countFromFileAndStdin(true, true, false, inputStream, "-", filePathB);
                assertEquals(expected, result);
            });
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

}
