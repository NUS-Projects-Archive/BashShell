package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

/**
 * LsArgsParser is a class to parse the arguments for ls command.
 */
public class LsArgsParser extends ArgsParser {
    private static final char FLAG_RECURSIVE = 'R';
    private static final char FLAG_SORT_BY_EXT = 'X';

    /**
     * Constructor for LsArgsParser. Initializes legal flags for ls command.
     */
    public LsArgsParser() {
        super();
        legalFlags.add(FLAG_RECURSIVE);
        legalFlags.add(FLAG_SORT_BY_EXT);
    }

    /**
     * Checks if the recursive flag is present in the parsed flags.
     * 
     * @return true if recursive flag is present, false otherwise
     */
    public Boolean isRecursive() {
        return flags.contains(FLAG_RECURSIVE);
    }

    /**
     * Checks if the sort by extension flag is present in the parsed flags.
     * 
     * @return true if sort by extension flag is present, false otherwise
     */
    public Boolean isSortByExt() {
        return flags.contains(FLAG_SORT_BY_EXT);
    }

    /**
     * Returns a list of directories to be listed.
     * @return List of directories
     */
    public List<String> getDirectories() {
        return nonFlagArgs;
    }
}
