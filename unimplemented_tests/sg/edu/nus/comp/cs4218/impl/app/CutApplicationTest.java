package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.CutException;

class CutApplicationTest {

    private static final String TEMP_FILE = "file.txt";
    private static final String TEMP_CONTENT = "1234567890";
    private static final String ONE_TO_FIVE = "12345";

    @TempDir
    private Path tempDir;
    private Path tempFilePath;
    private CutApplication app;

    @BeforeEach
    void setUp() throws IOException {
        app = new CutApplication();

        // Create temporary file, automatically deletes after test execution
        tempFilePath = tempDir.resolve(TEMP_FILE);
        Files.createFile(tempFilePath);

        // Writes content to temporary file
        Files.write(tempFilePath, TEMP_CONTENT.getBytes());
    }

    // The tests do not cover scenarios where no flag is provided, more than one flag is given,
    // or the invalidity of the range, as exceptions are expected to be thrown before reaching the cutFromFiles method.
    @Test
    void cutFromFiles_CutByChar_ReturnsCutRange() {
        List<int[]> range = List.of(new int[]{1, 5});
        String result = assertDoesNotThrow(() -> app.cutFromFiles(true, false, range, tempFilePath.toString()));
        assertEquals(ONE_TO_FIVE, result);
    }

    @Test
    void cutFromFiles_CutByByte_ReturnsCutRange() {
        List<int[]> range = List.of(new int[]{1, 5});
        String result = assertDoesNotThrow(() -> app.cutFromFiles(false, true, range, tempFilePath.toString()));
        assertEquals(ONE_TO_FIVE, result);
    }

    @Test
    void cutFromFiles_EmptyFile_ReturnsEmptyString() {
        // Overwrites the file content with an empty string
        assertDoesNotThrow(() -> Files.write(tempFilePath, "".getBytes()));
        String expected = "";

        List<int[]> range = List.of(new int[]{1, 5});
        String result = assertDoesNotThrow(() -> app.cutFromFiles(true, false, range, tempFilePath.toString()));

        assertEquals(expected, result);
    }

    @Test
    void cutFromFiles_FileDoNotExist_ThrowsCutException() {
        String expectedMsg = "cut: No such file or directory";
        CutException exception = assertThrowsExactly(CutException.class, () -> {
            Path nonExistFilePath = tempDir.resolve("nonExistFile.txt");
            app.cutFromFiles(true, false, null, nonExistFilePath.toString());
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void cutFromFiles_FileGivenAsDirectory_ThrowsCutException() {
        String expectedMsg = "cut: This is a directory";
        CutException exception = assertThrowsExactly(CutException.class, () -> {
            List<int[]> range = List.of(new int[]{1, 5});
            app.cutFromFiles(true, false, range, tempDir.toString());
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void cutFromFiles_FileNoPermissionToRead_ThrowsCutException() {
        String expectedMsg = "cut: Permission denied";
        boolean isReadable = tempFilePath.toFile().setReadable(false);
        if (isReadable) {
            fail("Failed to set read permission to false for test");
        }

        CutException exception = assertThrowsExactly(CutException.class, () -> {
            List<int[]> range = List.of(new int[]{1, 5});
            app.cutFromFiles(true, false, range, tempFilePath.toString());
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void cutFromStdin_CutByChar_ReturnsCutRange() {
        List<int[]> range = List.of(new int[]{1, 5});
        InputStream stdin = new ByteArrayInputStream(TEMP_CONTENT.getBytes());
        String result = assertDoesNotThrow(() -> app.cutFromStdin(true, false, range, stdin));
        assertEquals(ONE_TO_FIVE, result);
    }

    @Test
    void cutFromStdin_CutByByte_ReturnsCutRange() {
        List<int[]> range = List.of(new int[]{1, 5});
        InputStream stdin = new ByteArrayInputStream(TEMP_CONTENT.getBytes());
        String result = assertDoesNotThrow(() -> app.cutFromStdin(true, false, range, stdin));
        assertEquals(ONE_TO_FIVE, result);
    }

    @Test
    void cutFromStdin_EmptyStdin_ReturnsEmptyString() {
        String expected = "";
        List<int[]> range = List.of(new int[]{1, 5});
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        String result = assertDoesNotThrow(() -> app.cutFromStdin(true, false, range, stdin));
        assertEquals(expected, result);
    }
}
