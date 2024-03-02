package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class CutArgsParser extends ArgsParser {

    public CutArgsParser() {
        super();
    }

    public Boolean isCharPo() {
        return false;
    }

    public Boolean isBytePo() {
        return false;
    }

    public List<Integer[]> getRangeList() {
        return null;
    }

    public List<String> getFileNames() {
        return null;
    }
}
