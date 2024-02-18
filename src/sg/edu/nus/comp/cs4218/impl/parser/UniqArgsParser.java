package sg.edu.nus.comp.cs4218.impl.parser;

public class UniqArgsParser extends ArgsParser {
    public static final char FLAG_IS_COUNT_OCCUR = 'c';
    public static final char FLAG_IS_DUPLICATE_GRP = 'd';
    public static final char FLAG_IS_DUPLICATE = 'D';
    public static final char NO_IN_FILE = '-';

    public UniqArgsParser() {
        super();
        legalFlags.add(FLAG_IS_COUNT_OCCUR);
        legalFlags.add(FLAG_IS_DUPLICATE_GRP);
        legalFlags.add(FLAG_IS_DUPLICATE);
    }

    public Boolean isPrefixWithOccurrencesCount() {
        return flags.contains(FLAG_IS_COUNT_OCCUR);
    }

    public Boolean isPrintDuplicateOncePerGroup() {
        return flags.contains(FLAG_IS_DUPLICATE_GRP);
    }

    public Boolean isPrintAllDuplicate() {
        return flags.contains(FLAG_IS_DUPLICATE);
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
