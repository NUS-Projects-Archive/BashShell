package sg.edu.nus.comp.cs4218.skeleton.parser;

import java.util.List;

import sg.edu.nus.comp.cs4218.impl.parser.ArgsParser;

public class TeeArgsParser extends ArgsParser {

    public TeeArgsParser() {
        super();
    }

    public Boolean isAppend() {
        return false;
    }

    public List<String> getFileNames() {
        return null;
    }
}
