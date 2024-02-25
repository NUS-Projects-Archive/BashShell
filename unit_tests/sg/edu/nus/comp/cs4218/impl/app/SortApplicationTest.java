package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import sg.edu.nus.comp.cs4218.exception.SortException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;

class SortApplicationTest {

    private final String SORT_EXCEPTION_MSG = "sort: ";
    private final String PROB_SORT_FILE = "Problem sort from file: ";
    private final String PROB_SORT_STDIN = "Problem sort from stdin: ";
    private final String EXISTING_FILE = "existingFile";
    private final String EXISTING_DIRECTORY = "existingParent/existingChild/existingGrandchild";
    private final String NON_EXISTING_FILE = "nonExistingFile";

    private SortApplication app;

    private void createFile(String fileName) throws IOException {
        File file = new File(fileName);
        file.createNewFile();
    }

    private void createDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        directory.mkdirs();
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

    @BeforeEach
    void setUp() throws IOException {
        this.app = new SortApplication();
        createFile(EXISTING_FILE);
        createDirectory(EXISTING_DIRECTORY);
    }

    @AfterEach
    void tearDown() {
        deleteFile(EXISTING_FILE);
        deleteFile(EXISTING_DIRECTORY);
        deleteFile(NON_EXISTING_FILE);
    }

    @Test
    void run_NoStdout_ThrowsSortException() {
        Throwable result = assertThrows(SortException.class, () -> {
            app.run(null, null, null);
        });
        assertEquals(SORT_EXCEPTION_MSG + ERR_NULL_STREAMS, result.getMessage());
    }

    @Test
    void sortFromFiles_EmptyFiles_ThrowsSortException() {
        Throwable result = assertThrows(SortException.class, () -> {
            app.sortFromFiles(true, true, true, null);
        });
        assertEquals(SORT_EXCEPTION_MSG + PROB_SORT_FILE + ERR_NULL_ARGS, result.getMessage());
    }

    @Test
    void sortFromFiles_FileDoNotExist_ThrowsSortException() {
        Throwable result = assertThrows(SortException.class, () -> {
            app.sortFromFiles(true, true, true, NON_EXISTING_FILE);
        });
        assertEquals(SORT_EXCEPTION_MSG + PROB_SORT_FILE + ERR_FILE_NOT_FOUND, result.getMessage());
    }

    @Test
    void sortFromFiles_FileGivenAsDirectory_ThrowsSortException() {
        Throwable result = assertThrows(SortException.class, () -> {
            app.sortFromFiles(true, true, true, EXISTING_DIRECTORY);
        });
        assertEquals(SORT_EXCEPTION_MSG + PROB_SORT_FILE + ERR_IS_DIR, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void sortFromFiles_FileNoPermissionToRead_ThrowsSortException() throws IOException {
        Path filePath = Paths.get(EXISTING_FILE);
        Files.setPosixFilePermissions(filePath, new HashSet<>(Collections.singleton(PosixFilePermission.OWNER_READ)));
        Throwable result = assertThrows(SortException.class, () -> {
            app.sortFromFiles(true, true, true, EXISTING_FILE);
        });
        assertEquals(SORT_EXCEPTION_MSG + PROB_SORT_FILE + ERR_NO_PERM, result.getMessage());
    }
}