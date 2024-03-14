package sg.edu.nus.comp.cs4218.impl.util;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.impl.util.AssertUtils.assertFileDoNotExists;
import static sg.edu.nus.comp.cs4218.impl.util.AssertUtils.assertFileExists;
import static sg.edu.nus.comp.cs4218.impl.util.FileUtils.createNewDirectory;
import static sg.edu.nus.comp.cs4218.impl.util.FileUtils.createNewFile;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.joinStringsByNewline;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ShellException;

@SuppressWarnings("PMD.ClassNamingConventions")
public class ApplicationRunnerIT {

    private ApplicationRunner appRunner;
    private InputStream stdin;
    private OutputStream stdout;

    @BeforeEach
    void setUp() {
        appRunner = new ApplicationRunner();
        stdin = System.in;
        stdout = new ByteArrayOutputStream();
    }

    @Test
    void runApp_EchoCommand_WritesEchoToStdout() {
        assertDoesNotThrow(() -> appRunner.runApp("echo", new String[]{"Hello", "World!"}, null, stdout));
        assertEquals("Hello World!" + STRING_NEWLINE, stdout.toString());
    }

    @Test
    void runApp_CdCommand_ChangeCurrentDirectory(@TempDir Path parentDir) {
        String childDir = createNewDirectory(parentDir, "childDir").toString();
        assertDoesNotThrow(() -> appRunner.runApp("cd", new String[]{childDir}, null, null));
        assertEquals(childDir, Environment.currentDirectory);
    }

    @Test
    void runApp_WcCommand_WritesCountToStdout() {
        String file = createNewFile("file.txt", "This is a sample text\nTo test Wc Application\n For CS4218\n").toString();
        assertDoesNotThrow(() -> appRunner.runApp("wc", new String[]{file}, stdin, stdout));
        String expected = String.format(" %7d %7d %7d %s", 3, 11, 57, file) + STRING_NEWLINE;
        assertEquals(expected, stdout.toString());
    }

    @Test
    void runApp_MkdirCommand_CreatesFolder(@TempDir Path tempDir) {
        String file = tempDir.resolve("file.txt").toString();
        assertFileDoNotExists(file);

        assertDoesNotThrow(() -> appRunner.runApp("mkdir", new String[]{file}, stdin, stdout));
        assertFileExists(file);
    }

    @Test
    void runApp_SortCommand_WritesSortedListToStdout() {
        String content = "5\nA\n2\nB\n10\no\n1\na\n3\nb";
        String file = createNewFile("file.txt", content).toString();
        assertDoesNotThrow(() -> appRunner.runApp("sort", new String[]{file}, stdin, stdout));
        String[] expectedContent = {"1", "10", "2", "3", "5", "A", "B", "a", "b", "o"};
        String expected = joinStringsByNewline(expectedContent) + STRING_NEWLINE;
        assertEquals(expected, stdout.toString());
    }

    @Test
    void runApp_CatCommand_WritesToStdout() {
        String content = "Hello" + STRING_NEWLINE + "World";
        String file = createNewFile("file.txt", content).toString();
        assertDoesNotThrow(() -> appRunner.runApp("cat", new String[]{file}, stdin, stdout));
        String expected = "Hello" + STRING_NEWLINE + "World" + STRING_NEWLINE;
        assertEquals(expected, stdout.toString());
    }

    @Test
    void runApp_ExitCommand_ReturnsExitCodeZero() {
        int exitCode = assertDoesNotThrow(() ->
                catchSystemExit(() -> appRunner.runApp("exit", null, stdin, stdout)) // When
        );
        assertEquals(0, exitCode);
    }

    @Test
    void runApp_LsCommand_WritesCwdContentsToStdout(@TempDir Path tempDir) {
        Environment.currentDirectory = tempDir.toString();
        createNewDirectory(tempDir, "dir1");
        createNewDirectory(tempDir, "dir2");
        createNewDirectory(tempDir, "dir3");
        assertDoesNotThrow(() -> appRunner.runApp("ls", new String[0], stdin, stdout));
        String expected = "dir1" + STRING_NEWLINE + "dir2" + STRING_NEWLINE + "dir3" + STRING_NEWLINE;
        assertEquals(expected, stdout.toString());
    }

    @Test
    void runApp_PasteCommand_WritesToStdout() {

    }

    @Test
    void runApp_UniqCommand_WritesToStdout() {

    }

    @Test
    void runApp_MvCommand_WritesToStdout() {

    }

    @Test
    void runApp_CutCommand_WritesToStdout() {

    }

    @Test
    void runApp_RmCommand_WritesToStdout() {

    }

    @Test
    void runApp_TeeCommand_WritesToStdout() {

    }

    @Test
    void runApp_GrepCommand_WritesToStdout() {

    }

    @ParameterizedTest
    @ValueSource(strings = {"doNotExistApp", "invalidApp", "incorrectApp"})
    void runApp_InvalidApp_ThrowsShellException(String app) {
        String[] args = {"arg1"};
        ShellException result = assertThrowsExactly(ShellException.class, () -> {
            InputStream stdin = System.in;
            OutputStream stdout = System.out;
            appRunner.runApp(app, args, stdin, stdout);
        });
        String expected = String.format("shell: %s: Invalid app", app);
        assertEquals(expected, result.getMessage());
    }
}
