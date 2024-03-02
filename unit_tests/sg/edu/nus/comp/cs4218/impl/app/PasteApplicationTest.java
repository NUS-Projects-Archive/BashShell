package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

public class PasteApplicationTest {
    private static final String FILE_NAME_A = "A.txt";
    private static final String FILE_NAME_B = "B.txt";
    private static final String NON_EXISTENT_FILE = "NonExistentFile.txt";
    private static final String STDIN = "-";
    private PasteApplication app;
    @TempDir
    private Path pasteTestDir;
    private String filePathA;
    private String filePathB;

    @BeforeEach
    void setUp() throws IOException {
        app = new PasteApplication();
        pasteTestDir = Files.createTempDirectory("pasteTestDir");

        Path pathA = pasteTestDir.resolve(FILE_NAME_A);
        Path pathB = pasteTestDir.resolve(FILE_NAME_B);

        filePathA = pathA.toString();
        filePathB = pathB.toString();

        String contentFileA = "A\nB\nC\nD\nE";
        Files.write(pathA, Arrays.asList(contentFileA.split("\n")));

        String contentFileB = "1\n2\n3\n4\n5";
        Files.write(pathB, Arrays.asList(contentFileB.split("\n")));

    }

    @Test
    void mergeStdin_StdinWithoutFlag_MergesStdinInParallel() {
        String expected = "A" + StringUtils.STRING_NEWLINE + "B" +
                StringUtils.STRING_NEWLINE + "C" +
                StringUtils.STRING_NEWLINE + "D" +
                StringUtils.STRING_NEWLINE + "E";

        try (InputStream inputStream = IOUtils.openInputStream(filePathA)) {
            assertDoesNotThrow(() -> {
                String result = app.mergeStdin(false, inputStream);
                assertEquals(result, expected);
            });
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void mergeStdin_StdinWithFlag_MergesStdinInSerial() {
        String expected = "A" + StringUtils.STRING_TAB + "B" +
                StringUtils.STRING_TAB + "C" +
                StringUtils.STRING_TAB + "D" +
                StringUtils.STRING_TAB + "E";

        try (InputStream inputStream = IOUtils.openInputStream(filePathA)) {
            assertDoesNotThrow(() -> {
                String result = app.mergeStdin(true, inputStream);
                assertEquals(result, expected);
            });
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void mergeStdin_NullStdin_ThrowsNullStreamsException() {
        assertThrowsExactly(PasteException.class, () -> app.mergeStdin(false, null));
    }

    @Test
    void mergeFile_FilesWithoutFlag_MergesFilesInParallel() {
        String expected = "A" + StringUtils.STRING_TAB + "1" +
                StringUtils.STRING_NEWLINE + "B" + StringUtils.STRING_TAB + "2" +
                StringUtils.STRING_NEWLINE + "C" + StringUtils.STRING_TAB + "3" +
                StringUtils.STRING_NEWLINE + "D" + StringUtils.STRING_TAB + "4" +
                StringUtils.STRING_NEWLINE + "E" + StringUtils.STRING_TAB + "5";
        assertDoesNotThrow(() -> {
            String result = app.mergeFile(false, filePathA, filePathB);
            assertEquals(expected, result);
        });
    }

    @Test
    void mergeFile_FilesWithFlag_MergesFilesInSerial() {
        String expected = "A" + StringUtils.STRING_TAB + "B" +
                StringUtils.STRING_TAB + "C" + StringUtils.STRING_TAB + "D" +
                StringUtils.STRING_TAB + "E" + StringUtils.STRING_NEWLINE + "1" +
                StringUtils.STRING_TAB + "2" + StringUtils.STRING_TAB + "3" +
                StringUtils.STRING_TAB + "4" + StringUtils.STRING_TAB + "5";
        assertDoesNotThrow(() -> {
            String result = app.mergeFile(true, filePathA, filePathB);
            assertEquals(expected, result);
        });
    }

    @Test
    void mergeFile_NonExistentFile_ThrowsFileNotFoundException() {
        assertThrowsExactly(PasteException.class, () -> app.mergeFile(false, NON_EXISTENT_FILE));
    }

    @Test
    void mergeFileAndStdin_FileAndStdinWithoutFlag_MergesFileAndStdinInParallel() {
        String expected = "A" + StringUtils.STRING_TAB + "1" + StringUtils.STRING_TAB + "B" +
                StringUtils.STRING_NEWLINE + "C" + StringUtils.STRING_TAB + "2" + StringUtils.STRING_TAB + "D" +
                StringUtils.STRING_NEWLINE + "E" + StringUtils.STRING_TAB + "3" +
                StringUtils.STRING_TAB + StringUtils.STRING_NEWLINE + StringUtils.STRING_TAB + "4" +
                StringUtils.STRING_TAB + StringUtils.STRING_NEWLINE + StringUtils.STRING_TAB + "5" +
                StringUtils.STRING_TAB;

        try (InputStream inputStream = IOUtils.openInputStream(filePathA)) {
            assertDoesNotThrow(() -> {
                String result = app.mergeFileAndStdin(false, inputStream, STDIN, filePathB, STDIN);
                assertEquals(result, expected);
            });
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void mergeFileAndStdin_FileAndStdinWithFlag_MergesFileAndStdinInSerial() {
        String expected = "A" + StringUtils.STRING_TAB + "B" +
                StringUtils.STRING_TAB + "C" + StringUtils.STRING_TAB + "D" +
                StringUtils.STRING_TAB + "E" + StringUtils.STRING_NEWLINE + "1" +
                StringUtils.STRING_TAB + "2" + StringUtils.STRING_TAB + "3" +
                StringUtils.STRING_TAB + "4" + StringUtils.STRING_TAB + "5" + StringUtils.STRING_NEWLINE;

        try (InputStream inputStream = IOUtils.openInputStream(filePathA)) {
            assertDoesNotThrow(() -> {
                String result = app.mergeFileAndStdin(true, inputStream, STDIN, filePathB, STDIN);
                assertEquals(expected, result);
            });
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void mergeFileAndStdin_NonExistentFileAndStdin_ThrowsFileNotFoundException() {
        try (InputStream inputStream = IOUtils.openInputStream(filePathA)) {
            assertThrowsExactly(PasteException.class, () -> app.mergeFileAndStdin(false, inputStream, NON_EXISTENT_FILE));
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }
}
