package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.testutils.AssertUtils.assertEmptyString;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

@SuppressWarnings({"PMD.ClassNamingConventions", "PMD.LongVariable"})
public class UniqApplicationPublicIT {
    private static final String HELLO_WORLD = "Hello World";
    private static final String ALICE = "Alice";
    private static final String BOB = "Bob";

    private static final String INPUT_FILE_TXT = "input_file.txt";
    private static final String OUTPUT_FILE_TXT = "output_file.txt";

    private static final Deque<Path> FILES = new ArrayDeque<>();
    private static final String TEST_INPUT = HELLO_WORLD + STRING_NEWLINE +
            HELLO_WORLD + STRING_NEWLINE +
            ALICE + STRING_NEWLINE +
            ALICE + STRING_NEWLINE +
            BOB + STRING_NEWLINE +
            ALICE + STRING_NEWLINE +
            BOB + STRING_NEWLINE;
    private static final String WITHOUT_FLAG_OUTPUT = HELLO_WORLD + STRING_NEWLINE +
            ALICE + STRING_NEWLINE +
            BOB + STRING_NEWLINE +
            ALICE + STRING_NEWLINE +
            BOB;
    private static final String WITH_COUNT_FLAG_OUTPUT = "2 Hello World" + STRING_NEWLINE +
            "2 Alice" + STRING_NEWLINE +
            "1 Bob" + STRING_NEWLINE +
            "1 Alice" + STRING_NEWLINE +
            "1 Bob";
    private static final String WITH_DUPLICATE_FLAG_OUTPUT = HELLO_WORLD + STRING_NEWLINE +
            ALICE;
    private static final String WITH_ALL_DUPLICATE_FLAG_OUTPUT = HELLO_WORLD + STRING_NEWLINE +
            HELLO_WORLD + STRING_NEWLINE +
            ALICE + STRING_NEWLINE +
            ALICE;
    private static final String WITH_COUNT_AND_DUPLICATE_FLAGS_OUTPUT = "2 Hello World" + STRING_NEWLINE +
            "2 Alice";

    private static Path currPath;

    private UniqApplication uniqApplication;

