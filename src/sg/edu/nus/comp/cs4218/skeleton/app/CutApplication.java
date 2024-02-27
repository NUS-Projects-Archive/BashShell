package sg.edu.nus.comp.cs4218.skeleton.app;

import sg.edu.nus.comp.cs4218.app.CutInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.exception.RmException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class CutApplication implements CutInterface {
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        throw new CutException("EF2 not implemented");
    }

    @Override
    public String cutFromFiles(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, String... fileName) throws AbstractApplicationException {
        throw new CutException("EF2 not implemented");
    }

    @Override
    public String cutFromStdin(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, InputStream stdin) throws AbstractApplicationException {
        throw new CutException("EF2 not implemented");
    }
}
