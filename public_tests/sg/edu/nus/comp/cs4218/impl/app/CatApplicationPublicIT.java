package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

@SuppressWarnings("PMD.ClassNamingConventions")
public class CatApplicationPublicIT {

    private static final String TEMP = "temp-cat";
    private static final String DIR = "dir";
    private static final String ERR_IS_DIR = String.format("cat: '%s': Is a directory", DIR);
    private static final String NON_EXIST_FILE = "nonExistFile.txt";
    private static final String ERR_NO_SUCH_FILE = String.format("cat: '%s': No such file or directory", NON_EXIST_FILE);
    private static final String LINE_1 = "Test line 1";
    private static final String LINE_2 = "Test line 2";
    private static final String LINE_3 = "Test line 3";
    private static final String L1_TO_L3 = LINE_1 + STRING_NEWLINE + LINE_2 + STRING_NEWLINE + LINE_3;
    private static final String NUMBERED_LINE_1 = "1 Test line 1";
    private static final String NUMBERED_LINE_2 = "2 Test line 2";
    private static final String NUMBERED_LINE_3 = "3 Test line 3";
    private static final String NUMBERED_L1_TO_L3 = NUMBERED_LINE_1 + STRING_NEWLINE + NUMBERED_LINE_2 + STRING_NEWLINE + NUMBERED_LINE_3;
    private static final String LINE_1_DOT_1 = "Test line 1.1";
    private static final String LINE_1_DOT_2 = "Test line 1.2";
    private static final String LINE_1_DOT_3 = "Test line 1.3";
    private static final String L11_TO_L13 = LINE_1_DOT_1 + STRING_NEWLINE + LINE_1_DOT_2 + STRING_NEWLINE + LINE_1_DOT_3;
    private static final String LINE_2_DOT_1 = "Test line 2.1";
    private static final String LINE_2_DOT_2 = "Test line 2.2";
    private static final String L21_TO_L22 = LINE_2_DOT_1 + STRING_NEWLINE + LINE_2_DOT_2;
    private static final String L11_TO_L22 = LINE_1_DOT_1 + STRING_NEWLINE +
            LINE_1_DOT_2 + STRING_NEWLINE +
            LINE_1_DOT_3 + STRING_NEWLINE +
            LINE_2_DOT_1 + STRING_NEWLINE +
            LINE_2_DOT_2;
    private static final Deque<Path> FILES = new ArrayDeque<>();
    private static Path tempPath;
    private static Path dirPath;

    private CatApplication catApplication;

    @BeforeEach
    void setUp() {
        catApplication = new CatApplication();
    }

