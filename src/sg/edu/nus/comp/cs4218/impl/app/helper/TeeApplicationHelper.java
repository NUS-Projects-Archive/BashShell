package sg.edu.nus.comp.cs4218.impl.app.helper;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_GENERAL;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.TeeException;

/**
 * A helper class that provides functionality to write to a file (tee).
 */
public final class TeeApplicationHelper {
    /**
     * Creates an empty file with the given filename if file does not exist.
     * Returns string containing the absolute filepath of the file.
     *
     * @param file String of name of file to be created.
     * @return A string of the absolute file path of the created empty file.
     * @throws TeeException
     */
    public static String createEmptyFile(String file) throws TeeException {
        Path path = Paths.get(file).normalize();
        if (!path.isAbsolute()) {
            path = Paths.get(Environment.currentDirectory).resolve(path);
        }
        File newFile = new File(path.toString());

        try {
            if (newFile.createNewFile()) {
                return path.toString();
            } else {
                throw new TeeException(ERR_GENERAL);
            }
        } catch (IOException e) {
            throw new TeeException(e.getMessage(), e);
        }
    }

    /**
     * Writes specified content to the given file. If isAppend is true, appends to the existing text
     * in the file. Else, overwrites files contents with given content.
     *
     * @param isAppend Boolean option to append input to existing file content.
     * @param content  String to write to file.
     * @param filePath String of absolute path of file to be written to.
     * @throws TeeException
     */
    public static void writeToFile(Boolean isAppend, String content, String filePath) throws TeeException {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new TeeException(ERR_FILE_NOT_FOUND);
            }
            if (isAppend) {
                Files.write(path, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            } else {
                Files.write(path, content.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new TeeException(e.getMessage(), e);
        }
    }
}
