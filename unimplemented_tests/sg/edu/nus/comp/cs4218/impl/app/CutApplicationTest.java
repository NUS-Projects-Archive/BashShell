package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.skeleton.app.CutApplication;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class CutApplicationTest {

    private static final String TEMP_FILE = "file.txt";
    private static final String TEMP_CONTENT = "1234567890";
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
    void cutFromFiles_CutByChar_ReturnsCutRange() throws CutException {
        String expected = "12345";
        List<int[]> range = List.of(new int[]{1, 5});
        String result = app.cutFromFiles(true, false, range, tempFilePath.toString());
        assertEquals(expected, result);
    }

    @Test
    void cutFromFiles_CutByByte_ReturnsCutRange() throws CutException {
        String expected = "12345";
        List<int[]> range = List.of(new int[]{1, 5});
        String result = app.cutFromFiles(false, true, range, tempFilePath.toString());
        assertEquals(expected, result);
    }

    @Test
    void cutFromFiles_EmptyFile_ReturnsEmptyString() throws CutException, IOException {
        Files.write(tempFilePath, "".getBytes()); // Overwrites the file content with an empty string
        String expected = "";

        List<int[]> range = List.of(new int[]{1, 5});
        String result = app.cutFromFiles(true, false, range, tempFilePath.toString());

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
    @DisabledOnOs(value = OS.WINDOWS)
    void cutFromFiles_FileNoPermissionToRead_ThrowsCutException() throws IOException {
        String expectedMsg = "cut: Permission denied";
        Files.setPosixFilePermissions(tempFilePath,
                new HashSet<>(Collections.singleton(PosixFilePermission.OWNER_READ)));
        CutException exception = assertThrowsExactly(CutException.class, () -> {
            List<int[]> range = List.of(new int[]{1, 5});
            app.cutFromFiles(true, false, range, tempFilePath.toString());
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void cutFromStdin_CutByChar_ReturnsCutRange() throws CutException {
        String expected = "12345";
        List<int[]> range = List.of(new int[]{1, 5});
        InputStream stdin = new ByteArrayInputStream(TEMP_CONTENT.getBytes());
        String result = app.cutFromStdin(true, false, range, stdin);
        assertEquals(expected, result);
    }

    @Test
    void cutFromStdin_CutByByte_ReturnsCutRange() throws CutException {
        String expected = "12345";
        List<int[]> range = List.of(new int[]{1, 5});
        InputStream stdin = new ByteArrayInputStream(TEMP_CONTENT.getBytes());
        String result = app.cutFromStdin(true, false, range, stdin);
        assertEquals(expected, result);
    }

    @Test
    void cutFromStdin_EmptyStdin_ReturnsEmptyString() throws CutException {
        String expected = "";
        List<int[]> range = List.of(new int[]{1, 5});
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        String result = app.cutFromStdin(true, false, range, stdin);
        assertEquals(expected, result);
    }
}
