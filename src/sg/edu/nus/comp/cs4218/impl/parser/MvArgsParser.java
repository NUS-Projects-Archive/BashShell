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

    /**
     * Returns a list of source directories to be moved to the destination directory.
     *
     * @return A list of string representing the source directories.
     */
    public List<String> getSourceDirectories() {
        return nonFlagArgs.subList(0, nonFlagArgs.size() - 1);
    }

    /**
     * Returns the destination directory where the source directories will be moved.
     *
     * @return A string representing the destination directory.
     */
    public String getDestinationDirectory() {
        return nonFlagArgs.get(nonFlagArgs.size() - 1);
    }
}
