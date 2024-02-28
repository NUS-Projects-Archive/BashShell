package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.exception.LsException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
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
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;

// To give a meaningful variable name
@SuppressWarnings("PMD.LongVariable")
class LsApplicationTest {
    // Main temporary dir
    @TempDir
    private static Path cwdPath;
    private static String cwdName;
    private static String cwdPathName;
    private static final String[] CWD_NON_DIRS = { "a.z", "z.a", "z" };
    private static final String DIR_A_NAME = "dirA";
    private static final String[] CWD_DIRS = { DIR_A_NAME };

    // Temporary dir A in main temporary dir
    private static final String[] DIR_A_NON_DIRS = { "0" };

    private LsApplication app;
    private ByteArrayOutputStream outContent;

    private static String getCwdContents() {
        List<String> fileList = Stream.concat(Arrays.stream(CWD_NON_DIRS), Arrays.stream(CWD_DIRS))
                .sorted()
                .collect(Collectors.toList());
        return String.join(System.lineSeparator(), fileList);
    }

    private static String getDirAContents() {
        List<String> fileList = new ArrayList<>(Arrays.asList(DIR_A_NON_DIRS));
        Collections.sort(fileList);
        return String.join(System.lineSeparator(), fileList) + System.lineSeparator();
    }

    private static String joinStringsByLineSeparator(String... strings) {
        return String.join(System.lineSeparator(), strings);
    }

    /**
     * To provide valid arguments and expected output for run_ValidArgs_PrintsCorrectDirectoryContents.
     */
    static Stream<Arguments> validArgs() {
        String listedCwdContents = String.format("%s%s", joinStringsByLineSeparator(".:", getCwdContents()),
                System.lineSeparator());
        String listedDirAContents = DIR_A_NAME + joinStringsByLineSeparator(":", getDirAContents());
        return Stream.of(
            // Relative paths
            Arguments.of(new String[] { "." }, listedCwdContents),
            Arguments.of(new String[] { String.format("..%s%s", CHAR_FILE_SEP, cwdName) }, listedCwdContents),
            Arguments.of(new String[] { DIR_A_NAME }, listedDirAContents),
            // Absolute path
            Arguments.of(new String[] { cwdPathName }, listedCwdContents)
        );
    }

