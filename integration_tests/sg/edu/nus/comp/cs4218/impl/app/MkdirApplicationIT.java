package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.testutils.AssertUtils.assertFileExists;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_EXISTS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOP_LEVEL_MISSING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.MkdirException;

@SuppressWarnings("PMD.ClassNamingConventions")
public class MkdirApplicationIT {

    private static final String MKDIR_EX_MSG = "mkdir: ";
    private static final String FILE = "file.txt";

    @TempDir
    private Path tempDir;
    private String file;
    private MkdirApplication app;

    @BeforeEach
    void setUp() throws IOException {
        app = new MkdirApplication();

        // Create temporary file, automatically deletes after test execution
        Path filePath = tempDir.resolve(FILE);
        file = filePath.toString();
        Files.createFile(filePath);
    }

    @Test
    void run_EmptyArgs_ThrowsMkdirException() {
        MkdirException result = assertThrowsExactly(MkdirException.class, () -> app.run(null, null, null));
        String expected = MKDIR_EX_MSG + "Null arguments";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_NoDirectoriesArgs_ThrowsMkdirException() {
        String[] args = {"-p"};
        MkdirException result = assertThrowsExactly(MkdirException.class, () -> app.run(args, null, null));
        String expected = MKDIR_EX_MSG + "No folder names are supplied";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_MissingTopLevel_ThrowsMkdirException() {
        Path missTopLevelPath = tempDir.resolve("missingTopLevel/" + FILE);
        String[] args = {missTopLevelPath.toString()};
        MkdirException result = assertThrowsExactly(MkdirException.class, () -> app.run(args, null, null));
        String expected = MKDIR_EX_MSG + String.format("cannot create directory '%s': %s", missTopLevelPath, ERR_TOP_LEVEL_MISSING);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_RootDirectory_ThrowsMkdirException() {
        String[] args = {"/"};
        MkdirException result = assertThrowsExactly(MkdirException.class, () -> app.run(args, null, null));
        String expected = MKDIR_EX_MSG + String.format("cannot create directory '%s': %s", "/", ERR_FILE_EXISTS);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_FolderExists_ThrowsMkdirException() {
        String[] args = {file};
        MkdirException result = assertThrowsExactly(MkdirException.class, () -> app.run(args, null, null));
        String expected = MKDIR_EX_MSG + String.format("cannot create directory '%s': %s", file, ERR_FILE_EXISTS);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_MissingTopLevelAndIsCreateParent_CreateFolderSuccessfully() {
        String missTopLevelFile = tempDir.resolve("missingTopLevel/" + FILE).toString();
        String[] args = {"-p", missTopLevelFile};
        assertDoesNotThrow(() -> app.run(args, null, null));
        assertFileExists(missTopLevelFile);
    }

    @Test
    void run_RootDirectoryAndIsCreateParent_CreateFolderSuccessfully() {
        String[] args = {"-p", "/"};
        assertDoesNotThrow(() -> app.run(args, null, null));
        String rootFile = new File("/").toString();
        assertFileExists(rootFile);
    }

    @Test
    void run_FolderExistsAndIsCreateParent_CreateFolderSuccessfully() {
        String[] args = {"-p", file};
        assertDoesNotThrow(() -> app.run(args, null, null));
        assertFileExists(file);
    }

    @Test
    void run_FolderDoNotExists_CreateFolderSuccessfully() {
        String nonExistFile = tempDir.resolve("nonExistentFile.txt").toString();
        String[] args = {nonExistFile};
        assertDoesNotThrow(() -> app.run(args, null, null));
        assertFileExists(nonExistFile);
    }
}
