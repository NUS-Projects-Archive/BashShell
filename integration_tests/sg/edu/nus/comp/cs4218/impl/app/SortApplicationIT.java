package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.joinStringsByNewline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.SortException;

@SuppressWarnings("PMD.ClassNamingConventions")
public class SortApplicationIT {

    private static final String BIG_A = "A";
    private static final String BIG_B = "B";
    private static final String SMALL_A = "a";
    private static final String SMALL_B = "b";
    private static final String SMALL_O = "o";

    private static final String[] FILE_CONTENT = {"5", BIG_A, "2", BIG_B, "10", SMALL_O, "1", SMALL_A, "3", SMALL_B};
    private static final String[] OUT_NO_FLAGS = {"1", "10", "2", "3", "5", BIG_A, BIG_B, SMALL_A, SMALL_B, SMALL_O};
    private static final String[] OUT_FIRST_NUM = {"1", "2", "3", "5", "10", BIG_A, BIG_B, SMALL_A, SMALL_B, SMALL_O};
    private static final String[] OUT_REV_ORDER = {SMALL_O, SMALL_B, SMALL_A, BIG_B, BIG_A, "5", "3", "2", "10", "1"};
    private static final String[] OUT_CASE_IGNORE = {"1", "10", "2", "3", "5", BIG_A, SMALL_A, BIG_B, SMALL_B, SMALL_O};

    private SortApplication app;
    private OutputStream stdout;

    @BeforeEach
    void setUp() throws IOException {
        app = new SortApplication();
        stdout = new ByteArrayOutputStream();
    }

    @Test
    void run_NullStdin_ThrowsSortException() {
        SortException result = assertThrowsExactly(SortException.class, () -> app.run(null, null, null));
        String expected = "sort: InputStream not provided";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_NullStdout_ThrowsSortException() {
        SortException result = assertThrowsExactly(SortException.class, () -> {
            InputStream mockStdin = mock(InputStream.class);
            app.run(null, mockStdin, null);
        });
        String expected = "sort: OutputStream not provided";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_FailsToWriteToOutputStream_ThrowsSortException() {
        String[] args = {};
        SortException result = assertThrowsExactly(SortException.class, () -> {
            InputStream mockStdin = new ByteArrayInputStream("mock data".getBytes());
            OutputStream mockedStdout = mock(OutputStream.class);
            doThrow(new IOException()).when(mockedStdout).write(any(byte[].class));
            app.run(args, mockStdin, mockedStdout);
        });
        String expected = "sort: Could not write to output stream";
        assertEquals(expected, result.getMessage());
    }

    @Nested
    class FileInputTests {

        private InputStream mockStdin;
        private String file;

        @BeforeEach
        void setUp(@TempDir Path tempDir) throws IOException {
            mockStdin = mock(InputStream.class);

            // Create temporary file, automatically deletes after test execution
            String content = joinStringsByNewline(FILE_CONTENT);
            Path filePath = tempDir.resolve("file.txt");
            Files.createFile(filePath);
            Files.write(filePath, content.getBytes());
            file = filePath.toString();
        }

        @Test
        void run_NoFlags_WritesSortedListToStdout() {
            String[] args = {file};
            assertDoesNotThrow(() -> app.run(args, mockStdin, stdout));
            String expected = joinStringsByNewline(OUT_NO_FLAGS) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_IsFirstWordNumberFlag_WritesSortedListToStdout() {
            String[] args = {"-n", file};
            assertDoesNotThrow(() -> app.run(args, mockStdin, stdout));
            String expected = joinStringsByNewline(OUT_FIRST_NUM) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_IsReverseOrderFlag_WritesReverseSortedListToStdout() {
            String[] args = {"-r", file};
            assertDoesNotThrow(() -> app.run(args, mockStdin, stdout));
            String expected = joinStringsByNewline(OUT_REV_ORDER) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_IsCaseIndependentFlag_WritesCaseIndependentSortedListToStdout() {
            String[] args = {"-f", file};
            assertDoesNotThrow(() -> app.run(args, mockStdin, stdout));
            String expected = joinStringsByNewline(OUT_CASE_IGNORE) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_IsFirstWordNumberAndIsCaseIndependentFlag_IsFirstWordNumberTakesPrecedence() {
            String[] args = {"-n", "-f", file};
            assertDoesNotThrow(() -> app.run(args, mockStdin, stdout));
            String expected = joinStringsByNewline(OUT_FIRST_NUM) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }
    }

    @Nested
    class StdinInputTests {

        private InputStream stdin;

        @BeforeEach
        void setUp() {
            String content = joinStringsByNewline(FILE_CONTENT);
            stdin = new ByteArrayInputStream(content.getBytes());
        }

        @Test
        void run_NoFlags_WritesSortedListToStdout() {
            String[] args = {};
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));
            String expected = joinStringsByNewline(OUT_NO_FLAGS) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_IsFirstWordNumberFlag_WritesSortedListToStdout() {
            String[] args = {"-n"};
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));
            String expected = joinStringsByNewline(OUT_FIRST_NUM) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_IsReverseOrderFlag_WritesReverseSortedListToStdout() {
            String[] args = {"-r"};
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));
            String expected = joinStringsByNewline(OUT_REV_ORDER) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_IsCaseIndependentFlag_WritesCaseIndependentSortedListToStdout() {
            String[] args = {"-f"};
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));
            String expected = joinStringsByNewline(OUT_CASE_IGNORE) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_IsFirstWordNumberAndIsCaseIndependentFlag_IsFirstWordNumberTakesPrecedence() {
            String[] args = {"-n", "-f"};
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));
            String expected = joinStringsByNewline(OUT_FIRST_NUM) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }
    }
}
