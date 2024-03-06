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

    private static final String FILE = "file.txt";
    private static final String FILE_CONTENT = "1234567890";
    private static final String ONE_TO_FIVE = "12345";

    @TempDir
    private Path tempDir;
    private Path filePath;
    private String file;
    private CutApplication app;

    @BeforeEach
    void setUp() throws IOException {
        app = new CutApplication();

        // Create temporary file, automatically deletes after test execution
        filePath = tempDir.resolve(FILE);
        file = filePath.toString();
        Files.createFile(filePath);

        // Writes content to temporary file
        Files.write(filePath, FILE_CONTENT.getBytes());
    }

    // The tests do not cover scenarios where no flag is provided, more than one flag is given,
    // or the invalidity of the range, as exceptions are expected to be thrown before reaching the cutFromFiles method.
    @Test
    void cutFromFiles_CutByChar_ReturnsCutRange() {
        List<int[]> range = List.of(new int[]{1, 5});
        String result = assertDoesNotThrow(() -> app.cutFromFiles(true, false, range, file));
        assertEquals(ONE_TO_FIVE, result);
    }

    @Test
    void cutFromFiles_CutByByte_ReturnsCutRange() {
        List<int[]> range = List.of(new int[]{1, 5});
        String result = assertDoesNotThrow(() -> app.cutFromFiles(false, true, range, file));
        assertEquals(ONE_TO_FIVE, result);
    }

    @Test
    void cutFromFiles_EmptyFile_ReturnsEmptyString() {
        // Given: overwrites the file content with an empty string
        assertDoesNotThrow(() -> Files.write(filePath, "".getBytes()));

        List<int[]> range = List.of(new int[]{1, 5});
        String result = assertDoesNotThrow(() -> app.cutFromFiles(true, false, range, file));

        String expected = "";
        assertEquals(expected, result);
    }

    @Test
    void cutFromFiles_FileDoNotExist_ThrowsCutException() {
        String nonExistFile = tempDir.resolve("nonExistFile.txt").toString();
        CutException result = assertThrowsExactly(CutException.class, () -> {
            app.cutFromFiles(true, false, null, nonExistFile);
        });
        String expected = "cut: No such file or directory";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void cutFromFiles_FileGivenAsDirectory_ThrowsCutException() {
        List<int[]> range = List.of(new int[]{1, 5});
        CutException result = assertThrowsExactly(CutException.class, () -> {
            app.cutFromFiles(true, false, range, tempDir.toString());
        });
        String expected = "cut: This is a directory";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void cutFromFiles_FileNoPermissionToRead_ThrowsCutException() {
        boolean isReadable = filePath.toFile().setReadable(false);
        if (isReadable) {
            fail("Failed to set read permission to false for test");
        }

        List<int[]> range = List.of(new int[]{1, 5});
        CutException result = assertThrowsExactly(CutException.class, () -> {
            app.cutFromFiles(true, false, range, filePath.toString());
        });
        String expected = "cut: Permission denied";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void cutFromStdin_CutByChar_ReturnsCutRange() {
        List<int[]> range = List.of(new int[]{1, 5});
        InputStream stdin = new ByteArrayInputStream(FILE_CONTENT.getBytes());
        String result = assertDoesNotThrow(() -> app.cutFromStdin(true, false, range, stdin));
        assertEquals(ONE_TO_FIVE, result);
    }

    @Test
    void cutFromStdin_CutByByte_ReturnsCutRange() {
        List<int[]> range = List.of(new int[]{1, 5});
        InputStream stdin = new ByteArrayInputStream(FILE_CONTENT.getBytes());
        String result = assertDoesNotThrow(() -> app.cutFromStdin(true, false, range, stdin));
        assertEquals(ONE_TO_FIVE, result);
    }

    @Test
    void cutFromStdin_EmptyStdin_ReturnsEmptyString() {
        List<int[]> range = List.of(new int[]{1, 5});
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        String result = assertDoesNotThrow(() -> app.cutFromStdin(true, false, range, stdin));
        String expected = "";
        assertEquals(expected, result);
    }
}
