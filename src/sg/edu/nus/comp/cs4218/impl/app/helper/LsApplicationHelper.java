package sg.edu.nus.comp.cs4218.impl.app.helper;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_CURR_DIR;

@SuppressWarnings("PMD.PreserveStackTrace")
public class LsApplicationHelper {
    private final static String PATH_CURR_DIR = STRING_CURR_DIR + CHAR_FILE_SEP;
    private final static String colonNewLine = ":" + StringUtils.STRING_NEWLINE;

    /**
     * Lists only the current directory's content and RETURNS. This does not account
     * for recursive
     * mode in cwd.
     *
     * @param isSortByExt Sorts folder contents alphabetically by file extension
     *                    (characters after the last ‘.’ (without quotes)). Files
     *                    with no extension are sorted first
     * @return
     */
    public static String listCwdContent(Boolean isSortByExt) throws LsException {
        final String cwd = Environment.currentDirectory;
        try {
            return formatContents(getContents(Paths.get(cwd)), isSortByExt);
        } catch (InvalidDirectoryException | DirectoryAccessDeniedException e) {
            throw new LsException(e.getMessage());
        }
    }

    /**
     * Builds the resulting string to be written into the output stream.
     * <p>
     * NOTE: This is recursively called if user wants recursive mode.
     *
     * @param paths                 List of java.nio.Path objects to list
     * @param isRecursive           Recursive mode, repeatedly ls the child directories
     * @param isSortByExt           Sorts folder contents alphabetically by file extension
     *                              (characters after the last ‘.’ (without quotes)). Files
 *                                  with no extension are sorted first
     * @param isFolderNameSpecified Boolean to indicate if folder name is specified
     * @return String to be written to output stream
     */
    public static String buildResult(List<Path> paths, Boolean isRecursive, Boolean isSortByExt,
                                     boolean isFolderNameSpecified) {
        StringBuilder result = new StringBuilder();
        StringBuilder error = new StringBuilder();
        for (Path path : paths) {
            try {
                List<Path> contents = getContents(path);
                if (contents == null) {
                    // Path is non-folder, don't need to list anything
                    continue;
                }

                final String formatted = formatContents(contents, isSortByExt);
                final String relativePath = getRelativeToCwd(path).toString();
                result.append(StringUtils.isBlank(relativePath)
                        ? STRING_CURR_DIR
                        : isFolderNameSpecified
                        ? relativePath
                        : PATH_CURR_DIR + relativePath);
                result.append(colonNewLine);
                result.append(formatted);

                if (!formatted.isEmpty()) {
                    // Empty directories should not have an additional new line
                    result.append(StringUtils.STRING_NEWLINE);
                }
                result.append(StringUtils.STRING_NEWLINE);

                // RECURSE!
                if (isRecursive) {
                    result.append(buildResult(contents, isRecursive, isSortByExt, isFolderNameSpecified));
                }
            } catch (InvalidDirectoryException e) {
                // If the directory is invalid, print the errors at the top
                error.append(e.getMessage());
                error.append(StringUtils.STRING_NEWLINE);
            } catch (DirectoryAccessDeniedException e) {
                // Append the error message to the result normally
                // Trim the last newline
                if (result.length() > 0) {
                    result.deleteCharAt(result.length() - 1);
                }
                result.append(e.getMessage());
                result.append(StringUtils.STRING_NEWLINE + StringUtils.STRING_NEWLINE);
            }
        }

        return error.toString() + result.toString();
    }

    /**
     * Formats the contents of a directory into a single string.
     *
     * @param contents    List of items in a directory
     * @param isSortByExt Sorts folder contents alphabetically by file extension
     *                    (characters after the last ‘.’ (without quotes)). Files
     *                    with no extension are sorted first
     * @return
     */
    private static String formatContents(List<Path> contents, Boolean isSortByExt) {
        StringBuilder result = new StringBuilder();

        List<String> fileNames = contents.stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());

        // Sort file names
        Collections.sort(fileNames);

        if (isSortByExt) {
            fileNames.sort(getFileExtensionComparator());
        }

