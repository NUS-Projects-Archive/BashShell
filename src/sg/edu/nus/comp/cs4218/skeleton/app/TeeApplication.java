package sg.edu.nus.comp.cs4218.skeleton.app;

import java.io.InputStream;
import java.io.OutputStream;

import sg.edu.nus.comp.cs4218.app.TeeInterface;
import sg.edu.nus.comp.cs4218.exception.TeeException;

public class TeeApplication implements TeeInterface {

    @Override
    public String teeFromStdin(Boolean isAppend, InputStream stdin, String... fileName) throws TeeException {
        throw new TeeException("EF2 not implemented");
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws TeeException {
        throw new TeeException("EF2 not implemented");
    }
}
