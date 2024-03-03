package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

/**
 * MkdirArgsParser is a class to parse the arguments for mkdir command.
 */
public class MkdirArgsParser extends ArgsParser {

    private static final char FLAG_CR_PARENT = 'p';

    /**
     * Constructor for MkdirArgsParser. Initializes legal flags for mkdir command.
     */
    public MkdirArgsParser() {
        super();
        legalFlags.add(FLAG_CR_PARENT);
    }

    /**
     * Checks if the create parent flag is present in the parsed flags.
     * 
     * @return true if create parent flag is present, false otherwise
     */
    public Boolean isCreateParent() {
        return flags.contains(FLAG_CR_PARENT);
    }

    /**
     * Returns a list of directories to be created.
     * 
     * @return List of directories
     */
    public List<String> getDirectories() {
        return nonFlagArgs;
    }
}
