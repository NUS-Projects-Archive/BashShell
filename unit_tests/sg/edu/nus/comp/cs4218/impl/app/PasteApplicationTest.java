package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_TAB;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.joinStringsByNewline;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.joinStringsByTab;
import static sg.edu.nus.comp.cs4218.test.FileUtils.createNewFile;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

public class PasteApplicationTest {

    private static final String FILE_A = "A.txt";
    private static final String FILE_B = "B.txt";
    private static final String NON_EXISTENT_FILE = "NonExistentFile.txt";
    private static final String STDIN = "-";
    private static final String FILE_CONTENT_A = joinStringsByNewline("A", "B", "C", "D", "E");
    private static final String FILE_CONTENT_B = joinStringsByNewline("1", "2", "3", "4", "5");

    private PasteApplication app;
    private String fileA;
    private String fileB;

    @BeforeEach
    void setUp() throws IOException {
        app = new PasteApplication();
        fileA = createNewFile(FILE_A, FILE_CONTENT_A).toString();
        fileB = createNewFile(FILE_B, FILE_CONTENT_B).toString();
    }

    @Test
    void mergeStdin_StdinWithoutFlag_MergesStdinInParallel() {
        String result = assertDoesNotThrow(() -> {
            InputStream inputStream = IOUtils.openInputStream(fileA);
            return app.mergeStdin(false, inputStream);
        });
        String expected = joinStringsByNewline("A", "B", "C", "D", "E");
        assertEquals(expected, result);
    }

    @Test
    void mergeStdin_StdinWithFlag_MergesStdinInSerial() {
        String result = assertDoesNotThrow(() -> {
            InputStream inputStream = IOUtils.openInputStream(fileA);
            return app.mergeStdin(true, inputStream);
        });
        String expected = joinStringsByTab("A", "B", "C", "D", "E");
        assertEquals(expected, result);
    }

    @Test
    void mergeStdin_NullStdin_ThrowsNullStreamsException() {
        assertThrowsExactly(PasteException.class, () -> app.mergeStdin(false, null));
    }

    @Test
    void mergeFile_FilesWithoutFlag_MergesFilesInParallel() {
        String result = assertDoesNotThrow(() -> app.mergeFile(false, fileA, fileB));
        String expected = "A" + STRING_TAB + "1" + STRING_NEWLINE +
                "B" + STRING_TAB + "2" + STRING_NEWLINE +
                "C" + STRING_TAB + "3" + STRING_NEWLINE +
                "D" + STRING_TAB + "4" + STRING_NEWLINE +
                "E" + STRING_TAB + "5";
        assertEquals(expected, result);
    }

    @Test
    void mergeFile_FilesWithFlag_MergesFilesInSerial() {
        String result = assertDoesNotThrow(() -> app.mergeFile(true, fileA, fileB));
        String expected = joinStringsByTab("A", "B", "C", "D", "E") + STRING_NEWLINE +
                joinStringsByTab("1", "2", "3", "4", "5");
        assertEquals(expected, result);
    }

    @Test
    void mergeFile_NonExistentFile_ThrowsFileNotFoundException() {
        assertThrowsExactly(PasteException.class, () -> app.mergeFile(false, NON_EXISTENT_FILE));
    }

    @Test
    void mergeFileAndStdin_FileAndStdinWithoutFlag_MergesFileAndStdinInParallel() {
        String result = assertDoesNotThrow(() -> {
            InputStream inputStream = IOUtils.openInputStream(fileA);
            return app.mergeFileAndStdin(false, inputStream, STDIN, fileB, STDIN);
        });
        String expected = "A" + STRING_TAB + "1" + STRING_TAB + "B" +
                STRING_NEWLINE + "C" + STRING_TAB + "2" + STRING_TAB + "D" +
                STRING_NEWLINE + "E" + STRING_TAB + "3" +
                STRING_TAB + STRING_NEWLINE + STRING_TAB + "4" +
                STRING_TAB + STRING_NEWLINE + STRING_TAB + "5" +
                STRING_TAB;
        assertEquals(expected, result);
    }

    @Test
    void mergeFileAndStdin_FileAndStdinWithFlag_MergesFileAndStdinInSerial() {
        String result = assertDoesNotThrow(() -> {
            InputStream inputStream = IOUtils.openInputStream(fileA);
            return app.mergeFileAndStdin(true, inputStream, STDIN, fileB, STDIN);
        });
        String expected = joinStringsByTab("A", "B", "C", "D", "E") + STRING_NEWLINE +
                joinStringsByTab("1", "2", "3", "4", "5");
        assertEquals(expected, result);
    }

    @Test
    void mergeFileAndStdin_NonExistentFileAndStdin_ThrowsFileNotFoundException() {
        try (InputStream inputStream = IOUtils.openInputStream(fileA)) {
            assertThrowsExactly(PasteException.class, () -> app.mergeFileAndStdin(false, inputStream, NON_EXISTENT_FILE));
        } catch (IOException | ShellException e) {
            e.printStackTrace();
        }
    }

    @Test
    void mergeInSerial() {
        List<List<String>> arg = new ArrayList<>();
        arg.add(List.of("A", "B", "C"));
        arg.add(List.of("1", "2", "3", "4"));
        arg.add(List.of("5", "4", "3"));
        arg.add(List.of("a", "b"));
        String actual = app.mergeInSerial(arg);
        System.out.println(actual);
    }

    @Test
    void mergeInParallel() {
        List<List<String>> arg = new ArrayList<>();
        arg.add(List.of("A", "B", "C"));
        arg.add(List.of("1", "2", "3", "4"));
        String actual = app.mergeInParallel(arg);
        System.out.println(actual);
    }
}
