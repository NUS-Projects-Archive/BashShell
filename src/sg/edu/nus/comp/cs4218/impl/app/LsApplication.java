package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.app.helper.LsApplicationHelper.buildResult;
import static sg.edu.nus.comp.cs4218.impl.app.helper.LsApplicationHelper.formatContents;
import static sg.edu.nus.comp.cs4218.impl.app.helper.LsApplicationHelper.listCwdContent;
import static sg.edu.nus.comp.cs4218.impl.app.helper.LsApplicationHelper.resolvePaths;
import static sg.edu.nus.comp.cs4218.impl.util.CollectionsUtils.listToArray;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.LsInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser;

/**
 * The ls command lists information about files.
 *
 * <p>
 * <b>Command format:</b> <code>ls [Options][FILES]</code>
 * </p>
 */
public class LsApplication implements LsInterface {

    /**
     * Runs the ls application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to
     *               a file (non-folders or folders)
     * @param stdin  An InputStream, not used
     * @param stdout An OutputStream. The output of the command is written to this OutputStream
     * @throws LsException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws LsException {
        if (args == null) {
            throw new LsException(ERR_NULL_ARGS);
        }
        if (stdout == null) {
            throw new LsException(ERR_NO_OSTREAM);
        }

        final LsArgsParser parser = new LsArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new LsException(e.getMessage(), e);
        }

        final Boolean recursive = parser.isRecursive();
        final Boolean sortByExt = parser.isSortByExt();
        final String[] directories = listToArray(parser.getDirectories());
        final String result = listFolderContent(recursive, sortByExt, directories);

        try {
            stdout.write(result.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (IOException e) {
            throw new LsException(ERR_WRITE_STREAM, e);
        }
    }

    /**
     * Returns the string listing the folder content of the specified folders. If no folder names are
     * specified, list the content of the current folder.
     *
     * @param isRecursive Boolean option to recursively list the folder contents (traversing
     *                    through all folders inside the specified folder)
     * @param isSortByExt Boolean option to sort folder contents alphabetically by file extension
     *                    (characters after the last '.' (without quotes)) Files with no extension are sorted first
     * @param folderName  Array of String of folder names
     * @throws LsException
     */
    @Override
    public String listFolderContent(Boolean isRecursive, Boolean isSortByExt,
                                    String... folderName) throws LsException {
        boolean hasFolder = folderName.length > 0;

        if (folderName.length == 0 && !isRecursive) {
            return listCwdContent(isSortByExt);
        }

        if (folderName.length == 1) {
            List<Path> paths = resolvePaths(folderName);
            boolean isValidFolderName = paths.size() == 1 && paths.get(0).toFile().isFile();
            if (isValidFolderName) {
                // Returns file name if only one valid file is specified
                return paths.get(0).getFileName().toString();
            }
        }

        List<Path> paths;
        List<Path> files = new ArrayList<>();
        if (folderName.length == 0 && isRecursive) {
            String[] directories = new String[1];
            directories[0] = Environment.currentDirectory;
            paths = resolvePaths(directories);
        } else {
            paths = resolvePaths(folderName);
            resolvePaths(folderName).forEach(folder -> {
                if (!folder.toFile().isDirectory()) {
                    files.add(folder);
                }
            });
        }

        // End of output should not have newline
        String output = buildResult(paths, isRecursive, isSortByExt, hasFolder).trim();
        String formattedFiles = formatContents(files, isSortByExt);
        if (files.isEmpty()) {
            return output;
        }
        if (output.isEmpty()) {
            return formattedFiles;
        }
        return formattedFiles + STRING_NEWLINE + STRING_NEWLINE + output;
    }
}
