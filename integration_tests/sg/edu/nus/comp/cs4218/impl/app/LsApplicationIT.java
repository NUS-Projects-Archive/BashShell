package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewDirectory;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFile;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.deleteFileOrDirectory;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.joinStringsByNewline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryLsException;
import sg.edu.nus.comp.cs4218.exception.LsException;

@SuppressWarnings("PMD.ClassNamingConventions")
class LsApplicationIT {

    private static final String DIR_A_NAME = "dirA";
    private static final String[] CWD_DIRS = {DIR_A_NAME};
    private static final String[] CWD_NON_DIRS = {"a.z", "z.a", "z"};
    // Temporary dir A in main temporary dir
    private static final String[] DIR_A_NON_DIRS = {"0"};

    // Main temporary dir
    @TempDir
    private static Path cwdPath;
    private static String cwdName;
    private static String cwdPathName;

    private LsApplication app;
    private ByteArrayOutputStream outContent;

    private static String getCwdContents() {
        String[] fileList = Stream.concat(Arrays.stream(CWD_NON_DIRS), Arrays.stream(CWD_DIRS))
                .sorted()
                .toArray(String[]::new);
        return joinStringsByNewline(fileList);
    }

    private static String getDirAContents() {
        return joinStringsByNewline(DIR_A_NON_DIRS);
    }

