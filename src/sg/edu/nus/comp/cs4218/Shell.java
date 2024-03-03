package sg.edu.nus.comp.cs4218;

import java.io.FileNotFoundException;
import java.io.OutputStream;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

/**
 * The Shell interface is used to parse and evaluate user's command line.
 */
public interface Shell {

    /**
     * Parses and evaluates user's command line.
     */
    void parseAndEvaluate(String cmdline, OutputStream stdout) throws AbstractApplicationException, ShellException, FileNotFoundException;
}
