package sg.edu.nus.comp.cs4218.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileUtils {
    private FileUtils() { /* Does nothing */}

    /**
     * Create a temporary file with the given contents in the default temporary-file directory.
     *
     * @param fileName Name of file to be created
     * @param contents Contents to be written to the file
     * @return
     */
    public static Path createNewFile(String fileName, String contents) {
        try {
            Path file = Files.createTempFile(fileName, "");
            Files.write(file, contents.getBytes());
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a temporary file with the given contents in the specified directory.
     *
     * @param dir      Directory where file is to be created in
     * @param fileName Name of file to be created
     * @param contents Contents to be written to the file
     * @return
     */
    public static Path createNewFileInDir(Path dir, String fileName, String contents) {
        try {
            Path file = Files.createTempFile(dir, fileName, "");
            Files.write(file, contents.getBytes());
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a temporary directory with no contents.
     *
     * @param parentPath New directory to be created in this parentPath
     * @param dirName    Name of directory to be created
     * @return
     */
    public static Path createNewDirectory(Path parentPath, String dirName) {
        try {
            Path path = parentPath.resolve(dirName);
            return Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes specified file/directory if it exists.
     *
     * @param file File/directory to be deleted.
     */
    public static void deleteFileOrDirectory(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
