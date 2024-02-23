package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class WcArgsParser extends ArgsParser {
    public static final char FLAG_IS_BYTE_COUNT = 'c';

    public static final char FLAG_IS_LINE_COUNT = 'l';

    public static final char FLAG_IS_WORD_COUNT = 'w';

    public WcArgsParser() {
        super();

        legalFlags.add(FLAG_IS_BYTE_COUNT);
        legalFlags.add(FLAG_IS_LINE_COUNT);
        legalFlags.add(FLAG_IS_WORD_COUNT);
    }

    public Boolean isByteCount() {
        return hasNoFlags() || flags.contains(FLAG_IS_BYTE_COUNT);
    }

    public Boolean isLineCount() {
        return hasNoFlags() || flags.contains(FLAG_IS_LINE_COUNT);
    }

    public Boolean isWordCount() {
        return hasNoFlags() || flags.contains(FLAG_IS_WORD_COUNT);
    }

    public Boolean hasNoFlags() {
        return !flags.contains(FLAG_IS_BYTE_COUNT) && !flags.contains(FLAG_IS_LINE_COUNT)
                && !flags.contains(FLAG_IS_WORD_COUNT);
    }

    public List<String> getFileNames() {
        return nonFlagArgs;
    }

    public Boolean isStdinOnly() {
        return nonFlagArgs.isEmpty();
    }
}
