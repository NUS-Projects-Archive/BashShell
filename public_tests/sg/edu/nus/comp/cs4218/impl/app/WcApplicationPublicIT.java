package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.joinStringsByNewline;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

@SuppressWarnings("PMD.ClassNamingConventions")
public class WcApplicationPublicIT {
    private static final String TEMP = "temp-wc";
    private static final String NUMBER_FORMAT = " %7d";
    private static final String STDIN = "-";
    private static final String FILE_CONTENT =
            joinStringsByNewline("First line", "Second line", "Third line", "Fourth line") + STRING_NEWLINE;
    private static final Deque<Path> FILES = new ArrayDeque<>();
    private static final Path TEMP_PATH = Paths.get(TEMP);
    private static String currPathString;

    private WcApplication wcApplication;

    private static String formatCounts(int lineCount, int wordCount, long byteCount, String lastLine) {
        StringBuilder stringBuilder = new StringBuilder();
        if (lineCount > -1) {
            stringBuilder.append(String.format(NUMBER_FORMAT, lineCount));
        }
        if (wordCount > -1) {
            stringBuilder.append(String.format(NUMBER_FORMAT, wordCount));
        }
        if (byteCount > -1) {
            stringBuilder.append(String.format(NUMBER_FORMAT, byteCount));
        }
        if (!"".equals(lastLine)) {
            stringBuilder.append(' ').append(lastLine);
        }
        return stringBuilder.toString();
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
        for (Path file : FILES) {
            Files.deleteIfExists(file);
        }
    }

    private Path createFile(String name) throws IOException {
        Path path = TEMP_PATH.resolve(name);
        Files.createFile(path);
        Files.write(path, FILE_CONTENT.getBytes(StandardCharsets.UTF_8));
        FILES.push(path);
        return path;
    }

    private String[] toArgs(String flag, String... files) {
        List<String> args = new ArrayList<>();
        if (!flag.isEmpty()) {
            args.add("-" + flag);
        }
        if (files == null) {
            args.add(null);
        } else {
            for (String file : files) {
                args.add(Paths.get(file).toString());
            }
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
        String expected = formatCounts(4, 8, fileSize, fileName);
        assertEquals(expected + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileLinesFlag_DisplaysLinesFilename() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileB.txt";
        createFile(fileName);
        wcApplication.run(toArgs("l", fileName), System.in, output);
        String expected = formatCounts(4, -1, -1, fileName);
        assertEquals(expected + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileWordsFlag_DisplaysWordsFilename() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileC.txt";
        createFile(fileName);
        wcApplication.run(toArgs("w", fileName), System.in, output);
        String expected = formatCounts(-1, 8, -1, fileName);
        assertEquals(expected + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileBytesFlag_DisplaysBytesFilename() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileD.txt";
        Path filePath = createFile(fileName);
        long fileSize = Files.size(filePath);
        wcApplication.run(toArgs("c", fileName), System.in, output);
        String expected = formatCounts(-1, -1, fileSize, fileName);
        assertEquals(expected + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileAllFlags_DisplaysLinesWordsBytesFilename() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileE.txt";
        Path filePath = createFile(fileName);
        long fileSize = Files.size(filePath);
        wcApplication.run(toArgs("clw", fileName), System.in, output);
        String expected = formatCounts(4, 8, fileSize, fileName);
        assertEquals(expected + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
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
        String expected = formatCounts(4, 8, fileSize, "");
        assertEquals(expected + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleInputDash_DisplaysLinesWordsBytes() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String input = "First line\nSecond line\nThird line\nFourth line\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        long fileSize = input.getBytes(StandardCharsets.UTF_8).length;
        wcApplication.run(toArgs("", "-"), inputStream, output);
        String expected = formatCounts(4, 8, fileSize, STDIN);
        assertEquals(expected + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
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
        List<String> expectedList = new ArrayList<>();
        expectedList.add(formatCounts(4, 8, fileGSize, fileGName));
        expectedList.add(formatCounts(4, 8, fileHSize, fileHName));
        expectedList.add(formatCounts(8, 16, fileGSize + fileHSize, "total"));
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertEquals(expected + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
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
        List<String> expectedList = new ArrayList<>();
        expectedList.add(formatCounts(4, 8, fileISize, fileIName));
        expectedList.add(formatCounts(4, 8, inputSize, STDIN));
        expectedList.add(formatCounts(8, 16, fileISize + inputSize, "total"));
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertEquals(expected + STRING_NEWLINE, output.toString(StandardCharsets.UTF_8));
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
