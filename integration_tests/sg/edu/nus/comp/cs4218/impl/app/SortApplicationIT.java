package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFile;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.joinStringsByNewline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.SortException;

@SuppressWarnings("PMD.ClassNamingConventions")
public class SortApplicationIT {

    private static final String TEST_RESOURCES = "resources/sort/";
    private static final String TEST_INPUT_FILE = TEST_RESOURCES + "large-text.txt";
    private static final String BIG_A = "A";
    private static final String BIG_B = "B";
    private static final String SMALL_A = "a";
    private static final String SMALL_B = "b";
    private static final String SMALL_O = "o";
    private static final String PLUS = "+";
    private static final String MINUS = "-";

    private static final String[] CONTENT = {"5", BIG_A, "2", BIG_B, "10", SMALL_O, "1", SMALL_A, "3", PLUS, SMALL_B, MINUS};
    private static final String[] OUT_NO_FLAGS = {PLUS, MINUS, "1", "10", "2", "3", "5", BIG_A, BIG_B, SMALL_A, SMALL_B, SMALL_O};
    private static final String[] OUT_FIRST_NUM = {PLUS, MINUS, BIG_A, BIG_B, SMALL_A, SMALL_B, SMALL_O, "1", "2", "3", "5", "10"};
    private static final String[] OUT_REV_ORDER = {SMALL_O, SMALL_B, SMALL_A, BIG_B, BIG_A, "5", "3", "2", "10", "1", MINUS, PLUS};
    private static final String[] OUT_CASE_IGNORE = {PLUS, MINUS, "1", "10", "2", "3", "5", BIG_A, SMALL_A, BIG_B, SMALL_B, SMALL_O};


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
            InputStream mockStdin = System.in;
            app.run(null, mockStdin, null);
        });
        String expected = "sort: OutputStream not provided";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_FailsToWriteToOutputStream_ThrowsSortException() {
        String[] args = {};
        String content = joinStringsByNewline(CONTENT);
        SortException result = assertThrowsExactly(SortException.class, () -> {
            InputStream mockStdin = new ByteArrayInputStream(content.getBytes());
            OutputStream mockStdout = mock(OutputStream.class);
            doThrow(new IOException()).when(mockStdout).write(any(byte[].class));
            app.run(args, mockStdin, mockStdout);
        });
        String expected = "sort: Could not write to output stream";
        assertEquals(expected, result.getMessage());
    }

    @Nested
    class FileInputTests {

        private InputStream mockStdin;
        private String file;

        @BeforeEach
        void setUp() throws IOException {
            mockStdin = mock(InputStream.class);

            // Create temporary file, automatically deletes after test execution
            String content = joinStringsByNewline(CONTENT);
            file = createNewFile("file.txt", content).toString();
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

        @Test
        void run_IsFirstWordNumberAndIsCaseIndependentFlagForLargeTextFile_IsFirstWordNumberTakesPrecedence() {
            String[] args = {"-f", "-n", TEST_INPUT_FILE};
            assertDoesNotThrow(() -> app.run(args, mockStdin, stdout));
            Path expectedFilePath = Paths.get(TEST_RESOURCES + "out-numeric.txt");
            String expectedFileContent = assertDoesNotThrow(() -> Files.readString(expectedFilePath));
            assertEquals(expectedFileContent, stdout.toString());
        }

        @Test
        void run_IsFirstWordNumberAndIsReverseAndIsCaseIndependentFlagForLargeTextFile_IsFirstWordNumberTakesPrecedence() {
            String[] args = {"-fr", "-n", TEST_INPUT_FILE};
            assertDoesNotThrow(() -> app.run(args, mockStdin, stdout));
            Path expectedFilePath = Paths.get(TEST_RESOURCES + "out-numeric-reverse.txt");
            String expectedFileContent = assertDoesNotThrow(() -> Files.readString(expectedFilePath));
            assertEquals(expectedFileContent, stdout.toString());
        }
    }

    @Nested
    class StdinInputTests {

        private InputStream stdin;

        @BeforeEach
        void setUp() {
            String content = joinStringsByNewline(CONTENT);
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
