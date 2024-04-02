package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.impl.util.CollectionsUtils.listToArray;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_TAB;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.joinStringsByNewline;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.joinStringsByTab;

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

import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

@SuppressWarnings("PMD.ClassNamingConventions")
public class PasteApplicationPublicIT {

    private static final String TEMP = "temp-paste";
    private static final String DIR = "dir";
    private static final String LINE_1 = "Test line 1";
    private static final String LINE_2 = "Test line 2";
    private static final String LINE_3 = "Test line 3";
    public static final String L1_TO_L3_TAB = joinStringsByTab(LINE_1, LINE_2, LINE_3);
    private static final String LINE_1_DOT_1 = "Test line 1.1";
    private static final String LINE_1_DOT_2 = "Test line 1.2";
    private static final String LINE_1_DOT_3 = "Test line 1.3";
    private static final String LINE_2_DOT_1 = "Test line 2.1";
    private static final String LINE_2_DOT_2 = "Test line 2.2";
    private static final String L1_TO_L3 = joinStringsByNewline(LINE_1, LINE_2, LINE_3);
    private static final String L11_TO_L13 = joinStringsByNewline(LINE_1_DOT_1, LINE_1_DOT_2, LINE_1_DOT_3);
    private static final String L21_TO_L22 = joinStringsByNewline(LINE_2_DOT_1, LINE_2_DOT_2);
    private static final String ERR_NO_SUCH_FILE = "paste: '%s': No such file or directory";
    private static final Deque<Path> FILES = new ArrayDeque<>();
    private static Path tempPath;
    private static Path dirPath;

    private PasteApplication pasteApplication;
    private ByteArrayOutputStream output;

