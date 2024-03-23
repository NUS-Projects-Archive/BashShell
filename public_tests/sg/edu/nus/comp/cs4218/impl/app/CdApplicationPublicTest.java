package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CdApplicationPublicTest {

    private static final String DIR = "temp-cd";
    private static final Path DIR_PATH = Paths.get(DIR).toAbsolutePath();
    private static final String INVALID_DIR_PATH = "invalid/testDir";
    private static String initialDirectory;
    private CdApplication cdApplication;

    @BeforeEach
    void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        cdApplication = new CdApplication();
        initialDirectory = TestEnvironmentUtil.getCurrentDirectory();
        TestEnvironmentUtil.setCurrentDirectory(System.getProperty("user.dir"));
        Files.createDirectories(DIR_PATH);
    }

    @AfterEach
    void tearDown() throws IOException, NoSuchFieldException, IllegalAccessException {
        Files.deleteIfExists(DIR_PATH);
        TestEnvironmentUtil.setCurrentDirectory(initialDirectory);
    }

    @Test
    void changeToDirectory_ValidPath_CorrectlyChangesEnvironment() throws NoSuchFieldException,
            IllegalAccessException {
        assertDoesNotThrow(() -> cdApplication.changeToDirectory(DIR));
        assertEquals(DIR_PATH.toString(), TestEnvironmentUtil.getCurrentDirectory());
    }

    @Test
    void changeToDirectory_InvalidPath_CorrectlyChangesEnvironment() throws NoSuchFieldException,
            IllegalAccessException {
        assertThrows(CdException.class, () -> cdApplication.changeToDirectory(INVALID_DIR_PATH));
        assertNotEquals(Paths.get(INVALID_DIR_PATH).toAbsolutePath().toString(),
                        TestEnvironmentUtil.getCurrentDirectory());
    }
}