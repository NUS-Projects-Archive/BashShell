package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class MkdirArgsParser extends ArgsParser {

    private final static char FLAG_IS_CREATE_PARENT = 'p';

    public MkdirArgsParser() {
        super();
        legalFlags.add(FLAG_IS_CREATE_PARENT);
    }

    public Boolean isCreateParent() {
        return flags.contains(FLAG_IS_CREATE_PARENT);
    }

    public List<String> getDirectories() {
        return nonFlagArgs;
    }
}