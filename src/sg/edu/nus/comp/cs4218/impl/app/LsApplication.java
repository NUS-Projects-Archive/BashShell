package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.app.helper.LsApplicationHelper.buildResult;
import static sg.edu.nus.comp.cs4218.impl.app.helper.LsApplicationHelper.formatContents;
import static sg.edu.nus.comp.cs4218.impl.app.helper.LsApplicationHelper.listCwdContent;
import static sg.edu.nus.comp.cs4218.impl.app.helper.LsApplicationHelper.resolvePaths;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;

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
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

public class LsApplication implements LsInterface {
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
            List<Path> directories = new ArrayList<>();
            resolvePaths(folderName).forEach(folder -> {
                if (folder.toFile().isDirectory()) {
                    directories.add(folder);
                } else {
                    files.add(folder);
                }
            });
            paths = directories;
        }

        // End of output should not have newline
        String output = buildResult(paths, isRecursive, isSortByExt, hasFolder).trim();
        if (files.isEmpty()) {
            return output;
        }

        String formattedFiles = formatContents(files, isSortByExt);
        if (output.isEmpty()) {
            return formattedFiles;
        }

        return formattedFiles + StringUtils.STRING_NEWLINE + StringUtils.STRING_NEWLINE + output;
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
            throw new LsException(e.getMessage(), e);
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
            throw new LsException(ERR_WRITE_STREAM, e);
        }
    }
}
