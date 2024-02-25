package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.SortException;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;

class SortApplicationTest {

    private final String SORT_EXCEPTION_MSG = "sort: ";
    private final String PROB_SORT_FILE = "Problem sort from file: ";
    private final String PROB_SORT_STDIN = "Problem sort from stdin: ";
    private final String EXISTING_FILE = "existingFile";
    private final String NON_EXISTING_FILE = "nonExistingFile";

    private SortApplication app;

    @BeforeEach
    void setUp() {
        this.app = new SortApplication();
        deleteFile(NON_EXISTING_FILE);
    }

    private void deleteFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            file.delete();
        }
    }

    private void deleteDirectory(File directory) {
        if (directory != null && directory.exists()) {
            directory.delete();
            deleteDirectory(directory.getParentFile());
        }
    }

    @Test
    void run_NoStdout_ThrowsNullStreamException() {
        Throwable result = assertThrows(SortException.class, () -> {
            app.run(null, null, null);
        });
        assertEquals(SORT_EXCEPTION_MSG + ERR_NULL_STREAMS, result.getMessage());
    }

    @Test
    void sortFromFiles_EmptyFiles_ThrowsNullArgsException() {
        Throwable result = assertThrows(SortException.class, () -> {
            app.sortFromFiles(true, true, true, null);
        });
        assertEquals(SORT_EXCEPTION_MSG + PROB_SORT_FILE + ERR_NULL_ARGS, result.getMessage());
    }

    @Test
    void sortFromFiles_FileDoNotExist_ThrowsFileNotFoundException() {
        Throwable result = assertThrows(SortException.class, () -> {
            app.sortFromFiles(true, true, true, NON_EXISTING_FILE);
        });
        assertEquals(SORT_EXCEPTION_MSG + PROB_SORT_FILE + ERR_FILE_NOT_FOUND, result.getMessage());
    }

}