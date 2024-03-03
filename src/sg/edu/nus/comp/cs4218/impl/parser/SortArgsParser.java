package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

/**
 * SortArgsParser is a class to parse the arguments for sort command.
 */
public class SortArgsParser extends ArgsParser {
    public static final char FLAG_FIRST_NUM = 'n';
    public static final char FLAG_REV_ORDER = 'r';
    public static final char FLAG_CASE_IGNORE = 'f';

    /**
     * Constructor for SortArgsParser. Initializes legal flags for sort command.
     */
    public SortArgsParser() {
        super();
        legalFlags.add(FLAG_FIRST_NUM);
        legalFlags.add(FLAG_REV_ORDER);
        legalFlags.add(FLAG_CASE_IGNORE);
    }

    /**
     * Checks if first word is a number flag is present in the parsed flags.
     * 
     * @return true if first word is a number flag is present, false otherwise
     */
    public Boolean isFirstWordNumber() {
        return flags.contains(FLAG_FIRST_NUM);
    }

    /**
     * Checks if reverse order flag is present in the parsed flags.
     * 
     * @return true if reverse order flag is present, false otherwise
     */
    public Boolean isReverseOrder() {
        return flags.contains(FLAG_REV_ORDER);
    }

    /**
     * Checks if case insensitive flag is present in the parsed flags.
     * 
     * @return true if case insensitive flag is present, false otherwise
     */
    public Boolean isCaseIndependent() {
        return flags.contains(FLAG_CASE_IGNORE);
    }

    /**
     * Returns a list of file names to be sorted.
     * 
     * @return List of file names
     */
    public List<String> getFileNames() {
        return nonFlagArgs;
    }
}
