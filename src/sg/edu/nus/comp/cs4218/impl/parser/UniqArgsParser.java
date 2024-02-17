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
        }
        else {
            return inputFile;
        }
    }

    public String getOutputFileName() {
        return nonFlagArgs.size() == 2 ? nonFlagArgs.get(1) : null;
    }
}
