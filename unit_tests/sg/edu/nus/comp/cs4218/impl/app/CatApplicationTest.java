package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.CatException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class CatApplicationTest {

    private CatApplication app;
    private BufferedReader brMock;
    private InputStream inputStreamMock;
    private OutputStream out;
    private String fileA;
    private String fileB;
    private String fileC;
    private static final String CAT_EXCEPTION_MSG = "cat: ";
    private static final String NON_EXISTENT_FILE = "nonexistent.txt";

    @TempDir
    Path testDir;

    @BeforeEach
    void setUp() throws IOException {
        this.app = new CatApplication();
        this.out = new ByteArrayOutputStream();
        brMock = mock(BufferedReader.class);
        inputStreamMock = mock(InputStream.class);
        testDir = Files.createTempDirectory("testDir");

        Path pathA = testDir.resolve("A.txt");
        Path pathB = testDir.resolve("B.txt");
        Path pathC = testDir.resolve("C.txt");

        this.fileA = pathA.toString();
        this.fileB = pathB.toString();
        this.fileC = pathC.toString();

        String contentFileA = "Hello" + STRING_NEWLINE + "World";
        Files.write(pathA, List.of(contentFileA));

        String contentFileB = "Software" + STRING_NEWLINE + "Testing";
        Files.write(pathB, List.of(contentFileB));

        String contentFileC = "";
        Files.write(pathC, List.of(contentFileC));
    }
    @Test
    void readFile_FileNotFound_ThrowsCatException() {
        Throwable result = assertThrows(CatException.class, () -> {
            app.readFile(false, new File(NON_EXISTENT_FILE));
        });
        assertTrue(result.getMessage().contains(CAT_EXCEPTION_MSG + ERR_FILE_NOT_FOUND));
    }

    @Test
    void readFile_ValidFileNoLineNumber_ReturnsFileContent() {
        try {
            String result = app.readFile(false, new File(this.fileA));
            String expected = "Hello" + STRING_NEWLINE + "World" + STRING_NEWLINE;
            assertEquals(expected, result);
        } catch (CatException | IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void readFile_ValidFileHasLineNumber_ReturnsLineNumberedFileContent() {
        try {
            String result = app.readFile(true, new File(this.fileA));
            String expected = "1 Hello" + STRING_NEWLINE + "2 World" + STRING_NEWLINE;
            assertEquals(expected, result);
        } catch (CatException | IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void readStdin_IOExceptionWhenReadingInput_ThrowsCatException() {
        try {
            when(brMock.readLine()).thenThrow(new IOException());
            Throwable result = assertThrows(CatException.class, () -> {
                app.readStdIn(false, brMock);
            });
            assertTrue(result.getMessage().contains(CAT_EXCEPTION_MSG + ERR_IO_EXCEPTION));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Hello", "Hello\r\nWorld"})
    void readStdin_ValidInputStreamNoLineNumber_ReturnsUserInput(String args) {
        try {
            brMock = new BufferedReader(new StringReader(args));
            String result = app.readStdIn(false, brMock);
            String expected = args + STRING_NEWLINE;
            assertEquals(expected, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void readStdin_ValidInputStreamHasLineNumber_ReturnsLineNumberedUserInput() {
        try {
            brMock = new BufferedReader(new StringReader("Hello\r\nWorld"));
            String result = app.readStdIn(true, brMock);
            String expected = "1 Hello" + STRING_NEWLINE + "2 World" + STRING_NEWLINE;
            assertEquals(expected, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catStdin_IOExceptionWhenReadingInput_ThrowsCatException() {
        try {
            when(inputStreamMock.read()).thenThrow(new IOException());
            Throwable result = assertThrows(CatException.class, () -> {
                app.catStdin(false, inputStreamMock);
            });
            assertTrue(result.getMessage().contains(CAT_EXCEPTION_MSG + ERR_IO_EXCEPTION));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Hello", "Hello\r\nWorld"})
    void catStdin_ValidInputStreamNoLineNumber_ReturnsUserInput(String args) {
        try {
            inputStreamMock = new ByteArrayInputStream(args.getBytes(StandardCharsets.UTF_8));
            String result = app.catStdin(false, inputStreamMock);
            assertEquals(args + STRING_NEWLINE, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catStdin_ValidInputStreamHasLineNumber_ReturnsLineNumberedUserInput() {
        try {
            inputStreamMock = new ByteArrayInputStream("Hello\r\nWorld".getBytes(StandardCharsets.UTF_8));
            String result = app.catStdin(true, inputStreamMock);
            String expected = "1 Hello" + STRING_NEWLINE + "2 World" + STRING_NEWLINE;
            assertEquals(expected, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFiles_NoFiles_ReturnsEmptyString() {
        try {
            String result = app.catFiles(false, new String[0]);
            assertEquals("", result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFiles_OneFileNoLineNumber_ReturnsFileContent() {
        try {
            String result = app.catFiles(false, this.fileA);
            assertEquals("Hello\r\nWorld" + STRING_NEWLINE, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFiles_OneFileHasLineNumber_ReturnsLineNumberedFileContent() {
        try {
            String result = app.catFiles(true, this.fileA);
            assertEquals("1 Hello\r\n2 World" + STRING_NEWLINE, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFiles_MultipleFilesNoLineNumber_ReturnsConcatenatedFileContent() {
        try {
            String result = app.catFiles(false, new String[]{this.fileA, this.fileB});
            assertEquals("Hello\r\nWorld\r\nSoftware\r\nTesting" + STRING_NEWLINE, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFiles_MultipleFilesHasLineNumber_ReturnsLineNumberedConcatenatedFileContent() {
        try {
            String result = app.catFiles(true, new String[]{this.fileA, this.fileB});
            assertEquals("1 Hello\r\n2 World\r\n1 Software\r\n2 Testing" + STRING_NEWLINE, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Hello", "Hello\r\nWorld"})
    void catFileAndStdin_StdinOnlyNoLineNumber_ReturnsUserInput(String args) {
        try {
            inputStreamMock = new ByteArrayInputStream(args.getBytes(StandardCharsets.UTF_8));
            String result = app.catFileAndStdin(false, inputStreamMock, "-");
            assertEquals(args + STRING_NEWLINE, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFileAndStdin_StdinOnlyHasLineNumber_ReturnsLineNumberedUserInput() {
        try {
            inputStreamMock = new ByteArrayInputStream("Hello\r\nWorld".getBytes(StandardCharsets.UTF_8));
            String result = app.catFileAndStdin(true, inputStreamMock, "-");
            String expected = "1 Hello" + STRING_NEWLINE + "2 World" + STRING_NEWLINE;
            assertEquals(expected, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFileAndStdin_FileOnlyNoLineNumber_ReturnsFileContent() {
        try {
            String result = app.catFileAndStdin(false, inputStreamMock, new String[]{this.fileA, this.fileB});
            assertEquals("Hello\r\nWorld\r\nSoftware\r\nTesting" + STRING_NEWLINE, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFileAndStdin_FileOnlyHasLineNumber_ReturnsLineNumberedFileContent() {
        try {
            String result = app.catFileAndStdin(true, inputStreamMock, new String[]{this.fileA, this.fileB});
            assertEquals("1 Hello\r\n2 World\r\n1 Software\r\n2 Testing" + STRING_NEWLINE, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFileAndStdin_FileAndStdInNoLineNumber_ReturnsConcatenatedFileAndStdin() {
        try {
            inputStreamMock = new ByteArrayInputStream("From\r\nStdin".getBytes(StandardCharsets.UTF_8));
            String result = app.catFileAndStdin(false, inputStreamMock, new String[]{this.fileA, "-", this.fileB});
            String expected = "Hello\r\nWorld\r\nFrom\r\n" +
                    "Stdin\r\nSoftware\r\nTesting" + STRING_NEWLINE;
            assertEquals(expected, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void catFileAndStdin_FileAndStdInHasLineNumber_ReturnsLineNumberedConcatenatedFileAndStdin() {
        try {
            inputStreamMock = new ByteArrayInputStream("From\r\nStdin".getBytes(StandardCharsets.UTF_8));
            String result = app.catFileAndStdin(true, inputStreamMock, new String[]{this.fileA, "-", this.fileB});
            String expected = "1 Hello\r\n2 World\r\n1 From\r\n" +
                    "2 Stdin\r\n1 Software\r\n2 Testing" + STRING_NEWLINE;
            assertEquals(expected, result);
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Hello", "Hello\r\nWorld"})
    void run_NoArgsNoLineNumber_PrintsStdin(String args) {
        // Given
        String[] tokens = new String[0];
        inputStreamMock = new ByteArrayInputStream(args.getBytes(StandardCharsets.UTF_8));
        // When
        try {
            this.app.run(tokens, inputStreamMock, this.out);
            // Then
            assertEquals(args + STRING_NEWLINE, this.out.toString());
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void run_NoArgsHasLineNumber_PrintsLineNumberedStdin() {
        // Given
        String[] tokens = new String[]{"-n"};
        inputStreamMock = new ByteArrayInputStream("Hello\r\nWorld".getBytes(StandardCharsets.UTF_8));
        // When
        try {
            this.app.run(tokens, inputStreamMock, this.out);
            // Then
            assertEquals("1 Hello\r\n2 World" + STRING_NEWLINE, this.out.toString());
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Hello", "Hello\r\nWorld"})
    void run_DashOnlyNoLineNumber_PrintsStdin(String args) {
        // Given
        String[] tokens = new String[]{"-"};
        inputStreamMock = new ByteArrayInputStream(args.getBytes(StandardCharsets.UTF_8));
        // When
        try {
            this.app.run(tokens, inputStreamMock, this.out);
            // Then
            assertEquals(args + STRING_NEWLINE, this.out.toString());
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void run_DashOnlyHasLineNumber_PrintsLineNumberedStdin() {
        // Given
        String[] tokens = new String[]{"-n", "-"};
        inputStreamMock = new ByteArrayInputStream("Hello\r\nWorld".getBytes(StandardCharsets.UTF_8));
        // When
        try {
            this.app.run(tokens, inputStreamMock, this.out);
            // Then
            assertEquals("1 Hello\r\n2 World" + STRING_NEWLINE, this.out.toString());
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void run_FileOnlyNoLineNumber_PrintsConcatenatedFilesContent() {
        // Given
        String[] tokens = new String[]{this.fileA, this.fileB};
        // When
        try {
            this.app.run(tokens, inputStreamMock, this.out);
            // Then
            assertEquals("Hello\r\nWorld\r\nSoftware\r\n" +
                    "Testing" + STRING_NEWLINE, this.out.toString());
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void run_FileOnlyHasLineNumber_PrintsLineNumberedConcatenatedFilesContent() {
        // Given
        String[] tokens = new String[]{"-n", this.fileA, this.fileB};
        // When
        try {
            this.app.run(tokens, inputStreamMock, this.out);
            // Then
            assertEquals("1 Hello\r\n2 World\r\n1 Software\r\n" +
                    "2 Testing" + STRING_NEWLINE, this.out.toString());
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void run_FileAndDashNoLineNumber_PrintsConcatenatedContent() {
        // Given
        String[] tokens = new String[]{"-", this.fileA, this.fileB};
        inputStreamMock = new ByteArrayInputStream("From\r\nStdin".getBytes(StandardCharsets.UTF_8));
        // When
        try {
            this.app.run(tokens, inputStreamMock, this.out);
            // Then
            assertEquals("From\r\nStdin\r\nHello\r\nWorld\r\nSoftware\r\n" +
                    "Testing" + STRING_NEWLINE, this.out.toString());
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void run_FileAndDashHasLineNumber_PrintsLineNumberedConcatenatedContent() {
        // Given
        String[] tokens = new String[]{"-n", "-", this.fileA, this.fileB};
        inputStreamMock = new ByteArrayInputStream("From\r\nStdin".getBytes(StandardCharsets.UTF_8));
        // When
        try {
            this.app.run(tokens, inputStreamMock, this.out);
            // Then
            assertEquals("1 From\r\n2 Stdin\r\n1 Hello\r\n2 World\r\n1 Software\r\n" +
                    "2 Testing" + STRING_NEWLINE, this.out.toString());
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void run_InputRedirOnlyNoLineNum_PrintsInputStream() {
        // Given
        String[] tokens = new String[]{"<", this.fileA};
        // When
        try {
            this.app.run(tokens, inputStreamMock, this.out);
            // Then
            assertEquals("Hello\r\nWorld" + STRING_NEWLINE, this.out.toString());
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void run_InputRedirOnlyHasLineNum_PrintsLineNumberedInputStream() {
        // Given
        String[] tokens = new String[]{"-n", "<", this.fileA};
        // When
        try {
            this.app.run(tokens, inputStreamMock, this.out);
            // Then
            assertEquals("1 Hello\r\n2 World" + STRING_NEWLINE, this.out.toString());
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void run_MultipleInputRedirNoLineNum_PrintsLatestInputStream() {
        // Given
        String[] tokens = new String[]{"<", this.fileA, "<", this.fileB};
        // When
        try {
            this.app.run(tokens, inputStreamMock, this.out);
            // Then
            assertEquals("Software\r\nTesting" + STRING_NEWLINE, this.out.toString());
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void run_MultipleInputRedirHasLineNum_PrintsLineNumberedLatestInputStream() {
        // Given
        String[] tokens = new String[]{"-n", "<", this.fileA, "<", this.fileB};
        // When
        try {
            this.app.run(tokens, inputStreamMock, this.out);
            // Then
            assertEquals("1 Software\r\n2 Testing" + STRING_NEWLINE, this.out.toString());
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void run_InputRedirAndFilesOnlyNoLineNum_PrintsConcatenatedFilesOnly() {
        // Given
        String[] tokens = new String[]{"<", this.fileA, this.fileB};
        // When
        try {
            this.app.run(tokens, inputStreamMock, this.out);
            // Then
            assertEquals("Software\r\nTesting" + STRING_NEWLINE, this.out.toString());
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void run_InputRedirAndFilesOnlyHasLineNum_PrintsLineNumberedConcatenatedFilesOnly() {
        // Given
        String[] tokens = new String[]{"-n", "<", this.fileA, this.fileB};
        // When
        try {
            this.app.run(tokens, inputStreamMock, this.out);
            // Then
            assertEquals("1 Software\r\n2 Testing" + STRING_NEWLINE, this.out.toString());
        } catch (CatException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void run_OutputRedirNoLineNumber_WritesContentIntoOutputFile() {
        // Given
        String[] tokens = new String[]{this.fileA, this.fileB, ">", this.fileC};
        // When
        try {
            this.app.run(tokens, inputStreamMock, this.out);
            String result = app.readFile(false, new File(this.fileC));
            // Then
            assertEquals("Hello\r\nWorld\r\nSoftware\r\n" +
                    "Testing" + STRING_NEWLINE, result);
        } catch (CatException | IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void run_OutputRedirHasLineNumber_WritesLineNumberedContentIntoOutputFile() {
        // Given
        String[] tokens = new String[]{"-n", this.fileA, this.fileB, ">", this.fileC};
        // When
        try {
            this.app.run(tokens, inputStreamMock, this.out);
            String result = app.readFile(false, new File(this.fileC));
            // Then
            assertEquals("1 Hello\r\n2 World\r\n1 Software\r\n" +
                    "2 Testing" + STRING_NEWLINE, result);
        } catch (CatException | IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void run_MultipleOutputRedirNoLineNumber_WritesContentIntoLatestOutputFile() {
        // Given
        String[] tokens = new String[]{this.fileA, ">", this.fileB, ">", this.fileC};
        // When
        try {
            this.app.run(tokens, inputStreamMock, this.out);
            String result = app.readFile(false, new File(this.fileC));
            // Then
            assertEquals("Hello\r\nWorld" + STRING_NEWLINE, result);
        } catch (CatException | IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void run_MultipleOutputRedirasLineNumber_WritesLineNumberedContentIntoLatestOutputFile() {
        // Given
        String[] tokens = new String[]{"-n", this.fileA, ">", this.fileB, ">", this.fileC};
        // When
        try {
            this.app.run(tokens, inputStreamMock, this.out);
            String result = app.readFile(false, new File(this.fileC));
            // Then
            assertEquals("1 Hello\r\n2 World" + STRING_NEWLINE, result);
        } catch (CatException | IOException e) {
            fail(e.getMessage());
        }
    }
}