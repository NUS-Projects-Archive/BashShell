package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import sg.edu.nus.comp.cs4218.exception.CatException;

@SuppressWarnings("PMD.ClassNamingConventions")
public class CatApplicationIT {

    private static final String HELLO_WORLD = "Hello" + STRING_NEWLINE + "World";
    private static final String L1_HELLO_L2_WORLD = "1 Hello" + STRING_NEWLINE + "2 World";
    private static final String HEY_JUNIT = "Hey" + STRING_NEWLINE + "Junit";
    private static final String L1_HEY_L2_JUNIT = "1 Hey" + STRING_NEWLINE + "2 Junit";
    private static final String FROM_STDIN = "From" + STRING_NEWLINE + "Stdin";
    private static final String L1_FROM_L2_STDIN = "1 From" + STRING_NEWLINE + "2 Stdin";
    private static final String[] PARAM_TEST_VALUES = {"hello", HELLO_WORLD};
    private CatApplication app;
    private InputStream mockStdin;
    private OutputStream stdout;
    private String fileA;
    private String fileB;

    private static List<String> getParams() {
        return Arrays.asList(PARAM_TEST_VALUES);
    }

    @BeforeEach
    void setUp() throws IOException {
        app = new CatApplication();
        stdout = new ByteArrayOutputStream();
        mockStdin = mock(InputStream.class);

        fileA = createNewFile("fileA.txt", HELLO_WORLD).toString();
        fileB = createNewFile("fileB.txt", HEY_JUNIT).toString();
    }

