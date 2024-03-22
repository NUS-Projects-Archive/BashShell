package sg.edu.nus.comp.cs4218.impl.cmd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

/**
 * A Pipe Command is a sub-command consisting of two Call Commands separated with a pipe,
 * or a Pipe Command and a Call Command separated with a pipe.
 * <p>
 * Command format: <Call> | <Call> or <Pipe> | <Call>
 */
public class PipeCommand implements Command {
    private final List<CallCommand> callCommands;

    /**
     * Constructor to create a new PipeCommand. The constructor creates a list of CallCommand.
     * 
     * @param callCommands  List of CallCommand
     */
    public PipeCommand(final List<CallCommand> callCommands) {
        this.callCommands = callCommands;
    }

    /**
     * Evaluates a sequence of piped commands. Each command in the sequence is executed in order,
     * with the output of one command serving as the input to the next. If any command in the sequence
     * throws an exception, the remaining commands are terminated.
     *
     * @param stdin The input stream to be used as the input for the first command in the sequence.
     * @param stdout The output stream to be used as the output for the last command in the sequence.
     * @throws AbstractApplicationException If any command in the sequence throws an AbstractApplicationException,
     *                                      this exception is thrown at the end of this method.
     * @throws ShellException If any command in the sequence throws a ShellException,
     *                        this exception is thrown at the end of this method.
     * @throws FileNotFoundException If an operation attempts to open a file that does not exist.
     */
    @SuppressWarnings("PMD.CloseResource")
    @Override
    public void evaluate(final InputStream stdin, final OutputStream stdout)
            throws AbstractApplicationException, ShellException, FileNotFoundException {
        AbstractApplicationException absAppException = null;
        ShellException shellException = null;

        InputStream nextInputStream = stdin;
        OutputStream nextOutputStream = null;

        for (int i = 0; i < callCommands.size(); i++) {
            final CallCommand callCommand = callCommands.get(i);

            if (absAppException != null || shellException != null) {
                continue;
            }

            try {
                nextOutputStream = new ByteArrayOutputStream();
                if (i == callCommands.size() - 1) {
                    nextOutputStream = stdout;
                }
                callCommand.evaluate(nextInputStream, nextOutputStream);
                if (i != callCommands.size() - 1) {
                    nextInputStream = new ByteArrayInputStream(((ByteArrayOutputStream) nextOutputStream).toByteArray());
                }
            } catch (AbstractApplicationException e) {
                absAppException = e;
            } catch (ShellException e) {
                shellException = e;
            }
        }

        IOUtils.closeInputStream(nextInputStream);
        IOUtils.closeOutputStream(nextOutputStream);

        if (absAppException != null) {
            throw absAppException;
        }
        if (shellException != null) {
            throw shellException;
        }
    }

    @Override
    public void terminate() { /* Unused for now */}

    /**
     * Returns the list of CallCommand.
     * 
     * @return
     */
    public List<CallCommand> getCallCommands() {
        return callCommands;
    }
}
