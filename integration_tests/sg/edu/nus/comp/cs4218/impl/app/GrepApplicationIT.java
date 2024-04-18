package sg.edu.nus.comp.cs4218.impl.app;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_EMPTY_PATTERN;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_REGEX;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_OUT;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFileInDir;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.deleteFileOrDirectory;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.joinStringsByNewline;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.removeTrailing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.impl.util.CollectionsUtils;

@SuppressWarnings("PMD.ClassNamingConventions")
class GrepApplicationIT {

    private static final String FLAG_I = "-i";
    private static final String FLAG_C = "-c";
    private static final String FLAG_H = "-H";
    private static final String VALID_PAT_BIG = "AB";
    private static final String VALID_PAT_SMALL = "ab";
    private static final String GREP_STRING = "grep: ";
    private static final String COLON_SPACE = ": ";
    private static final String DASH = "-";
    private static final String INPUT_CONTENTS = joinStringsByNewline("aabb", "x", "ab");
    private static final String TEST_INPUT_FILE = "c.md";
    private static final String TEST_OUTPUT_FILE = "expected_c.md";
    private static final String TEST_RESOURCES = "resources/grep";
    private static final String[] OUTPUT_CONTENTS = {"aabb", "ab"};

    @TempDir
    private Path tempDir;
    private Path fileOne;
    private Path fileTwo;
    private String fileOneAbsPath;
    private String fileTwoAbsPath;
    private String fileOneName;

    private GrepApplication app;
    private InputStream stdin;
    private OutputStream stdout;

    private String[] getValidOutputArrWithFileName(String... fileNames) {
        List<String> expectedOutputArr = new ArrayList<>();
        for (String name : fileNames) {
            for (String line : OUTPUT_CONTENTS) {
                expectedOutputArr.add(name + COLON_SPACE + line);
            }
        }
        return CollectionsUtils.listToArray(expectedOutputArr);
    }

