package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.exception.LsException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    // Main temporary folder
    @TempDir
    private static Path cwdPath;
    private static String cwdName;
    private static String cwdPathName;
    private static final String[] CWD_NON_FOLDERS = { "a.z", "z.a", "z" };
    private static final String FOLDER_A_NAME = "folderA";
    private static final String[] CWD_FOLDERS = { FOLDER_A_NAME };

    // Temporary folder A in main temporary folder
    private static final String[] FOLDER_A_NON_FOLDERS = { "0" };

    private LsApplication app;
    private ByteArrayOutputStream outContent;

    private static String getCwdContents() {
        List<String> fileList = Stream.concat(Arrays.stream(CWD_NON_FOLDERS), Arrays.stream(CWD_FOLDERS))
                .sorted()
                .collect(Collectors.toList());
        return String.join(System.lineSeparator(), fileList);
    }

    private static String getFolderAContents() {
        List<String> fileList = new ArrayList<>(Arrays.asList(FOLDER_A_NON_FOLDERS));
        Collections.sort(fileList);
        return String.join(System.lineSeparator(), fileList) + System.lineSeparator();
    }

    private static String joinStringsByLineSeparator(String... strings) {
        return String.join(System.lineSeparator(), strings);
    }

    /**
     * To provide valid arguments and expected output for run_ValidArgs_PrintsCorrectDirectoryContents.
     */
    static Stream<Arguments> provideValidArgs() {
        String listedCwdContents = String.format("%s%s", joinStringsByLineSeparator(".:", getCwdContents()),
                System.lineSeparator());
        String listedFolderAContents = FOLDER_A_NAME + joinStringsByLineSeparator(":", getFolderAContents());
        return Stream.of(
                // Relative paths
                Arguments.of(new String[] { "." }, listedCwdContents),
                Arguments.of(new String[] { String.format("..%s%s", CHAR_FILE_SEP, cwdName) }, listedCwdContents),
                Arguments.of(new String[] { FOLDER_A_NAME }, listedFolderAContents),
                // Absolute path
                Arguments.of(new String[] { cwdPathName }, listedCwdContents));
    }

    /**
     * To provide invalid flags as input and the first invalid flag as expected output for
     * run_InvalidFlags_ThrowLsException.
     */
    static Stream<Arguments> provideInvalidFlags() {
        return Stream.of(
                Arguments.of(new String[] { "-a" }, "a"),
                Arguments.of(new String[] { "-abc", "-X" }, "a"),
                Arguments.of(new String[] { "-Ra" }, "a"));
    }

    @BeforeAll
    static void setUpEnvironment() throws IOException {
        for (String file : CWD_NON_FOLDERS) {
            cwdPath.resolve(file).toFile().createNewFile();
        }
        for (String folder : CWD_FOLDERS) {
            Files.createDirectory(cwdPath.resolve(folder));
        }
        for (String file : FOLDER_A_NON_FOLDERS) {
            cwdPath.resolve(CWD_FOLDERS[0]).resolve(file).toFile().createNewFile();
        }

        cwdPathName = cwdPath.toString();
        cwdName = Paths.get(cwdPathName).getFileName().toString();

        // Set current working directory to the temporary folder
        System.setProperty("user.dir", cwdPathName);
    }

    @BeforeEach
    void setUp() {
        this.app = new LsApplication();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    /**
     * To test run to throw LsException when OutputStream is null.
     */
    @Test
    void run_NullStdOut_ThrowsLsException() {
        Throwable result = assertThrows(LsException.class, () -> app.run(new String[0], System.in, null));
        assertEquals(String.format("ls: %s", ERR_NO_OSTREAM), result.getMessage());
    }

    /**
     * To test run to throw LsException when args is null.
     */
    @Test
    void run_NullArgs_ThrowsLsException() {
        Throwable result = assertThrows(LsException.class, () -> app.run(null, System.in, System.out));
        assertEquals(String.format("ls: %s", ERR_NULL_ARGS), result.getMessage());
    }

    /**
     * To test run to print current directory contents when args is empty.
     */
    @Test
    void run_EmptyArgs_PrintsCwdContents() throws LsException {
        // Given
        String expected = String.format("%s%s", getCwdContents(), System.lineSeparator()); //NOPMD - suppressed AvoidDuplicateLiterals - Is not duplicating literals

        // When
        app.run(new String[0], System.in, System.out);
        String actual = outContent.toString();

        // Then
        assertEquals(expected, actual);
    }

    /**
     * To test run to print the specified directory contents when args is a directory.
     *
     * @param args     The directory to list.
     * @param expected The specified directory contents.
     * @throws LsException To be thrown by run if an error occurs when listing the directory.
     */
    @ParameterizedTest
    @MethodSource("provideValidArgs")
    void run_ValidArgs_PrintsCorrectDirectoryContents(String[] args, String expected) throws LsException {
        // When
        app.run(args, System.in, System.out);
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
        String fileName = CWD_NON_FOLDERS[0];
        String expected = String.format("%s%s", fileName, System.lineSeparator());

        // When
        app.run(new String[] { fileName }, System.in, System.out);

        // Then
        String actual = outContent.toString();
        assertEquals(expected, actual);
    }

    /**
     * To test run to print file not found error when non existent folder is entered as argument.
     *
     * @throws LsException To be thrown by run if an error occurs.
     */
    @Test
    void run_NonExistentFolder_PrintsFileNotFoundError() throws LsException {
        // Given
        String nonExistentFolderName = "nonExistentFolder";
        String expected = String.format("ls: cannot access '%s': %s%s", nonExistentFolderName, ERR_FILE_NOT_FOUND,
                System.lineSeparator());

        // When
        app.run(new String[] { nonExistentFolderName }, System.in, System.out);

        // Then
        String actual = outContent.toString();
        assertEquals(expected, actual);
    }

    /**
     * To test run to print permission denied error when name of folder without read permission is entered as argument.
     * It creates a folder with no read permission and tries to list it.
     * This method is disabled on Windows as it does not support PosixFilePermission.
     */
    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void run_NoReadPermissionFolder_PrintsPermDeniedError() throws IOException, LsException {
        // Given
        String noReadPermissionFolderName = "noReadPermissionFolder";
        Path noReadPermissionFolderPath = cwdPath.resolve(noReadPermissionFolderName);
        Files.createDirectories(noReadPermissionFolderPath);
        boolean isSetReadableSuccess = noReadPermissionFolderPath.toFile().setReadable(false);
        if (!isSetReadableSuccess) {
            fail("Failed to set read permission for directory to test.");
            Files.deleteIfExists(noReadPermissionFolderPath);
            return;
        }
        
        // When
        app.run(new String[]{noReadPermissionFolderName}, System.in, System.out);

        // Then
        String actual = outContent.toString();
        String expected = String.format("ls: cannot open directory '%s': %s%s", noReadPermissionFolderName, ERR_NO_PERM,
                System.lineSeparator());
        assertEquals(expected, actual);

        Files.deleteIfExists(noReadPermissionFolderPath);
    }

    /**
     * To test run to throw Ls exception when invalid flags are entered as arguments.
     * 
     * @param args             The invalid flags.
     * @param invalidArgOutput The first invalid flag.
     */
    // Tests for the errors thrown from ArgsParser.validateArgs(...)
    @ParameterizedTest
    @MethodSource("provideInvalidFlags")
    void run_InvalidFlags_ThrowsLsException(String[] args, String invalidArgOutput) {
        Throwable result = assertThrows(LsException.class, () -> app.run(args, System.in, System.out));
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
        Throwable result;
        try (OutputStream mockedStdout = Mockito.mock(OutputStream.class)) {
            doThrow(new IOException()).when(mockedStdout).write(Mockito.any(byte[].class));
            result = assertThrows(LsException.class, () -> app.run(new String[0], null, mockedStdout));
        }
        assertEquals(String.format("ls: %s", ERR_WRITE_STREAM), result.getMessage());

        Files.delete(tempFile);
    }

    /**
     * To test run to return current working directory contents in String format when no folder name is specified.
     * 
     * @throws LsException To be thrown by run if an error occurs.
     */
    // Essentially the same as run_EmptyArgs_PrintCurrentDirectoryContents
    @Test
    void listFolderContent_NoFolderNameSpecifiedAndNotRecursive_ReturnsCwdContent() throws LsException {
        String expected = getCwdContents();

        // When
        String actual = app.listFolderContent(false, false);

        // Then
        assertEquals(expected, actual);
    }
}
