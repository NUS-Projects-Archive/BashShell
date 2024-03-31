package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.AssertUtils.assertFileDoNotExists;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.testutils.FileUtils;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

public class RmApplicationPublicTest {
    private static final String TEMP = "temp-rm" + File.separator;
    private static final String DIR_A = "rmDirectoryA";
    private static final String DIR_B = "rmDirectoryB";
    private static final String FILE_A = "rmA";
    private static final String FILE_B = "rmB";
    private static final String FILE_C = "rmC";
    private static final String FILE_D = "rmD";

    private static Path tempPath;
    private RmApplication rmApplication;


    @BeforeAll
    static void createTemp() throws NoSuchFieldException, IllegalAccessException {
        tempPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP);
    }

    @BeforeEach
    public void setUp() throws Exception {
        rmApplication = new RmApplication();
        Files.createDirectories(tempPath);
        FileUtils.createNewDirectory(tempPath, DIR_A);
        FileUtils.createNewDirectory(tempPath, DIR_B);
        FileUtils.createNewFile(tempPath, FILE_A);
        FileUtils.createNewFile(tempPath, FILE_B);
        FileUtils.createNewFile(tempPath, FILE_C);
        Files.createFile(Paths.get(tempPath.toString(), DIR_A, FILE_D));
    }

    @AfterEach
    public void clean() throws Exception {
        Files.walk(tempPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void removeTest_RemoveSingleFileNoFlag_RemovesFile() throws Exception {
        rmApplication.remove(false, false, TEMP + FILE_A);
        assertFileDoNotExists(Paths.get(TEMP + FILE_A));
    }

    @Test
    public void removeTest_RemoveEmptyFolderNoFlag_ThrowsException() {
        assertThrows(RmException.class, () -> rmApplication.remove(false, false, TEMP + DIR_B));
    }

    @Test
    public void removeTest_RemoveEmptyFolderEmptyFlag_RemovesFolder() throws Exception {
        rmApplication.remove(true, false, TEMP + DIR_B);
        assertFileDoNotExists(Paths.get(TEMP + DIR_B));
    }

    @Test
    public void removeTest_RemoveFolderNoFlag_ThrowsException() {
        assertThrows(RmException.class, () -> rmApplication.remove(false, false, TEMP + DIR_A));
    }

    @Test
    public void removeTest_RemoveFolderEmptyFlag_ThrowsException() {
        assertThrows(RmException.class, () -> rmApplication.remove(true, false, TEMP + DIR_A));
    }

    @Test
    public void removeTest_RemoveMultipleFilesNoFlag_RemovesSelectedFiles() throws Exception {
        rmApplication.remove(false, false, TEMP + FILE_A, TEMP + FILE_B, TEMP + FILE_C);
        assertFileDoNotExists(Paths.get(TEMP + FILE_A));
        assertFileDoNotExists(Paths.get(TEMP + FILE_B));
        assertFileDoNotExists(Paths.get(TEMP + FILE_C));
    }

    @Test
    public void removeTest_RemoveFolderRecurseFlag_RemovesAllFoldersAndFiles() throws Exception {
        rmApplication.remove(false, true, TEMP + DIR_A);
        assertFileDoNotExists(Paths.get(TEMP + DIR_A));
        assertFileDoNotExists(Paths.get(TEMP + DIR_A + File.separator + FILE_D));
    }
}
