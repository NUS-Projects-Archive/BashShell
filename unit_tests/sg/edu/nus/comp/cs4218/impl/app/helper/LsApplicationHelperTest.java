package sg.edu.nus.comp.cs4218.impl.app.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.test.FileUtils.createNewDirectory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.Environment;

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
        dirAPath = Paths.get(cwdPathName, DIR_A_NAME);
        // Set current working directory to cwdPath
        Environment.currentDirectory = cwdPathName;
    }

    @Test
    void formatContents_IsSortByExtIsNull_ThrowsNullPointerException() {
        List<Path> contents = Arrays.stream(CWD_NON_DIRS)
                .map(Paths::get)
                .collect(Collectors.toList());
        assertThrows(NullPointerException.class, () -> LsApplicationHelper.formatContents(contents, null));
    }

    @Test
    void formatContents_IsSortByExtIsTrue_ReturnsSortedFormattedContents() {
        // Given
        String expected = String.join(STRING_NEWLINE, STRING_Z, STRING_ZA, STRING_AZ);
        List<Path> contents = Arrays.stream(CWD_NON_DIRS)
                .map(Paths::get)
                .collect(Collectors.toList());
        // When
        String actual = LsApplicationHelper.formatContents(contents, true);

        // Then
        assertEquals(expected, actual);
    }

    @Test
    void formatContents_IsSortByExtIsFalse_ReturnsFormattedContents() {
        // Given
        String expected = String.join(STRING_NEWLINE, STRING_AZ, STRING_Z, STRING_ZA);
        List<Path> contents = Arrays.stream(CWD_NON_DIRS)
                .map(Paths::get)
                .collect(Collectors.toList());
        // When
        String actual = LsApplicationHelper.formatContents(contents, false);

        // Then
        assertEquals(expected, actual);
    }

    /***
     * Tests if resolvePaths returns a list of paths when given a valid directory.
     */
    @Test
    void resolvePaths_ValidDirectory_ReturnsValidListOfPath() {
        // Given
        List<Path> expected = List.of(dirAPath);

        // When
        List<Path> actual = assertDoesNotThrow(() -> LsApplicationHelper.resolvePaths(DIR_A_NAME));

        // Then
        assertEquals(expected, actual);
    }
}
