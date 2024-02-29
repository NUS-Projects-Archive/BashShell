package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.LsInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.app.helper.LsApplicationHelper.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;

@SuppressWarnings("PMD.PreserveStackTrace")
public class LsApplication implements LsInterface {
    @Override
    public String listFolderContent(Boolean isRecursive, Boolean isSortByExt,
            String... folderName) throws LsException {
        boolean isFolderNameSpecified = folderName.length > 0;

        if (folderName.length == 0 && !isRecursive) {
            return listCwdContent(isSortByExt);
        }

        if (folderName.length == 1) {
            List<Path> paths = resolvePaths(folderName);
            boolean isFolderNameAValidFile = paths.size() == 1 && paths.get(0).toFile().isFile();
            if (isFolderNameAValidFile) {
                // Returns file name if only one valid file is specified
                return paths.get(0).getFileName().toString();
            }
        }

        List<Path> paths;
        if (folderName.length == 0 && isRecursive) {
            String[] directories = new String[1];
            directories[0] = Environment.currentDirectory;
            paths = resolvePaths(directories);
        } else {
            paths = resolvePaths(folderName);
        }

        // End of output should not have newline
        return buildResult(paths, isRecursive, isSortByExt, isFolderNameSpecified).trim();
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws LsException {
        if (args == null) {
            throw new LsException(ERR_NULL_ARGS);
        }

        if (stdout == null) {
            throw new LsException(ERR_NO_OSTREAM);
        }

        LsArgsParser parser = new LsArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new LsException(e.getMessage());
        }

        Boolean recursive = parser.isRecursive();
        Boolean sortByExt = parser.isSortByExt();
        String[] directories = parser.getDirectories()
                .toArray(new String[parser.getDirectories().size()]);
        String result = listFolderContent(recursive, sortByExt, directories);

        try {
            stdout.write(result.getBytes());
            stdout.write(StringUtils.STRING_NEWLINE.getBytes());
        } catch (Exception e) {
            throw new LsException(ERR_WRITE_STREAM);
        }
    }
}
