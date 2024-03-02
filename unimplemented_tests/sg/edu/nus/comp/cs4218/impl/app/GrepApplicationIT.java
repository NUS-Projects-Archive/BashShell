package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.EMPTY_PATTERN;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.FileUtils.createNewFile;
import static sg.edu.nus.comp.cs4218.impl.util.FileUtils.deleteFileOrDirectory;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.GrepException;

class GrepApplicationIT { //NOPMD - suppressed ClassNamingConventions - Following the class name convention for IT
    private static final String VALID_PATTERN_A_B = "ab";
    private static final String GREP_STRING = "grep: ";
    private static final String COLON_SPACE = ": ";
    private static final String INPUT_CONTENTS = String.join(STRING_NEWLINE, "aabb", "x", "ab");
    private static final String[] OUTPUT_CONTENTS = new String[]{"aabb", "ab"};
    private GrepApplication grepApplication;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        grepApplication = new GrepApplication();

        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    /**
     * Tests with no "file" specified.
     * "file" includes name of input file or stdin.
     */
    @Test
    void run_NullStdinAndNoInputFilesSpecified_ThrowsGrepException() {
        // Given
        String[] args = new String[]{""};

        // When
        GrepException result = assertThrows(GrepException.class,
                () -> grepApplication.run(args, null, mock(OutputStream.class)));

        // Then
        assertEquals(GREP_STRING + ERR_NO_INPUT, result.getMessage());
    }

    /**
     * Tests with pattern that cannot be extracted as it is null.
     */
    @Test
    void run_NullPattern_ThrowsGrepException() {
        // When
        GrepException result = assertThrows(GrepException.class,
                () -> grepApplication.run(new String[]{}, mock(InputStream.class), mock(OutputStream.class)));

        // Then
        assertEquals(GREP_STRING + ERR_SYNTAX, result.getMessage());
    }

    /**
     * Tests with empty pattern.
     * Pattern is a required argument, so an exception should be thrown.
     */
    @Test
    void run_PatternIsEmpty_ThrowsGrepException() {
        // When
        GrepException result = assertThrows(GrepException.class,
                () -> grepApplication.run(new String[]{""}, mock(InputStream.class), mock(OutputStream.class)));

        // Then
        assertEquals(GREP_STRING + EMPTY_PATTERN, result.getMessage());
    }

    /**
     * Tests with valid pattern and stdin is not null.
     */
    @Test
    void run_GetInputFromStdin_ReturnsResultBasedOnStdin() {
        // Given
        String[] args = new String[]{VALID_PATTERN_A_B};
        InputStream stdin = new ByteArrayInputStream(INPUT_CONTENTS.getBytes(StandardCharsets.UTF_8));

        String expected = String.join(STRING_NEWLINE, OUTPUT_CONTENTS) + STRING_NEWLINE;

        // When
        assertDoesNotThrow(() -> grepApplication.run(args, stdin, System.out));

        // Then
        assertEquals(expected, outContent.toString());
    }

    /**
     * Tests with valid pattern and file is not null.
     */
    @Test
    void run_GetInputFromValidFile_ReturnsResultBasedOnFileContents() {
        // Given
        Path file = createNewFile("tempFile", INPUT_CONTENTS);
        String fileAbsPath = file.toString();
        String[] args = new String[]{VALID_PATTERN_A_B, fileAbsPath};

        String expected = String.join(STRING_NEWLINE, OUTPUT_CONTENTS) + STRING_NEWLINE;

        // When
        assertDoesNotThrow(() -> grepApplication.run(args, mock(InputStream.class), System.out));

        // Then
        assertEquals(expected, outContent.toString());

        // Clean up
        deleteFileOrDirectory(file);
    }

    /**
     * Tests with valid pattern and multiple files are specified.
     */
    @Test
    void run_GetInputFromMultipleValidFiles_ReturnsResultBasedOnFileContents() {
        // Given
        // Create two files with same contents
        Path fileOne = createNewFile("tempFileOne", INPUT_CONTENTS);
        Path fileTwo = createNewFile("tempFileTwo", INPUT_CONTENTS);

        String fileOneAbsPath = fileOne.toString();
        String fileTwoAbsPath = fileTwo.toString();

        String[] args = new String[]{VALID_PATTERN_A_B, fileOneAbsPath, fileTwoAbsPath};

        String expected = String.join(STRING_NEWLINE,
                fileOneAbsPath + COLON_SPACE + OUTPUT_CONTENTS[0],
                fileOneAbsPath + COLON_SPACE + OUTPUT_CONTENTS[1],
                fileTwoAbsPath + COLON_SPACE + OUTPUT_CONTENTS[0],
                fileTwoAbsPath + COLON_SPACE + OUTPUT_CONTENTS[1]
        ) + STRING_NEWLINE;

        // When
        assertDoesNotThrow(() -> grepApplication.run(args, mock(InputStream.class), System.out));

        // Then
        assertEquals(expected, outContent.toString());

        // Clean up
        deleteFileOrDirectory(fileOne);
        deleteFileOrDirectory(fileTwo);
    }

    /**
     * Tests in an environment where file does not exist. Should print on stdout file not found error.
     */
    @Test
    void run_GetInputFromNonExistentFile_ReturnsFileNotFoundError() {
        // Given
        String fileName = "Nonexistent";
        String[] args = new String[]{VALID_PATTERN_A_B, fileName};

        String expected = fileName + COLON_SPACE + ERR_FILE_NOT_FOUND + STRING_NEWLINE;

        // When
        assertDoesNotThrow(() -> grepApplication.run(args, mock(InputStream.class), System.out));

        // Then
        assertEquals(expected, outContent.toString());
    }

    /**
     * Tests with unreadable existing file.
     */
    @Test
    void run_GetInputFromFileWithNoReadPermission_ReturnsPermDeniedErr() {
        // Given
        Path file = createNewFile("tempFile", INPUT_CONTENTS);
        if (!file.toFile().setReadable(false)) {
            fail("Unable to set file to not readable");
            deleteFileOrDirectory(file);
            return;
        }

        String fileAbsPath = file.toString();
        String[] args = new String[]{VALID_PATTERN_A_B, fileAbsPath};

        String expected = fileAbsPath + COLON_SPACE + ERR_NO_PERM + STRING_NEWLINE;

        // When
        assertDoesNotThrow(() -> grepApplication.run(args, mock(InputStream.class), System.out));

        // Then
        assertEquals(expected, outContent.toString());

        // Clean up
        deleteFileOrDirectory(file);
    }
}
