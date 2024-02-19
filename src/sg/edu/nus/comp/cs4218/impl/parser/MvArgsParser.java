package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class MvArgsParser extends ArgsParser {

    private final static char FLAG_NO_OVERWRITE = 'n';

    public MvArgsParser() {
        super();
        legalFlags.add(FLAG_NO_OVERWRITE);
    }

    public Boolean isOverwrite() {
        return flags.contains(FLAG_NO_OVERWRITE);
    }

    public List<String> getSourceDirectories() {
        return nonFlagArgs.subList(0, nonFlagArgs.size() - 1);
    }

    public String getDestinationDirectory() {
        return nonFlagArgs.get(nonFlagArgs.size() - 1);
    }
}