        for (String fileName : fileNames) {
            result.append(fileName);
            result.append(StringUtils.STRING_NEWLINE);
        }

        return result.toString().trim();
    }

    /**
     * Gets the contents in a single specified directory.
     *
     * @param directory Directory to get contents from
     * @return List of files + directories in the passed directory
     */
    private static List<Path> getContents(Path directory)
            throws InvalidDirectoryException, DirectoryAccessDeniedException {
        if (Files.isDirectory(directory)) {
            if (Files.isReadable(directory)) {
                // Get contents from directory
                return getContentsFromReadableDirectory(directory);
            } else {
                // Directory has no read access
                throw new DirectoryAccessDeniedException(getRelativeToCwd(directory).toString());
            }
        } else if (Files.isRegularFile(directory)) {
            // Path is a non-folder
            return null;
        } else {
            // Path does not exist
            throw new InvalidDirectoryException(getRelativeToCwd(directory).toString());
        }
    }

    /**
     * Gets the contents of a directory that is readable.
     *
     * @param directory Directory to get contents from
     * @return List of files and directories in the passed directory
     */
    private static List<Path> getContentsFromReadableDirectory(Path directory) {
        List<Path> result = new ArrayList<>();
        File pwd = directory.toFile();
        for (File f : pwd.listFiles()) {
            if (!f.isHidden()) {
                result.add(f.toPath());
            }
        }

        Collections.sort(result);
        return result;
    }

    /**
     * Resolve all paths given as arguments into a list of Path objects for easy
     * path management.
     *
     * @param directories List of directories to be resolved into Path objects
     * @return List of java.nio.Path objects
     */
    public static List<Path> resolvePaths(String... directories) {
        List<Path> paths = new ArrayList<>();
        for (int i = 0; i < directories.length; i++) {
            paths.add(resolvePath(directories[i]));
        }

        return paths;
    }

    /**
     * Converts a String into a java.nio.Path objects. Also resolves if the current
     * path provided is an absolute path or already the current directory.
     *
     * @param directory Directory to be converted into a Path object
     * @return
     */
    private static Path resolvePath(String directory) {
        if (directory.charAt(0) == '/' || directory.equals(Environment.currentDirectory)) {
            return Paths.get(directory).normalize();
        }

        // Construct path relative to current directory
        return Paths.get(Environment.currentDirectory, directory).normalize();
    }

    /**
     * Converts a path to a relative path to the current directory.
     *
     * @param path Path to be converted
     * @return
     */
    private static Path getRelativeToCwd(Path path) {
        return Paths.get(Environment.currentDirectory).relativize(path);
    }

    /**
     * Comparator for sorting files alphabetically by file extension.
     * Files with no extension are sorted first.
     * <p>
     * The comparator first compares files alphabetically based on their file
     * extension,
     * followed by comparing based on their full string representation.
     *
     * @return A comparator for sorting files by file extension
     */
    private static Comparator<String> getFileExtensionComparator() {
        return Comparator.comparing((String s) -> getFileExtension(s)).thenComparing(String::toString);
    }

    /**
     * Returns the file extension from a given file name.
     *
     * @param file The file name to extract the extension
     * @return The file extension if it exists; Otherwise, an empty string
     */
    private static String getFileExtension(String file) {
        int lastDotIndex = file.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : file.substring(lastDotIndex + 1);
    }

    /**
     * This exception is thrown only when the directory passed as an argument is
     * invalid.
     * It is considered invalid if it does not exist.
     */
    private static class InvalidDirectoryException extends Exception {
        InvalidDirectoryException(String directory) {
            super(String.format("ls: cannot access '%s': No such file or directory", directory));
        }
    }

    /**
     * This exception is thrown only when the directory passed as an argument is
     * valid but the user does not have permission to access it.
     */
    private static class DirectoryAccessDeniedException extends Exception {
        DirectoryAccessDeniedException(String directory) {
            super(String.format("ls: cannot open directory '%s': Permission denied", directory));
        }
    }
}
