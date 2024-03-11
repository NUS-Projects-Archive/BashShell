package sg.edu.nus.comp.cs4218.impl.app.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.app.helper.GrepApplicationHelper.IS_DIRECTORY;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_REGEX;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.impl.app.GrepApplication;

@SuppressWarnings("PMD.ClassNamingConventions")
class GrepApplicationHelperIT {

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
        fileOneName = fileOne.getFileName().toString();

        fileTwo = createNewFileInDir(tempDir, "tempFile2", INPUT_CONTENTS);
        fileTwoName = fileTwo.getFileName().toString();
    }

    @AfterEach
    void tearDown() {
        deleteFileOrDirectory(fileOne);
        deleteFileOrDirectory(fileTwo);
    }

    @Test
    void grepFromFileAndStdin_WithValidPatternAndFileAndStdin_ReturnsMatchingLinesFromFileAndStdin() {
        // Given
        String[] args = new String[]{VALID_PAT_SMALL, fileOneName, DASH};
        String expected = String.join(STRING_NEWLINE,
                getValidOutputArrWithFileName(fileOneName, STRING_STDIN_OUTPUT)) + STRING_NEWLINE;

        // When
        assertDoesNotThrow(() -> app.run(args, stdin, stdout));

        // Then
        assertEquals(expected, stdout.toString());
    }

    @Test
    void grepResultsFromFiles_WithValidPatternAndMultipleValidFiles_ReturnsMatchingLinesFromFiles() {
        // Given
        String[] args = new String[]{VALID_PAT_SMALL, fileOneName, fileTwoName};
        String expected = String.join(STRING_NEWLINE,
                getValidOutputArrWithFileName(fileOneName, fileTwoName)) + STRING_NEWLINE;

        // When
        assertDoesNotThrow(() -> app.run(args, mock(InputStream.class), stdout));

        // Then
        assertEquals(expected, stdout.toString());
    }

    @Test
    void grepResultsFromFiles_WithDirectory_ReturnsIsADirectoryErr() {
        // Given
        String[] args = new String[]{VALID_PAT_SMALL, tempDir.toString()};
        String expected = GREP_STRING + tempDir + COLON_SPACE + IS_DIRECTORY + STRING_NEWLINE;

        // When
        assertDoesNotThrow(() -> app.run(args, mock(InputStream.class), stdout));

        // Then
        assertEquals(expected, stdout.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"[", "+", "(", "{"})
    void grepResultsFromFiles_InvalidPattern_ReturnsInvalidPatternErr(String regex) {
        // Given
        String[] args = new String[]{regex, fileOneName};

        // When
        GrepException exception = assertThrows(GrepException.class, () -> app.run(args, mock(InputStream.class), stdout));

        // Then
        assertEquals(GREP_STRING + ERR_INVALID_REGEX, exception.getMessage());
    }
}
