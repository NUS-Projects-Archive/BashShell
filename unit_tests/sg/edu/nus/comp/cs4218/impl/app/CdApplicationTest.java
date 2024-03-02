package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CdException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

class CdApplicationTest {
    private static final String DIR_NAME = "tempDir";
    private static final String CHILD_DIR_NAME = "tempChildDir";

    @TempDir
    static Path parentDir;
    private static String parentDirAbsPath;

    private static Path dir;
    private static String dirAbsPath;

    private static String childDirAbsPath;

    private static CdApplication cdApplication;

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
        cdApplication = new CdApplication();

        parentDirAbsPath = parentDir.toString();

        try {
            // Add tempDir to tempParentDir
            dir = Files.createDirectory(parentDir.resolve(DIR_NAME));
            dirAbsPath = dir.toString();

            // Add tempChildDir to tempDir
            Path tempChildDir = Files.createDirectory(dir.resolve(CHILD_DIR_NAME));
            childDirAbsPath = tempChildDir.toString();

            // Set cwd to tempDir
            Environment.currentDirectory = dirAbsPath;
        } catch (IOException e) {
            fail("Setup failed due to exception: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        // Reset cwd to tempDir
        Environment.currentDirectory = dirAbsPath;
    }

    /**
     * Tests run method in CdApplication with null args and expects a CdException to be thrown.
     */
    @Test
    void run_NullArgs_ThrowsErrNullArgs() {
        InputStream mockInputStream = mock(InputStream.class);
        OutputStream mockOutputStream = mock(OutputStream.class);

        CdException result = assertThrows(CdException.class, () -> cdApplication.run(null, mockInputStream, mockOutputStream));
        assertEquals("cd: " + ERR_NULL_ARGS, result.getMessage());
    }

    /**
     * Tests run method in CdApplication with multiple args and expects a CdException to be thrown.
     */
    @Test
    void run_MultipleArgs_ThrowsErrTooManyArgs() {
        InputStream mockInputStream = mock(InputStream.class);
        OutputStream mockOutputStream = mock(OutputStream.class);

        CdException result = assertThrows(CdException.class, () -> cdApplication.run(new String[] { "a", "b" },
                mockInputStream, mockOutputStream));
        assertEquals(String.format("cd: %s", ERR_TOO_MANY_ARGS), result.getMessage());
    }

    /**
     * Tests run method in CdApplication with empty args and expects the current directory to remain unchanged.
     */
    @Test
    void run_EmptyArgs_DoNothing() {
        InputStream mockInputStream = mock(InputStream.class);
        OutputStream mockOutputStream = mock(OutputStream.class);

        assertDoesNotThrow(() -> cdApplication.run(new String[] {}, mockInputStream, mockOutputStream));
        assertEquals(Environment.currentDirectory, Environment.currentDirectory);
    }

    /**
     * Tests run method in CdApplication with valid directory and expects the current directory to be changed to the
     * valid directory.
     */
    @ParameterizedTest
    @MethodSource("validDirs")
    void run_ValidDir_ChangeCurrentDirectoryToArg(String validDir, Path expectedDir) {
        InputStream mockInputStream = mock(InputStream.class);
        OutputStream mockOutputStream = mock(OutputStream.class);

        assertDoesNotThrow(() -> cdApplication.run(new String[] { validDir }, mockInputStream, mockOutputStream));
        assertEquals(expectedDir.toString(), Environment.currentDirectory);
    }

    /**
     * Tests changeToDirectory method in CdApplication and expects the current directory to be changed to the
     * valid directory.
     *
     * @param validDir      String of a valid directory path or name that can be executed.
     * @param expectedDir   Path of the expected directory.
     */
    @ParameterizedTest
    @MethodSource("validDirs")
    void changeToDirectory_ValidDirectory_ChangeCurrentDirectoryToValidDirectory(String validDir, Path expectedDir) {
        assertDoesNotThrow(() -> cdApplication.changeToDirectory(validDir));
        assertEquals(expectedDir.toString(), Environment.currentDirectory);
    }

    /**
     * Test case for changeToDirectory method in CdApplication.
     * Tests with null path and expects a CdException to be thrown.
     */
    @Test
    void changeToDirectory_NullPathStr_ThrowsErrNoArgs() {
        CdException result = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(null));
        assertEquals(String.format("cd: %s", ERR_NO_ARGS), result.getMessage());
    }

    /**
     * Test case for changeToDirectory method in CdApplication.
     * Tests with empty path and expects a CdException to be thrown.
     */
    @Test
    void changeToDirectory_EmptyPathStr_ThrowsErrNoArgs() {
        CdException result = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(""));
        assertEquals(String.format("cd: %s", ERR_NO_ARGS), result.getMessage());
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
        Path noPermDir = null;

        // Given
        try {
            noPermDir = Files.createDirectory(dir.resolve(dirName));
        } catch (IOException e) {
            fail("IOException occurred during test setup: " + e.getMessage());
        }

        boolean isSetExecutableSuccess = noPermDir.toFile().setExecutable(false);
        if (!isSetExecutableSuccess) {
            fail("Failed to set executable permission for directory to test.");
        }

        // When
        CdException result = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(dirName));

        // Then
        assertEquals(String.format("cd: %s: %s", dirName, ERR_NO_PERM), result.getMessage());

        try {
            Files.deleteIfExists(noPermDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        CdException result = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(dirName));

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
        Path tempFile = null;

        try {
            tempFile = Files.createFile(dir.resolve(fileName));
        } catch (IOException e) {
            fail("IOException occurred during test setup: " + e.getMessage());
        }

        // When
        CdException result = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(fileName));

        // Then
        assertEquals(String.format("cd: %s: %s", fileName, ERR_IS_NOT_DIR), result.getMessage());

        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}