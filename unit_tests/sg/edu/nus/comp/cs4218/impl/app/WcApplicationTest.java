package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewDirectory;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

public class WcApplicationTest {

    private static final String FILE_NAME_A = "A.txt";
    private static final String FILE_NAME_B = "B.txt";
    private static final String NON_EXISTENT_FILE = "nonExistentFile.txt";
    private static final String NUMBER_FORMAT = " %7d";
    private static final String STRING_FORMAT = " %s";
    private static final String TOTAL_LAST_LINE = " total";
    private static final String ERR_NON_EXIST = "wc: 'nonExistentFile.txt': No such file or directory";

    private WcApplication app;
    private Path pathA;
    private String fileA;
    private String fileAName;
    private String fileB;

    private static String appendString(int lineCount, int wordCount, int byteCount, String lastLine) {
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
        stringBuilder.append(lastLine);
        return stringBuilder.toString();
    }

    @BeforeEach
    void setUp() throws IOException {
        app = new WcApplication();

        String contentFileA = "This is a sample text\nTo test Wc Application\n For CS4218\n";
        String contentFileB = "Lorem Ipsum is simply\ndummy text of the printing\nand typesetting industry.\n";
        pathA = createNewFile(FILE_NAME_A, contentFileA);
        fileA = pathA.toString();
        fileAName = pathA.toFile().getName();
        fileB = createNewFile(FILE_NAME_B, contentFileB).toString();
    }

    @Test
    void countFromFiles_NonExistentFile_ThrowsWcException() {
        WcException result = assertThrowsExactly(WcException.class, () ->
                app.countFromFiles(false, false, false, NON_EXISTENT_FILE)
        );
        assertEquals(ERR_NON_EXIST, result.getMessage());
    }

    @Test
    void countFromFiles_NonExistentFileAndValidFile_ThrowsWcException() {
        WcException result = assertThrowsExactly(WcException.class, () ->
                app.countFromFiles(false, false, false, NON_EXISTENT_FILE, fileA)
        );
        assertEquals(ERR_NON_EXIST, result.getMessage());
    }

    @Test
    void countFromFiles_FileGivenAsDirectory_ThrowsWcException(@TempDir Path tempDir) {
        String dir = createNewDirectory(tempDir, "directory").toString();
        WcException result = assertThrowsExactly(WcException.class, () ->
                app.countFromFiles(false, false, false, dir)
        );
        String expected = "wc: 'directory': Is a directory";
        assertEquals(expected, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void countFromFiles_FileNoPermissionToRead_ThrowsCatException() {
        boolean isSetReadable = pathA.toFile().setReadable(false);
        assertTrue(isSetReadable, "Failed to set read permission to false for test");
        WcException result = assertThrowsExactly(WcException.class, () -> app.countFromFiles(false, false, false, fileA));
        String expected = String.format("wc: '%s': Could not read file", fileAName);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void countFromFiles_CountFromFilesWithAllFlags_ReturnsAllCounts() {
        String result = assertDoesNotThrow(() -> app.countFromFiles(true, true, true, fileA, fileB));
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, 57, String.format(STRING_FORMAT, fileA)));
        expectedList.add(appendString(3, 12, 75, String.format(STRING_FORMAT, fileB)));
        expectedList.add(appendString(6, 23, 132, TOTAL_LAST_LINE));
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertEquals(expected, result);
    }

    @Test
    void countFromFiles_CountFromFilesWithBytesAndLinesFlags_ReturnsBytesAndLinesCounts() {
        String result = assertDoesNotThrow(() -> app.countFromFiles(false, true, true, fileA, fileB));
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, -1, String.format(STRING_FORMAT, fileA)));
        expectedList.add(appendString(3, 12, -1, String.format(STRING_FORMAT, fileB)));
        expectedList.add(appendString(6, 23, -1, TOTAL_LAST_LINE));
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertEquals(expected, result);
    }

    @Test
    void countFromStdin_NullStdin_ThrowsWcException() {
        WcException result = assertThrowsExactly(WcException.class, () -> app.countFromStdin(true, true, true, null));
        String expected = "wc: Null Pointer Exception";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void countFromStdin_EmptyStdin_ReturnsCorrectCounts() {
        String result = assertDoesNotThrow(() -> app.countFromStdin(true, true, true, new ByteArrayInputStream("".getBytes())));
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(0, 0, 0, ""));
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertEquals(expected, result);
    }

    @Test
    void countFromStdin_CountFromStdinWithAllFlags_ReturnsAllCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, 57, ""));
        String expected = String.join(STRING_NEWLINE, expectedList);

        try (InputStream inputStream = IOUtils.openInputStream(fileA)) {
            String result = assertDoesNotThrow(() -> app.countFromStdin(true, true, true, inputStream));
            assertEquals(expected, result);
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void countFromStdin_CountFromStdinWithLinesAndWordsFlags_ReturnsLinesAndWordsCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, -1, ""));
        String expected = String.join(STRING_NEWLINE, expectedList);

        try (InputStream inputStream = IOUtils.openInputStream(fileA)) {
            String result = assertDoesNotThrow(() -> app.countFromStdin(false, true, true, inputStream));
            assertEquals(expected, result);
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }
}
