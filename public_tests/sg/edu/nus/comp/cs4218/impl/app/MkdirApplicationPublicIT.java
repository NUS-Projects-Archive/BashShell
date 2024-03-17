package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.MkdirException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

@SuppressWarnings("PMD.ClassNamingConventions")
public class MkdirApplicationPublicIT {

    private static final String PATH_TO_TEST_DIR = "testing_utilities" + File.separator + "mkdirTestDir" + File.separator;
    private static final String TEMP_DIR = PATH_TO_TEST_DIR + "mkdirTest";
    private static final String TEMP_DIR_2 = PATH_TO_TEST_DIR + "mkdirTest2";
    private static final String TEMP_PARENT = PATH_TO_TEST_DIR + "mkdirTestParent";
    private static final String TEMP_CHILD = TEMP_PARENT + File.separator + "mkdirTestChild";
    private MkdirApplication mkdirApplication;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        mkdirApplication = new MkdirApplication();
        TestEnvironmentUtil.setCurrentDirectory(System.getProperty("user.dir"));
        deleteDirectory(null, new File(PATH_TO_TEST_DIR).listFiles());
    }

    @AfterEach
    void tearDown() throws IOException {
        File file = new File(PATH_TO_TEST_DIR + File.separator + "EmptyFileForGitTracking.txt");
        file.createNewFile();
    }

    public static void deleteDirectory(File directory, File... files) {
        if (null != files) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i], files[i].listFiles());
                } else {
                    files[i].delete();
                }
            }
        }
        if (directory != null) {
            directory.delete();
        }
    }

    @Test
    void run_NullInput_ThrowsMkdirException() {
        assertThrows(MkdirException.class, () -> {
            mkdirApplication.run(null, null, null);
        });
    }

    @Test
    void run_EmptyInput_ThrowsMkdirException() {
        String[] args = new String[0];
        assertThrows(MkdirException.class, () -> {
            mkdirApplication.run(args, null, null);
        });
    }

    @Test
    void run_OneNewDirectoryInput_Success() {
        String[] args = new String[1];
        args[0] = TEMP_DIR;
        assertDoesNotThrow(() -> mkdirApplication.run(args, null, null));
        assertTrue(new File(TEMP_DIR).exists());
    }

    @Test
    void run_TwoNewDirectoryInput_Success() {
        String[] args = new String[2];
        args[0] = TEMP_DIR;
        args[1] = TEMP_DIR_2;
        assertDoesNotThrow(() -> mkdirApplication.run(args, null, null));
        assertTrue(new File(TEMP_DIR).exists());
        assertTrue(new File(TEMP_DIR_2).exists());
    }

    @Test
    void run_DirectoryInDirectoryInput_Success() {
        String[] args = new String[2];
        args[0] = TEMP_PARENT;
        args[1] = TEMP_CHILD;
        assertDoesNotThrow(() -> mkdirApplication.run(args, null, null));
        assertTrue(new File(TEMP_CHILD).exists());
    }
}
