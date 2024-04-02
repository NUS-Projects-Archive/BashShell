package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.AssertUtils.assertEmptyString;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.CutException;

@SuppressWarnings("PMD.ClassNamingConventions")
public class CutApplicationIT {

    private static final String FLAG_CUT_BY_CHAR = "-c";
    private static final String FILE_ONE = "file1.txt";
    private static final String FILE_TWO = "file2.txt";
    private static final String FILE_ONE_CONTENT = "1234567890";
    private static final String FILE_TWO_CONTENT = "0987654321";
    private static final String STDIN_CONTENT = "lorem ipsum";
    private static final String ONE_TO_FIVE = "12345";
    private static final String ZERO_TO_SIX = "09876";
    private static final String STDIN_FIRST_FIVE = "lorem";
    private static final String RANGE_ONE_TO_FIVE = "1-5";

    private String fileOne;
    private String fileTwo;
    private CutApplication app;

    @BeforeEach
    void setUp() throws IOException {
        app = new CutApplication();

        // Create temporary file, automatically deletes after test execution
        fileOne = createNewFile(FILE_ONE, FILE_ONE_CONTENT).toString();
        fileTwo = createNewFile(FILE_TWO, FILE_TWO_CONTENT).toString();
    }

    @Test
    void run_NullArgs_ThrowsCutException() {
        CutException result = assertThrowsExactly(CutException.class, () -> app.run(null, null, null));
        String expected = "cut: Null arguments";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_EmptyArgs_ThrowsCutException() {
        CutException result = assertThrowsExactly(CutException.class, () -> app.run(new String[0], null, null));
        String expected = "cut: Null arguments";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_InsufficientArgs_ThrowsCutException() {
        String[] args = {FLAG_CUT_BY_CHAR};
        CutException result = assertThrowsExactly(CutException.class, () -> app.run(args, null, null));
        String expected = "cut: Insufficient arguments";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_NoStdin_ThrowsCutException() {
        String[] args = {FLAG_CUT_BY_CHAR, RANGE_ONE_TO_FIVE};
        CutException result = assertThrowsExactly(CutException.class, () -> app.run(args, null, null));
        String expected = "cut: InputStream not provided";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_NoStdout_ThrowsCutException() {
        String[] args = {FLAG_CUT_BY_CHAR, RANGE_ONE_TO_FIVE, fileOne};
        CutException result = assertThrowsExactly(CutException.class, () -> {
            InputStream mockStdin = mock(InputStream.class);
            app.run(args, mockStdin, null);
        });
        String expected = "cut: OutputStream not provided";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_FailsToReadFromInputStream_ThrowsCutException() {
        String[] args = {FLAG_CUT_BY_CHAR, RANGE_ONE_TO_FIVE};
        CutException result = assertThrowsExactly(CutException.class, () -> {
            InputStream mockStdin = mock(InputStream.class);
            doThrow(new IOException()).when(mockStdin).read(any(byte[].class));
            OutputStream mockStdout = mock(OutputStream.class);
            app.run(args, mockStdin, mockStdout);
        });
        String expected = "cut: IOException";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_FailsToWriteToOutputStream_ThrowsCutException() {
        String[] args = {FLAG_CUT_BY_CHAR, RANGE_ONE_TO_FIVE, fileOne};
        CutException result = assertThrowsExactly(CutException.class, () -> {
            InputStream mockStdin = mock(InputStream.class);
            OutputStream mockStdout = mock(OutputStream.class);
            doThrow(new IOException()).when(mockStdout).write(any(byte[].class));
            app.run(args, mockStdin, mockStdout);
        });
        String expected = "cut: Could not write to output stream";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_ValidFilesAndStdin_WritesToOutput() {
        String[] args = {FLAG_CUT_BY_CHAR, RANGE_ONE_TO_FIVE, fileOne, "-", fileTwo};
        InputStream stdin = new ByteArrayInputStream(STDIN_CONTENT.getBytes());
        OutputStream stdout = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> app.run(args, stdin, stdout));
        String expected = ONE_TO_FIVE + STRING_NEWLINE + STDIN_FIRST_FIVE + STRING_NEWLINE + ZERO_TO_SIX + STRING_NEWLINE;
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_SomeInvalidFiles_WritesToOutput(@TempDir Path tempDir) {
        String nonExistFile = tempDir.resolve("nonExistFile.txt").toString();
        String[] args = {FLAG_CUT_BY_CHAR, RANGE_ONE_TO_FIVE, fileOne, "-", fileTwo, nonExistFile};
        InputStream stdin = new ByteArrayInputStream(STDIN_CONTENT.getBytes());
        OutputStream stdout = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> app.run(args, stdin, stdout));
        String expected = ONE_TO_FIVE + STRING_NEWLINE + STDIN_FIRST_FIVE + STRING_NEWLINE + ZERO_TO_SIX + STRING_NEWLINE +
                "cut: 'nonExistFile.txt': No such file or directory" + STRING_NEWLINE;
        assertEquals(expected, stdout.toString());
    }

    @Test
    void cutFromFileAndStdin_BothCutOptionsFalse_ThrowsCutException() {
        CutException result = assertThrowsExactly(CutException.class, () ->
                app.cutFromFileAndStdin(false, false, null, null)
        );
        String expected = "cut: Exactly one flag (cut by character or byte) should be selected, but not both";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void cutFromFileAndStdin_BothCutOptionsTrue_ThrowsCutException() {
        CutException result = assertThrowsExactly(CutException.class, () ->
                app.cutFromFileAndStdin(true, true, null, null)
        );
        String expected = "cut: Exactly one flag (cut by character or byte) should be selected, but not both";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void cutFromFileAndStdin_NoFilesAndStdin_ReturnsEmptyString() {
        String result = assertDoesNotThrow(() -> app.cutFromFileAndStdin(true, false, null, null));
        assertEmptyString(result);
    }

    @Test
    void cutFromFileAndStdin_CutByCharAndStdinOnly_ReturnsCutString() {
        List<int[]> range = List.of(new int[]{1, 5});
        InputStream stdin = new ByteArrayInputStream(STDIN_CONTENT.getBytes());
        String result = assertDoesNotThrow(() -> app.cutFromFileAndStdin(true, false, range, stdin, "-"));
        assertEquals(STDIN_FIRST_FIVE, result);
    }

    @Test
    void cutFromFileAndStdin_CutByByteAndStdinOnly_ReturnsCutString() {
        List<int[]> range = List.of(new int[]{1, 5});
        InputStream stdin = new ByteArrayInputStream(STDIN_CONTENT.getBytes());
        String result = assertDoesNotThrow(() -> app.cutFromFileAndStdin(false, true, range, stdin, "-"));
        assertEquals(STDIN_FIRST_FIVE, result);
    }

    @Test
    void cutFromFileAndStdin_CutByCharFileOnly_ReturnsCutString() {
        List<int[]> range = List.of(new int[]{1, 5});
        String result = assertDoesNotThrow(() -> app.cutFromFileAndStdin(true, false, range, null, fileOne));
        assertEquals(ONE_TO_FIVE, result);
    }

    @Test
    void cutFromFileAndStdin_CutByByteFileOnly_ReturnsCutString() {
        List<int[]> range = List.of(new int[]{1, 5});
        String result = assertDoesNotThrow(() -> app.cutFromFileAndStdin(false, true, range, null, fileOne));
        assertEquals(ONE_TO_FIVE, result);
    }

    @Test
    void cutFromFileAndStdin_ValidFilesAndStdin_WritesToOutput() {
        List<int[]> range = List.of(new int[]{1, 5});
        InputStream stdin = new ByteArrayInputStream(STDIN_CONTENT.getBytes());
        String result = assertDoesNotThrow(() ->
                app.cutFromFileAndStdin(true, false, range, stdin, fileOne, "-", fileTwo)
        );
        String expected = ONE_TO_FIVE + STRING_NEWLINE + STDIN_FIRST_FIVE + STRING_NEWLINE + ZERO_TO_SIX;
        assertEquals(expected, result);
    }
}
