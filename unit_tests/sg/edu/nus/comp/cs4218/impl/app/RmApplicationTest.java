package sg.edu.nus.comp.cs4218.impl.app;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.removeTrailing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.RmException;

public class RmApplicationTest {

    private static final String TEST_RESOURCES = "resources/rm/";
    private static final String EMPTY_DIRECTORY = "empty_directory";
    private static final String TEST_DIRECTORY = "test_folder";
    private static final String NON_EXIST_FILE = "does-not-exist.txt";
    private static final String TEST_FILE_ONE = "test-file-1.txt";
    private static final String TEST_FILE_TWO = "test-file-2.txt";

    @TempDir
    private Path testingDirectory;

    private RmApplication app;

    @BeforeEach
    void setUp(@TempDir(cleanup = CleanupMode.ALWAYS) Path tempDir) throws IOException {

        final String resourceDirectory = removeTrailing(TEST_RESOURCES, "/");
        testingDirectory = tempDir;
        app = new RmApplication();

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

        // Add an empty directory
        Files.createDirectory(testingDirectory.resolve(EMPTY_DIRECTORY));

        // Set CWD to be the test directory
        Environment.currentDirectory = testingDirectory.toString();
    }

    @Test
    void remove_ExistingFile_SuccessfullyRemoveFile() {
        assertDoesNotThrow(() -> app.remove(false, false, TEST_FILE_ONE));
        assertTrue(Files.notExists(Paths.get(testingDirectory.toString(), TEST_FILE_ONE)));
    }

    @Test
    void remove_NonExistentFile_ThrowsRmException() {
        final String expectedMsg = String.format("rm: cannot remove '%s': No such file or directory", NON_EXIST_FILE);
        RmException exception = assertThrowsExactly(RmException.class, () ->
                app.remove(false, false, NON_EXIST_FILE)
        );
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void remove_MixExistingAndNonExistentFiles_ThrowsRmExceptionButDeletesExistingFile() {
        final String expectedMsg = String.format("rm: cannot remove '%s': No such file or directory"
                        + STRING_NEWLINE
                        + "rm: cannot remove '%s': No such file or directory",
                NON_EXIST_FILE, NON_EXIST_FILE);
        RmException exception = assertThrowsExactly(RmException.class, () ->
                app.remove(false, false, NON_EXIST_FILE, TEST_FILE_ONE, NON_EXIST_FILE, TEST_FILE_TWO)
        );
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void remove_DirectoryWithoutFlag_ThrowsRmException() {
        final String expectedMsg = String.format("rm: cannot remove '%s': Is a directory", TEST_DIRECTORY);
        RmException exception = assertThrowsExactly(RmException.class, () ->
                app.remove(false, false, TEST_DIRECTORY)
        ); // When
        assertEquals(expectedMsg, exception.getMessage()); // Then
    }

    @Test
    void remove_EmptyDirectory_SuccessfullyRemoveDirectory() {
        assertDoesNotThrow(() -> app.remove(true, false, EMPTY_DIRECTORY));
        assertTrue(Files.notExists(Paths.get(testingDirectory.toString(), EMPTY_DIRECTORY)));
    }

    @Test
    void remove_NonEmptyDirectory_ThrowsRmException() {
        final String expectedMsg = String.format("rm: cannot remove '%s': Directory not empty", TEST_DIRECTORY);
        RmException exception = assertThrowsExactly(RmException.class, () ->
                app.remove(true, false, TEST_DIRECTORY)
        ); // When
        assertEquals(expectedMsg, exception.getMessage()); // Then
    }

    @Test
    void remove_SingleDirectoryRecursively_SuccessfullyRemoveAllFilesAndDirectory() {
        assertDoesNotThrow(() -> app.remove(false, true, TEST_DIRECTORY));
        assertTrue(Files.exists(Paths.get(testingDirectory.toString(), TEST_FILE_ONE))); // should exist
        assertTrue(Files.exists(Paths.get(testingDirectory.toString(), EMPTY_DIRECTORY))); // should exist
        assertTrue(Files.notExists(Paths.get(testingDirectory.toString(), TEST_DIRECTORY)));
    }
}
