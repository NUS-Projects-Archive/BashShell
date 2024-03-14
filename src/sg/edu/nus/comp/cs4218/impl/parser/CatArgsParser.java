package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

/**
 * CatArgsParser is a class to parse the arguments for cat command.
 *
 * <p>
 * <b>Command format:</b> cat [OPTIONS] [FILE]
 * </p>
 */
public class CatArgsParser extends ArgsParser {

    private static final char FLAG_LINE_NUMBER = 'n';

    /**
     * Constructor for CatArgsParser. Initializes legal flags for cat command.
     */
    public CatArgsParser() {
        super();
        legalFlags.add(FLAG_LINE_NUMBER);
    }

    /**
     * Checks if the line number flag is present in the parsed flags.
     *
     * @return true if line number flag is present, false otherwise
     */
    public Boolean isLineNumber() {
        return flags.contains(FLAG_LINE_NUMBER);
    }

    /**
     * Returns the non-flag arguments (files).
     *
     * @return List of non-flag arguments (files)
     */
    public List<String> getFiles() {
        return nonFlagArgs;
    }
}
