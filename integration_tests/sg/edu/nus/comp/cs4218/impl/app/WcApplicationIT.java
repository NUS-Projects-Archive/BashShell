package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

@SuppressWarnings("PMD.ClassNamingConventions")
public class WcApplicationIT {
    private static final String FILE_NAME_A = "A.txt";
    private static final String FILE_NAME_B = "B.txt";
    private static final String NON_EXISTENT_FILE = "nonExistentFile.txt";
    private static final String WC_EXCEPTION_MSG = "wc: ";
    private static final String STDIN = "-";
    private static final String NUMBER_FORMAT = " %7d";
    private static final String STRING_FORMAT = " %s";
    private ByteArrayOutputStream outputStream;
    private WcApplication wcApplication;
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
        this.wcApplication = new WcApplication();
        this.outputStream = new ByteArrayOutputStream();

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
    void run_NullStdin_ThrowsWcException() {
        Throwable result = assertThrows(WcException.class, () -> {
            wcApplication.run(new String[]{filePathA}, null, System.out);
        });
        assertEquals(WC_EXCEPTION_MSG + ERR_NO_ISTREAM, result.getMessage());
    }

    @Test
    void run_NullStdout_ThrowsWcException() {
        Throwable result = assertThrows(WcException.class, () -> {
            wcApplication.run(new String[]{filePathA}, System.in, null);
        });
        assertEquals(WC_EXCEPTION_MSG + ERR_NULL_STREAMS, result.getMessage());
    }

    @Test
    void run_InvalidFlags_ThrowsException() {
        String expected = WC_EXCEPTION_MSG + "illegal option -- W";
        WcException exception = assertThrowsExactly(WcException.class, () -> {
            this.wcApplication.run(new String[]{"-W"}, System.in, System.out);
        });
        assertEquals(expected, exception.getMessage());
    }

