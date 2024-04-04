package sg.edu.nus.comp.cs4218.impl.app;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFile;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.joinStringsByNewline;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.removeTrailing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.SortException;

@SuppressWarnings("PMD.ClassNamingConventions")
public class SortApplicationIT {

    private static final String TEST_RESOURCES = "resources/sort/";
    private static final String TEST_INPUT_FILE = "large-text.txt";
    private static final String TEST_OUT_NUMERIC = "out-numeric.txt";
    private static final String TEST_OUT_NUM_REV = "out-numeric-reverse.txt";
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

        @TempDir
        private Path testingDirectory;
        private InputStream mockStdin;
        private String file;

        @BeforeEach
        void setUp(@TempDir(cleanup = CleanupMode.ALWAYS) Path tempDir) throws IOException {
            testingDirectory = tempDir;
            final String resourceDirectory = removeTrailing(TEST_RESOURCES, "/");
            try (Stream<Path> stream = Files.walk(Paths.get(resourceDirectory))) {
                stream.forEach(source -> {
                    Path destination = Paths.get(testingDirectory.toString(),
                            source.toString().substring(resourceDirectory.length()));

                    try {
                        Files.copy(source, destination, REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException("Unable to copy test resources to temp directory.", e);
                    }
                });
            }

            // Set CWD to be the test directory
            Environment.currentDirectory = testingDirectory.toString();

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
            Path expectedFilePath = Paths.get(testingDirectory.toString(), TEST_OUT_NUMERIC);
            String expectedContent = assertDoesNotThrow(() -> Files.readString(expectedFilePath));
            assertEquals(expectedContent, stdout.toString());
        }

        @Test
        void run_IsFirstWordNumberAndIsReverseAndIsCaseIndependentFlagForLargeTextFile_IsFirstWordNumberTakesPrecedence() {
            String[] args = {"-fr", "-n", TEST_INPUT_FILE};
            assertDoesNotThrow(() -> app.run(args, mockStdin, stdout));
            Path expectedFilePath = Paths.get(testingDirectory.toString(), TEST_OUT_NUM_REV);
            String expectedContent = assertDoesNotThrow(() -> Files.readString(expectedFilePath));
            assertEquals(expectedContent, stdout.toString());
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
