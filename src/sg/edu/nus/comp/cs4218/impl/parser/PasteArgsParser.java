package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class PasteArgsParser extends ArgsParser {

    public static final char FLAG_SERIAL = 's';

    public PasteArgsParser() {
        super();
        legalFlags.add(FLAG_SERIAL);
    }

    public Boolean isSerial() {
        return flags.contains(FLAG_SERIAL);
    }

    public List<String> getNonFlagArgs() {
        return nonFlagArgs;
    }
}
