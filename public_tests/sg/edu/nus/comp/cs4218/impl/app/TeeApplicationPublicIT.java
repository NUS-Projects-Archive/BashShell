package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

@SuppressWarnings("PMD.ClassNamingConventions")
class TeeApplicationPublicIT {
    private static final String STDIN_STRING = "Hello world!" + STRING_NEWLINE
            + "Welcome to CS4218!" + STRING_NEWLINE;
    private static final String EXISTING_FILE = "existing.txt";
    private static final String EXISTING_FILE_2 = "existing2.txt";
    private static final String NON_EXIST_FILE = "nonExistent.txt";
    private static final String NON_EXIST_FILE2 = "nonExistent2.txt";
    @TempDir
    static File tempDir;
    private static TeeApplication teeApplication;
    private static InputStream stdin;
    private static OutputStream stdout;
    private static PrintStream standardOut;

    @AfterAll
    static void tearDownAll() throws NoSuchFieldException, IllegalAccessException {
        TestEnvironmentUtil.setCurrentDirectory(System.getProperty("user.dir"));
    }

    @BeforeEach
    void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        teeApplication = new TeeApplication();
        stdin = new ByteArrayInputStream(STDIN_STRING.getBytes(StandardCharsets.UTF_8));
        standardOut = System.out;
        stdout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdout));
        TestEnvironmentUtil.setCurrentDirectory(tempDir.getAbsolutePath());
        File existing = new File(tempDir, EXISTING_FILE);
        FileWriter fileWriter = new FileWriter(existing, false);
        File existing2 = new File(tempDir, EXISTING_FILE_2);
        FileWriter fileWriter2 = new FileWriter(existing2, false);
        try {
            fileWriter.write("Hello World" + STRING_NEWLINE);
            fileWriter2.write("Hello CS4218" + STRING_NEWLINE);
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            fileWriter.close();
            fileWriter2.close();
        }
        File unwritable = new File(tempDir, "unwritable.txt");
        unwritable.createNewFile();
        unwritable.setReadOnly();
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
        for (File file : tempDir.listFiles()) {
            file.delete();
        }
    }

    // tee with single file: write to stdout and file
    @Test
    public void run_SingleFileNoAppend_WriteToFileAndStdout() throws Exception {
        String[] argList = new String[]{NON_EXIST_FILE};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
        File outputFile = new File(tempDir, NON_EXIST_FILE);
        assertTrue(outputFile.exists());
        String fileContents = Files.readString(Paths.get(outputFile.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals(STDIN_STRING, fileContents);
    }

    // tee with absolute path: write to stdout and file
    @Test
    public void run_SingleFileAbsolutePath_WriteToFileAndStdout() throws Exception {
        String[] argList = new String[]{tempDir.getAbsolutePath() + CHAR_FILE_SEP + NON_EXIST_FILE};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
        File outputFile = new File(tempDir, NON_EXIST_FILE);
        assertTrue(outputFile.exists());
        String fileContents = Files.readString(Paths.get(outputFile.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals(STDIN_STRING, fileContents);
    }

    // tee with multiple files: write to stdout and files
    @Test
    public void run_MultipleFileNoAppend_WriteToFileAndStdout() throws Exception {
        String[] argList = new String[]{NON_EXIST_FILE, NON_EXIST_FILE2};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
        File outputFile = new File(tempDir, NON_EXIST_FILE);
        assertTrue(outputFile.exists());
        String fileContents = Files.readString(Paths.get(outputFile.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals(STDIN_STRING, fileContents);
        File outputFile2 = new File(tempDir, NON_EXIST_FILE2);
        assertTrue(outputFile2.exists());
        String fileContents2 = Files.readString(Paths.get(outputFile2.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals(STDIN_STRING, fileContents2);
    }

    // tee: write to stdout
    @Test
    public void run_NoFile_WriteToStdout() throws Exception {
        String[] argList = new String[]{};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
    }

    // tee -a with single file: write to stdout and append to file
    @Test
    public void run_SingleFileAppend_WriteToFileAndStdout() throws Exception {
        String[] argList = new String[]{"-a", EXISTING_FILE};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
        File outputFile = new File(tempDir, EXISTING_FILE);
        assertTrue(outputFile.exists());
        String fileContents = Files.readString(Paths.get(outputFile.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals("Hello World" + STRING_NEWLINE + STDIN_STRING, fileContents);
    }

    // tee -a with multiple files: write to stdout and append to file
    @Test
    public void run_MultipleFileAppend_WriteToFileAndStdout() throws Exception {
        String[] argList = new String[]{"-a", EXISTING_FILE, EXISTING_FILE_2};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
        File outputFile = new File(tempDir, EXISTING_FILE);
        assertTrue(outputFile.exists());
        String fileContents = Files.readString(Paths.get(outputFile.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals("Hello World" + STRING_NEWLINE + STDIN_STRING, fileContents);

        File outputFile2 = new File(tempDir, EXISTING_FILE_2);
        assertTrue(outputFile2.exists());
        String fileContents2 = Files.readString(Paths.get(outputFile2.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals("Hello CS4218" + STRING_NEWLINE + STDIN_STRING, fileContents2);
    }

    // tee -a: write to stdout
    @Test
    public void run_NoFileWithFlag_WriteToStdout() throws Exception {
        String[] argList = new String[]{"-a"};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
    }


    // tee with invalid flag: throws exception
    @Test
    public void run_InvalidArgs_ThrowsTeeException() {
        String[] argList = new String[]{"-d", NON_EXIST_FILE2};
        assertThrows(TeeException.class, () -> teeApplication.run(argList, stdin, stdout));
    }

    // null streams: throws exception
    @Test
    public void run_StdinIsNull_ThrowsTeeException() {
        assertThrows(TeeException.class, () -> teeApplication.run(new String[0], null, stdout));
    }

    @Test
    public void run_StdoutIsNull_ThrowsTeeException() {
        assertThrows(TeeException.class, () -> teeApplication.run(new String[0], System.in, null));
    }

}
