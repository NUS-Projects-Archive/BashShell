package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

/**
 * WcArgsParser is a class to parse the arguments for wc command.
 */
public class WcArgsParser extends ArgsParser {
    public static final char FLAG_BYTE_COUNT = 'c';

    public static final char FLAG_LINE_COUNT = 'l';

    public static final char FLAG_WORD_COUNT = 'w';

    /**
     * Constructor for WcArgsParser. Initializes legal flags for wc command.
     */
    public WcArgsParser() {
        super();
        legalFlags.add(FLAG_BYTE_COUNT);
        legalFlags.add(FLAG_LINE_COUNT);
        legalFlags.add(FLAG_WORD_COUNT);
    }

    /**
     * Checks if the byte count flag is present in the parsed flags or if no flags are present.
     * 
     * @return true if byte count flag is present or no flags are present, false otherwise
     */
    public Boolean isByteCount() {
        return hasNoFlags() || flags.contains(FLAG_BYTE_COUNT);
    }

    /**
     * Checks if the line count flag is present in the parsed flags or if no flags are present.
     * 
     * @return true if line count flag is present or no flags are present, false otherwise
     */
    public Boolean isLineCount() {
        return hasNoFlags() || flags.contains(FLAG_LINE_COUNT);
    }

    /**
     * Checks if the word count flag is present in the parsed flags or if no flags are present.
     * 
     * @return true if word count flag is present or no flags are present, false otherwise
     */
    public Boolean isWordCount() {
        return hasNoFlags() || flags.contains(FLAG_WORD_COUNT);
    }

    /**
     * Checks if no valid wc flags are present in the parsed flags.
     * 
     * @return true if no valid wc flags are present, false otherwise
     */
    public Boolean hasNoFlags() {
        return !flags.contains(FLAG_BYTE_COUNT) && !flags.contains(FLAG_LINE_COUNT)
                && !flags.contains(FLAG_WORD_COUNT);
    }

    /**
     * Returns the non-flag arguments as a list of file names.
     * 
     * @return List of file names
     */
    public List<String> getFileNames() {
        return nonFlagArgs;
    }

    /**
     * Checks if the command is to be executed on stdin only using the presence of non-flag arguments.
     * 
     * @return true if command is to be executed on stdin only, false otherwise
     */
    public Boolean isStdinOnly() {
        return nonFlagArgs.isEmpty();
    }
}