    /**
     * Provides valid arguments and expected output for run_ValidArgs_PrintsCorrectDirectoryContents.
     */
    static Stream<Arguments> validArgs() {
        String listCwdContents = joinStringsByNewline(".:", getCwdContents()) + STRING_NEWLINE;
        String listDirAContents = DIR_A_NAME + joinStringsByNewline(":", getDirAContents()) + STRING_NEWLINE;
        return Stream.of(
                // Relative paths
                Arguments.of(new String[]{"."}, listCwdContents),
                Arguments.of(new String[]{String.format("..%s%s", CHAR_FILE_SEP, cwdName)}, listCwdContents),
                Arguments.of(new String[]{DIR_A_NAME}, listDirAContents),
                // Absolute path
                Arguments.of(new String[]{cwdPathName}, listCwdContents)
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

    /**
     * Sets up for each test.
     */
    @BeforeEach
    void setUp() {
        app = new LsApplication();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    /**
     * Tests run to throw LsException when OutputStream is null.
     */
    @Test
    void run_NullStdOut_ThrowsLsException() {
        InputStream mockedInputStream = new ByteArrayInputStream("".getBytes());
        LsException result = assertThrowsExactly(LsException.class, () -> app.run(new String[0], mockedInputStream,
                null));
        assertEquals(String.format("ls: %s", ERR_NO_OSTREAM), result.getMessage());
    }

    /**
     * Tests run to throw LsException when Args is null.
     */
    @Test
    void run_NullArgs_ThrowsLsException() {
        InputStream mockedInputStream = new ByteArrayInputStream("".getBytes());
        LsException result = assertThrowsExactly(LsException.class, () -> app.run(null, mockedInputStream, System.out));
        assertEquals(String.format("ls: %s", ERR_NULL_ARGS), result.getMessage());
    }

    /**
     * Tests run to print current directory contents when args is empty.
     */
    @Test
    void run_EmptyArgs_PrintsCwdContents() {
        // Given
        String expected = getCwdContents() + STRING_NEWLINE;
        InputStream mockedInputStream = new ByteArrayInputStream("".getBytes());

        // When
        assertDoesNotThrow(() -> app.run(new String[0], mockedInputStream, System.out));
        String actual = outContent.toString();

        // Then
        assertEquals(expected, actual);
    }

    /**
     * Tests run to print the specified directory contents when args is a directory.
     *
     * @param args     The directory to list
     * @param expected The specified directory contents
     */
    @ParameterizedTest
    @MethodSource("validArgs")
    void run_ValidArgs_PrintsCorrectDirectoryContents(String[] args, String expected) {
        // Given
        InputStream mockedInputStream = new ByteArrayInputStream("".getBytes());

        // When
        assertDoesNotThrow(() -> app.run(args, mockedInputStream, System.out));
        String actual = outContent.toString();

        // Then
        assertEquals(expected, actual);
    }

    /**
     * Tests run to print the specified file when args is a valid file.
     */
    @Test
    void run_ValidFile_PrintsFileName() {
        // Given
        String fileName = CWD_NON_DIRS[0];
        InputStream mockedInputStream = new ByteArrayInputStream("".getBytes());

        // When
        assertDoesNotThrow(() -> app.run(new String[]{fileName}, mockedInputStream, System.out));

        // Then
        String actual = outContent.toString();
        String expected = String.format("%s%s", fileName, STRING_NEWLINE);
        assertEquals(expected, actual);
    }

    /**
     * Tests run to print file not found error when non-existent dir is entered as argument.
     */
    @Test
    void run_NonExistentDir_ThrowsInvalidDirectoryLsException() {
        // Given
        String nonExistDirName = "nonExistDir";
        InputStream mockedInputStream = new ByteArrayInputStream("".getBytes());

        // When
        InvalidDirectoryLsException result = assertThrowsExactly(InvalidDirectoryLsException.class, () ->
                app.run(new String[]{nonExistDirName}, mockedInputStream, System.out)
        );

        // Then
        String expected = String.format("ls: cannot access '%s': %s", nonExistDirName, ERR_FILE_NOT_FOUND);
        assertEquals(expected, result.getMessage());
    }

    /**
     * Tests run to print permission denied error when name of dir without read permission is entered as argument.
     * It creates a dir with no read permission and tries to list it.
     * This method is disabled on Windows as it does not support PosixFilePermission.
     */
    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void run_NoReadPermissionDir_PrintsPermDeniedError() {
        // Given
        InputStream mockedInputStream = new ByteArrayInputStream("".getBytes());
        String noReadPermDir = "noReadPermissionDir";
        Path noReadPermDirPath = createNewDirectory(cwdPath, noReadPermDir);
        boolean isSetReadable = noReadPermDirPath.toFile().setReadable(false);
        if (!isSetReadable) {
            fail("Failed to set read permission for directory to test.");
            deleteFileOrDirectory(noReadPermDirPath);
            return;
        }

        // When
        assertDoesNotThrow(() -> app.run(new String[]{noReadPermDir}, mockedInputStream, System.out));

        // Then
        String actual = outContent.toString();
        String expected = String.format("ls: cannot open directory '%s': %s%s", noReadPermDir, ERR_NO_PERM,
                STRING_NEWLINE);
        assertEquals(expected, actual);

        deleteFileOrDirectory(noReadPermDirPath);
    }

    /**
     * Tests run to throw Ls exception when invalid flags are entered as arguments.
     *
     * @param args             The invalid flags
     * @param invalidArgOutput The first invalid flag
     */
    @ParameterizedTest
    @MethodSource("provideInvalidFlags")
    void run_InvalidFlags_ThrowsLsException(String[] args, String invalidArgOutput) {
        InputStream mockedInputStream = new ByteArrayInputStream("".getBytes());
        LsException result = assertThrowsExactly(LsException.class, () -> app.run(args, mockedInputStream, System.out));
        assertEquals(String.format("ls: %s%s", ILLEGAL_FLAG_MSG, invalidArgOutput), result.getMessage());
    }

    /**
     * Tests run to throw Ls exception when run fails to write to output stream.
     * Creates a temporary file and tries to write to it, but it should fail and throw an exception.
     */
    @Test
    void run_FailsToWriteToOutputStream_ThrowsLsException() {
        Path tempFile = createNewFile("temp.txt", getCwdContents());
        LsException result;
        try (OutputStream mockedStdout = Mockito.mock(OutputStream.class)) {
            assertDoesNotThrow(() -> doThrow(new IOException()).when(mockedStdout).write(Mockito.any(byte[].class)));
            result = assertThrowsExactly(LsException.class, () -> app.run(new String[0], null, mockedStdout));
        } catch (IOException e) {
            fail("Failed to create mocked output stream.");
            return;
        }

        assertEquals(String.format("ls: %s", ERR_WRITE_STREAM), result.getMessage());

        deleteFileOrDirectory(tempFile);
    }
}
