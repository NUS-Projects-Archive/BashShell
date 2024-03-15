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

    private static final String FILE_A = "A.txt";
    private static final String FILE_B = "B.txt";
    private static final String NON_EXISTENT_FILE = "NonExistentFile.txt";
    private static final String STDIN = "-";
    private PasteApplication app;
    private String filePathA;
    private String filePathB;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        app = new PasteApplication();

        Path pathA = tempDir.resolve(FILE_A);
        Path pathB = tempDir.resolve(FILE_B);

        filePathA = pathA.toString();
        filePathB = pathB.toString();

        String contentFileA = "A\nB\nC\nD\nE";
        Files.write(pathA, Arrays.asList(contentFileA.split("\n")));

        String contentFileB = "1\n2\n3\n4\n5";
        Files.write(pathB, Arrays.asList(contentFileB.split("\n")));
    }

    @Test
    void mergeStdin_StdinWithoutFlag_MergesStdinInParallel() {
        String result = assertDoesNotThrow(() -> {
            InputStream inputStream = IOUtils.openInputStream(filePathA);
            return app.mergeStdin(false, inputStream);
        });
        String expected = "A" +
                StringUtils.STRING_NEWLINE + "B" +
                StringUtils.STRING_NEWLINE + "C" +
                StringUtils.STRING_NEWLINE + "D" +
                StringUtils.STRING_NEWLINE + "E";
        assertEquals(expected, result);
    }

    @Test
    void mergeStdin_StdinWithFlag_MergesStdinInSerial() {
        String result = assertDoesNotThrow(() -> {
            InputStream inputStream = IOUtils.openInputStream(filePathA);
            return app.mergeStdin(true, inputStream);
        });
        String expected = "A" +
                StringUtils.STRING_TAB + "B" +
                StringUtils.STRING_TAB + "C" +
                StringUtils.STRING_TAB + "D" +
                StringUtils.STRING_TAB + "E";
        assertEquals(expected, result);
    }

    @Test
    void mergeStdin_NullStdin_ThrowsNullStreamsException() {
        assertThrowsExactly(PasteException.class, () -> app.mergeStdin(false, null));
    }

    @Test
    void mergeFile_FilesWithoutFlag_MergesFilesInParallel() {
        String result = assertDoesNotThrow(() -> app.mergeFile(false, filePathA, filePathB));
        String expected = "A" + StringUtils.STRING_TAB + "1" +
                StringUtils.STRING_NEWLINE + "B" + StringUtils.STRING_TAB + "2" +
                StringUtils.STRING_NEWLINE + "C" + StringUtils.STRING_TAB + "3" +
                StringUtils.STRING_NEWLINE + "D" + StringUtils.STRING_TAB + "4" +
                StringUtils.STRING_NEWLINE + "E" + StringUtils.STRING_TAB + "5";
        assertEquals(expected, result);
    }

    @Test
    void mergeFile_FilesWithFlag_MergesFilesInSerial() {
        String result = assertDoesNotThrow(() -> app.mergeFile(true, filePathA, filePathB));
        String expected = "A" + StringUtils.STRING_TAB + "B" +
                StringUtils.STRING_TAB + "C" + StringUtils.STRING_TAB + "D" +
                StringUtils.STRING_TAB + "E" + StringUtils.STRING_NEWLINE + "1" +
                StringUtils.STRING_TAB + "2" + StringUtils.STRING_TAB + "3" +
                StringUtils.STRING_TAB + "4" + StringUtils.STRING_TAB + "5";
        assertEquals(expected, result);
    }

    @Test
    void mergeFile_NonExistentFile_ThrowsFileNotFoundException() {
        assertThrowsExactly(PasteException.class, () -> app.mergeFile(false, NON_EXISTENT_FILE));
    }

    @Test
    void mergeFileAndStdin_FileAndStdinWithoutFlag_MergesFileAndStdinInParallel() {
        String result = assertDoesNotThrow(() -> {
            InputStream inputStream = IOUtils.openInputStream(filePathA);
            return app.mergeFileAndStdin(false, inputStream, STDIN, filePathB, STDIN);
        });
        String expected = "A" + StringUtils.STRING_TAB + "1" + StringUtils.STRING_TAB + "B" +
                StringUtils.STRING_NEWLINE + "C" + StringUtils.STRING_TAB + "2" + StringUtils.STRING_TAB + "D" +
                StringUtils.STRING_NEWLINE + "E" + StringUtils.STRING_TAB + "3" +
                StringUtils.STRING_TAB + StringUtils.STRING_NEWLINE + StringUtils.STRING_TAB + "4" +
                StringUtils.STRING_TAB + StringUtils.STRING_NEWLINE + StringUtils.STRING_TAB + "5" +
                StringUtils.STRING_TAB;
        assertEquals(expected, result);
    }

    @Test
    void mergeFileAndStdin_FileAndStdinWithFlag_MergesFileAndStdinInSerial() {
        String result = assertDoesNotThrow(() -> {
            InputStream inputStream = IOUtils.openInputStream(filePathA);
            return app.mergeFileAndStdin(true, inputStream, STDIN, filePathB, STDIN);
        });
        String expected = "A" + StringUtils.STRING_TAB + "B" +
                StringUtils.STRING_TAB + "C" + StringUtils.STRING_TAB + "D" +
                StringUtils.STRING_TAB + "E" + StringUtils.STRING_NEWLINE + "1" +
                StringUtils.STRING_TAB + "2" + StringUtils.STRING_TAB + "3" +
                StringUtils.STRING_TAB + "4" + StringUtils.STRING_TAB + "5";
        assertEquals(expected, result);
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
