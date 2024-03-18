package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

/**
 * PasteArgsParser is a class to parse the arguments for paste command.
 */
public class PasteArgsParser extends ArgsParser {

    public static final char FLAG_SERIAL = 's';

    /**
     * Constructor for PasteArgsParser. Initializes legal flags for paste command.
     */
    public PasteArgsParser() {
        super();
        legalFlags.add(FLAG_SERIAL);
    }

    /**
     * Checks if the serial flag is present in the parsed flags.
     *
     * @return true if serial flag is present, false otherwise
     */
    public Boolean isSerial() {
        return flags.contains(FLAG_SERIAL);
    }

    /**
     * Returns the non-flag arguments.
     *
     * @return List of non-flag arguments
     */
    public List<String> getNonFlagArgs() {
        return nonFlagArgs;
    }
}