    @Test
    void run_NonExistentFileAndValidFile_ReturnsFileCountAndException() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(WC_EXCEPTION_MSG + ERR_FILE_NOT_FOUND);
        expectedList.add(appendString(3, 11, 57, String.format(STRING_FORMAT, filePathA)));
        expectedList.add(appendString(3, 11, 57, " total"));
        expectedList.add("");
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertDoesNotThrow(() -> {
            this.wcApplication.run(new String[]{NON_EXISTENT_FILE, filePathA}, System.in, this.outputStream);
        });
        assertEquals(expected, this.outputStream.toString());
    }


    @Test
    void run_MultipleFilesNoStdinNoFlags_PrintsFilesAllCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, 57, String.format(STRING_FORMAT, filePathA)));
        expectedList.add(appendString(3, 12, 75, String.format(STRING_FORMAT, filePathB)));
        expectedList.add(appendString(6, 23, 132, " total"));
        expectedList.add("");
        String expected = String.join(STRING_NEWLINE, expectedList);

        assertDoesNotThrow(() -> {
            this.wcApplication.run(new String[]{filePathA, filePathB}, System.in, this.outputStream);
        });
        assertEquals(expected, this.outputStream.toString());
    }

    @Test
    void run_MultipleFilesBytesAndLinesFlags_PrintsBytesAndLineCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, -1, 57, String.format(STRING_FORMAT, filePathA)));
        expectedList.add(appendString(3, -1, 75, String.format(STRING_FORMAT, filePathB)));
        expectedList.add(appendString(6, -1, 132, " total"));
        expectedList.add("");
        String expected = String.join(STRING_NEWLINE, expectedList);

        assertDoesNotThrow(() -> {
            this.wcApplication.run(new String[]{"-c", "-l", filePathA, filePathB}, System.in, this.outputStream);
        });
        assertEquals(expected, this.outputStream.toString());
    }

    @Test
    void run_MultipleFilesAndWordFlag_PrintsWordCount() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(-1, 11, -1, String.format(STRING_FORMAT, filePathA)));
        expectedList.add(appendString(-1, 12, -1, String.format(STRING_FORMAT, filePathB)));
        expectedList.add(appendString(-1, 23, -1, " total"));
        expectedList.add("");
        String expected = String.join(STRING_NEWLINE, expectedList);

        assertDoesNotThrow(() -> {
            this.wcApplication.run(new String[]{"-w", filePathA, filePathB}, System.in, this.outputStream);
        });
        assertEquals(expected, this.outputStream.toString());
    }

    @Test
    void run_StdinNoFilesNoFlags_PrintsStdinAllCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, 57, ""));
        expectedList.add("");
        String expected = String.join(STRING_NEWLINE, expectedList);

        try (InputStream inputStream = IOUtils.openInputStream(filePathA)) {
            assertDoesNotThrow(() -> {
                this.wcApplication.run(new String[]{}, inputStream, this.outputStream);
            });
            assertEquals(expected, this.outputStream.toString());
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void run_StdinNoFilesWordAndByteFlags_PrintsWordAndByteCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(-1, 11, 57, ""));
        expectedList.add("");
        String expected = String.join(STRING_NEWLINE, expectedList);

        try (InputStream inputStream = IOUtils.openInputStream(filePathA)) {
            assertDoesNotThrow(() -> {
                this.wcApplication.run(new String[]{"-c", "-w"}, inputStream, this.outputStream);
            });
            assertEquals(expected, this.outputStream.toString());
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void run_FilesAndStdinNoFlags_PrintsFilesAndStdinAllCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, 57, " -"));
        expectedList.add(appendString(3, 12, 75, String.format(STRING_FORMAT, filePathB)));
        expectedList.add(appendString(6, 23, 132, " total"));
        expectedList.add("");
        String expected = String.join(STRING_NEWLINE, expectedList);

        try (InputStream inputStream = IOUtils.openInputStream(filePathA)) {
            assertDoesNotThrow(() -> {
                this.wcApplication.run(new String[]{STDIN, filePathB}, inputStream, this.outputStream);
            });
            assertEquals(expected, this.outputStream.toString());
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void run_FilesAndStdinWordAndLineFlags_PrintsWordAndLineCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, -1, " -"));
        expectedList.add(appendString(3, 12, -1, String.format(STRING_FORMAT, filePathB)));
        expectedList.add(appendString(6, 23, -1, " total"));
        expectedList.add("");
        String expected = String.join(STRING_NEWLINE, expectedList);

        try (InputStream inputStream = IOUtils.openInputStream(filePathA)) {
            assertDoesNotThrow(() -> {
                this.wcApplication.run(new String[]{"-w", "-l", STDIN, filePathB}, inputStream, this.outputStream);
            });
            assertEquals(expected, this.outputStream.toString());
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void run_NonExistentFileAndStdin_ReturnsCorrectCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, 57, " -"));
        expectedList.add(WC_EXCEPTION_MSG + ERR_FILE_NOT_FOUND);
        expectedList.add(appendString(3, 11, 57, " total"));
        expectedList.add("");
        String expected = String.join(STRING_NEWLINE, expectedList);

        try (InputStream inputStream = IOUtils.openInputStream(filePathA)) {
            assertDoesNotThrow(() -> {
                this.wcApplication.run(new String[]{STDIN, NON_EXISTENT_FILE}, inputStream, this.outputStream);
            });
            assertEquals(expected, this.outputStream.toString());
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }

    }

    @Test
    void run_ValidFileAndEmptyStdin_ReturnsCorrectCounts() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(0, 0, 0, " -"));
        expectedList.add(appendString(3, 11, 57, String.format(STRING_FORMAT, filePathA)));
        expectedList.add(appendString(3, 11, 57, " total"));
        expectedList.add("");
        String expected = String.join(STRING_NEWLINE, expectedList);

        InputStream mockedInputStream = new ByteArrayInputStream("".getBytes());
        assertDoesNotThrow(() -> {
            this.wcApplication.run(new String[]{STDIN, filePathA}, mockedInputStream, this.outputStream);
        });
        assertEquals(expected, this.outputStream.toString());
    }
}