    @BeforeAll
    static void createTemp() throws IOException, NoSuchFieldException, IllegalAccessException {
        TestEnvironmentUtil.setCurrentDirectory(System.getProperty("user.dir"));
        String initialDir = TestEnvironmentUtil.getCurrentDirectory();
        tempPath = Paths.get(initialDir, TEMP);
        dirPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP + CHAR_FILE_SEP + DIR);
        Files.createDirectory(tempPath);
        Files.createDirectory(dirPath);
    }

    @AfterAll
    static void deleteFiles() throws IOException {
        for (Path file : FILES) {
            Files.deleteIfExists(file);
        }
        Files.deleteIfExists(dirPath);
        Files.delete(tempPath);
    }

    private void createFile(String name, String text) throws IOException {
        Path path = tempPath.resolve(name);
        Files.createFile(path);
        Files.write(path, text.getBytes(StandardCharsets.UTF_8));
        FILES.push(path);
    }

    private String[] toArgs(String flag, String... files) {
        List<String> args = new ArrayList<>();
        if (!flag.isEmpty()) {
            args.add("-" + flag);
        }
        for (String file : files) {
            if ("-".equals(file)) {
                args.add(file);
            } else {
                args.add(Paths.get(TEMP, file).toString());
            }
        }
        return args.toArray(new String[0]);
    }

    //catStdin cases
    @Test
    void run_SingleStdinNoFlag_DisplaysStdinContents() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(L1_TO_L3.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs(""), inputStream, output);
        assertEquals((L1_TO_L3 + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleStdinFlag_DisplaysNumberedStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(L1_TO_L3.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("n"), inputStream, output);
        assertEquals(NUMBERED_L1_TO_L3 + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleStdinDashNoFlag_DisplaysStdinContents() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(L1_TO_L3.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("", "-"), inputStream, output);
        assertEquals((L1_TO_L3 + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleStdinDashFlag_DisplaysNumberedStdinContents() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(L1_TO_L3.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("n", "-"), inputStream, output);
        assertEquals((NUMBERED_L1_TO_L3 + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleEmptyStdinNoFlag_DisplaysEmpty() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = "";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs(""), inputStream, output);
        assertEquals(text, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleEmptyStdinFlag_DisplaysEmpty() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = "";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("n"), inputStream, output);
        assertEquals(text, output.toString(StandardCharsets.UTF_8));
    }

    //catFiles cases
    @Test
    void run_NonexistentFileNoFlag_DisplaysErrMsg() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        catApplication.run(toArgs("", NON_EXIST_FILE), System.in, output);
        assertEquals(ERR_NO_SUCH_FILE + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_DirectoryNoFlag_DisplaysErrMsg() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        catApplication.run(toArgs("", DIR), System.in, output);
        assertEquals(ERR_IS_DIR + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileNoFlag_DisplaysFileContents() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileA.txt";
        String text = L1_TO_L3;
        createFile(fileName, text);
        catApplication.run(toArgs("", fileName), System.in, output);
        assertEquals((text + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileFlag_DisplaysNumberedFileContents() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileB.txt";
        createFile(fileName, L1_TO_L3);
        catApplication.run(toArgs("n", fileName), System.in, output);
        assertEquals(NUMBERED_L1_TO_L3 + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleEmptyFileNoFlag_DisplaysEmpty() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileC.txt";
        String text = "";
        createFile(fileName, text);
        catApplication.run(toArgs("", fileName), System.in, output);
        assertEquals(text, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleEmptyFileFlag_DisplaysEmpty() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileD.txt";
        String text = "";
        createFile(fileName, text);
        catApplication.run(toArgs("n", fileName), System.in, output);
        assertEquals(text, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileUnknownFlag_ThrowsException() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileE.txt";
        createFile(fileName, L1_TO_L3);
        assertThrows(CatException.class, () -> catApplication.run(toArgs("a", fileName), System.in, output));
    }

    @Test
    void run_MultipleFilesNoFlag_DisplaysCatFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileF.txt";
        String fileName2 = "fileG.txt";
        createFile(fileName1, L11_TO_L13);
        createFile(fileName2, L21_TO_L22);
        catApplication.run(toArgs("", fileName1, fileName2), System.in, output);
        assertEquals((L11_TO_L22 + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleFilesFlag_DisplaysNumberedCatFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileH.txt";
        String fileName2 = "fileI.txt";
        String expectedText = "1 Test line 1.1" + STRING_NEWLINE +
                "2 Test line 1.2" + STRING_NEWLINE +
                "3 Test line 1.3" + STRING_NEWLINE +
                "1 Test line 2.1" + STRING_NEWLINE +
                "2 Test line 2.2";
        createFile(fileName1, L11_TO_L13);
        createFile(fileName2, L21_TO_L22);
        catApplication.run(toArgs("n", fileName1, fileName2), System.in, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleEmptyFilesNoFlag_DisplaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileJ.txt";
        String fileName2 = "fileK.txt";
        String text = "";
        createFile(fileName1, text);
        createFile(fileName2, text);
        catApplication.run(toArgs("", fileName1, fileName2), System.in, output);
        assertEquals(text, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleEmptyFilesFlag_DisplaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileL.txt";
        String fileName2 = "fileM.txt";
        String text = "";
        createFile(fileName1, text);
        createFile(fileName2, text);
        catApplication.run(toArgs("n", fileName1, fileName2), System.in, output);
        assertEquals(text, output.toString(StandardCharsets.UTF_8));
    }

    //catFilesAndStdin cases
    @Test
    void run_SingleStdinNonexistentFileNoFlag_DisplaysErrMsg() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(L1_TO_L3.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("", NON_EXIST_FILE), inputStream, output);
        assertEquals(ERR_NO_SUCH_FILE + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleStdinDirectoryNoFlag_ThrowsException() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(L1_TO_L3.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("", DIR), inputStream, output);
        assertEquals(ERR_IS_DIR + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleStdinDashSingleFileNoFlag_DisplaysCatStdinFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(L11_TO_L13.getBytes(StandardCharsets.UTF_8));
        String fileName = "fileN.txt";
        createFile(fileName, L21_TO_L22);
        catApplication.run(toArgs("", "-", fileName), inputStream, output);
        assertEquals((L11_TO_L22 + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileSingleStdinDashNoFlag_DisplaysCatFileStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileO.txt";
        createFile(fileName, L11_TO_L13);
        InputStream inputStream = new ByteArrayInputStream(L21_TO_L22.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("", fileName, "-"), inputStream, output);
        assertEquals((L11_TO_L22 + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }
}

