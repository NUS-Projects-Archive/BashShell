package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.EMPTY_PATTERN;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_REGEX;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.FileUtils.createNewFileInDir;
import static sg.edu.nus.comp.cs4218.impl.util.FileUtils.deleteFileOrDirectory;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_OUTPUT;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.GrepException;

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
    private static final String INPUT_CONTENTS = String.join(STRING_NEWLINE, "aabb", "x", "ab");
    private static final String[] OUTPUT_CONTENTS = {"aabb", "ab"};

    @TempDir
    private Path tempDir;
    private Path fileOne;
    private Path fileTwo;
    private String fileOneAbsPath;
    private String fileTwoAbsPath;
    private String fileOneName;
    private String fileTwoName;

    private GrepApplication app;
    private InputStream stdin;
    private OutputStream stdout;

    private List<String> getValidOutputArrWithFileName(String... fileNames) {
        List<String> expectedOutputArr = new ArrayList<>();
        for (String name : fileNames) {
            for (String line : OUTPUT_CONTENTS) {
                expectedOutputArr.add(name + COLON_SPACE + line);
            }
        }
        return expectedOutputArr;
    }

    @BeforeEach
    void setUp() {
        app = new GrepApplication();
        stdin = new ByteArrayInputStream(INPUT_CONTENTS.getBytes(StandardCharsets.UTF_8));
        stdout = new ByteArrayOutputStream();

        Environment.currentDirectory = tempDir.toFile().getAbsolutePath();

        fileOne = createNewFileInDir(tempDir, "tempFile1", INPUT_CONTENTS);
        fileOneAbsPath = fileOne.toAbsolutePath().toString();
        fileOneName = fileOne.getFileName().toString();

        fileTwo = createNewFileInDir(tempDir, "tempFile2", INPUT_CONTENTS);
        fileTwoAbsPath = fileTwo.toAbsolutePath().toString();
        fileTwoName = fileTwo.getFileName().toString();
    }

    @AfterEach
    void tearDown() {
        deleteFileOrDirectory(fileOne);
        deleteFileOrDirectory(fileTwo);
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
            assertEquals(GREP_STRING + EMPTY_PATTERN, result.getMessage());
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
            String expected = String.join(STRING_NEWLINE, OUTPUT_CONTENTS) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromStdinWithIFlag_ReturnsCaseInsensitiveMatchingLines() {
            // Given
            String[] args = new String[]{FLAG_I, VALID_PAT_BIG};

            // When
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));

            // Then
            String expected = String.join(STRING_NEWLINE, OUTPUT_CONTENTS) + STRING_NEWLINE;
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
            String expected = String.join(STRING_NEWLINE, getValidOutputArrWithFileName(STRING_STDIN_OUTPUT)) +
                    STRING_NEWLINE;
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
            String expected = String.join(STRING_NEWLINE, getValidOutputArrWithFileName(STRING_STDIN_OUTPUT)) +
                    STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromStdinWithCFlagHFlag_ReturnsFileNameWithCountOfMatchingLines() {
            // Given
            String[] args = new String[]{FLAG_C, FLAG_H, VALID_PAT_BIG};

            // When
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));

            // Then
            String expected = STRING_STDIN_OUTPUT + ": 0" + STRING_NEWLINE;
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
            String expected = String.join(STRING_NEWLINE, OUTPUT_CONTENTS) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromValidAbsoluteFilePath_ReturnsMatchingLinesFromFile() {
            // Given
            String[] args = new String[]{VALID_PAT_SMALL, fileOneAbsPath};

            // When
            assertDoesNotThrow(() -> app.run(args, mock(InputStream.class), stdout));

            // Then
            String expected = String.join(STRING_NEWLINE, OUTPUT_CONTENTS) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromDash_ReturnsMatchingLinesFromStdin() {
            // Given
            String[] args = new String[]{VALID_PAT_SMALL, DASH};

            // When
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));

            // Then
            String expected = String.join(STRING_NEWLINE, OUTPUT_CONTENTS) + STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        // TODO: Test involving multiple dashes

        @Test
        void run_GetInputFromMultipleValidFilesAndDash_ReturnsMatchingLinesFromFilesAndStdin() {
            // Given
            String[] args = new String[]{VALID_PAT_SMALL, fileOneAbsPath, DASH, fileTwoAbsPath};

            // When
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));

            // Then
            String expected = String.join(STRING_NEWLINE,
                    getValidOutputArrWithFileName(fileOneAbsPath, STRING_STDIN_OUTPUT, fileTwoAbsPath)) +
                    STRING_NEWLINE;
            assertEquals(expected, stdout.toString());
        }

        @Test
        void run_GetInputFromValidAndInvalidFilesAndDash_ReturnsMatchingLinesFromValidFileAndStdin() {
            // Given
            String invalidFileName = "invalidFile";
            String[] args = new String[]{VALID_PAT_SMALL, fileOneName, DASH, invalidFileName};
            List<String> expectedOutputArr = getValidOutputArrWithFileName(fileOneName, STRING_STDIN_OUTPUT);
            expectedOutputArr.add(GREP_STRING + invalidFileName + COLON_SPACE + ERR_FILE_NOT_FOUND);
            String expected = String.join(STRING_NEWLINE, expectedOutputArr) + STRING_NEWLINE;

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
            Path file = createNewFileInDir(tempDir, "noReadPermTempFile", INPUT_CONTENTS);
            boolean isSetReadable = file.toFile().setReadable(false);

            if (!isSetReadable) {
                fail("Unable to set file to not readable");
                deleteFileOrDirectory(file);
                return;
            }

            String fileAbsPath = file.toString();
            String[] args = new String[]{VALID_PAT_SMALL, fileAbsPath};

            String expected = GREP_STRING + fileAbsPath + COLON_SPACE + ERR_NO_PERM + STRING_NEWLINE;

            // When
            assertDoesNotThrow(() -> app.run(args, mock(InputStream.class), stdout));

            // Then
            assertEquals(expected, stdout.toString());

            // Clean up
            deleteFileOrDirectory(file);
        }

        @Test
        void run_GetInputFromValidFileWithIFlag_ReturnsCaseInsensitiveMatchingLinesFromFile() {
            // Given
            String[] args = new String[]{FLAG_I, VALID_PAT_SMALL, fileOneName};
            String expected = String.join(STRING_NEWLINE, OUTPUT_CONTENTS) + STRING_NEWLINE;

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
            String expected = String.join(STRING_NEWLINE, getValidOutputArrWithFileName(fileOneName)) + STRING_NEWLINE;

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
            String expected = String.join(STRING_NEWLINE, getValidOutputArrWithFileName(fileOneName)) + STRING_NEWLINE;

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
            String expected = fileOneName + ": " + "2" + STRING_NEWLINE + STRING_STDIN_OUTPUT + ": " + "2" + STRING_NEWLINE;

            // When
            assertDoesNotThrow(() -> app.run(args, stdin, stdout));

            // Then
            assertEquals(expected, stdout.toString());
        }
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
}
