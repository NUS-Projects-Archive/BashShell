package sg.edu.nus.comp.cs4218.impl.parser;

/**
 * UniqArgsParser is a class to parse the arguments for uniq command.
 */
public class UniqArgsParser extends ArgsParser {
    public static final char FLAG_COUNT_OCCUR = 'c';
    public static final char FLAG_DUP_GRP = 'd';
    public static final char FLAG_DUPLICATE = 'D';
    public static final char NO_IN_FILE = '-';

    /**
     * Constructor for UniqArgsParser. Initializes legal flags for uniq command.
     */
    public UniqArgsParser() {
        super();
        legalFlags.add(FLAG_COUNT_OCCUR);
        legalFlags.add(FLAG_DUP_GRP);
        legalFlags.add(FLAG_DUPLICATE);
    }

    /**
     * Checks if the prefix lines by the number of occurrences of adjacent duplicate lines flag
     * is present in the parsed flags.
     * 
     * @return true if prefix lines by the number of occurrences of adjacent duplicate lines flag is present, 
     * false otherwise
     */
    public Boolean isPrefixWithOccurrencesCount() {
        return flags.contains(FLAG_COUNT_OCCUR);
    }

    public Boolean isPrintDuplicateOncePerGroup() {
        return flags.contains(FLAG_DUP_GRP);
    }

    public Boolean isPrintAllDuplicate() {
        return flags.contains(FLAG_DUPLICATE);
    }

    /**
     * Returns input file name
     *
     * @return [String|null] name of input file if valid, otherwise return null
     * File is invalid if (1) file name is not specified or (2) file name is given as '-'
     */
    public String getInputFileName() {
        if (nonFlagArgs.isEmpty()) {
            return null;
        }

        final String inputFile = nonFlagArgs.get(0);

        if (inputFile.length() > 1) {
            return inputFile;
        }

        if (inputFile.charAt(0) == NO_IN_FILE) {
            return null;
        } else {
            return inputFile;
        }
    }

    /**
     * Returns output file name
     *
     * @return [String|null] name of output file if specified, otherwise return null
     */
    public String getOutputFileName() {
        // Only consider the 2nd non-flag argument as output file, even if more arguments are provided
        return nonFlagArgs.size() >= 2 ? nonFlagArgs.get(1) : null;
    }
}
