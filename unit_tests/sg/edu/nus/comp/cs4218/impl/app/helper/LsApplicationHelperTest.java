package sg.edu.nus.comp.cs4218.impl.app.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.impl.app.helper.LsApplicationHelper.formatContents;
import static sg.edu.nus.comp.cs4218.impl.app.helper.LsApplicationHelper.resolvePaths;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewDirectory;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.joinStringsByNewline;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryLsException;

// To give a meaningful variable name
@SuppressWarnings("PMD.LongVariable")
class LsApplicationHelperTest {

    private static final String DIR_A_NAME = "dirA";
    private static final String STRING_AZ = "a.z";
    private static final String STRING_ZA = "z.a";
    private static final String STRING_Z = "z";
    private static final String[] CWD_DIRS = {DIR_A_NAME};
    private static final String[] CWD_NON_DIRS = {STRING_AZ, STRING_ZA, STRING_Z};
    private static final String[] DIR_A_NON_DIRS = {"0"};

    // Main temporary dir
    @TempDir
    private Path cwdPath;
    // Temporary dir A is in main temporary dir
    private Path dirAPath;

    /**
     * Sets up the current working directory environment.
     * Current working directory contains of 3 files and 1 directory (dir A). Dir A contains 1 file.
     */
    @BeforeEach
    void setUp() {
        for (String file : CWD_NON_DIRS) {
            assertDoesNotThrow(() -> cwdPath.resolve(file).toFile().createNewFile());
        }
        for (String dir : CWD_DIRS) {
            createNewDirectory(cwdPath, dir);
        }
        for (String file : DIR_A_NON_DIRS) {
            assertDoesNotThrow(() -> cwdPath.resolve(CWD_DIRS[0]).resolve(file).toFile().createNewFile());
        }

        String cwdPathName = cwdPath.toString();
        // dirAPath = Paths.get(cwdPathName, DIR_A_NAME);
        dirAPath = createNewDirectory(cwdPath, DIR_A_NAME);
        // Set current working directory to cwdPath
        Environment.currentDirectory = cwdPathName;
    }

    @Test
    void formatContents_IsSortByExtIsNull_ThrowsNullPointerException() {
        List<Path> contents = Arrays.stream(CWD_NON_DIRS)
                .map(Paths::get)
                .collect(Collectors.toList());
        assertThrowsExactly(NullPointerException.class, () -> formatContents(contents, null));
    }

    @Test
    void formatContents_IsSortByExtIsTrue_ReturnsSortedFormattedContents() {
        List<Path> contents = Arrays.stream(CWD_NON_DIRS)
                .map(Paths::get)
                .collect(Collectors.toList());
        String result = formatContents(contents, true);
        String expected = joinStringsByNewline(STRING_Z, STRING_ZA, STRING_AZ);
        assertEquals(expected, result);
    }

    @Test
    void formatContents_IsSortByExtIsFalse_ReturnsFormattedContents() {
        List<Path> contents = Arrays.stream(CWD_NON_DIRS)
                .map(Paths::get)
                .collect(Collectors.toList());
        String result = formatContents(contents, false);
        String expected = joinStringsByNewline(STRING_AZ, STRING_Z, STRING_ZA);
        assertEquals(expected, result);
    }

    @Test
    void resolvePaths_InvalidDirectory_ThrowsInvalidDirectoryLsException() {
        String invalidDirectory = "invalid_directory\0";
        InvalidDirectoryLsException result = assertThrowsExactly(InvalidDirectoryLsException.class, () ->
                resolvePaths(invalidDirectory)
        );
        String expected = String.format("ls: cannot access '%s': No such file or directory", invalidDirectory);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void resolvePaths_ValidDirectory_ReturnsValidListOfPath() {
        List<Path> result = assertDoesNotThrow(() -> resolvePaths(DIR_A_NAME));
        List<Path> expected = List.of(dirAPath);
        assertEquals(expected, result);
    }

    @Test
    void resolvePaths_DirectoryStartsWithFileSeparator_ReturnsListOfPath() {
        // Unix OS considers path starting with '/' as an absolute path
        List<Path> result = assertDoesNotThrow(() -> resolvePaths(Paths.get(DIR_A_NAME).toString()));
        List<Path> expected = List.of(dirAPath);
        assertEquals(expected, result);
    }

    @Test
    void resolvePaths_DirectoryStartsWithDriveLetter_ReturnsListOfPath() {
        // dirAPath.toString() will return the absolute path, including the drive letter (e.g., "C:"), which is OS
        // dependent
        List<Path> result = assertDoesNotThrow(() -> resolvePaths(dirAPath.toString()));
        List<Path> expected = List.of(dirAPath);
        assertEquals(expected, result);
    }
}
