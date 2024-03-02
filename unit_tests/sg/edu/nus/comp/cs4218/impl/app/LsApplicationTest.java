package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_CURR_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.provider.Arguments;

import sg.edu.nus.comp.cs4218.Environment;

// To give a meaningful variable name
@SuppressWarnings("PMD.LongVariable")
class LsApplicationTest {
    private static final String[] CWD_NON_DIRS = {"a.z", "z.a", "z"};
    private static final String DIR_A_NAME = "dirA";
    private static final String[] CWD_DIRS = {DIR_A_NAME};
    // Temporary dir A in main temporary dir
    private static final String[] DIR_A_NON_DIRS = {"0"};
    // Main temporary dir
    @TempDir
    private static Path cwdPath;
    private static String cwdName;
    private static String cwdPathName;
  
    private LsApplication app;

    private static String getCwdContents() {
        List<String> fileList = Stream.concat(Arrays.stream(CWD_NON_DIRS), Arrays.stream(CWD_DIRS))
                .sorted()
                .collect(Collectors.toList());
        return String.join(STRING_NEWLINE, fileList);
    }

    private static String getDirAContents() {
        List<String> fileList = new ArrayList<>(Arrays.asList(DIR_A_NON_DIRS));
        Collections.sort(fileList);
        return String.join(STRING_NEWLINE, fileList);
    }

    /**
     * Provides valid arguments and expected output for run_ValidArgs_PrintsCorrectDirectoryContents.
     */
    static Stream<Arguments> validArgs() {
        String listedCwdContents = String.format("%s%s", String.join(STRING_NEWLINE, ".:", getCwdContents()),
                STRING_NEWLINE);
        String listedDirAContents = DIR_A_NAME + String.join(STRING_NEWLINE, ":", getDirAContents());
        return Stream.of(
                // Relative paths
                Arguments.of(new String[]{"."}, listedCwdContents),
                Arguments.of(new String[]{String.format("..%s%s", CHAR_FILE_SEP, cwdName)}, listedCwdContents),
                Arguments.of(new String[]{DIR_A_NAME}, listedDirAContents),
                // Absolute path
                Arguments.of(new String[]{cwdPathName}, listedCwdContents)
        );
    }

    /**
     * Provides invalid flags as input and the first invalid flag as expected output for
     * run_InvalidFlags_ThrowLsException.
     */
    static Stream<Arguments> provideInvalidFlags() {
        return Stream.of(
                Arguments.of(new String[]{"-a"}, "a"),
                Arguments.of(new String[]{"-abc", "-X"}, "a"),
                Arguments.of(new String[]{"-Ra"}, "a")
        );
    }

    /**
     * Sets up the current working directory environment.
     * Current working directory contains of 3 files and 1 directory (dir A). Dir A contains 1 file.
     */
    @BeforeAll
    static void setUpEnvironment() {
        try {
            for (String file : CWD_NON_DIRS) {
                cwdPath.resolve(file).toFile().createNewFile();
            }
            for (String dir : CWD_DIRS) {
                Files.createDirectory(cwdPath.resolve(dir));
            }
            for (String file : DIR_A_NON_DIRS) {
                cwdPath.resolve(CWD_DIRS[0]).resolve(file).toFile().createNewFile();
            }

            cwdPathName = cwdPath.toString();
            cwdName = Paths.get(cwdPathName).getFileName().toString();

            // Set current working directory to the temporary dir
            Environment.currentDirectory = cwdPathName;

        } catch (IOException e) {
            fail("Setup failed due to exception: " + e.getMessage());
        }
    }

    /**
     * Sets back output stream to its original.
     */
    @AfterAll
    static void tearDown() {
        System.setOut(System.out);
    }

    private String getCwdContentsRecursively(String slash) {
        return String.format("%s%s%s", String.join(STRING_NEWLINE, ".:", getCwdContents()),
                STRING_NEWLINE + STRING_NEWLINE,
                String.join(STRING_NEWLINE, STRING_CURR_DIR + slash + DIR_A_NAME + ":", getDirAContents()));
    }

    /**
     * Sets up for each test.
     */
    @BeforeEach
    void setUp() {
        this.app = new LsApplication();
    }

    /**
     * Tests listFolderContent return current working directory contents in String format when no -X and dir name is specified.
     */
    @Test
    void listFolderContent_NoDirNameSpecifiedAndNotRecursive_ReturnsCwdContent() {
        String expected = getCwdContents();

        // When
        String actual = assertDoesNotThrow(() -> app.listFolderContent(false, false));

        // Then
        assertEquals(expected, actual);
    }

    /**
     * Tests listFolderContent return specified directory contents in String format when no -X is specified.
     */
    @Test
    void listFolderContent_OneValidDirNameSpecified_ReturnsDirContent() {
        String expected = String.join(STRING_NEWLINE, DIR_A_NAME + ":", getDirAContents());

        // When
        String actual = assertDoesNotThrow(() -> app.listFolderContent(false, false, DIR_A_NAME));

        // Then
        assertEquals(expected, actual);
    }

    /**
     * Tests listFolderContent return valid file name if that is the only thing specified without any flags.
     */
    @Test
    void listFolderContent_OneValidFileNameSpecified_ReturnsFileName() {
        // When
        String actual = assertDoesNotThrow(() -> app.listFolderContent(false, false, CWD_NON_DIRS[0]));

        // Then
        assertEquals(CWD_NON_DIRS[0], actual);
    }

    /**
     * Tests listFolderContent return all items from current working directory if no folder name is specified and
     * -R is specified. Only for Windows.
     */
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void listFolderContent_NoDirNameSpecifiedAndIsRecursiveOnWindows_ReturnsAllItems() {
        String expected = getCwdContentsRecursively("\\");
  
        // When
        String actual = assertDoesNotThrow(() -> app.listFolderContent(true, false));
        // Then
        assertEquals(expected, actual);
    }

    /**
     * Tests listFolderContent return all items from current working directory if no folder name is specified and
     * -R is specified. Only for Unix and Mac.
     */
    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void listFolderContent_NoDirNameSpecifiedAndIsRecursiveOnUnix_ReturnsAllItems() {
        String expected = getCwdContentsRecursively("/");
        // When
        String actual = assertDoesNotThrow(() -> app.listFolderContent(true, false));
        // Then
        assertEquals(expected, actual);
    }
}
