package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

public class CatApplicationPublicTest {

    private static final String TEST_DIR = "temp-cat";
    private static final String TEST_FILE = "fileA.txt";
    private static final String TEXT_ONE = "Test line 1" +
            STRING_NEWLINE + "Test line 2" +
            STRING_NEWLINE + "Test line 3";
    private static final String EXPECT_ONE_NUM = "1 Test line 1" +
            STRING_NEWLINE + "2 Test line 2" +
            STRING_NEWLINE + "3 Test line 3";
    private static Path testDirPath;
    private static Path testFilePath;

    private CatApplication catApplication;

    @BeforeEach
    void setUp() {
        catApplication = new CatApplication();
    }

    @BeforeAll
    static void createTemp() throws IOException, NoSuchFieldException, IllegalAccessException {
        testDirPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEST_DIR);
        testFilePath = testDirPath.resolve(TEST_FILE);
        Files.createDirectories(testDirPath);
        Files.createFile(testFilePath);
        Files.write(testFilePath, TEXT_ONE.getBytes());
    }

    @AfterAll
    static void deleteFiles() throws IOException {
        Files.delete(testFilePath);
        Files.delete(testDirPath);
    }

    @Test
    void catFiles_SingleFileSpecifiedNoFlagAbsolutePath_ReturnsFileContentString() throws Exception {
        String actual = catApplication.catFiles(false, testFilePath.toString());
        assertEquals(TEXT_ONE, actual);
    }

    @Test
    void catFiles_FolderSpecifiedAbsolutePath_ThrowsException() {
        assertThrows(CatException.class, () -> catApplication.catFiles(false, testDirPath.toString()));
    }

    @Test
    void catStdin_NoFlag_ReturnsStdinString() throws AbstractApplicationException {
        InputStream input = new ByteArrayInputStream(TEXT_ONE.getBytes());
        String actual = catApplication.catStdin(false, input);
        assertEquals(TEXT_ONE, actual);
    }

    @Test
    void catStdin_EmptyStringNoFlag_ReturnsEmptyString() throws AbstractApplicationException {
        String text = "";
        InputStream input = new ByteArrayInputStream(text.getBytes());
        String actual = catApplication.catStdin(false, input);
        assertEquals(text, actual);
    }

    @Test
    void catStdin_IsLineNumberFlag_ReturnsStdinStringLineNo() throws AbstractApplicationException {
        InputStream input = new ByteArrayInputStream(TEXT_ONE.getBytes());
        String actual = catApplication.catStdin(true, input);
        assertEquals(EXPECT_ONE_NUM, actual);
    }
}
