package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class CatArgsParser extends ArgsParser {
    private final static char FLAG_IS_LINE_NUMBER = 'n';

    public CatArgsParser() {
        super();
        legalFlags.add(FLAG_IS_LINE_NUMBER);
    }

    public Boolean isLineNumber() {
        return flags.contains(FLAG_IS_LINE_NUMBER);
    }

    public List<String> getNonFlagArgs() {
        return nonFlagArgs;
    }

}
