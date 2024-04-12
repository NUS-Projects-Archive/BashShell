package sg.edu.nus.comp.cs4218.impl.util;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_ASTERISK;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_BACK_QUOTE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_DOUBLE_QUOTE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_SINGLE_QUOTE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_SPACE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

/**
 * ArgumentResolver handles quoting, globing, and command substitution for a list of arguments.
 */
// Suppressed as subCommand is always reinitialised before use in resolveOneArgument
@SuppressWarnings("PMD.AvoidStringBufferField")
public class ArgumentResolver {

    private final ApplicationRunner applicationRunner;
    private Stack<Character> unmatchedQuotes;
    private LinkedList<RegexArgument> parsedArgsSegment;
    private RegexArgument parsedArg;
    private StringBuilder subCommand;

    /**
     * Constructor for ArgumentResolver. Initializes applicationRunner.
     */
    public ArgumentResolver() {
        applicationRunner = new ApplicationRunner();
    }

    /**
     * Returns the ApplicationRunner.
     * 
     * @return ApplicationRunner
     */
    public ApplicationRunner getAppRunner() {
        return applicationRunner;
    }

    /**
     * Handles quoting + globing + command substitution for a list of arguments.
     *
     * @param argsList The original list of arguments
     * @return The list of parsed arguments
     * @throws ShellException If any of the arguments have an invalid syntax
     */
    public List<String> parseArguments(List<String> argsList) throws AbstractApplicationException, ShellException,
            FileNotFoundException {
        List<String> parsedArgsList = new LinkedList<>();
        for (String arg : argsList) {
            parsedArgsList.addAll(resolveOneArgument(arg));
        }
        return parsedArgsList;
    }

    /**
     * Unwraps single and double quotes from one argument.
     * Performs globing when there are unquoted asterisks.
     * Performs command substitution.
     * <p>
     * Single quotes disable the interpretation of all special characters.
     * Double quotes disable the interpretation of all special characters, except for back quotes.
     *
     * @param arg String containing one argument
     * @return A list containing one or more parsed args, depending on the outcome of the parsing
     */
    public List<String> resolveOneArgument(String arg) throws AbstractApplicationException, ShellException,
            FileNotFoundException {
        unmatchedQuotes = new Stack<>();
        parsedArgsSegment = new LinkedList<>();
        parsedArg = makeRegexArgument();
        subCommand = new StringBuilder();

        for (int i = 0; i < arg.length(); i++) {
            char chr = arg.charAt(i);

            if (chr == CHAR_BACK_QUOTE) {
                handleBackQuote(chr);
            } else if (chr == CHAR_SINGLE_QUOTE || chr == CHAR_DOUBLE_QUOTE) {
                handleSingleAndDoubleQuote(chr);
            } else if (chr == CHAR_ASTERISK) {
                handleAsterisk(chr);
            } else {
                handleOthers(chr);
            }
        }

        // should not have unmatched backquotes or double quotes within double quotes
        if (!unmatchedQuotes.isEmpty() && unmatchedQuotes.peek() == CHAR_BACK_QUOTE) {
            throw new ShellException(ERR_SYNTAX);
        }

        if (!parsedArg.isEmpty()) {
            appendParsedArgIntoSegment(parsedArgsSegment, parsedArg);
        }

        // perform globing
        return parsedArgsSegment.stream()
                .flatMap(regexArgument -> regexArgument.globFiles().stream())
                .collect(Collectors.toList());
    }

    private void handleBackQuote(char chr) throws FileNotFoundException, AbstractApplicationException, ShellException {
        if (unmatchedQuotes.isEmpty() || unmatchedQuotes.peek() == CHAR_DOUBLE_QUOTE) {
            // start of command substitution
            if (!parsedArg.isEmpty()) {
                appendParsedArgIntoSegment(parsedArgsSegment, parsedArg);
                parsedArg = makeRegexArgument();
            }

            unmatchedQuotes.add(chr);

        } else if (unmatchedQuotes.peek() == chr) {
            // end of command substitution
            unmatchedQuotes.pop();

            // evaluate subCommand and get the output
            String subCommandOutput = evaluateSubCommand(subCommand.toString());
            subCommand.setLength(0); // Clear the previous subCommand registered

            // check if back quotes are nested
            if (unmatchedQuotes.isEmpty()) {
                List<RegexArgument> subOutputSegment = Stream
                        .of(StringUtils.tokenize(subCommandOutput))
                        .map(str -> makeRegexArgument(str))
                        .collect(Collectors.toList());

                // append the first token to the previous parsedArg
                // e.g. arg: abc`1 2 3`xyz`4 5 6` (contents in `` is after command sub)
                // expected: [abc1, 2, 3xyz4, 5, 6]
                if (!subOutputSegment.isEmpty()) {
                    RegexArgument firstOutputArg = subOutputSegment.remove(0);
                    appendParsedArgIntoSegment(parsedArgsSegment, firstOutputArg);
                }
                // add remaining tokens to parsedArgsSegment
                parsedArgsSegment.addAll(new ArrayList<>(subOutputSegment));

            } else {
                // don't tokenize subCommand output
                appendParsedArgIntoSegment(parsedArgsSegment, makeRegexArgument(subCommandOutput));
            }
        } else {
            // ongoing single quote
            parsedArg.append(chr);
        }
    }

