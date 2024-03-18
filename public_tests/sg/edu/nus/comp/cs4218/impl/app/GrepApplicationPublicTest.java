package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

public class GrepApplicationPublicTest {
    private final static String LABEL_STDIN = "(standard input)";
    private final static String PATTERN_VALID = "test";
    private final static String PATTERN_INVALID = "[";

    private final static String TEXT_ONE = "test";
    private final static String TEXT_TWO = "another test.";
    private final static byte[] BYTES_SINGLE_LINE = TEXT_ONE.getBytes();
    private final static String TEXT_MULTI_LINE = TEXT_ONE + STRING_NEWLINE + TEXT_TWO + STRING_NEWLINE + "Test";
    private final static byte[] BYTES_MULTI_LINE = TEXT_MULTI_LINE.getBytes();

    private static final String TEST_FILE = "fileA.txt";
    private static final String TEMP = "temp-grep";
    private static Path tempPath;
    private static Path testFilePath;

    private GrepApplication grepApplication;

    @BeforeEach
    void setUp() throws IOException, NoSuchFieldException, IllegalAccessException, NoClassDefFoundError,
            NoSuchMethodException, InvocationTargetException, InstantiationException {
        grepApplication = new GrepApplication();
        TestEnvironmentUtil.setCurrentDirectory(System.getProperty("user.dir"));
        tempPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP);
        testFilePath = tempPath.resolve(TEST_FILE);
        Files.createDirectory(tempPath);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(testFilePath.toAbsolutePath());
        Files.delete(tempPath);
    }

    private Path createFile() throws IOException {
        Path path = tempPath.resolve(GrepApplicationPublicTest.TEST_FILE);
        Files.createFile(path);
        return path;
    }

    @Test
    void grepFromStdin_EmptyPattern_ThrowsException() {
        InputStream stdin = new ByteArrayInputStream(BYTES_SINGLE_LINE);

        assertThrows(GrepException.class, () ->
                grepApplication.grepFromStdin("", false, false, false, stdin));
    }

    @Test
    void grepFromStdin_CountLinesOptionPatternStdin_LinesFoundAddedToResults() throws AbstractApplicationException {
        InputStream stdin = new ByteArrayInputStream(BYTES_MULTI_LINE);
        String expected = "2";

        String actual = grepApplication.grepFromStdin(PATTERN_VALID, false, true, false, stdin);
        assertEquals(expected + STRING_NEWLINE, actual);
    }

    @Test
    void grepFromStdin_CountLinesOptionInvalidPatternStdin_LinesFoundAddedToResults() {
        InputStream stdin = new ByteArrayInputStream(BYTES_MULTI_LINE);

        assertThrows(GrepException.class, () ->
                grepApplication.grepFromStdin(PATTERN_INVALID, false, true, false, stdin));
    }

    @Test
    void grepFromStdin_CaseInsensitiveCountLinesOptionPatternStdin_LinesFoundAddedToResults() throws AbstractApplicationException {
        InputStream stdin = new ByteArrayInputStream(BYTES_MULTI_LINE);
        String expected = "3";

        String actual = grepApplication.grepFromStdin(PATTERN_VALID, true, true, false, stdin);
        assertEquals(expected + STRING_NEWLINE, actual);
    }

    @Test
    void grepFromStdin_CountLinesPrefixFileNameOptionPatternStdin_LinesFoundAddedToResults() throws AbstractApplicationException {
        InputStream stdin = new ByteArrayInputStream(BYTES_MULTI_LINE);
        String expected = String.format("%s: %d", LABEL_STDIN, 2);

        String actual = grepApplication.grepFromStdin(PATTERN_VALID, false, true, true, stdin);
        assertEquals(expected + STRING_NEWLINE, actual);
    }

    @Test
    void grepFromStdin_CaseInsensitiveCountLinesPrefixFilenamePatternStdin_LinesFoundAddedToResults() throws AbstractApplicationException {
        InputStream stdin = new ByteArrayInputStream(BYTES_MULTI_LINE);
        String expected = String.format("%s: %d", LABEL_STDIN, 3);

        String actual = grepApplication.grepFromStdin(PATTERN_VALID, true, true, true, stdin);
        assertEquals(expected + STRING_NEWLINE, actual);
    }

    @Test
    void grepFromFiles_EmptyPattern_ThrowsException() throws Exception {
        String[] fileNames = new String[]{testFilePath.toString()};
        Files.write(createFile(), BYTES_SINGLE_LINE);

        assertThrows(GrepException.class, () -> grepApplication.grepFromFiles("", false, false, false, fileNames));
    }

    @Test
    void grepFromFiles_CountLinesOptionPatternFile_LinesFoundAddedToResults() throws Exception {
        String[] fileNames = new String[]{testFilePath.toString()};
        Files.write(createFile(), BYTES_MULTI_LINE);
        String expected = "2";

        String actual = grepApplication.grepFromFiles(PATTERN_VALID, false, true, false, fileNames);
        assertEquals(expected + STRING_NEWLINE, actual);
    }

    @Test
    void grepFromFiles_CaseInsensitivePatternFile_LinesFoundAddedToResults() throws Exception {
        String[] fileNames = new String[]{testFilePath.toString()};
        Files.write(createFile(), BYTES_MULTI_LINE);

        String actual = grepApplication.grepFromFiles(PATTERN_VALID, true, false, false, fileNames);
        assertEquals(TEXT_MULTI_LINE + STRING_NEWLINE, actual);
    }

    @Test
    void grepFromFileAndStdin_EmptyPattern_ThrowsException() throws Exception {
        InputStream stdin = new ByteArrayInputStream(TEXT_ONE.getBytes());
        String[] fileNames = new String[]{TEST_FILE};
        Files.write(createFile(), BYTES_MULTI_LINE);

        assertThrows(GrepException.class, () ->
                grepApplication.grepFromFileAndStdin("", false, false, false, stdin, fileNames));
    }

}
