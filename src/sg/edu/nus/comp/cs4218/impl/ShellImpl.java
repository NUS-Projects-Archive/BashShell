package sg.edu.nus.comp.cs4218.impl;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.Shell;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.app.ExitApplication;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.CommandBuilder;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ShellImpl implements Shell {

    /**
     * Main method for the Shell Interpreter program.
     *
     * @param args List of strings arguments, unused.
     */
    public static void main(String... args) {
        String commandString;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Shell shell = new ShellImpl();

        // Forever loop to maintain shell until ExitCommand
        while (true) {

            try {
                String currentDirectory = Environment.currentDirectory;
                String commandString;
                System.out.print("> ");

                // Read inout from user
                try {
                    commandString = reader.readLine();
                } catch (IOException e) {
                    System.exit(1); // Streams are closed, terminate process with non-zero exit code
                    return;
                }

                // Exit loop if Ctrl+D or EOF is encountered
                if (commandString == null) {
                    new ExitApplication().terminateExecution(); // Let ExitApplication terminate gracefully
                }

                // Process given input
                if (!StringUtils.isBlank(commandString)) {
                    shell.parseAndEvaluate(commandString, System.out);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }
    }

    @Override
    public void parseAndEvaluate(String commandString, OutputStream stdout)
            throws AbstractApplicationException, ShellException, FileNotFoundException {
        Command command = CommandBuilder.parseCommand(commandString, new ApplicationRunner());
        command.evaluate(System.in, stdout);
    }
}
