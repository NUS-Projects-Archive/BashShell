package sg.edu.nus.comp.cs4218.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.Shell;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.app.ExitApplication;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.CommandBuilder;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

/**
 * The Shell is ann application that reads lines from a user, evaluates the command, and prints the result.
 * It is similar to the UNIX shell.
 */
public class ShellImpl implements Shell {

    /**
     * Main method for the Shell Interpreter program.
     *
     * @param args List of strings arguments, unused.
     */
    public static void main(String... args) {
        String commandString = null;
        Shell shell = new ShellImpl();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

            // Forever loop to maintain shell until ExitCommand
            while (true) {
                try {
                    System.out.print(Environment.currentDirectory + "$ ");

                    // Read input from user
                    try {
                        commandString = reader.readLine();
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        System.exit(1); // Streams are closed, terminate process with non-zero exit code
                    }

                    // Exit loop if Ctrl+D or EOF is encountered
                    if (commandString == null) {
                        new ExitApplication().terminateExecution(); // Let ExitApplication terminate gracefully
                    }

                    // Process given input
                    if (!StringUtils.isBlank(commandString)) {
                        shell.parseAndEvaluate(commandString, System.out);
                    }
                } catch (SecurityException e) { // This is to catch SystemLambda$CheckExitCalled when under test
                    return;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void parseAndEvaluate(String commandString, OutputStream stdout)
            throws AbstractApplicationException, ShellException, FileNotFoundException {
        Command command = CommandBuilder.parseCommand(commandString, new ApplicationRunner());
        command.evaluate(System.in, stdout);
    }
}
