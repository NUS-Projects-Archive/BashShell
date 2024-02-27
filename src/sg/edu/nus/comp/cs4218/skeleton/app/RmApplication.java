package sg.edu.nus.comp.cs4218.skeleton.app;

import java.io.InputStream;
import java.io.OutputStream;

import sg.edu.nus.comp.cs4218.app.RmInterface;
import sg.edu.nus.comp.cs4218.exception.RmException;

public class RmApplication implements RmInterface {

    @Override
    public void remove(Boolean isEmptyFolder, Boolean isRecursive, String... fileName) throws RmException {
        throw new RmException("EF2 not implemented");

    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws RmException {
        throw new RmException("EF2 not implemented");
    }
}
