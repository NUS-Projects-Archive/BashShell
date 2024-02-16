package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Application;
import sg.edu.nus.comp.cs4218.app.MkdirInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.exception.MkdirException;
import sg.edu.nus.comp.cs4218.impl.parser.MkdirArgsParser;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

public class MkdirApplication implements MkdirInterface {
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        if (args == null) {
            throw new MkdirException(ERR_NULL_ARGS);
        }

        System.out.print("args: " + Arrays.toString(args));

        MkdirArgsParser parser = new MkdirArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new MkdirException(e.getMessage());
        }

        boolean isCreateParentFolder = parser.isCreateParentFolder();
        String[] directories = parser.getDirectories()
                .toArray(new String[parser.getDirectories().size()]);
        if (isCreateParentFolder) {
            createFolderWithParents(directories);
        } else {
            createFolder(directories);
        }
    }

    @Override
    public void createFolder(String... folderName) throws AbstractApplicationException {
        for (String folder : folderName) {
            File file = new File(folder);
            file.mkdir();
        }
    }

    public void createFolderWithParents(String... folderName) throws AbstractApplicationException {
        for (String folder : folderName) {
            File file = new File(folder);
            file.mkdirs();
        }
    }
}
