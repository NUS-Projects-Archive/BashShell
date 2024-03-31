package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewDirectory;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.deleteFileOrDirectory;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CdException;

class CdApplicationTest {

    private static final String DIR_NAME = "tempDir";
    private static final String CHILD_DIR_NAME = "tempChildDir";

    private static Path dir;
    private static String dirAbsPath;

    private static String childDirAbsPath;

    private static CdApplication app;

    @TempDir
    private static Path parentDir;
    private static String parentDirAbsPath;

    static Stream<Arguments> validDirs() {
        return Stream.of(
                Arguments.of(".", dirAbsPath),
                Arguments.of("..", parentDirAbsPath),
                Arguments.of(String.format(".%s%s", CHAR_FILE_SEP, CHILD_DIR_NAME), childDirAbsPath),
                Arguments.of(String.format("..%s%s", CHAR_FILE_SEP, DIR_NAME), dirAbsPath),
                Arguments.of(childDirAbsPath, childDirAbsPath),
                Arguments.of(CHILD_DIR_NAME, childDirAbsPath)
        );
    }

    @BeforeAll
    static void setUp() {
        app = new CdApplication();

        parentDirAbsPath = parentDir.toString();

        // Add tempDir to tempParentDir
        dir = createNewDirectory(parentDir, DIR_NAME);
        dirAbsPath = dir.toString();

        // Add tempChildDir to tempDir
        Path tempChildDir = createNewDirectory(dir, CHILD_DIR_NAME);
        childDirAbsPath = tempChildDir.toString();

        // Set cwd to tempDir
        Environment.currentDirectory = dirAbsPath;
    }

    @AfterEach
    void tearDown() {
        // Reset cwd to tempDir
        Environment.currentDirectory = dirAbsPath;
    }

    /**
     * Tests changeToDirectory method in CdApplication and expects the current directory
     * to be changed to the valid directory.
     *
     * @param validDir    String of a valid directory path or name that can be executed.
     * @param expectedDir Path of the expected directory.
     */
    @ParameterizedTest
    @MethodSource("validDirs")
    void changeToDirectory_ValidDirectory_ChangeCurrentDirectoryToValidDirectory(String validDir, Path expectedDir) {
        assertDoesNotThrow(() -> app.changeToDirectory(validDir));
        assertEquals(expectedDir.toString(), Environment.currentDirectory);
    }

    /**
     * Test case for changeToDirectory method in CdApplication.
     * Tests with empty path and expects the current directory to be changed to the home directory.
     */
    @Test
    void changeToDirectory_EmptyPathStr_ChangeDirectoryToHome() {
        String expectedDir = System.getProperty("user.dir");

        // When
        assertDoesNotThrow(() -> app.changeToDirectory(""));

        // Then
        assertEquals(expectedDir, Environment.currentDirectory);
    }

    /**
     * Test case for changeToDirectory method in CdApplication.
     * Tests with the relative path of a directory with no execute permission and expects a CdException to be thrown.
     * This test is disabled on Windows OS as Windows does not use the same executable file permission concept.
     */
    @Test
    @DisabledOnOs(OS.WINDOWS)
    void changeToDirectory_NoExecutablePermissionDirectory_ThrowsErrNoPerm() {
        String dirName = "noPermDirectory";

        // Given
        Path noPermDir = createNewDirectory(dir, dirName);
        if (!noPermDir.toFile().setExecutable(false)) {
            fail("Failed to set executable permission for directory to test.");
        }

        // When
        CdException result = assertThrowsExactly(CdException.class, () -> app.changeToDirectory(dirName));

        // Then
        assertEquals(String.format("cd: %s: %s", dirName, ERR_NO_PERM), result.getMessage());

        deleteFileOrDirectory(noPermDir);
    }

    /**
     * Test case for changeToDirectory method in CdApplication.
     * Tests with the relative path of a non-existent directory and expects a CdException to be thrown.
     */
    @Test
    void changeToDirectory_NonExistentDirectory_ThrowsErrFileNotFound() {
        // Given
        String dirName = "nonExistentDirectory";

        // When
        CdException result = assertThrowsExactly(CdException.class, () -> app.changeToDirectory(dirName));

        // Then
        assertEquals(String.format("cd: %s: %s", dirName, ERR_FILE_NOT_FOUND), result.getMessage());
    }

    /**
     * Test case for changeToDirectory method in CdApplication.
     * Tests with the relative path of a valid file and expects a CdException to be thrown.
     */
    @Test
    void changeToDirectory_ValidFile_ThrowsErrIsNotDir() {
        // Given
        String fileName = "file";

        Path tempFile = assertDoesNotThrow(() -> Files.createFile(dir.resolve(fileName)),
                "IOException occurred during test setup");

        // When
        CdException result = assertThrowsExactly(CdException.class, () -> app.changeToDirectory(fileName));

        // Then
        assertEquals(String.format("cd: %s: %s", fileName, ERR_IS_NOT_DIR), result.getMessage());

        deleteFileOrDirectory(tempFile);
    }
}
