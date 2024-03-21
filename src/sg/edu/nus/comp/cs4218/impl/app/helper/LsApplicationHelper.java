package sg.edu.nus.comp.cs4218.impl.app.helper;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_CURR_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.isBlank;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.DirectoryAccessDeniedLsException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryLsException;
import sg.edu.nus.comp.cs4218.exception.LsException;

/**
 * A helper class that provides functionality to list the contents of a directory (ls).
 */
public final class LsApplicationHelper {

    private static final String PATH_CURR_DIR = STRING_CURR_DIR + CHAR_FILE_SEP;
    private static final String COLON_NEW_LINE = ":" + STRING_NEWLINE;

    private LsApplicationHelper() { /* Does nothing*/ }

    /**
     * Lists only the current directory's content and RETURNS.
     * This does not account for recursive mode in cwd.
     *
     * @param isSortByExt Sorts folder contents alphabetically by file extension
     *                    (characters after the last ‘.’ (without quotes)). Files
     *                    with no extension are sorted first
     * @return String containing the current directory's content
     * @throws LsException If there is an issue accessing or listing the current directory
     */
    public static String listCwdContent(Boolean isSortByExt) throws LsException {
        final String cwd = Environment.currentDirectory;
        try {
            return formatContents(getContents(Paths.get(cwd)), isSortByExt);
        } catch (InvalidDirectoryLsException | DirectoryAccessDeniedLsException e) {
            throw new LsException(e.getMessage(), e);
        }
    }

    /**
     * Builds the resulting string to be written into the output stream.
     * <p>
     * NOTE: This is recursively called if user wants recursive mode.
     *
     * @param paths       List of java.nio.Path objects to list
     * @param isRecursive Recursive mode, repeatedly ls the child directories
     * @param isSortByExt Sorts folder contents alphabetically by file extension
     *                    (characters after the last ‘.’ (without quotes)). Files
     *                    with no extension are sorted first
     * @param hasFolder   Boolean to indicate if folder name is specified
     * @return String to be written to output stream
     */
    public static String buildResult(List<Path> paths, Boolean isRecursive, Boolean isSortByExt, Boolean hasFolder) {
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
                result.append(isBlank(relativePath)
                        ? STRING_CURR_DIR : hasFolder
                        ? relativePath : PATH_CURR_DIR + relativePath);
                result.append(COLON_NEW_LINE);
                result.append(formatted);

                if (!formatted.isEmpty()) {
                    // Empty directories should not have an additional new line
                    result.append(STRING_NEWLINE);
                }
                result.append(STRING_NEWLINE);

                // RECURSE!
                if (isRecursive) {
                    result.append(buildResult(contents, isRecursive, isSortByExt, hasFolder));
                }
            } catch (InvalidDirectoryLsException e) {
                // If the directory is invalid, print the errors at the top
                error.append(e.getMessage());
                error.append(STRING_NEWLINE);
            } catch (DirectoryAccessDeniedLsException e) {
                // Trim the last newline
                boolean endsWithNewline = result.toString().endsWith(STRING_NEWLINE);
                if (endsWithNewline) {
                    result.deleteCharAt(result.length() - 1);
                }
                // Append the error message to the result normally
                result.append(e.getMessage());
                result.append(STRING_NEWLINE).append(STRING_NEWLINE);
            }
        }

        return error.toString() + result;
    }

    /**
     * Formats the contents of a directory into a single string.
     *
     * @param contents    List of items in a directory
     * @param isSortByExt Sorts folder contents alphabetically by file extension
     *                    (characters after the last ‘.’ (without quotes)). Files
     *                    with no extension are sorted first
     * @return Formatted string containing the directory's contents
     */
    public static String formatContents(List<Path> contents, Boolean isSortByExt) {
        StringBuilder result = new StringBuilder();

        List<String> fileNames = contents.stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());

        if (isSortByExt) {
            fileNames.sort(getFileExtensionComparator());
        } else {
            fileNames.sort(null); // natural ordering
        }

        for (String fileName : fileNames) {
            result.append(fileName);
            result.append(STRING_NEWLINE);
        }

        return result.toString().trim();
    }

    /**
     * Gets the contents in a single specified directory.
     *
     * @param directory Directory to get contents from
     * @return List of files and directories in the specified directory
     */
    private static List<Path> getContents(Path directory)
            throws InvalidDirectoryLsException, DirectoryAccessDeniedLsException {
        if (Files.isDirectory(directory)) {
            // Path is a folder
            if (Files.isReadable(directory)) {
                // Get contents from directory
                return getContentsFromReadableDirectory(directory);
            } else {
                // Directory has no read access
                throw new DirectoryAccessDeniedLsException(getRelativeToCwd(directory).toString().isEmpty() ? "." : getRelativeToCwd(directory).toString());
            }
        } else if (Files.isRegularFile(directory)) {
            // Path is a non-folder
            return null;
        } else {
            // Path does not exist
            throw new InvalidDirectoryLsException(getRelativeToCwd(directory).toString());
        }
    }

    /**
     * Gets the contents of a directory that is readable.
     *
     * @param directory Directory to get contents from
     * @return List of files and directories in the specified directory
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
     * @return List of java.nio.Path objects representing the resolved directories
     * @throws InvalidDirectoryLsException If any of the specified directories is invalid or inaccessible
     */
    public static List<Path> resolvePaths(String... directories) throws InvalidDirectoryLsException {
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
     * @return A java.nio.Path object representing the resolved directory
     * @throws InvalidDirectoryLsException If the specified directory is invalid or inaccessible
     */
    private static Path resolvePath(String directory) throws InvalidDirectoryLsException {
        try {
            Path path;
            if (directory.charAt(0) == '/' || (directory.length() > 2 && directory.charAt(1) == ':') ||
                    directory.equals(Environment.currentDirectory)) {
                path = Paths.get(directory).normalize();
            } else {
                // Construct path relative to current directory
                path = Paths.get(Environment.currentDirectory, directory).normalize();
            }

            if (!Files.exists(path)) {
                throw new InvalidDirectoryLsException(getRelativeToCwd(path).toString());
            }

            return path;

        } catch (InvalidPathException e) {
            throw new InvalidDirectoryLsException(directory, e);
        }
    }

    /**
     * Converts a path to a relative path to the current directory.
     *
     * @param path Path to be converted
     * @return A java.nio.Path object representing the relative path to the current directory
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
}