    @Test
    void run_NoStdin_ThrowsCatException() {
        CatException result = assertThrowsExactly(CatException.class, () -> app.run(null, null, null));
        String expected = "cat: InputStream not provided";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_NoStdout_ThrowsCatException() {
        CatException result = assertThrowsExactly(CatException.class, () -> {
            InputStream mockStdin = mock(InputStream.class);
            app.run(null, mockStdin, null);
        });
        String expected = "cat: OutputStream not provided";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_FailsToWriteToOutputStream_ThrowsCatException() {
        String[] args = new String[0];
        CatException result = assertThrowsExactly(CatException.class, () -> {
            OutputStream mockStdout = mock(OutputStream.class);
            doThrow(new IOException()).when(mockStdout).write(any(byte[].class));
            app.run(args, mockStdin, mockStdout);
        });
        String expected = "cat: IOException";
        assertEquals(expected, result.getMessage());
    }

    @ParameterizedTest
    @MethodSource("getParams")
    void run_NoArgsNoLineNumber_WritesStdinToOutput(String params) {
        String[] args = new String[0];
        mockStdin = new ByteArrayInputStream(params.getBytes(StandardCharsets.UTF_8));
        assertDoesNotThrow(() -> app.run(args, mockStdin, stdout));
        String expected = params + STRING_NEWLINE;
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_NoArgsHasLineNumber_WritesLineNumberedStdinToOutput() {
        String[] args = new String[]{"-n"};
        mockStdin = new ByteArrayInputStream(HELLO_WORLD.getBytes(StandardCharsets.UTF_8));
        assertDoesNotThrow(() -> app.run(args, mockStdin, stdout));
        String expected = L1_HELLO_L2_WORLD + STRING_NEWLINE;
        assertEquals(expected, stdout.toString());
    }

    @ParameterizedTest
    @MethodSource("getParams")
    void run_DashOnlyNoLineNumber_WritesStdinToOutput(String params) {
        String[] args = new String[]{"-"};
        mockStdin = new ByteArrayInputStream(params.getBytes(StandardCharsets.UTF_8));
        assertDoesNotThrow(() -> app.run(args, mockStdin, stdout));
        String expected = params + STRING_NEWLINE;
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_DashOnlyHasLineNumber_WritesLineNumberedStdinToOutput() {
        String[] args = new String[]{"-n", "-"};
        mockStdin = new ByteArrayInputStream(HELLO_WORLD.getBytes(StandardCharsets.UTF_8));
        assertDoesNotThrow(() -> app.run(args, mockStdin, stdout));
        String expected = L1_HELLO_L2_WORLD + STRING_NEWLINE;
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_FileOnlyNoLineNumber_WritesConcatenatedFilesContentToOutput() {
        String[] args = new String[]{fileA, fileB};
        assertDoesNotThrow(() -> app.run(args, mockStdin, stdout));
        String expected = HELLO_WORLD + STRING_NEWLINE + HEY_JUNIT + STRING_NEWLINE;
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_FileOnlyHasLineNumber_WritesLineNumberedConcatenatedFilesContentToOutput() {
        String[] args = new String[]{"-n", fileA, fileB};
        assertDoesNotThrow(() -> app.run(args, mockStdin, stdout));
        String expected = L1_HELLO_L2_WORLD + STRING_NEWLINE + L1_HEY_L2_JUNIT + STRING_NEWLINE;
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_FileAndDashNoLineNumber_WritesConcatenatedContentToOutput() {
        String[] args = new String[]{"-", fileA, fileB};
        mockStdin = new ByteArrayInputStream(FROM_STDIN.getBytes(StandardCharsets.UTF_8));
        assertDoesNotThrow(() -> app.run(args, mockStdin, stdout));
        String expected = FROM_STDIN + STRING_NEWLINE + HELLO_WORLD + STRING_NEWLINE + HEY_JUNIT + STRING_NEWLINE;
        assertEquals(expected, stdout.toString());
    }

    @Test
    void run_FileAndDashHasLineNumber_WritesLineNumberedConcatenatedContentToOutput() {
        String[] args = new String[]{"-n", "-", fileA, fileB};
        mockStdin = new ByteArrayInputStream(FROM_STDIN.getBytes(StandardCharsets.UTF_8));
        assertDoesNotThrow(() -> app.run(args, mockStdin, stdout));
        String expected = L1_FROM_L2_STDIN + STRING_NEWLINE + L1_HELLO_L2_WORLD + STRING_NEWLINE + L1_HEY_L2_JUNIT + STRING_NEWLINE;
        assertEquals(expected, stdout.toString());
    }

    @ParameterizedTest
    @MethodSource("getParams")
    void catFileAndStdin_StdinOnlyNoLineNumber_ReturnsUserInput(String args) {
        mockStdin = new ByteArrayInputStream(args.getBytes(StandardCharsets.UTF_8));
        String result = assertDoesNotThrow(() -> app.catFileAndStdin(false, mockStdin, "-"));
        assertEquals(args, result);
    }

    @Test
    void catFileAndStdin_StdinOnlyHasLineNumber_ReturnsLineNumberedUserInput() {
        mockStdin = new ByteArrayInputStream(HELLO_WORLD.getBytes(StandardCharsets.UTF_8));
        String result = assertDoesNotThrow(() -> app.catFileAndStdin(true, mockStdin, "-"));
        assertEquals(L1_HELLO_L2_WORLD, result);
    }

    @Test
    void catFileAndStdin_FileOnlyNoLineNumber_ReturnsFileContent() {
        String result = assertDoesNotThrow(() -> app.catFileAndStdin(false, mockStdin, fileA, fileB));
        String expected = HELLO_WORLD + STRING_NEWLINE + HEY_JUNIT;
        assertEquals(expected, result);
    }

    @Test
    void catFileAndStdin_FileOnlyHasLineNumber_ReturnsLineNumberedFileContent() {
        String result = assertDoesNotThrow(() -> app.catFileAndStdin(true, mockStdin, fileA, fileB));
        String expected = L1_HELLO_L2_WORLD + STRING_NEWLINE + L1_HEY_L2_JUNIT;
        assertEquals(expected, result);
    }

    @Test
    void catFileAndStdin_FileAndStdInNoLineNumber_ReturnsConcatenatedFileAndStdin() {
        mockStdin = new ByteArrayInputStream(FROM_STDIN.getBytes(StandardCharsets.UTF_8));
        String result = assertDoesNotThrow(() -> app.catFileAndStdin(false, mockStdin, fileA, "-", fileB));
        String expected = HELLO_WORLD + STRING_NEWLINE + FROM_STDIN + STRING_NEWLINE + HEY_JUNIT;
        assertEquals(expected, result);
    }

    @Test
    void catFileAndStdin_FileAndStdInHasLineNumber_ReturnsLineNumberedConcatenatedFileAndStdin() {
        mockStdin = new ByteArrayInputStream(FROM_STDIN.getBytes(StandardCharsets.UTF_8));
        String result = assertDoesNotThrow(() -> app.catFileAndStdin(true, mockStdin, fileA, "-", fileB));
        String expected = L1_HELLO_L2_WORLD + STRING_NEWLINE + L1_FROM_L2_STDIN + STRING_NEWLINE + L1_HEY_L2_JUNIT;
        assertEquals(expected, result);
    }
}
