package sg.edu.nus.comp.cs4218;

import java.io.InputStream;
import java.io.OutputStream;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;

/**
 * The Application is a common interface for all applications such as cat, echo, etc.
 */
public interface Application {

    /**
     * Runs application with specified input data and specified output stream.
     */
    void run(String[] args, InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException;

}