    @BeforeAll
    static void setUp() throws NoSuchFieldException, IllegalAccessException {
        TestEnvironmentUtil.setCurrentDirectory(System.getProperty("user.dir"));
        currPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory());
    }

    @BeforeEach
    void init() {
        uniqApplication = new UniqApplication();
    }

    @AfterEach
    void deleteTemp() throws IOException {
        for (Path file : FILES) {
            Files.deleteIfExists(file);
        }
    }

    private Path createFile(String name) throws IOException {
        Path path = currPath.resolve(name);
        Files.createFile(path);
        FILES.push(path);
        return path;
    }

    private void writeToFile(Path path, String content) throws IOException {
        Files.write(path, content.getBytes());
    }

    @Test
    void run_NoFilesWithoutFlag_ReadsFromInputAndDisplaysAdjacentLines() throws Exception {
        String[] args = {};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(WITHOUT_FLAG_OUTPUT + STRING_NEWLINE, outputStream.toString());
    }

    @Test
    void run_NoFilesWithCountFlag_ReadsFromInputAndDisplaysCountOfAdjacentLines() throws Exception {
        String[] args = {"-c"};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(WITH_COUNT_FLAG_OUTPUT + STRING_NEWLINE, outputStream.toString());
    }

    @Test
    void run_NoFilesWithDuplicateFlag_ReadsFromInputAndDisplaysRepeatedAdjacentLinesOnlyOnce() throws Exception {
        String[] args = {"-d"};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(WITH_DUPLICATE_FLAG_OUTPUT + STRING_NEWLINE, outputStream.toString());
    }

    @Test
    void run_NoFilesWithAllDuplicateFlag_ReadsFromInputAndDisplaysRepeatedAdjacentLinesRepeatedly() throws Exception {
        String[] args = {"-D"};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(WITH_ALL_DUPLICATE_FLAG_OUTPUT + STRING_NEWLINE, outputStream.toString());
    }

    @Test
    void run_NoFilesWithDuplicateAndAllDuplicateFlags_ReadsFromInputAndDisplaysRepeatedAdjacentLinesRepeatedly() throws Exception {
        String[] args = {"-d", "-D"};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(WITH_ALL_DUPLICATE_FLAG_OUTPUT + STRING_NEWLINE, outputStream.toString());
    }

    @Test
    void run_NoFilesWithCountAndDuplicateFlags_ReadsFromInputAndDisplaysCountOfRepeatedAdjacentLinesOnlyOnce() throws Exception {
        String[] args = {"-c", "-d"};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(WITH_COUNT_AND_DUPLICATE_FLAGS_OUTPUT + STRING_NEWLINE, outputStream.toString());
    }

    @Test
    void run_NoFilesWithUnknownFlag_Throws() {
        String[] args = {"-x"};
        InputStream stdin = new ByteArrayInputStream(TEST_INPUT.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(UniqException.class, () -> uniqApplication.run(args, stdin, outputStream));
    }

    @Test
    void run_NonemptyInputFile_ReadsFileAndDisplaysAdjacentLines() throws Exception {
        Path inputPath = createFile(INPUT_FILE_TXT);
        writeToFile(inputPath, TEST_INPUT);
        String[] args = {INPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(WITHOUT_FLAG_OUTPUT + STRING_NEWLINE, outputStream.toString());
    }

    @Test
    void run_EmptyInputFile_ReadsFileAndDisplaysNewline() throws Exception {
        createFile(INPUT_FILE_TXT);
        String[] args = {INPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);
        assertEquals(STRING_NEWLINE, outputStream.toString());
    }

    @Test
    void run_NonexistentInputFile_Throws() {
        String[] args = {"nonexistent_file.txt"};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(UniqException.class, () -> uniqApplication.run(args, stdin, outputStream));
    }

    @Test
    void run_InputFileToOutputFile_DisplaysNewlineAndOverwritesOutputFile() throws Exception {
        Path inputPath = createFile(INPUT_FILE_TXT);
        Path outputPath = createFile(OUTPUT_FILE_TXT);
        writeToFile(inputPath, TEST_INPUT);
        writeToFile(outputPath, "This is the output file.");
        FILES.push(outputPath);

        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);

        assertEmptyString(outputStream.toString());
        assertArrayEquals(WITHOUT_FLAG_OUTPUT.getBytes(), Files.readAllBytes(outputPath));
    }

    @Test
    void run_InputFileToNonexistentOutputFile_DisplaysNewlineAndCreatesOutputFile() throws Exception {
        Path inputPath = createFile(INPUT_FILE_TXT);
        writeToFile(inputPath, TEST_INPUT);

        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        uniqApplication.run(args, stdin, outputStream);

        Path outputPath = currPath.resolve(OUTPUT_FILE_TXT);
        FILES.push(outputPath);

        assertEmptyString(outputStream.toString());
        assertTrue(Files.exists(outputPath));
        assertArrayEquals(WITHOUT_FLAG_OUTPUT.getBytes(), Files.readAllBytes(outputPath));
    }

    @Test
    void run_NonexistentInputFileToOutputFile_Throws() throws IOException {
        Path outputPath = createFile(OUTPUT_FILE_TXT);
        writeToFile(outputPath, "This is the output file.");
        FILES.push(outputPath);
        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(UniqException.class, () -> uniqApplication.run(args, stdin, outputStream));
    }

    @Test
    void run_NonexistentInputFileToNonexistentOutputFile_Throws() {
        String[] args = {INPUT_FILE_TXT, OUTPUT_FILE_TXT};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(UniqException.class, () -> uniqApplication.run(args, stdin, outputStream));
    }
}
