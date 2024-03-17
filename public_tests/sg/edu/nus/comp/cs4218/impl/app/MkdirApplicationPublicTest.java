package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.MkdirException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

public class MkdirApplicationPublicTest {
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
    void createFolder_NullInput_ThrowsMkdirException() {
        assertThrows(MkdirException.class, () -> {
            mkdirApplication.createFolder(null);
        });
    }

    @Test
    void createFolder_EmptyInput_ThrowsMkdirException() {
        assertThrows(MkdirException.class, () -> {
            mkdirApplication.createFolder("");
        });
        assertEquals(0, new File(PATH_TO_TEST_DIR).list().length);
    }

    @Test
    void createFolder_OneNewDirectoryInput_Success() {
        assertDoesNotThrow(() -> mkdirApplication.createFolder(TEMP_DIR));
        assertTrue(new File(TEMP_DIR).exists());
    }

    @Test
    void createFolder_TwoNewDirectoryInput_Success() {
        assertDoesNotThrow(() -> mkdirApplication.createFolder(TEMP_DIR, TEMP_DIR_2));
        assertTrue(new File(TEMP_DIR).exists());
        assertTrue(new File(TEMP_DIR_2).exists());
    }

    @Test
    void createFolder_DirectoryInDirectoryInput_Success() {
        assertDoesNotThrow(() -> mkdirApplication.createFolder(TEMP_PARENT, TEMP_CHILD));
        assertTrue(new File(TEMP_CHILD).exists());
    }
}
