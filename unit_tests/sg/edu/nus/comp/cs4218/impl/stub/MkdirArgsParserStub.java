package sg.edu.nus.comp.cs4218.impl.stub;

import sg.edu.nus.comp.cs4218.impl.parser.MkdirArgsParser;

import java.util.List;

public class MkdirArgsParserStub extends MkdirArgsParser {

    private Boolean isCreateParent;
    private List<String> directories;

    public void setValues(Boolean isCreateParent, List<String> directories) {
        this.isCreateParent = isCreateParent;
        this.directories = directories;
    }

    @Override
    public boolean isCreateParent() {
        return this.isCreateParent;
    }

    @Override
    public List<String> getDirectories() {
        return this.directories;
    }
}
