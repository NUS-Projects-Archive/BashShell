package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class RmArgsParser extends ArgsParser {

    public static final char FLAG_RECURSIVE = 'r';
    public static final char FLAG_EMPTY_DIR = 'd';

    /**
     * Constructor for RmArgsParser. Initializes legal flags for rm command.
     */
    public RmArgsParser() {
        super();
        legalFlags.add(FLAG_RECURSIVE);
        legalFlags.add(FLAG_EMPTY_DIR);
    }

    /**
     * Check if rm command should traverse recursively to delete directories and their contents
     *
     * @return true if rm should behave recursively, otherwise false
     */
    public boolean isRecursive() {
        return flags.contains(FLAG_RECURSIVE);
    }

    /**
     * Check if rm command should remove empty directories
     *
     * @return true of rm should remove empty directories, otherwise false
     */
    public boolean isEmptyDirectory() {
        return flags.contains(FLAG_EMPTY_DIR);
    }

    /**
     * Returns a list of files to be removed.
     *
     * @return List of files
     */
    public List<String> getFiles() {
        return nonFlagArgs;
    }
}
