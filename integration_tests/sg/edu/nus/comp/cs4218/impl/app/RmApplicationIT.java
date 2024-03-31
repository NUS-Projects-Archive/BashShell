package sg.edu.nus.comp.cs4218.impl.app;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

@SuppressWarnings("PMD.ClassNamingConventions")
public class RmApplicationIT {

    private static final String TEST_RESOURCES = "resources/rm/";
    private static final String EMPTY_DIRECTORY = "empty_directory";
    private static final String TEST_DIRECTORY = "test_folder";
    private static final String NON_EXIST_FILE = "does-not-exist.txt";
    private static final String TEST_FILE_ONE = "test-file-1.txt";

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
    void run_NoArgs_ThrowsRmException() {
        final String expectedMsg = "rm: Null arguments";
        RmException exception = assertThrowsExactly(RmException.class, () -> app.run(null, null, null));
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_RemoveExistingFile_SuccessfullyRemoveFile() {
        final String[] args = {TEST_FILE_ONE};
        assertDoesNotThrow(() -> app.run(args, null, null));
        assertTrue(Files.notExists(Paths.get(testingDirectory.toString(), TEST_FILE_ONE)));
    }

    @Test
    void run_RemoveNonExistentFile_ThrowsRmException() {
        // Given
        final String[] args = {NON_EXIST_FILE};
        final String expectedMsg = String.format("rm: cannot remove '%s': No such file or directory", NON_EXIST_FILE);

        RmException exception = assertThrowsExactly(RmException.class, () ->app.run(args, null, null)); // When
        assertEquals(expectedMsg, exception.getMessage()); // Then
    }

    @Test
    void run_RemoveDirectoryWithoutFlag_ThrowsRmException() {
        // Given
        final String[] args = {TEST_DIRECTORY};
        final String expectedMsg = String.format("rm: cannot remove '%s': Is a directory", TEST_DIRECTORY);

        RmException exception = assertThrowsExactly(RmException.class, () -> app.run(args, null, null)); // When
        assertEquals(expectedMsg, exception.getMessage()); // Then
    }

    @Test
    void run_RemoveEmptyDirectory_SuccessfullyRemoveDirectory() {
        final String[] args = {"-d", EMPTY_DIRECTORY};
        assertDoesNotThrow(() -> app.run(args, null, null));
        assertTrue(Files.notExists(Paths.get(testingDirectory.toString(), EMPTY_DIRECTORY)));
    }

    @Test
    void run_RemoveNonEmptyDirectory_ThrowsRmException() {
        // Given
        final String[] args = {"-d", TEST_DIRECTORY};
        final String expectedMsg = String.format("rm: cannot remove '%s': Directory not empty", TEST_DIRECTORY);

        RmException exception = assertThrowsExactly(RmException.class, () -> app.run(args, null, null)); // When
        assertEquals(expectedMsg, exception.getMessage()); // Then
    }

    @Test
    void run_RemoveSingleDirectoryRecursively_SuccessfullyRemoveAllFilesAndDirectory() {
        final String[] args = {"-r", TEST_DIRECTORY};
        assertDoesNotThrow(() -> app.run(args, null, null));
        assertTrue(Files.exists(Paths.get(testingDirectory.toString(), TEST_FILE_ONE))); // should exist
        assertTrue(Files.exists(Paths.get(testingDirectory.toString(), EMPTY_DIRECTORY))); // should exist
        assertTrue(Files.notExists(Paths.get(testingDirectory.toString(), TEST_DIRECTORY)));
    }

    @Test
    void run_RemoveMultipleDirectoriesRecursively_SuccessfullyRemoveAllFilesAndDirectory() {
        final String[] args = {"-r", TEST_DIRECTORY, EMPTY_DIRECTORY};
        assertDoesNotThrow(() -> app.run(args, null, null));
        assertTrue(Files.exists(Paths.get(testingDirectory.toString(), TEST_FILE_ONE))); // should exist
        assertTrue(Files.notExists(Paths.get(testingDirectory.toString(), EMPTY_DIRECTORY)));
        assertTrue(Files.notExists(Paths.get(testingDirectory.toString(), TEST_DIRECTORY)));
    }
}
