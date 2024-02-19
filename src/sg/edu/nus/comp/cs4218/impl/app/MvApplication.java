package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;

import java.io.InputStream;
import java.io.OutputStream;

public class MvApplication implements MvInterface {

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {

    }

    @Override
    public String mvSrcFileToDestFile(Boolean isOverwrite, String srcFile, String destFile) throws AbstractApplicationException {
        return null;
    }

    @Override
    public String mvFilesToFolder(Boolean isOverwrite, String destFolder, String... fileName) throws AbstractApplicationException {
        return null;
    }
}