    private void handleSingleAndDoubleQuote(char chr) {
        if (unmatchedQuotes.isEmpty()) {
            // start of quote
            unmatchedQuotes.add(chr);
        } else if (unmatchedQuotes.peek() == chr) {
            // end of quote
            unmatchedQuotes.pop();

            // make sure parsedArgsSegment is not empty
            appendParsedArgIntoSegment(parsedArgsSegment, makeRegexArgument());
        } else if (unmatchedQuotes.peek() == CHAR_BACK_QUOTE) {
            // ongoing back quote: add chr to subCommand
            subCommand.append(chr);
        } else {
            // ongoing single/double quote
            parsedArg.append(chr);
        }
    }

    private void handleAsterisk(char chr) {
        if (unmatchedQuotes.isEmpty()) {
            // each unquoted * matches a (possibly empty) sequence of non-slash chars
            parsedArg.appendAsterisk();
        } else if (unmatchedQuotes.peek() == CHAR_BACK_QUOTE) {
            // ongoing back quote: add chr to subCommand
            subCommand.append(chr);
        } else {
            // ongoing single/double quote
            parsedArg.append(chr);
        }
    }

    private void handleOthers(char chr) {
        if (unmatchedQuotes.isEmpty()) {
            // not a special character
            parsedArg.append(chr);
        } else if (unmatchedQuotes.peek() == CHAR_BACK_QUOTE) {
            // ongoing back quote: add chr to subCommand
            subCommand.append(chr);
        } else {
            // ongoing single/double quote
            parsedArg.append(chr);
        }
    }

    /**
     * Returns a new RegexArgument.
     * 
     * @return RegexArgument
     */
    public RegexArgument makeRegexArgument() {
        return new RegexArgument();
    }

    /**
     * Returns a new RegexArgument with the given string.
     * 
     * @param str String to be set in the new RegexArgument
     * @return RegexArgument
     */
    public RegexArgument makeRegexArgument(String str) {
        return new RegexArgument(str);
    }

    private String evaluateSubCommand(String commandString) throws AbstractApplicationException, ShellException,
            FileNotFoundException {
        if (StringUtils.isBlank(commandString)) {
            return "";
        }

        OutputStream outputStream = new ByteArrayOutputStream();
        Command command = CommandBuilder.parseCommand(commandString, getAppRunner());
        command.evaluate(System.in, outputStream);

        String result = removeTrailingLineSeparator(outputStream.toString());
        // replace newlines with spaces
        return result.replace(STRING_NEWLINE, String.valueOf(CHAR_SPACE));
    }

    /**
     * Removes trailing line separators (if any) from the end of a string input and returns the result
     *
     * @param str   String input to remove trailing line separators from
     */
    public String removeTrailingLineSeparator(String str) {
        int length = str.length();
        int lineSepLength = STRING_NEWLINE.length();

        // Find the index of the last character that is not a line separator
        while (length >= lineSepLength && str.substring(length - lineSepLength, length).equals(STRING_NEWLINE)) {
            length -= lineSepLength;
        }

        // Return the substring up to the last non-line separator character
        return str.substring(0, length);
    }

    /**
     * Append current parsedArg to the last parsedArg in parsedArgsSegment.
     * If parsedArgsSegment is empty, then just add current parsedArg.
     */
    private void appendParsedArgIntoSegment(LinkedList<RegexArgument> parsedArgsSegment,
                                            RegexArgument parsedArg) {
        if (parsedArgsSegment.isEmpty()) {
            parsedArgsSegment.add(parsedArg);
        } else {
            RegexArgument lastParsedArg = parsedArgsSegment.removeLast();
            parsedArgsSegment.add(lastParsedArg);
            lastParsedArg.merge(parsedArg);
        }
    }
}
