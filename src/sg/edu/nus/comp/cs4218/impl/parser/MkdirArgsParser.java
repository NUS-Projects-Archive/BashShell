package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class MkdirArgsParser extends ArgsParser {

    private static final char FLAG_CR_PARENT = 'p';

    public MkdirArgsParser() {
        super();
        legalFlags.add(FLAG_CR_PARENT);
    }

    public Boolean isCreateParent() {
        return flags.contains(FLAG_CR_PARENT);
    }

    public List<String> getDirectories() {
        return nonFlagArgs;
    }
}