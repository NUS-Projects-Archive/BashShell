package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.exception.GrepException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

class GrepApplicationIntegrationTest {
    private GrepApplication grepApplication;
    private ByteArrayOutputStream outContent;

    private final String INPUT_CONTENTS = joinStringsByLineSeparator("aabb", "x", "ab");
    private final String[] OUTPUT_CONTENTS = new String[] {"aabb", "ab"};

    private static final String VALID_PATTERN_A_B = "ab";

    Path createNewFile(String fileName, String contents) {
        Path file = null;
        try {
            file = Files.createTempFile(fileName, "");
        } catch (IOException e) {
            fail("Unable to create temporary file");
        }
        try {
            Files.write(file, contents.getBytes());
        } catch (IOException e) {
            fail("Unable to write to temporary file");
        }
        return file;
    }

    void deleteFile(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            fail("Unable to delete temporary file");
        }
    }


    @BeforeEach
    void setUp() {
        grepApplication = new GrepApplication();

        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    String joinStringsByLineSeparator(String... strings) {
        StringBuilder joinedStr = new StringBuilder();
        for (String str: strings) {
            joinedStr.append(str).append(System.lineSeparator());
        }
        return joinedStr.toString();
    }

    /**
     * Test case where there are no "file" specified.
     * "file" includes name of input file or stdin.
     */
    @Test
    void run_NullStdinAndNoInputFilesSpecified_ThrowsGrepException() {
        // Given
        String[] args = new String[] {""};

        // When
        GrepException result = assertThrows(GrepException.class,
                () -> grepApplication.run(args, null, mock(OutputStream.class)));

        // Then
        assertEquals("grep: " + ERR_NO_INPUT, result.getMessage());
    }

    /**
     * Test case where pattern cannot be extracted and is null.
     */
    @Test
    void run_NullPattern_ThrowsGrepException() {
        // When
        GrepException result = assertThrows(GrepException.class,
                () -> grepApplication.run(new String[]{}, mock(InputStream.class), mock(OutputStream.class)));

        // Then
        assertEquals("grep: " + ERR_SYNTAX, result.getMessage());
    }

    /**
     * Test case where the pattern is empty.
     * Pattern is a required argument, so an exception should be thrown.
     */
    @Test
    void run_PatternIsEmpty_ThrowsGrepException() {
        // When
        GrepException result = assertThrows(GrepException.class,
                () -> grepApplication.run(new String[] {""}, mock(InputStream.class), mock(OutputStream.class)));

        // Then
        assertEquals("grep: " + EMPTY_PATTERN, result.getMessage());
    }

    /**
     * Test case where the pattern is valid and stdin is not null.
     */
    @Test
    void run_GetInputFromStdin_ReturnsResultBasedOnStdin() {
        // Given
        String[] args = new String[] {VALID_PATTERN_A_B};
        InputStream stdin = new ByteArrayInputStream(INPUT_CONTENTS.getBytes(StandardCharsets.UTF_8));

        String expected = joinStringsByLineSeparator(OUTPUT_CONTENTS);

        // When
        assertDoesNotThrow(() -> grepApplication.run(args, stdin, System.out));

        // Then
        assertEquals(expected, outContent.toString());
    }

    /**
     * Test case where the pattern is valid and file is not null.
     */
    @Test
    void run_GetInputFromValidFile_ReturnsResultBasedOnFileContents() {
        // Given
        Path file = createNewFile("tempFile", INPUT_CONTENTS);
        String fileAbsPath = file.toString();
        String[] args = new String[] {VALID_PATTERN_A_B, fileAbsPath};

        String expected = joinStringsByLineSeparator(OUTPUT_CONTENTS);

        // When
        assertDoesNotThrow(() -> grepApplication.run(args, mock(InputStream.class), System.out));

        // Then
        assertEquals(expected, outContent.toString());

        // Clean up
        deleteFile(file);
    }

    /**
     * Test case where the pattern is valid and multiple files are specified.
     */
    @Test
    void run_GetInputFromMultipleValidFiles_ReturnsResultBasedOnFileContents() {
        // Given
        // Create two files with same contents
        Path fileOne = createNewFile("tempFileOne", INPUT_CONTENTS);
        Path fileTwo = createNewFile("tempFileTwo", INPUT_CONTENTS);

        String fileOneAbsPath = fileOne.toString();
        String fileTwoAbsPath = fileTwo.toString();

        String[] args = new String[] {VALID_PATTERN_A_B, fileOneAbsPath, fileTwoAbsPath};

        String expected = joinStringsByLineSeparator(
            fileOneAbsPath + ": " + OUTPUT_CONTENTS[0],
            fileOneAbsPath + ": " + OUTPUT_CONTENTS[1],
            fileTwoAbsPath + ": " + OUTPUT_CONTENTS[0],
            fileTwoAbsPath + ": " + OUTPUT_CONTENTS[1]
        );

        // When
        assertDoesNotThrow(() -> grepApplication.run(args, mock(InputStream.class), System.out));

        // Then
        assertEquals(expected, outContent.toString());

        // Clean up
        deleteFile(fileOne);
        deleteFile(fileTwo);
    }

    /**
     * Test case where existing file is not readable.
     * Disabled as this feature is not implemented yet.
     */
    @Test
    @Disabled
    void run_GetInputFromFileWithNoReadPermission_ReturnsPermDeniedErr() {
        // Given
        Path file = createNewFile("tempFile", INPUT_CONTENTS);
        boolean isSetReadableSuccess = file.toFile().setReadable(false); //NOPMD - suppressed LongVariable - To have meaningful variable name
        if (!isSetReadableSuccess) {
            fail("Unable to set file to not readable");
            deleteFile(file);
            return;
        }

        String fileAbsPath = file.toString();
        String[] args = new String[] {VALID_PATTERN_A_B, fileAbsPath};

        String expected = fileAbsPath + ": " + ERR_NO_PERM + System.lineSeparator();

        // When
        assertDoesNotThrow(() -> grepApplication.run(args, mock(InputStream.class), System.out));

        // Then
        assertEquals(expected, outContent.toString());

        // Clean up
        deleteFile(file);
    }

    /**
     * Test case where file does not exist. Should print on stdout file not found error.
     */
    @Test
    void run_GetInputFromNonExistentFile_ReturnsFileNotFoundError() {
        // Given
        String fileName = "Nonexistent";
        String[] args = new String[] {VALID_PATTERN_A_B, fileName};

        String expected = fileName + ": " + ERR_FILE_NOT_FOUND + System.lineSeparator();

        // When
        assertDoesNotThrow(() -> grepApplication.run(args, mock(InputStream.class), System.out));

        // Then
        assertEquals(expected, outContent.toString());
    }
}