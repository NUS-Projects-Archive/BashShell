package sg.edu.nus.comp.cs4218.impl.cmd;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

/**
 * A Sequence Command is a sub-command consisting of two Commands separated with a semicolon.
 * <p>
 * Command format: <Command> ; <Command>
 */
public class SequenceCommand implements Command {

    private final List<Command> commands;

    public SequenceCommand(List<Command> commands) {
        this.commands = commands;
    }

    /**
     * Writes the order-preserved output of a series of sub commands to stdout, including exception messages if any.
     *
     * @param stdin  An InputStream. The first sub command processing an InputStream will be evaluated with this as its
     *               initial InputStream.
     * @param stdout An OutputStream for the order-preserved output of the sub commands to be written to.
     * @throws ExitException  If ExitException is thrown from any sub-commands. The ExitException is only thrown at the
     *                        end of execution, even if the exception did not come from the last sub-command.
     * @throws ShellException If an I/O exception occurs when writing to stdout.
     */
    @Override
    public void evaluate(InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException, ShellException, FileNotFoundException {
        ExitException exitException = null;

        for (Command command : commands) {
            try {
                OutputStream outputStream = new ByteArrayOutputStream();
                command.evaluate(stdin, outputStream);

                String outputLine = outputStream.toString();
                if (!outputLine.isEmpty()) {
                    write(stdout, outputLine);
                }
            } catch (ExitException e) {
                exitException = e;

            } catch (AbstractApplicationException | ShellException e) {
                write(stdout, e.getMessage() + STRING_NEWLINE);
            }
        }

        if (exitException != null) {
            throw exitException;
        }
    }

    public void write(OutputStream outputStream, String message) throws ShellException {
        try {
            outputStream.write(message.getBytes());
        } catch (IOException e) {
            throw new ShellException(ERR_WRITE_STREAM, e);
        }
    }

    @Override
    public void terminate() { /* Unused for now */}

    /**
     * Returns a list of commands.
     *
     * @return
     */
    public List<Command> getCommands() {
        return commands;
    }
}
