package sg.edu.nus.comp.cs4218.impl.app;

import java.io.InputStream;
import java.io.OutputStream;

import sg.edu.nus.comp.cs4218.app.ExitInterface;
import sg.edu.nus.comp.cs4218.exception.ExitException;

/**
 * The exit command terminates the execution.
 *
 * <p>
 * <b>Command format:</b> <code>exit</code>
 * </p>
 */
public class ExitApplication implements ExitInterface {

    /**
     * Runs the exit application.
     *
     * @param args   Array of arguments for the application, not used.
     * @param stdin  An InputStream, not used.
     * @param stdout An OutputStream, not used.
     * @throws ExitException Not used.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws ExitException {
        terminateExecution();
    }

    /**
     * Terminates shell.
     *
     * @throws ExitException Not used.
     */
    @Override
    public void terminateExecution() throws ExitException {
        System.exit(0);
    }
}
