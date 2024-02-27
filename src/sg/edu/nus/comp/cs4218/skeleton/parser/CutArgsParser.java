package sg.edu.nus.comp.cs4218.skeleton.parser;

import sg.edu.nus.comp.cs4218.impl.parser.ArgsParser;

import java.util.List;

public class CutArgsParser extends ArgsParser {

    public CutArgsParser() {
        super();
    }

    public Boolean isCutByChar() {
        return false;
    }

    public Boolean isCutByByte() {
        return false;
    }

    public List<String> getFileNames() {
        return null;
    }
}
