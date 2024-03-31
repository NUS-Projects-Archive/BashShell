package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.joinStringsByNewline;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

public class PasteApplicationPublicTest {

    private static final File DIRECTORY = new File("pasteTestDirectory");
    private static final File NONEXISTENT = new File("paste_nonexistent.txt");
    private static final File FILE_1 = new File("paste_1.txt");
    private static final File FILE_2 = new File("paste_2.txt");
    private static final String TEXT_FILE_1 = joinStringsByNewline("A", "B", "C", "D", "E");
    private static final String TEXT_FILE_2 = joinStringsByNewline("1", "2", "3", "4", "5");

    private PasteApplication pasteApplication;
    private String file1;
    private String file2;

    @BeforeAll
    static void setUpBeforeAll() throws IOException {
        writeToFileWithText(FILE_1, TEXT_FILE_1);
        writeToFileWithText(FILE_2, TEXT_FILE_2);
        DIRECTORY.mkdirs();
    }

    public static void writeToFileWithText(File file, String text) throws IOException {
        boolean fileCreated = file.createNewFile();
        assertTrue(fileCreated, "Failed to create file: " + file.getAbsolutePath());
        try (FileWriter writer = new FileWriter(file)) {
            if (text == null || text.isBlank()) {
                writer.close();
                return;
            }
            writer.write(text);
        }
    }

    @AfterAll
    static void tearDownAfterAll() {
        FILE_1.delete();
        FILE_2.delete();
        DIRECTORY.delete();
    }

    private void assertEqualsReplacingNewlines(String expected, String actual) {
        assertEquals(expected.replaceAll("\r\n", "\n"), actual.replaceAll("\r\n", "\n"));
    }

    @BeforeEach
    void setUp() {
        pasteApplication = new PasteApplication();
        file1 = FILE_1.getAbsolutePath();
        file2 = FILE_2.getAbsolutePath();
    }

    @Test
    void mergeFile_FileNotFound_ThrowsPasteException() {
        assertThrowsExactly(PasteException.class, () -> pasteApplication.mergeFile(true, NONEXISTENT.toString()));
    }

    @Test
    void mergeFileAndStdin_NullInputStream_ThrowsPasteException() {
        assertThrowsExactly(PasteException.class, () -> pasteApplication.mergeFileAndStdin(true, null));
    }

    @Test
    void mergeFileAndStdin_NullFilename_ThrowsPasteException() {
        assertThrowsExactly(PasteException.class, () -> pasteApplication.mergeFileAndStdin(true, System.in, null));
    }

    @Test
    void mergeStdin_NullStream_ThrowsPasteException() {
        assertThrowsExactly(PasteException.class, () -> pasteApplication.mergeStdin(true, null));
    }

    @Test
    void mergeStdin_NoSerial_ReturnsItself() {
        InputStream input = new ByteArrayInputStream(TEXT_FILE_1.getBytes());
        String result = assertDoesNotThrow(() -> pasteApplication.mergeStdin(false, input));
        assertEquals(TEXT_FILE_1, result);
    }

    @Test
    void mergeStdin_Serial_ReturnsNewlinesReplacedByTabs() {
        InputStream stream = new ByteArrayInputStream(TEXT_FILE_1.getBytes());
        String result = assertDoesNotThrow(() -> pasteApplication.mergeStdin(true, stream));
        assertEquals(TEXT_FILE_1.replaceAll(STRING_NEWLINE, String.valueOf(TestStringUtils.CHAR_TAB)), result);
    }

    @Test
    void mergeFile_NullFilename_ThrowsPasteException() {
        assertThrowsExactly(PasteException.class, () -> pasteApplication.mergeFile(true, null));
    }

    @Test
    void mergeFile_NoSerialOneFile_ReturnsItself() {
        String result = assertDoesNotThrow(() -> pasteApplication.mergeFile(false, file1));
        assertEqualsReplacingNewlines(TEXT_FILE_1, result);
    }


    @Test
    void mergeFile_NoSerialTwoFiles_ReturnsInterleaving() {
        String expected = "A\t1\nB\t2\nC\t3\nD\t4\nE\t5";
        String result = assertDoesNotThrow(() -> pasteApplication.mergeFile(false, file1, file2));
        assertEqualsReplacingNewlines(expected, result);
    }

    @Test
    void mergeFile_SerialTwoFiles_ReturnsParallel() {
        String expected = "A\tB\tC\tD\tE\n1\t2\t3\t4\t5";
        String result = assertDoesNotThrow(() -> pasteApplication.mergeFile(true, file1, file2));
        assertEqualsReplacingNewlines(expected, result);
    }
}
