package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class SortArgsParser extends ArgsParser {
    public static final char FLAG_FIRST_NUM = 'n';
    public static final char FLAG_REV_ORDER = 'r';
    public static final char FLAG_CASE_IGNORE = 'f';
    private final static int INDEX_FILES = 0;

    public SortArgsParser() {
        super();
        legalFlags.add(FLAG_FIRST_NUM);
        legalFlags.add(FLAG_REV_ORDER);
        legalFlags.add(FLAG_CASE_IGNORE);
    }

    public Boolean isFirstWordNumber() {
        return flags.contains(FLAG_FIRST_NUM);
    }

    public Boolean isReverseOrder() {
        return flags.contains(FLAG_REV_ORDER);
    }

    public Boolean isCaseIndependent() {
        return flags.contains(FLAG_CASE_IGNORE);
    }

    public List<String> getFileNames() {
        return nonFlagArgs;
    }
}