    /**
     * To provide invalid flags as input and the first invalid flag as expected output for
     * run_InvalidFlags_ThrowLsException.
     */
    static Stream<Arguments> provideInvalidFlags() {
        return Stream.of(
            Arguments.of(new String[] { "-a" }, "a"),
            Arguments.of(new String[] { "-abc", "-X" }, "a"),
            Arguments.of(new String[] { "-Ra" }, "a")
        );
    }

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
            System.setProperty("user.dir", cwdPathName);
        } catch (IOException e) {
            fail("Setup failed due to exception: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        this.app = new LsApplication();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterAll
    static void tearDown() {
        System.setIn(System.in);
    }

    /**
     * To test run to throw LsException when OutputStream is null.
     */
    @Test
    void run_NullStdOut_ThrowsLsException() {
        InputStream mockedInputStream = new ByteArrayInputStream("".getBytes());
        LsException result = assertThrows(LsException.class, () -> app.run(new String[0], mockedInputStream, null));
        assertEquals(String.format("ls: %s", ERR_NO_OSTREAM), result.getMessage());
    }

    /**
     * To test run to throw LsException when args is null.
     */
    @Test
    void run_NullArgs_ThrowsLsException() {
        InputStream mockedInputStream = new ByteArrayInputStream("".getBytes());
        LsException result = assertThrows(LsException.class, () -> app.run(null, mockedInputStream, System.out));
        assertEquals(String.format("ls: %s", ERR_NULL_ARGS), result.getMessage());
    }

    /**
     * To test run to print current directory contents when args is empty.
     */
    @Test
    void run_EmptyArgs_PrintsCwdContents() {
        // Given
        String expected = getCwdContents() + System.lineSeparator();
        InputStream mockedInputStream = new ByteArrayInputStream("".getBytes());

        // When
        assertDoesNotThrow(() -> app.run(new String[0], mockedInputStream, System.out));
        String actual = outContent.toString();

        // Then
        assertEquals(expected, actual);
    }

    /**
     * To test run to print the specified directory contents when args is a directory.
     *
     * @param args     The directory to list.
     * @param expected The specified directory contents.
     */
    @ParameterizedTest
    @MethodSource("validArgs")
    void run_ValidArgs_PrintsCorrectDirectoryContents(String[] args, String expected) throws LsException {
        // Given
        InputStream mockedInputStream = new ByteArrayInputStream("".getBytes());

        // When
        app.run(args, mockedInputStream, System.out);
        String actual = outContent.toString();

        // Then
        assertEquals(expected, actual);
    }

    /**
     * To test run to print the specified file when args is a valid file.
     *
     * @throws LsException To be thrown by run if an error occurs.
     */
    @Test
    void run_ValidFile_PrintsFileName() throws LsException {
        // Given
        String fileName = CWD_NON_DIRS[0];
        String expected = String.format("%s%s", fileName, System.lineSeparator());
        InputStream mockedInputStream = new ByteArrayInputStream("".getBytes());

        // When
        app.run(new String[] { fileName }, mockedInputStream, System.out);

        // Then
        String actual = outContent.toString();
        assertEquals(expected, actual);
    }

    /**
     * To test run to print file not found error when non-existent dir is entered as argument.
     *
     * @throws LsException To be thrown by run if an error occurs.
     */
    @Test
    void run_NonExistentDir_PrintsFileNotFoundError() throws LsException {
        // Given
        String nonExistentDirName = "nonExistentDir";
        String expected = String.format("ls: cannot access '%s': %s%s", nonExistentDirName, ERR_FILE_NOT_FOUND,
                System.lineSeparator());
        InputStream mockedInputStream = new ByteArrayInputStream("".getBytes());

        // When
        app.run(new String[] { nonExistentDirName }, mockedInputStream, System.out);

        // Then
        String actual = outContent.toString();
        assertEquals(expected, actual);
    }

    /**
     * To test run to print permission denied error when name of dir without read permission is entered as argument.
     * It creates a dir with no read permission and tries to list it.
     * This method is disabled on Windows as it does not support PosixFilePermission.
     */
    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void run_NoReadPermissionDir_PrintsPermDeniedError() throws IOException, LsException {
        // Given
        InputStream mockedInputStream = new ByteArrayInputStream("".getBytes());
        String noReadPermissionDirName = "noReadPermissionDir";
        Path noReadPermissionDirPath = cwdPath.resolve(noReadPermissionDirName);
        Files.createDirectories(noReadPermissionDirPath);
        boolean isSetReadableSuccess = noReadPermissionDirPath.toFile().setReadable(false);
        if (!isSetReadableSuccess) {
            fail("Failed to set read permission for directory to test.");
            Files.deleteIfExists(noReadPermissionDirPath);
            return;
        }
        
        // When
        app.run(new String[]{noReadPermissionDirName}, mockedInputStream, System.out);

        // Then
        String actual = outContent.toString();
        String expected = String.format("ls: cannot open directory '%s': %s%s", noReadPermissionDirName, ERR_NO_PERM,
                System.lineSeparator());
        assertEquals(expected, actual);

        Files.deleteIfExists(noReadPermissionDirPath);
    }

    /**
     * To test run to throw Ls exception when invalid flags are entered as arguments.
     * 
     * @param args             The invalid flags.
     * @param invalidArgOutput The first invalid flag.
     */
    @ParameterizedTest
    @MethodSource("provideInvalidFlags")
    void run_InvalidFlags_ThrowsLsException(String[] args, String invalidArgOutput) {
        InputStream mockedInputStream = new ByteArrayInputStream("".getBytes());
        LsException result = assertThrows(LsException.class, () -> app.run(args, mockedInputStream, System.out));
        assertEquals(String.format("ls: %s%s", ILLEGAL_FLAG_MSG, invalidArgOutput), result.getMessage());
    }

    /**
     * To test run to throw Ls exception when run fails to write to output stream.
     * Creates a temporary file and tries to write to it, but it should fail and throw an exception.
     *
     * @throws IOException To be thrown by run if an error occurs.
     */
    @Test
    void run_FailsToWriteToOutputStream_ThrowsLsException() throws IOException {
        Path tempFile = Files.createTempFile(cwdPath, "temp", ".txt");
        Files.write(tempFile, getCwdContents().getBytes());
        LsException result;
        try (OutputStream mockedStdout = Mockito.mock(OutputStream.class)) {
            doThrow(new IOException()).when(mockedStdout).write(Mockito.any(byte[].class));
            result = assertThrows(LsException.class, () -> app.run(new String[0], null, mockedStdout));
        }
        assertEquals(String.format("ls: %s", ERR_WRITE_STREAM), result.getMessage());

        Files.delete(tempFile);
    }

    /**
     * To test run to return current working directory contents in String format when no dir name is specified.
     */
    // Essentially the same as run_EmptyArgs_PrintCurrentDirectoryContents
    @Test
    void listDirContent_NoDirNameSpecifiedAndNotRecursive_ReturnsCwdContent() {
        String expected = getCwdContents();

        // When
        String actual = assertDoesNotThrow(() -> app.listFolderContent(false, false));

        // Then
        assertEquals(expected, actual);
    }
}
