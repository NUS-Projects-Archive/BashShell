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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;

import java.io.IOException;
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

    static Stream<Arguments> getValidDirs() {
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
	static void setUp() throws IOException {
		cdApplication = new CdApplication();

		parentDirAbsPath = parentDir.toString();

		// Add tempDir to tempParentDir
		dir = Files.createDirectory(parentDir.resolve(DIR_NAME));
		dirAbsPath = dir.toString();

		// Add tempChildDir to tempDir
        Path tempChildDir = Files.createDirectory(dir.resolve(CHILD_DIR_NAME));
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
     * Tests run method in CdApplication with null args and expects a CdException to be thrown.
     */
	@Test
	void run_NullArgs_ThrowsErrNullArgs() {
		Throwable result = assertThrows(CdException.class, () -> cdApplication.run(null, System.in, System.out));
		assertEquals("cd: " + ERR_NULL_ARGS, result.getMessage());
	}

	/**
	 * Tests run method in CdApplication with multiple args and expects a CdException to be thrown.
	 */
	@Test
	void run_MultipleArgs_ThrowsErrTooManyArgs() {
		Throwable result = assertThrows(CdException.class, () -> cdApplication.run(new String[] { "a", "b" }, System.in,
                System.out));
		assertEquals(String.format("cd: %s", ERR_TOO_MANY_ARGS), result.getMessage());
	}

	/**
	 * Tests run method in CdApplication with empty args and expects the current directory to remain unchanged.
     *
     * @throws CdException  To be thrown by run if there is any error.
	 */
	@Test
	void run_EmptyArgs_DoNothing() throws CdException {
		cdApplication.run(new String[] {}, System.in, System.out);
		assertEquals(Environment.currentDirectory, Environment.currentDirectory);
	}

	/**
	 * Tests run method in CdApplication with valid directory and expects the current directory to be changed to the
     * valid directory.
     *
     * @throws CdException  To be thrown by run if there is any error.
	 */
	@ParameterizedTest
	@MethodSource("getValidDirs")
	void run_ValidDir_ChangeCurrentDirectoryToArg(String validDir, Path expectedDir) throws CdException {
        cdApplication.run(new String[] { validDir }, System.in, System.out);
		assertEquals(expectedDir.toString(), Environment.currentDirectory);
	}

    /**
     * Tests changeToDirectory method in CdApplication and expects the current directory to be changed to the
     * valid directory.
     *
     * @param validDir      String of a valid directory path or name that can be executed.
     * @param expectedDir   Path of the expected directory.
     * @throws CdException  To be thrown by changeToDirectory if there is any error.
     */
    @ParameterizedTest
    @MethodSource("getValidDirs")
	void changeToDirectory_ValidDirectory_ChangeCurrentDirectoryToValidDirectory(String validDir, Path expectedDir)
            throws CdException {
		cdApplication.changeToDirectory(validDir);
		assertEquals(expectedDir.toString(), Environment.currentDirectory);
	}

	/**
	 * Test case for changeToDirectory method in CdApplication.
	 * Tests with null path and expects a CdException to be thrown.
	 */
	@Test
	void changeToDirectory_NullPathStr_ThrowsErrNoArgs() {
		Throwable result = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(null));
		assertEquals(String.format("cd: %s", ERR_NO_ARGS), result.getMessage());
	}

    /**
	 * Test case for changeToDirectory method in CdApplication.
	 * Tests with empty path and expects a CdException to be thrown.
	 */
	@Test
	void changeToDirectory_EmptyPathStr_ThrowsErrNoArgs() {
		Throwable result = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(""));
		assertEquals(String.format("cd: %s", ERR_NO_ARGS), result.getMessage());
	}

	/**
	 * Test case for changeToDirectory method in CdApplication.
	 * Tests with the relative path of a directory with no execute permission and expects a CdException to be thrown.
	 * This test is disabled on Windows as Windows does not use the same executable file permission concept.
     *
     * @throws IOException  To be thrown by Files functions if there is any error.
	 */
	@Test
	@DisabledOnOs(OS.WINDOWS)
	void changeToDirectory_NoExecutablePermissionDirectory_ThrowsErrNoPerm() throws IOException {
		// Given
		String dirName = "noPermDirectory";
		Path noPermDir = Files.createDirectory(dir.resolve(dirName));
		boolean isSetExecutableSuccess = noPermDir.toFile().setExecutable(false);
		if (!isSetExecutableSuccess) {
			fail("Failed to set executable permission for directory to test.");
			Files.delete(noPermDir);
			return;
		}

		// When
		Throwable result = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(dirName));

		// Then
		assertEquals(String.format("cd: %s: %s", dirName, ERR_NO_PERM), result.getMessage());

		Files.delete(noPermDir);
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
		Throwable result = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(dirName));

		// Then
		assertEquals(String.format("cd: %s: %s", dirName, ERR_FILE_NOT_FOUND), result.getMessage());
	}

	/**
	 * Test case for changeToDirectory method in CdApplication.
	 * Tests with the relative path of a valid file and expects a CdException to be thrown.
     *
     * @throws IOException  To be thrown by Files functions if there is any error.
	 */
	@Test
	void changeToDirectory_ValidFile_ThrowsErrIsNotDir() throws IOException {
		// Given
		String fileName = "file";
		Path tempFile = Files.createFile(dir.resolve(fileName));

		// When
		Throwable result = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(fileName));

		// Then
		assertEquals(String.format("cd: %s: %s", fileName, ERR_IS_NOT_DIR), result.getMessage());

		Files.delete(tempFile);
	}
}
