package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.CHAR_TAB;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

public class WcApplicationPublicIT {
    private static final String TEMP = "temp-wc";
    
    private static String currPathString;
    private static final Deque<Path> files = new ArrayDeque<>();
    private static Path TEMP_PATH;
    
    private WcApplication wcApplication;

    @BeforeAll
    static void setUp() throws NoSuchFieldException, IllegalAccessException {
        TEMP_PATH = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP);
    }
    
    @BeforeEach
    void changeDirectory() throws IOException, NoSuchFieldException, IllegalAccessException {
        wcApplication = new WcApplication();
        currPathString = TestEnvironmentUtil.getCurrentDirectory();
        Files.createDirectory(TEMP_PATH);
        TestEnvironmentUtil.setCurrentDirectory(TEMP_PATH.toString());
    }

    @AfterEach
    void deleteFiles() throws IOException, NoSuchFieldException, IllegalAccessException {
        TestEnvironmentUtil.setCurrentDirectory(currPathString);
        Files.walk(TEMP_PATH)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        for (Path file : files) {
            Files.deleteIfExists(file);
        }
    }

    private Path createFile(String name) throws IOException {
        String content = "First line\nSecond line\nThird line\nFourth line\n";
        Path path = TEMP_PATH.resolve(name);
        Files.createFile(path);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        files.push(path);
        return path;
    }

    private String[] toArgs(String flag, String... files) {
        List<String> args = new ArrayList<>();
        if (!flag.isEmpty()) {
            args.add("-" + flag);
        }
        for (String file : files) {
            args.add(Paths.get(file).toString());
        }
        return args.toArray(new String[0]);
    }

    @Test
    void run_SingleFileNoFlags_DisplaysLinesWordsBytesFilename() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileA.txt";
        Path filePath = createFile(fileName);
        long fileSize = Files.size(filePath);
        wcApplication.run(toArgs("", fileName), System.in, output);
        assertEquals(CHAR_TAB + "4" + CHAR_TAB + "8" + CHAR_TAB + String.format("%s %s", fileSize, fileName) + STRING_NEWLINE,
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileLinesFlag_DisplaysLinesFilename() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileB.txt";
        createFile(fileName);
        wcApplication.run(toArgs("l", fileName), System.in, output);
        assertEquals(CHAR_TAB + "4 " + fileName + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileWordsFlag_DisplaysWordsFilename() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileC.txt";
        createFile(fileName);
        wcApplication.run(toArgs("w", fileName), System.in, output);
        assertEquals(CHAR_TAB + "8 " + fileName + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileBytesFlag_DisplaysBytesFilename() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileD.txt";
        Path filePath = createFile(fileName);
        long fileSize = Files.size(filePath);
        wcApplication.run(toArgs("c", fileName), System.in, output);
        assertEquals(CHAR_TAB + String.format("%s %s", fileSize, fileName) + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileAllFlags_DisplaysLinesWordsBytesFilename() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileE.txt";
        Path filePath = createFile(fileName);
        long fileSize = Files.size(filePath);
        wcApplication.run(toArgs("clw", fileName), System.in, output);
        assertEquals(CHAR_TAB + "4" + CHAR_TAB + "8" + CHAR_TAB + String.format("%s %s", fileSize, fileName) + STRING_NEWLINE,
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileUnknownFlag_Throws() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileF.txt";
        createFile(fileName);
        assertThrows(WcException.class, () -> wcApplication.run(toArgs("x", fileName), System.in, output));
    }

    @Test
    void run_SingleInputNoFileSpecified_DisplaysLinesWordsBytes() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String input = "First line\nSecond line\nThird line\nFourth line\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        long fileSize = input.getBytes(StandardCharsets.UTF_8).length;
        wcApplication.run(toArgs(""), inputStream, output);
        assertEquals(CHAR_TAB + "4" + CHAR_TAB + "8" + CHAR_TAB + String.format("%s", fileSize) + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleInputDash_DisplaysLinesWordsBytes() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String input = "First line\nSecond line\nThird line\nFourth line\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        long fileSize = input.getBytes(StandardCharsets.UTF_8).length;
        wcApplication.run(toArgs("", "-"), inputStream, output);
        assertEquals(CHAR_TAB + "4" + CHAR_TAB + "8" + CHAR_TAB + String.format("%s -", fileSize) + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleFiles_DisplaysLinesWordsBytesFilenameTotal() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileGName = "fileG.txt";
        String fileHName = "fileH.txt";
        Path fileGPath = createFile(fileGName);
        Path fileHPath = createFile(fileHName);
        long fileGSize = Files.size(fileGPath);
        long fileHSize = Files.size(fileHPath);
        wcApplication.run(toArgs("", fileGName, fileHName), System.in, output);
        assertEquals(CHAR_TAB + "4" + CHAR_TAB + "8" + CHAR_TAB + String.format("%s %s", fileGSize, fileGName) + STRING_NEWLINE
                + CHAR_TAB + "4" + CHAR_TAB + "8" + CHAR_TAB + String.format("%s %s", fileHSize, fileHName) + STRING_NEWLINE
                + CHAR_TAB + "8" + CHAR_TAB + "16" + CHAR_TAB + String.format("%s total", fileGSize + fileHSize) + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileAndSingleInput_DisplaysLinesWordsBytesTotal() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileIName = "fileI.txt";
        Path fileIPath = createFile(fileIName);
        long fileISize = Files.size(fileIPath);
        String input = "First line\nSecond line\nThird line\nFourth line\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        long inputSize = input.getBytes(StandardCharsets.UTF_8).length;
        wcApplication.run(toArgs("", fileIName, "-"), inputStream, output);
        assertEquals(CHAR_TAB + "4" + CHAR_TAB + "8" + CHAR_TAB + String.format("%s %s", fileISize, fileIName) + STRING_NEWLINE
                + CHAR_TAB + "4" + CHAR_TAB + "8" + CHAR_TAB + String.format("%s -", inputSize) + STRING_NEWLINE
                + CHAR_TAB + "8" + CHAR_TAB + "16" + CHAR_TAB + String.format("%s total", fileISize + inputSize) + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_FilenameIsNull_Throws() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThrows(WcException.class, () -> wcApplication.run(toArgs("", null), System.in, output));
    }

    @Test
    void run_OutputStreamIsNull_Throws() throws IOException {
        String fileKName = "fileK.txt";
        createFile(fileKName);
        assertThrows(WcException.class, () -> wcApplication.run(toArgs("", fileKName), System.in, null));
    }

    @Test
    void run_InputStreamIsNull_Throws() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThrows(WcException.class, () -> wcApplication.run(toArgs("", ""), null, output));
    }

}
