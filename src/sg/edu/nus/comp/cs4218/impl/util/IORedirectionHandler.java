package sg.edu.nus.comp.cs4218.impl.util;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_OUTPUT;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

/**
 * IORedirectionHandler handles input and output redirection for a list of arguments.
 */
public class IORedirectionHandler {
    private final List<String> argsList;
    private final ArgumentResolver argumentResolver;
    private List<String> noRedirArgsList;
    private InputStream inputStream;
    private OutputStream outputStream;

    /**
     * Constructor for IORedirectionHandler.
     * 
     * @param argsList         List of arguments for the command
     * @param origInputStream  Original InputStream
     * @param origOutputStream Original OutputStream
     * @param argumentResolver ArgumentResolver to resolve the arguments
     */
    public IORedirectionHandler(List<String> argsList, InputStream origInputStream,
                                OutputStream origOutputStream, ArgumentResolver argumentResolver) {
        this.argsList = argsList;
        this.inputStream = origInputStream;
        this.outputStream = origOutputStream;
        this.argumentResolver = argumentResolver;
    }

    /**
     * Extracts redirection options from the arguments list. This method identifies and processes
     * input and output redirection operators ("<" and ">") in the arguments list. It replaces
     * the existing input and output streams with new streams based on the specified files.
     * 
     * This method also handles quoting, globing, and command substitution in the file argument.
     * If the file argument resolves to more than one parsed argument, it throws a ShellException
     * for ambiguous redirect.
     *
     * @throws AbstractApplicationException If an application-specific exception occurs.
     * @throws ShellException If a shell-specific exception occurs, such as invalid syntax or ambiguous redirect.
     * @throws FileNotFoundException If an operation attempts to open a file that does not exist.
     */
    public void extractRedirOptions() throws AbstractApplicationException, ShellException, FileNotFoundException {
        if (argsList == null || argsList.isEmpty()) {
            throw new ShellException(ERR_SYNTAX);
        }

        noRedirArgsList = new LinkedList<>();

        // extract redirection operators (with their corresponding files) from argsList
        ListIterator<String> argsIterator = argsList.listIterator();
        while (argsIterator.hasNext()) {
            String arg = argsIterator.next();

            // leave the other args untouched
            if (!isRedirOperator(arg)) {
                noRedirArgsList.add(arg);
                continue;
            }

            // if current arg is < or >, fast-forward to the next arg to extract the specified file
            String file = argsIterator.next();

            if (isRedirOperator(file)) {
                throw new ShellException(ERR_SYNTAX);
            }

            // handle quoting + globing + command substitution in file arg
            List<String> fileSegment = argumentResolver.resolveOneArgument(file);
            if (fileSegment.size() > 1) {
                // ambiguous redirect if file resolves to more than one parsed arg
                throw new ShellException(ERR_SYNTAX);
            }
            file = fileSegment.get(0);

            // replace existing inputStream / outputStream
            if (arg.equals(String.valueOf(CHAR_REDIR_INPUT))) {
                IOUtils.closeInputStream(inputStream);
                inputStream = IOUtils.openInputStream(file);
            } else if (arg.equals(String.valueOf(CHAR_REDIR_OUTPUT))) {
                IOUtils.closeOutputStream(outputStream);
                outputStream = IOUtils.openOutputStream(file);
            }
        }
    }

    /**
     * Returns the list of arguments for the command without redirection operators.
     * 
     * @return
     */
    public List<String> getNoRedirArgsList() {
        return noRedirArgsList;
    }

    /**
     * Returns the InputStream.
     * 
     * @return
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Returns the OutputStream.
     * 
     * @return
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    private boolean isRedirOperator(String str) {
        return str.equals(String.valueOf(CHAR_REDIR_INPUT)) || str.equals(String.valueOf(CHAR_REDIR_OUTPUT));
    }
}
