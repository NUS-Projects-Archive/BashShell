package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class LsArgsParser extends ArgsParser {
    private static final char FLAG_RECURSIVE = 'R';
    private static final char FLAG_SORT_BY_EXT = 'X';

    public LsArgsParser() {
        super();
        legalFlags.add(FLAG_RECURSIVE);
        legalFlags.add(FLAG_SORT_BY_EXT);
    }

    public Boolean isRecursive() {
        return flags.contains(FLAG_RECURSIVE);
    }

    public Boolean isSortByExt() {
        return flags.contains(FLAG_SORT_BY_EXT);
    }

    public List<String> getDirectories() {
        return nonFlagArgs;
    }
}
