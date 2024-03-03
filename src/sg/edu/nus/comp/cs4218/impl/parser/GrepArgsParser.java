package sg.edu.nus.comp.cs4218.impl.parser;

public class GrepArgsParser extends ArgsParser {
    private static final char FLAG_IS_INVERT = 'v';
    private static final int INDEX_PATTERN = 0;
    private static final int INDEX_FILES = 1;

    public GrepArgsParser() {
        super();
        legalFlags.add(FLAG_IS_INVERT);
    }

    public Boolean isInvert() {
        return flags.contains(FLAG_IS_INVERT);
    }

    public String getPattern() {
        // Bug fix
        return nonFlagArgs.isEmpty() ? null: nonFlagArgs.get(INDEX_PATTERN);
    }

    public String[] getFileNames() {
        return nonFlagArgs.size() <= 1 ? null : nonFlagArgs.subList(INDEX_FILES, nonFlagArgs.size())
                .toArray(new String[0]);
    }
}
