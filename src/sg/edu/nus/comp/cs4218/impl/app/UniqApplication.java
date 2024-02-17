package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;

import java.io.InputStream;
import java.io.OutputStream;

import sg.edu.nus.comp.cs4218.app.UniqInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.SortException;
import sg.edu.nus.comp.cs4218.exception.UniqException;

public class UniqApplication implements UniqInterface {

    /**
     * Runs the uniq application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws UniqException
     */
    @Override
    public void run(final String[] args, final InputStream stdin, final OutputStream stdout)
            throws UniqException {
        // Format: uniq [Options] [INPUT_FILE [OUTPUT_FILE]]
        // TODO: To implement
    }

    @Override
    public String uniqFromFile(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, String inputFileName, String outputFileName) throws AbstractApplicationException {
        // TODO: To implement
        return null;
    }

    @Override
    public String uniqFromStdin(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, InputStream stdin, String outputFileName) throws AbstractApplicationException {
        // TODO: To implement
        return null;
    }
}