    @BeforeEach
    void setUp(@TempDir(cleanup = CleanupMode.ALWAYS) Path setUpTempDir) throws IOException {
        tempDir = setUpTempDir;
        final String resourceDirectory = removeTrailing(TEST_RESOURCES, "/");
        try (Stream<Path> stream = Files.walk(Paths.get(resourceDirectory))) {
            stream.forEach(source -> {
                Path destination = Paths.get(tempDir.toString(),
                        source.toString().substring(resourceDirectory.length()));

                try {
                    Files.copy(source, destination, REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException("Unable to copy test resources to temp directory.", e);
                }
            });
        }

        // Set CWD to be the test directory
        Environment.currentDirectory = tempDir.toString();

        app = new GrepApplication();
        stdin = new ByteArrayInputStream(INPUT_CONTENTS.getBytes(StandardCharsets.UTF_8));
        stdout = new ByteArrayOutputStream();

        fileOne = createNewFileInDir(tempDir, "tempFile1", INPUT_CONTENTS);
        fileOneAbsPath = fileOne.toAbsolutePath().toString();
        fileOneName = fileOne.getFileName().toString();

        fileTwo = createNewFileInDir(tempDir, "tempFile2", INPUT_CONTENTS);
        fileTwoAbsPath = fileTwo.toAbsolutePath().toString();
    }

    @AfterEach
    void tearDown() {
        deleteFileOrDirectory(fileOne);
        deleteFileOrDirectory(fileTwo);
    }

    /**
     * Tests with valid regex but no input file and stdin specified.
     */
    @Test
    void run_NullStdinAndNoInputFilesSpecified_ThrowsGrepException() {
        // When
        GrepException result = assertThrowsExactly(GrepException.class, () ->
                app.run(new String[]{"abc"}, null, mock(OutputStream.class))
        );

        // Then
        assertEquals(GREP_STRING + ERR_NO_INPUT, result.getMessage());
    }

    @Test
    void run_FailsToWriteToOutputStream_ThrowsGrepException() {
        String[] args = new String[]{VALID_PAT_SMALL};
        GrepException result = assertThrowsExactly(GrepException.class, () -> {
            InputStream mockStdin = new ByteArrayInputStream("".getBytes());
            OutputStream mockStdout = mock(OutputStream.class);
            doThrow(new IOException()).when(mockStdout).write(any(byte[].class));
            app.run(args, mockStdin, mockStdout);
        });
        assertEquals(GREP_STRING + ERR_WRITE_STREAM, result.getMessage());
    }

    /**
     * Test case where no flags are specified.
     */
    @Test
    void grepFromFileAndStdin_NoFlagsSpecified_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() ->
                app.grepFromFileAndStdin(VALID_PAT_SMALL, false, false, false, stdin, fileOneName, "-")
        );
        String expected = joinStringsByNewline(getValidOutputArrWithFileName(fileOneName, STRING_STDIN_OUT)) +
                STRING_NEWLINE;
        assertEquals(expected, result);
    }

    /**
     * Test case where -i is specified only.
     */
    @Test
    void grepFromFileAndStdin_isCaseSensitiveIsFalse_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() ->
                app.grepFromFileAndStdin(VALID_PAT_BIG, true, false, false, stdin, fileOneName, "-")
        );
        String expected = joinStringsByNewline(getValidOutputArrWithFileName(fileOneName, STRING_STDIN_OUT)) +
                STRING_NEWLINE;
        assertEquals(expected, result);
    }

    /**
     * Test case where -c is specified only.
     */
    @Test
    void grepFromFileAndStdin_isCountLinesIsTrue_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() ->
                app.grepFromFileAndStdin(VALID_PAT_BIG, false, true, false, stdin, fileOneName, "-")
        );
        String expected = joinStringsByNewline(fileOneName + ": 0", STRING_STDIN_OUT + ": 0") + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    /**
     * Test case where -H is specified and filename is to be specified in output.
     */
    @Test
    void grepFromFileStdin_isPrefixFileNameIsTrue_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() ->
                app.grepFromFileAndStdin(VALID_PAT_SMALL, false, false, true, stdin, fileOneName, "-")
        );
        String expected = joinStringsByNewline(getValidOutputArrWithFileName(fileOneName, STRING_STDIN_OUT))
                + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    /**
     * Test case where -i -c -H are specified.
     */
    @Test
    void grepFromFileAndStdin_AllFlagsSpecified_ReturnsCorrectOutput() {
        String result = assertDoesNotThrow(() ->
                app.grepFromFileAndStdin(VALID_PAT_SMALL, true, true, true, stdin, fileOneName)
        );
        String expected = joinStringsByNewline(fileOneName + ": 2") + STRING_NEWLINE;
        assertEquals(expected, result);
    }

    @Nested
    class UnacceptedPattern {
        /**
         * Tests with pattern that cannot be extracted as it is null.
         */
        @Test
        void run_NullPattern_ThrowsGrepException() {
            // When
            GrepException result = assertThrowsExactly(GrepException.class, () ->
                    app.run(new String[]{}, mock(InputStream.class), mock(OutputStream.class))
            );

            // Then
            assertEquals(GREP_STRING + ERR_SYNTAX, result.getMessage());
        }

        /**
         * Tests with empty pattern. Pattern is a required argument, so an exception should be thrown.
         */
        @Test
        void run_EmptyPattern_ThrowsGrepException() {
            // When
            GrepException result = assertThrowsExactly(GrepException.class, () ->
                    app.run(new String[]{""}, mock(InputStream.class), mock(OutputStream.class))
            );

            // Then
            assertEquals(GREP_STRING + ERR_EMPTY_PATTERN, result.getMessage());
        }

        /**
         * Tests with invalid pattern.
         *
         * @param regex Invalid pattern
         */
        @ParameterizedTest
        @ValueSource(strings = {"[", "+", "(", "{"})
        void run_InvalidPattern_ThrowsGrepException(String regex) {
            // When
            GrepException result = assertThrowsExactly(GrepException.class, () ->
                    app.run(new String[]{regex}, mock(InputStream.class), mock(OutputStream.class))
            );

            // Then
            assertEquals(GREP_STRING + ERR_INVALID_REGEX, result.getMessage());
        }
    }

    /**
     * Tests involving purely stdin, no input file is specified.
     */
    @Nested
    class Stdin {
        @Test
        void run_GetInputFromStdinWithNoFlag_ReturnsMatchingLines() {
            // Given
            String[] args = new String[]{VALID_PAT_SMALL};

            // When
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));

            // Then
            String expected = joinStringsByNewline(OUTPUT_CONTENTS) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromStdinWithIFlag_ReturnsCaseInsensitiveMatchingLines() {
            // Given
            String[] args = new String[]{FLAG_I, VALID_PAT_BIG};

            // When
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));

            // Then
            String expected = joinStringsByNewline(OUTPUT_CONTENTS) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromStdinWithCFlag_ReturnsCountOfMatchingLines() {
            // Given
            String[] args = new String[]{FLAG_C, VALID_PAT_SMALL};

            // When
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));

            // Then
            String expected = "2" + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromStdinWithHFlag_ReturnsFileNameWithMatchingLines() {
            // Given
            String[] args = new String[]{FLAG_H, VALID_PAT_SMALL};

            // When
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));

            // Then
            String expected = joinStringsByNewline(getValidOutputArrWithFileName(STRING_STDIN_OUT)) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromStdinWithIFlagCFlag_ReturnsCountOfCaseInsensitiveMatchingLines() {
            // Given
            String[] args = new String[]{FLAG_I, FLAG_C, VALID_PAT_BIG};

            // When
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));

            // Then
            String expected = "2" + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromStdinWithIFlagHFlag_ReturnsFileNameWithCaseInsensitiveMatchingLines() {
            // Given
            String[] args = new String[]{FLAG_I, FLAG_H, VALID_PAT_BIG};

            // When
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));

            // Then
            String expected = joinStringsByNewline(getValidOutputArrWithFileName(STRING_STDIN_OUT)) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromStdinWithCFlagHFlag_ReturnsFileNameWithCountOfMatchingLines() {
            // Given
            String[] args = new String[]{FLAG_C, FLAG_H, VALID_PAT_BIG};

            // When
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));

            // Then
            String expected = STRING_STDIN_OUT + COLON_SPACE + "0" + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }
    }

    /**
     * Tests involving input files including "-", which is interpreted as stdin.
     */
    @Nested
    class InputFiles {
        @Test
        void run_GetInputFromValidFileName_ReturnsMatchingLinesFromFile() {
            // Given
            String[] args = new String[]{VALID_PAT_SMALL, fileOneName};

            // When
            assertDoesNotThrow(() -> app.run(args, mock(InputStream.class), stdout));

            // Then
            String expected = joinStringsByNewline(OUTPUT_CONTENTS) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromValidAbsoluteFilePath_ReturnsMatchingLinesFromFile() {
            // Given
            String[] args = new String[]{VALID_PAT_SMALL, fileOneAbsPath};

            // When
            assertDoesNotThrow(() -> app.run(args, mock(InputStream.class), stdout));

            // Then
            String expected = joinStringsByNewline(OUTPUT_CONTENTS) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromDash_ReturnsMatchingLinesFromStdin() {
            // Given
            String[] args = new String[]{VALID_PAT_SMALL, DASH};

            // When
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));

            // Then
            String expected = joinStringsByNewline(OUTPUT_CONTENTS) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromMultipleValidFilesAndDash_ReturnsMatchingLinesFromFilesAndStdin() {
            // Given
            String[] args = new String[]{VALID_PAT_SMALL, fileOneAbsPath, DASH, fileTwoAbsPath};

            // When
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));

            // Then
            String expected = joinStringsByNewline(
                    getValidOutputArrWithFileName(fileOneAbsPath, STRING_STDIN_OUT, fileTwoAbsPath)
            ) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromValidAndInvalidFilesAndDash_ReturnsMatchingLinesFromValidFileAndStdin() {
            // Given
            String invalidFileName = "invalidFile";
            String[] args = new String[]{VALID_PAT_SMALL, fileOneName, DASH, invalidFileName};
            String[] validExpected = getValidOutputArrWithFileName(fileOneName, STRING_STDIN_OUT);
            String[] expectedArray = Arrays.copyOf(validExpected, validExpected.length + 1);
            expectedArray[validExpected.length] = GREP_STRING + invalidFileName + COLON_SPACE + ERR_FILE_NOT_FOUND;
            String expected = joinStringsByNewline(expectedArray) + STRING_NEWLINE;

            // When
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));

            // Then
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromNonExistentFile_ReturnsFileNotFoundError() {
            // Given
            String invalidFileName = "Nonexistent";
            String[] args = new String[]{VALID_PAT_SMALL, invalidFileName};
            String expected = GREP_STRING + invalidFileName + COLON_SPACE + ERR_FILE_NOT_FOUND + STRING_NEWLINE;

            // When
            assertDoesNotThrow(() -> app.run(args, mock(InputStream.class), stdout));

            // Then
            assertEquals(expected, stdout.toString());
        }

        /**
         * Tests for input files with no read permission.
         * Disabled on Windows because setting file permissions this manner is not supported.
         */
        @Test
        @DisabledOnOs(OS.WINDOWS)
        void run_GetInputFromFileWithNoReadPermission_ReturnsPermDeniedErr() {
            // Given
            String[] args = new String[]{VALID_PAT_SMALL, fileOneAbsPath};
            String expected = GREP_STRING + fileOneAbsPath + COLON_SPACE + ERR_NO_PERM + STRING_NEWLINE;
            boolean isSetReadable = fileOne.toFile().setReadable(false);
            assertTrue(isSetReadable, "Unable to set file to not readable");

            // When
            assertDoesNotThrow(() -> app.run(args, mock(InputStream.class), stdout));

            // Then
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromValidFileWithIFlag_ReturnsCaseInsensitiveMatchingLinesFromFile() {
            // Given
            String[] args = new String[]{FLAG_I, VALID_PAT_SMALL, fileOneName};
            String expected = joinStringsByNewline(OUTPUT_CONTENTS) + STRING_NEWLINE;

            // When
            assertDoesNotThrow(() -> app.run(args, mock(InputStream.class), stdout));

            // Then
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromValidFileWithCFlag_ReturnsCountOfMatchingLinesFromFile() {
            // Given
            String[] args = new String[]{FLAG_C, VALID_PAT_BIG, fileOneName};
            String expected = "0" + STRING_NEWLINE;

            // When
            assertDoesNotThrow(() -> app.run(args, mock(InputStream.class), stdout));

            // Then
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromValidFileWithHFlag_ReturnsFileNameWithMatchingLinesFromFile() {
            // Given
            String[] args = new String[]{FLAG_H, VALID_PAT_SMALL, fileOneName};
            String expected = joinStringsByNewline(getValidOutputArrWithFileName(fileOneName)) + STRING_NEWLINE;

            // When
            assertDoesNotThrow(() -> app.run(args, mock(InputStream.class), stdout));

            // Then
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromValidFileWithIFlagCFlag_ReturnsCaseInsensitiveCountOfMatchingLinesFromFile() {
            // Given
            String[] args = new String[]{FLAG_I, FLAG_C, VALID_PAT_BIG, fileOneName};
            String expected = "2" + STRING_NEWLINE;

            // When
            assertDoesNotThrow(() -> app.run(args, mock(InputStream.class), stdout));

            // Then
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromValidFileWithIFlagHFlag_ReturnsCaseInsensitiveFileNameWithMatchingLinesFromFile() {
            // Given
            String[] args = new String[]{FLAG_I, FLAG_H, VALID_PAT_BIG, fileOneName};
            String expected = joinStringsByNewline(getValidOutputArrWithFileName(fileOneName)) + STRING_NEWLINE;

            // When
            assertDoesNotThrow(() -> app.run(args, mock(InputStream.class), stdout));

            // Then
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromValidFileWithCFlagHFlag_ReturnsCountOfMatchingLinesAndFileNameFromFile() {
            // Given
            String[] args = new String[]{FLAG_C, FLAG_H, VALID_PAT_BIG, fileOneName};
            String expected = fileOneName + ": 0" + STRING_NEWLINE;

            // When
            assertDoesNotThrow(() -> app.run(args, mock(InputStream.class), stdout));

            // Then
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromValidFileAndDashWithIFlagCFlagHFlag_ReturnsCaseInsensitiveCountOfMatchingLinesAndFileNameFromFileAndStdin() {
            // Given
            String[] args = new String[]{FLAG_I, FLAG_C, FLAG_H, VALID_PAT_BIG, fileOneName, DASH};
            String expected =
                    fileOneName + ": " + "2" + STRING_NEWLINE + STRING_STDIN_OUT + ": " + "2" + STRING_NEWLINE;

            // When
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));

            // Then
            assertEquals(expected, stdout.toString());
        }

        // Disabled as this test case is written for a Hackathon valid filed bug (Issue #220) that is not fixed.
        @Test
        @Disabled
        void run_GetInputFromValidFileNameWithValidDashPattern_ReturnsMatchingLinesFromFile() {
            // Given
            String[] args = new String[]{DASH, TEST_INPUT_FILE};

            // When
            assertDoesNotThrow(() -> app.run(args, mock(InputStream.class), stdout));

            // Then
            Path expectedFilePath = Paths.get(tempDir.toString(), TEST_OUTPUT_FILE);
            String expectedContent = assertDoesNotThrow(() -> Files.readString(expectedFilePath));
            assertEquals(expectedContent, stdout.toString());
        }
    }
}
