package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

@SuppressWarnings("PMD.ClassNamingConventions")
public class WcApplicationIT {

    private static final String FILE_NAME_A = "A.txt";
    private static final String FILE_NAME_B = "B.txt";
    private static final String NON_EXISTENT_FILE = "nonExistentFile.txt";
    private static final String STDIN = "-";
    private static final String NUMBER_FORMAT = " %7d";
    private static final String STRING_FORMAT = " %s";
    private static final String TOTAL_LAST_LINE = " total";

    private WcApplication app;
    private ByteArrayOutputStream output;
    private String fileA;
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
        output = new ByteArrayOutputStream();

        String contentFileA = "This is a sample text\nTo test Wc Application\n For CS4218\n";
        String contentFileB = "Lorem Ipsum is simply\ndummy text of the printing\nand typesetting industry.\n";
        fileA = createNewFile(FILE_NAME_A, contentFileA).toString();
        fileB = createNewFile(FILE_NAME_B, contentFileB).toString();
    }

    @Test
    void run_NullStdin_ThrowsWcException() {
        WcException result = assertThrowsExactly(WcException.class, () -> app.run(new String[]{fileA}, null, System.out));
        String expected = "wc: InputStream not provided";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_NullStdout_ThrowsWcException() {
        WcException result = assertThrowsExactly(WcException.class, () -> app.run(new String[]{fileA}, System.in, null));
        String expected = "wc: Null Pointer Exception";
        assertEquals(expected, result.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-C", "-L", "-W"})
    void run_InvalidFlags_ThrowsWcException(String args) {
        WcException result = assertThrowsExactly(WcException.class, () -> app.run(new String[]{args}, System.in, System.out));
        String expected = "wc: illegal option -- " + args.charAt(1);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_FailsToWriteToOutputStream_ThrowsWcException() {
        WcException result = assertThrowsExactly(WcException.class, () -> {
            InputStream mockStdin = mock(InputStream.class);
            OutputStream mockStdout = mock(OutputStream.class);
            doThrow(new IOException()).when(mockStdout).write(any(byte[].class));
            app.run(new String[]{fileA}, mockStdin, mockStdout);
        });
        String expected = "wc: Could not write to output stream";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_NonExistentFileAndValidFile_ReturnsFileCountAndException() {
        assertDoesNotThrow(() -> app.run(new String[]{NON_EXISTENT_FILE, fileA}, System.in, output));
        List<String> expectedList = new ArrayList<>();
        expectedList.add("wc: 'nonExistentFile.txt': No such file or directory");
        expectedList.add(appendString(3, 11, 57, String.format(STRING_FORMAT, fileA)));
        expectedList.add(appendString(3, 11, 57, TOTAL_LAST_LINE));
        String expected = String.join(STRING_NEWLINE, expectedList) + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }


    @Test
    void run_MultipleFilesNoStdinNoFlags_PrintsFilesAllCounts() {
        assertDoesNotThrow(() -> app.run(new String[]{fileA, fileB}, System.in, output));
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, 57, String.format(STRING_FORMAT, fileA)));
        expectedList.add(appendString(3, 12, 75, String.format(STRING_FORMAT, fileB)));
        expectedList.add(appendString(6, 23, 132, TOTAL_LAST_LINE));
        String expected = String.join(STRING_NEWLINE, expectedList) + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_MultipleFilesBytesAndLinesFlags_PrintsBytesAndLineCounts() {
        assertDoesNotThrow(() -> app.run(new String[]{"-c", "-l", fileA, fileB}, System.in, output));
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, -1, 57, String.format(STRING_FORMAT, fileA)));
        expectedList.add(appendString(3, -1, 75, String.format(STRING_FORMAT, fileB)));
        expectedList.add(appendString(6, -1, 132, TOTAL_LAST_LINE));
        String expected = String.join(STRING_NEWLINE, expectedList) + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_MultipleFilesAndWordFlag_PrintsWordCount() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(-1, 11, -1, String.format(STRING_FORMAT, fileA)));
        expectedList.add(appendString(-1, 12, -1, String.format(STRING_FORMAT, fileB)));
        expectedList.add(appendString(-1, 23, -1, TOTAL_LAST_LINE));
        String expected = String.join(STRING_NEWLINE, expectedList) + STRING_NEWLINE;

        assertDoesNotThrow(() -> app.run(new String[]{"-w", fileA, fileB}, System.in, output));
        assertEquals(expected, output.toString());
    }

    @Test
    void run_StdinNoFilesNoFlags_PrintsStdinAllCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, 57, ""));
        String expected = String.join(STRING_NEWLINE, expectedList) + STRING_NEWLINE;

        try (InputStream inputStream = IOUtils.openInputStream(fileA)) {
            assertDoesNotThrow(() -> app.run(new String[]{}, inputStream, output));
            assertEquals(expected, output.toString());
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void run_StdinNoFilesWordAndByteFlags_PrintsWordAndByteCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(-1, 11, 57, ""));
        String expected = String.join(STRING_NEWLINE, expectedList) + STRING_NEWLINE;

        try (InputStream inputStream = IOUtils.openInputStream(fileA)) {
            assertDoesNotThrow(() -> app.run(new String[]{"-c", "-w"}, inputStream, output));
            assertEquals(expected, output.toString());
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void run_FilesAndStdinNoFlags_PrintsFilesAndStdinAllCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, 57, " -"));
        expectedList.add(appendString(3, 12, 75, String.format(STRING_FORMAT, fileB)));
        expectedList.add(appendString(6, 23, 132, TOTAL_LAST_LINE));
        String expected = String.join(STRING_NEWLINE, expectedList) + STRING_NEWLINE;

        try (InputStream inputStream = IOUtils.openInputStream(fileA)) {
            assertDoesNotThrow(() -> app.run(new String[]{STDIN, fileB}, inputStream, output));
            assertEquals(expected, output.toString());
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void run_FilesAndStdinWordAndLineFlags_PrintsWordAndLineCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, -1, " -"));
        expectedList.add(appendString(3, 12, -1, String.format(STRING_FORMAT, fileB)));
        expectedList.add(appendString(6, 23, -1, TOTAL_LAST_LINE));
        String expected = String.join(STRING_NEWLINE, expectedList) + STRING_NEWLINE;

        try (InputStream inputStream = IOUtils.openInputStream(fileA)) {
            assertDoesNotThrow(() -> app.run(new String[]{"-w", "-l", STDIN, fileB}, inputStream, output));
            assertEquals(expected, output.toString());
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void run_NonExistentFileAndStdin_ReturnsCorrectCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, 57, " -"));
        expectedList.add("wc: 'nonExistentFile.txt': No such file or directory");
        expectedList.add(appendString(3, 11, 57, TOTAL_LAST_LINE));
        String expected = String.join(STRING_NEWLINE, expectedList) + STRING_NEWLINE;

        try (InputStream inputStream = IOUtils.openInputStream(fileA)) {
            assertDoesNotThrow(() -> app.run(new String[]{STDIN, NON_EXISTENT_FILE}, inputStream, output));
            assertEquals(expected, output.toString());
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void run_ValidFileAndEmptyStdin_ReturnsCorrectCounts() {
        // Given
        InputStream mockedInputStream = new ByteArrayInputStream("".getBytes());

        // When
        assertDoesNotThrow(() -> app.run(new String[]{STDIN, fileA}, mockedInputStream, output));

        // Then
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(0, 0, 0, " -"));
        expectedList.add(appendString(3, 11, 57, String.format(STRING_FORMAT, fileA)));
        expectedList.add(appendString(3, 11, 57, TOTAL_LAST_LINE));
        String expected = String.join(STRING_NEWLINE, expectedList) + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void countFromFileAndStdin_CountFromFileAndStdinWithAllFlags_ReturnsAllCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, 57, " -"));
        expectedList.add(appendString(3, 12, 75, String.format(STRING_FORMAT, fileB)));
        expectedList.add(appendString(6, 23, 132, TOTAL_LAST_LINE));
        String expected = String.join(STRING_NEWLINE, expectedList);

        try (InputStream inputStream = IOUtils.openInputStream(fileA)) {
            String result = assertDoesNotThrow(() -> app.countFromFileAndStdin(true, true, true, inputStream, "-", fileB));
            assertEquals(expected, result);
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void countFromFileAndStdin_CountFromFileAndStdinWithBytesAndLinesFlags_ReturnsBytesAndLinesCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, -1, 57, " -"));
        expectedList.add(appendString(3, -1, 75, String.format(STRING_FORMAT, fileB)));
        expectedList.add(appendString(6, -1, 132, TOTAL_LAST_LINE));
        String expected = String.join(STRING_NEWLINE, expectedList);

        try (InputStream inputStream = IOUtils.openInputStream(fileA)) {
            String result = assertDoesNotThrow(() -> app.countFromFileAndStdin(true, true, false, inputStream, "-", fileB));
            assertEquals(expected, result);
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }
}
