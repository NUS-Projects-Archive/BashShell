package sg.edu.nus.comp.cs4218.impl.cmd;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

/**
 * A Call Command is a sub-command consisting of at least one non-keyword or quoted.
 * <p>
 * Command format: (<non-keyword> or <quoted>) *
 */

public class CallCommand implements Command {
    private final List<String> argsList;
    private final ApplicationRunner appRunner;
    private final ArgumentResolver argumentResolver;

    /**
     * Constructor for CallCommand.
     * 
     * @param argsList          List of arguments for the command
     * @param appRunner         ApplicationRunner to run the command
     * @param argumentResolver  ArgumentResolver to resolve the arguments
     */
    public CallCommand(List<String> argsList, ApplicationRunner appRunner, ArgumentResolver argumentResolver) {
        this.argsList = argsList;
        this.appRunner = appRunner;
        this.argumentResolver = argumentResolver;
    }

    /**
     * Parses the sub-command's argsList to identify the redirected InputStream, redirected OutputStream, and actual
     * list of args after accounting for possible changes from quoting, globbing, and substitution.
     * <p>
     * Starts the call command's ApplicationRunner.
     *
     * @param stdin  An InputStream. If there is no input redirection, the call command's ApplicationRunner will be
     *               provided with the same InputStream as stdin.
     * @param stdout An OutputStream. If there is no output redirection, the call command's ApplicationRunner will be
     *               provided with the same OutputStream as stdout.
     * @throws ShellException If argsList attribute is null or empty.
     */
    @SuppressWarnings("PMD.CloseResource")
    @Override
    public void evaluate(InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException, ShellException, FileNotFoundException {
        if (argsList == null || argsList.isEmpty()) {
            throw new ShellException(ERR_SYNTAX);
        }

        // Handle IO redirection
        IORedirectionHandler redirHandler = new IORedirectionHandler(argsList, stdin, stdout, argumentResolver);
        redirHandler.extractRedirOptions();
        List<String> noRedirArgsList = redirHandler.getNoRedirArgsList();
        InputStream inputStream = redirHandler.getInputStream();
        OutputStream outputStream = redirHandler.getOutputStream();

        // Handle quoting + globing + command substitution
        List<String> parsedArgsList = argumentResolver.parseArguments(noRedirArgsList);
        if (!parsedArgsList.isEmpty()) {
            String app = parsedArgsList.remove(0);
            appRunner.runApp(app, parsedArgsList.toArray(new String[0]), inputStream, outputStream);
        }

        IOUtils.closeInputStream(inputStream);
        IOUtils.closeOutputStream(outputStream);
    }

    @Override
    public void terminate() { /* Unused for now */}

    /**
     * Returns the list of arguments for the command.
     * 
     * @return
     */
    public List<String> getArgsList() {
        return argsList;
    }
}
