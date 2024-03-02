package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {
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
     * Deletes specified file/directory if it exists.
     *
     * @param file File/directory to be deleted.
     */
    public static void deleteFileOrDirectory(Path file) {
        assertDoesNotThrow(() -> Files.deleteIfExists(file), "Unable to delete temporary file/directory");
    }
}
