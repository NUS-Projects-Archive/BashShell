package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.exception.CatException;

@SuppressWarnings("PMD.ClassNamingConventions")
public class CatApplicationIT {

    private CatApplication app;
    private InputStream inputStreamMock;
    private OutputStream out;
    private String fileA;
    private String fileB;
    private String fileC;

    @TempDir
    Path testDir;

    @BeforeEach
    void setUp() throws IOException {
        app = new CatApplication();
        out = new ByteArrayOutputStream();
        inputStreamMock = mock(InputStream.class);
        testDir = Files.createTempDirectory("testDir");

        Path pathA = testDir.resolve("A.txt");
        Path pathB = testDir.resolve("B.txt");
        Path pathC = testDir.resolve("C.txt");

        fileA = pathA.toString();
        fileB = pathB.toString();
        fileC = pathC.toString();

        String contentFileA = "Hello" + STRING_NEWLINE + "World";
        Files.write(pathA, List.of(contentFileA));

        String contentFileB = "Software" + STRING_NEWLINE + "Testing";
        Files.write(pathB, List.of(contentFileB));

        String contentFileC = "";
        Files.write(pathC, List.of(contentFileC));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Hello", "Hello\r\nWorld"})
    void run_NoArgsNoLineNumber_PrintsStdin(String args) {
        // Given
        String[] tokens = new String[0];
        inputStreamMock = new ByteArrayInputStream(args.getBytes(StandardCharsets.UTF_8));
        // When
        try {
            app.run(tokens, inputStreamMock, out);
            // Then
            assertEquals(args + STRING_NEWLINE, out.toString());
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
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals("1 Hello\r\n2 World" + STRING_NEWLINE, out.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Hello", "Hello\r\nWorld"})
    void run_DashOnlyNoLineNumber_PrintsStdin(String args) {
        // Given
        String[] tokens = new String[]{"-"};
        inputStreamMock = new ByteArrayInputStream(args.getBytes(StandardCharsets.UTF_8));
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals(args + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_DashOnlyHasLineNumber_PrintsLineNumberedStdin() {
        // Given
        String[] tokens = new String[]{"-n", "-"};
        inputStreamMock = new ByteArrayInputStream("Hello\r\nWorld".getBytes(StandardCharsets.UTF_8));
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals("1 Hello\r\n2 World" + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_FileOnlyNoLineNumber_PrintsConcatenatedFilesContent() {
        // Given
        String[] tokens = new String[]{fileA, fileB};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals("Hello\r\nWorld\r\nSoftware\r\n" + "Testing" + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_FileOnlyHasLineNumber_PrintsLineNumberedConcatenatedFilesContent() {
        // Given
        String[] tokens = new String[]{"-n", fileA, fileB};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals("1 Hello\r\n2 World\r\n1 Software\r\n" + "2 Testing" + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_FileAndDashNoLineNumber_PrintsConcatenatedContent() {
        // Given
        String[] tokens = new String[]{"-", fileA, fileB};
        inputStreamMock = new ByteArrayInputStream("From\r\nStdin".getBytes(StandardCharsets.UTF_8));
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals("From\r\nStdin\r\nHello\r\nWorld\r\nSoftware\r\n" + "Testing" + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_FileAndDashHasLineNumber_PrintsLineNumberedConcatenatedContent() {
        // Given
        String[] tokens = new String[]{"-n", "-", fileA, fileB};
        inputStreamMock = new ByteArrayInputStream("From\r\nStdin".getBytes(StandardCharsets.UTF_8));
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals("1 From\r\n2 Stdin\r\n1 Hello\r\n2 World\r\n1 Software\r\n" + "2 Testing" + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_InputRedirOnlyNoLineNum_PrintsInputStream() {
        // Given
        String[] tokens = new String[]{"<", fileA};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals("Hello\r\nWorld" + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_InputRedirOnlyHasLineNum_PrintsLineNumberedInputStream() {
        // Given
        String[] tokens = new String[]{"-n", "<", fileA};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals("1 Hello\r\n2 World" + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_MultipleInputRedirNoLineNum_PrintsLatestInputStream() {
        // Given
        String[] tokens = new String[]{"<", fileA, "<", fileB};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals("Software\r\nTesting" + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_MultipleInputRedirHasLineNum_PrintsLineNumberedLatestInputStream() {
        // Given
        String[] tokens = new String[]{"-n", "<", fileA, "<", fileB};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals("1 Software\r\n2 Testing" + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_InputRedirAndFilesOnlyNoLineNum_PrintsConcatenatedFilesOnly() {
        // Given
        String[] tokens = new String[]{"<", fileA, fileB};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals("Software\r\nTesting" + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_InputRedirAndFilesOnlyHasLineNum_PrintsLineNumberedConcatenatedFilesOnly() {
        // Given
        String[] tokens = new String[]{"-n", "<", fileA, fileB};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals("1 Software\r\n2 Testing" + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_OutputRedirNoLineNumber_WritesContentIntoOutputFile() {
        // Given
        String[] tokens = new String[]{fileA, fileB, ">", fileC};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        String result = assertDoesNotThrow(() -> app.readFile(false, new File(fileC)));
        // Then
        assertEquals("Hello\r\nWorld\r\nSoftware\r\n" + "Testing" + STRING_NEWLINE, result);
    }

    @Test
    void run_OutputRedirHasLineNumber_WritesLineNumberedContentIntoOutputFile() {
        // Given
        String[] tokens = new String[]{"-n", fileA, fileB, ">", fileC};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        String result = assertDoesNotThrow(() -> app.readFile(false, new File(fileC)));
        // Then
        assertEquals("1 Hello\r\n2 World\r\n1 Software\r\n" + "2 Testing" + STRING_NEWLINE, result);
    }

    @Test
    void run_MultipleOutputRedirNoLineNumber_WritesContentIntoLatestOutputFile() {
        // Given
        String[] tokens = new String[]{fileA, ">", fileB, ">", fileC};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        String result = assertDoesNotThrow(() -> app.readFile(false, new File(fileC)));
        // Then
        assertEquals("Hello\r\nWorld" + STRING_NEWLINE, result);
    }

    @Test
    void run_MultipleOutputRedirasLineNumber_WritesLineNumberedContentIntoLatestOutputFile() {
        // Given
        String[] tokens = new String[]{"-n", fileA, ">", fileB, ">", fileC};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        String result = assertDoesNotThrow(() -> app.readFile(false, new File(fileC)));
        // Then
        assertEquals("1 Hello\r\n2 World" + STRING_NEWLINE, result);
    }
}
