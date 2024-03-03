package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("PMD.ClassNamingConventions")
public class CatApplicationIT {

    private static final String HELLO_WORLD = "Hello" + STRING_NEWLINE + "World";
    private static final String L1_HELLO_L2_WORLD = "1 Hello" + STRING_NEWLINE + "2 World";
    private static final String HEY_JUNIT = "Hey" + STRING_NEWLINE + "Junit";
    private static final String L1_HEY_L2_JUNIT = "1 Hey" + STRING_NEWLINE + "2 Junit";
    private static final String FROM_STDIN = "From" + STRING_NEWLINE + "Stdin";
    private static final String L1_FROM_L2_STDIN = "1 From" + STRING_NEWLINE + "2 Stdin";
    private static final String[] PARAM_TEST_VALUES = {"", "hello", HELLO_WORLD};
    private CatApplication app;
    private InputStream inputStreamMock;
    private OutputStream out;
    private String fileA;
    private String fileB;
    private String fileC;

    private static List<String> getParams() {
        return Arrays.asList(PARAM_TEST_VALUES);
    }

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

        Files.write(pathA, List.of(HELLO_WORLD));
        Files.write(pathB, List.of(HEY_JUNIT));
        Files.write(pathC, List.of(""));
    }

    @ParameterizedTest
    @MethodSource("getParams")
    void run_NoArgsNoLineNumber_PrintsStdin(String args) {
        // Given
        String[] tokens = new String[0];
        inputStreamMock = new ByteArrayInputStream(args.getBytes(StandardCharsets.UTF_8));
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals(args + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_NoArgsHasLineNumber_PrintsLineNumberedStdin() {
        // Given
        String[] tokens = new String[]{"-n"};
        inputStreamMock = new ByteArrayInputStream(HELLO_WORLD.getBytes(StandardCharsets.UTF_8));
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals(L1_HELLO_L2_WORLD + STRING_NEWLINE, out.toString());
    }

    @ParameterizedTest
    @MethodSource("getParams")
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
        inputStreamMock = new ByteArrayInputStream(HELLO_WORLD.getBytes(StandardCharsets.UTF_8));
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals(L1_HELLO_L2_WORLD + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_FileOnlyNoLineNumber_PrintsConcatenatedFilesContent() {
        // Given
        String[] tokens = new String[]{fileA, fileB};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals(HELLO_WORLD + STRING_NEWLINE + HEY_JUNIT + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_FileOnlyHasLineNumber_PrintsLineNumberedConcatenatedFilesContent() {
        // Given
        String[] tokens = new String[]{"-n", fileA, fileB};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals(L1_HELLO_L2_WORLD + STRING_NEWLINE + L1_HEY_L2_JUNIT + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_FileAndDashNoLineNumber_PrintsConcatenatedContent() {
        // Given
        String[] tokens = new String[]{"-", fileA, fileB};
        inputStreamMock = new ByteArrayInputStream(FROM_STDIN.getBytes(StandardCharsets.UTF_8));
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals(FROM_STDIN + STRING_NEWLINE + HELLO_WORLD + STRING_NEWLINE + HEY_JUNIT + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_FileAndDashHasLineNumber_PrintsLineNumberedConcatenatedContent() {
        // Given
        String[] tokens = new String[]{"-n", "-", fileA, fileB};
        inputStreamMock = new ByteArrayInputStream(FROM_STDIN.getBytes(StandardCharsets.UTF_8));
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals(L1_FROM_L2_STDIN + STRING_NEWLINE + L1_HELLO_L2_WORLD + STRING_NEWLINE + L1_HEY_L2_JUNIT + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_InputRedirOnlyNoLineNum_PrintsInputStream() {
        // Given
        String[] tokens = new String[]{"<", fileA};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals(HELLO_WORLD + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_InputRedirOnlyHasLineNum_PrintsLineNumberedInputStream() {
        // Given
        String[] tokens = new String[]{"-n", "<", fileA};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals(L1_HELLO_L2_WORLD + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_MultipleInputRedirNoLineNum_PrintsLatestInputStream() {
        // Given
        String[] tokens = new String[]{"<", fileA, "<", fileB};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals(HEY_JUNIT + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_MultipleInputRedirHasLineNum_PrintsLineNumberedLatestInputStream() {
        // Given
        String[] tokens = new String[]{"-n", "<", fileA, "<", fileB};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals(L1_HEY_L2_JUNIT + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_InputRedirAndFilesOnlyNoLineNum_PrintsConcatenatedFilesOnly() {
        // Given
        String[] tokens = new String[]{"<", fileA, fileB};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals(HEY_JUNIT + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_InputRedirAndFilesOnlyHasLineNum_PrintsLineNumberedConcatenatedFilesOnly() {
        // Given
        String[] tokens = new String[]{"-n", "<", fileA, fileB};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        // Then
        assertEquals(L1_HEY_L2_JUNIT + STRING_NEWLINE, out.toString());
    }

    @Test
    void run_OutputRedirNoLineNumber_WritesContentIntoOutputFile() {
        // Given
        String[] tokens = new String[]{fileA, fileB, ">", fileC};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        String result = assertDoesNotThrow(() -> app.readFile(false, new File(fileC)));
        // Then
        assertEquals(HELLO_WORLD + STRING_NEWLINE + HEY_JUNIT + STRING_NEWLINE, result);
    }

    @Test
    void run_OutputRedirHasLineNumber_WritesLineNumberedContentIntoOutputFile() {
        // Given
        String[] tokens = new String[]{"-n", fileA, fileB, ">", fileC};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        String result = assertDoesNotThrow(() -> app.readFile(false, new File(fileC)));
        // Then
        assertEquals(L1_HELLO_L2_WORLD + STRING_NEWLINE + L1_HEY_L2_JUNIT + STRING_NEWLINE, result);
    }

    @Test
    void run_MultipleOutputRedirNoLineNumber_WritesContentIntoLatestOutputFile() {
        // Given
        String[] tokens = new String[]{fileA, ">", fileB, ">", fileC};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        String result = assertDoesNotThrow(() -> app.readFile(false, new File(fileC)));
        // Then
        assertEquals(HELLO_WORLD + STRING_NEWLINE, result);
    }

    @Test
    void run_MultipleOutputRedirasLineNumber_WritesLineNumberedContentIntoLatestOutputFile() {
        // Given
        String[] tokens = new String[]{"-n", fileA, ">", fileB, ">", fileC};
        // When
        assertDoesNotThrow(() -> app.run(tokens, inputStreamMock, out));
        String result = assertDoesNotThrow(() -> app.readFile(false, new File(fileC)));
        // Then
        assertEquals(L1_HELLO_L2_WORLD + STRING_NEWLINE, result);
    }
}
