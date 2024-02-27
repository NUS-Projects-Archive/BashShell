package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.CutInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class CutApplication implements CutInterface {
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {

    }

    @Override
    public String cutFromFiles(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, String... fileName) throws AbstractApplicationException {
        return null;
    }

    @Override
    public String cutFromStdin(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, InputStream stdin) throws AbstractApplicationException {
        return null;
    }
}