    @BeforeAll
    static void createTemp() throws NoSuchFieldException, IllegalAccessException, IOException {
        tempPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP);
        dirPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP + CHAR_FILE_SEP + DIR);
        Files.createDirectories(tempPath);
        Files.createDirectories(dirPath);
    }

    @AfterAll
    static void deleteFiles() throws IOException {
        for (Path file : FILES) {
            Files.deleteIfExists(file);
        }
        Files.deleteIfExists(dirPath);
        Files.deleteIfExists(tempPath);
    }

    @BeforeEach
    void setUp() {
        pasteApplication = new PasteApplication();
        output = new ByteArrayOutputStream();
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
            if (("-").equals(file)) {
                args.add(file);
            } else {
                args.add(Paths.get(TEMP, file).toString());
            }
        }
        return listToArray(args);
    }

    @Test
    void run_SingleStdinNullStdout_ThrowsPasteException() {
        InputStream input = new ByteArrayInputStream(L1_TO_L3.getBytes());
        assertThrowsExactly(PasteException.class, () -> pasteApplication.run(toArgs(""), input, null));
    }

    @Test
    void run_NullStdinNullFilesNoFlag_ThrowsPasteException() {
        assertThrowsExactly(PasteException.class, () -> pasteApplication.run(toArgs(""), null, output));
    }

    @Test
    void run_NullStdinNullFilesFlag_ThrowsPasteException() {
        assertThrowsExactly(PasteException.class, () -> pasteApplication.run(toArgs("n"), null, output));
    }

    //mergeStdin cases
    @Test
    void run_SingleStdinNoFlag_DisplaysStdinContents() {
        InputStream input = new ByteArrayInputStream(L1_TO_L3.getBytes());
        assertDoesNotThrow(() -> pasteApplication.run(toArgs(""), input, output));
        assertEquals(L1_TO_L3 + STRING_NEWLINE, output.toString());
    }


    @Test
    void run_SingleStdinFlag_DisplaysNonParallelStdinContents() {
        InputStream input = new ByteArrayInputStream(L1_TO_L3.getBytes());
        assertDoesNotThrow(() -> pasteApplication.run(toArgs("s"), input, output));
        assertEquals(L1_TO_L3_TAB + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleStdinDashNoFlag_DisplaysStdinContents() {
        InputStream input = new ByteArrayInputStream(L1_TO_L3.getBytes());
        assertDoesNotThrow(() -> pasteApplication.run(toArgs("", "-"), input, output));
        assertEquals(L1_TO_L3 + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleStdinDashFlag_DisplaysNonParallelStdinContents() {
        InputStream input = new ByteArrayInputStream(L1_TO_L3.getBytes());
        assertDoesNotThrow(() -> pasteApplication.run(toArgs("s", "-"), input, output));
        assertEquals(L1_TO_L3_TAB + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleEmptyStdinNoFlag_DisplaysEmpty() {
        String text = "";
        InputStream input = new ByteArrayInputStream(text.getBytes());
        assertDoesNotThrow(() -> pasteApplication.run(toArgs(""), input, output));
        assertEquals(text, output.toString());
    }

    @Test
    void run_SingleEmptyStdinFlag_DisplaysEmpty() {
        String text = "";
        InputStream input = new ByteArrayInputStream(text.getBytes());
        assertDoesNotThrow(() -> pasteApplication.run(toArgs("s"), input, output));
        assertEquals(text, output.toString());
    }

    //mergeFiles cases
    @Test
    void run_NonexistentFileNoFlag_ThrowsPasteException() {
        String nonExistFile = "nonExistFile.txt";
        PasteException result = assertThrowsExactly(PasteException.class, () ->
                pasteApplication.run(toArgs("", nonExistFile), System.in, output)
        );
        assertEquals(String.format(ERR_NO_SUCH_FILE, nonExistFile), result.getMessage());
    }

    @Test
    void run_DirectoryNoFlag_DisplaysEmpty() {
        assertDoesNotThrow(() -> pasteApplication.run(toArgs("", DIR), System.in, output));
        assertEquals("", output.toString());
    }

    @Test
    void run_SingleFileNoFlag_DisplaysFileContents() {
        String fileName = "fileA.txt";
        assertDoesNotThrow(() -> createFile(fileName, L1_TO_L3));
        assertDoesNotThrow(() -> pasteApplication.run(toArgs("", fileName), System.in, output));
        assertEquals(L1_TO_L3 + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleFileFlag_DisplaysNonParallelFileContents() {
        String fileName = "fileB.txt";
        assertDoesNotThrow(() -> createFile(fileName, L1_TO_L3));
        assertDoesNotThrow(() -> pasteApplication.run(toArgs("s", fileName), System.in, output));
        assertEquals(L1_TO_L3_TAB + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleEmptyFileNoFlag_DisplaysEmpty() {
        String fileName = "fileC.txt";
        String text = "";
        assertDoesNotThrow(() -> createFile(fileName, text));
        assertDoesNotThrow(() -> pasteApplication.run(toArgs("", fileName), System.in, output));
        assertEquals(text, output.toString());
    }

    @Test
    void run_SingleEmptyFileFlag_DisplaysEmpty() {
        String fileName = "fileD.txt";
        String text = "";
        assertDoesNotThrow(() -> createFile(fileName, text));
        assertDoesNotThrow(() -> pasteApplication.run(toArgs("s", fileName), System.in, output));
        assertEquals(text, output.toString());
    }

    @Test
    void run_SingleFileUnknownFlag_Throws() {
        String fileName = "fileE.txt";
        assertDoesNotThrow(() -> createFile(fileName, L1_TO_L3));
        assertThrowsExactly(PasteException.class, () -> pasteApplication.run(toArgs("a", fileName), System.in, output));
    }

    @Test
    void run_MultipleFilesNoFlag_DisplaysMergedFileContents() {
        String fileName1 = "fileF.txt";
        String fileName2 = "fileG.txt";
        String expected = LINE_1_DOT_1 + STRING_TAB + LINE_2_DOT_1 + STRING_NEWLINE +
                LINE_1_DOT_2 + STRING_TAB + LINE_2_DOT_2 + STRING_NEWLINE +
                LINE_1_DOT_3 + STRING_TAB;
        assertDoesNotThrow(() -> createFile(fileName1, L11_TO_L13));
        assertDoesNotThrow(() -> createFile(fileName2, L21_TO_L22));
        assertDoesNotThrow(() -> pasteApplication.run(toArgs("", fileName1, fileName2), System.in, output));
        assertEquals(expected + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_MultipleFilesFlag_DisplaysNonParallelMergedFileContents() {
        String fileName1 = "fileH.txt";
        String fileName2 = "fileI.txt";
        String expected = joinStringsByTab(LINE_1_DOT_1, LINE_1_DOT_2, LINE_1_DOT_3) + STRING_NEWLINE +
                joinStringsByTab(LINE_2_DOT_1, LINE_2_DOT_2);
        assertDoesNotThrow(() -> createFile(fileName1, L11_TO_L13));
        assertDoesNotThrow(() -> createFile(fileName2, L21_TO_L22));
        assertDoesNotThrow(() -> pasteApplication.run(toArgs("s", fileName1, fileName2), System.in, output));
        assertEquals(expected + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_MultipleEmptyFilesNoFlag_DisplaysEmpty() {
        String fileName1 = "fileJ.txt";
        String fileName2 = "fileK.txt";
        String text = "";
        assertDoesNotThrow(() -> createFile(fileName1, text));
        assertDoesNotThrow(() -> createFile(fileName2, text));
        assertDoesNotThrow(() -> pasteApplication.run(toArgs("", fileName1, fileName2), System.in, output));
        assertEquals(text, output.toString());
    }

    @Test
    void run_MultipleEmptyFilesFlag_DisplaysEmpty() {
        String fileName1 = "fileL.txt";
        String fileName2 = "fileM.txt";
        String text = "";
        assertDoesNotThrow(() -> createFile(fileName1, text));
        assertDoesNotThrow(() -> createFile(fileName2, text));
        assertDoesNotThrow(() -> pasteApplication.run(toArgs("s", fileName1, fileName2), System.in, output));
        assertEquals(text, output.toString());
    }

    //mergeFilesAndStdin cases
    @Test
    void run_SingleStdinNonexistentFileNoFlag_ThrowsPasteException() {
        InputStream input = new ByteArrayInputStream(L11_TO_L13.getBytes());
        String nonExistFile = "nonExistFile.txt";
        PasteException result = assertThrowsExactly(PasteException.class, () ->
                pasteApplication.run(toArgs("", nonExistFile), input, output)
        );
        assertEquals(String.format(ERR_NO_SUCH_FILE, nonExistFile), result.getMessage());
    }

    @Test
    void run_SingleStdinDirectoryNoFlag_DisplaysMergedStdinFileContents() {
        InputStream input = new ByteArrayInputStream(L11_TO_L13.getBytes());
        assertDoesNotThrow(() -> pasteApplication.run(toArgs("", DIR, "-"), input, output));
        assertEquals(L11_TO_L13 + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleStdinDashSingleFileNoFlag_DisplaysMergedStdinFileContents() {
        InputStream input = new ByteArrayInputStream(L11_TO_L13.getBytes());
        String fileName = "fileN.txt";
        assertDoesNotThrow(() -> createFile(fileName, L21_TO_L22));
        String expected = LINE_1_DOT_1 + STRING_TAB + LINE_2_DOT_1 + STRING_NEWLINE +
                LINE_1_DOT_2 + STRING_TAB + LINE_2_DOT_2 + STRING_NEWLINE +
                LINE_1_DOT_3 + STRING_TAB;
        assertDoesNotThrow(() -> pasteApplication.run(toArgs("", "-", fileName), input, output));
        assertEquals(expected + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleFileSingleStdinDashNoFlag_DisplaysNonParallelMergedFileStdinContents() {
        InputStream input = new ByteArrayInputStream(L21_TO_L22.getBytes());
        String fileName = "fileO.txt";
        assertDoesNotThrow(() -> createFile(fileName, L11_TO_L13));
        String expected = LINE_1_DOT_1 + STRING_TAB + LINE_2_DOT_1 + STRING_NEWLINE +
                LINE_1_DOT_2 + STRING_TAB + LINE_2_DOT_2 + STRING_NEWLINE +
                LINE_1_DOT_3 + STRING_TAB;
        assertDoesNotThrow(() -> pasteApplication.run(toArgs("", fileName, "-"), input, output));
        assertEquals(expected + STRING_NEWLINE, output.toString());
    }
}
