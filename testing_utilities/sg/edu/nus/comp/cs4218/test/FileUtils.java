package sg.edu.nus.comp.cs4218.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileUtils {
    private FileUtils() { /* Does nothing */}

    /**
     * Create a temporary file with the given contents.
     *
     * @param fileName Name of file to be created
     * @param contents Contents to be written to the file
     * @return
     */
    public static Path createNewFile(String fileName, String contents) {
        Path file = assertDoesNotThrow(() -> Files.createTempFile(fileName, ""), "Unable to create temporary file");
        assertDoesNotThrow(() -> Files.write(file, contents.getBytes()), "Unable to write to temporary file");
        return file;
    }

    /**
     * Creates a new file at the given parent path.
     * Any non-existent parent directories are created.
     *
     * @param parentPath {@code Path} at which the file should be created
     * @param newFileName {@code String} of the name of the new file
     * @return {@code Path} of the newly-created file
     * @throws IOException If an I/O error occurs
     */
    public static Path createNewFile(Path parentPath, String newFileName) throws IOException {
        Files.createDirectories(parentPath);
        return Files.createFile(Paths.get(parentPath.toString(), newFileName));
    }

    /**
     * Create a temporary directory with no contents.
     *
     * @param parentPath New directory to be created in this parentPath
     * @param dirName    Name of directory to be created
     * @return
     */
    public static Path createNewDirectory(Path parentPath, String dirName) {
        Path path = parentPath.resolve(dirName);
        return assertDoesNotThrow(() -> Files.createDirectories(path), "Unable to create temporary directory");
    }

    /**
     * Deletes specified file/directory if it exists.
     *
     * @param file File/directory to be deleted.
     */
    public static void deleteFileOrDirectory(Path file) {
        assertDoesNotThrow(() -> Files.deleteIfExists(file), "Unable to delete temporary file/directory");
    }
}
