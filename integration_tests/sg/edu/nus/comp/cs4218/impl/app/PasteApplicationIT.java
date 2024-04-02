package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_TAB;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFile;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.joinStringsByNewline;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.joinStringsByTab;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

@SuppressWarnings("PMD.ClassNamingConventions")
public class PasteApplicationIT {

    private static final String FILE_A = "A.txt";
    private static final String FILE_B = "B.txt";
    private static final String FILE_CONTENT_A = joinStringsByNewline("A", "B", "C", "D", "E");
    private static final String FILE_CONTENT_B = joinStringsByNewline("1", "2", "3", "4", "5");
    private static final String STDIN = "-";
    private static final String PASTE_EX_MSG = "paste: ";

    private PasteApplication app;
    private ByteArrayOutputStream output;
    private String fileA;
    private String fileB;

    @BeforeEach
    void setUp() throws IOException {
        app = new PasteApplication();
        output = new ByteArrayOutputStream();
        fileA = createNewFile(FILE_A, FILE_CONTENT_A).toString();
        fileB = createNewFile(FILE_B, FILE_CONTENT_B).toString();
    }

    @Test
    void run_NullStdin_ThrowsPasteException() {
        PasteException result = assertThrowsExactly(PasteException.class, () -> app.run(new String[]{fileA}, null,
                System.out));
        assertEquals(PASTE_EX_MSG + ERR_NO_ISTREAM, result.getMessage());
    }

    @Test
    void run_NullStdout_ThrowsPasteException() {
        PasteException result = assertThrowsExactly(PasteException.class, () -> app.run(new String[]{fileA},
                System.in, null));
        assertEquals(PASTE_EX_MSG + ERR_NULL_STREAMS, result.getMessage());
    }

    @Test
    void run_NoFlagsAndOneFile_PrintsFileInParallel() {
        assertDoesNotThrow(() -> app.run(new String[]{fileA}, System.in, output));
        String expected = joinStringsByNewline("A", "B", "C", "D", "E") + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_NoFlagsAndMultipleFiles_PrintsFilesInParallel() {
        assertDoesNotThrow(() -> app.run(new String[]{fileA, fileB}, System.in, output));
        String expected = "A" + STRING_TAB + "1" + STRING_NEWLINE +
                "B" + STRING_TAB + "2" + STRING_NEWLINE +
                "C" + STRING_TAB + "3" + STRING_NEWLINE +
                "D" + STRING_TAB + "4" + STRING_NEWLINE +
                "E" + STRING_TAB + "5" + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }


    @Test
    void run_SerialFlagAndOneFile_PrintsFileInSerial() {
        assertDoesNotThrow(() -> app.run(new String[]{"-s", fileA}, System.in, output));
        String expected = joinStringsByTab("A", "B", "C", "D", "E") + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_SerialFlagAndMultipleFiles_PrintsFilesInSerial() {
        assertDoesNotThrow(() -> app.run(new String[]{fileA, fileB, "-s"}, System.in, output));
        String expected = joinStringsByTab("A", "B", "C", "D", "E") + STRING_NEWLINE +
                joinStringsByTab("1", "2", "3", "4", "5") + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_NoFlagAndStdin_PrintsStdinInParallel() {
        assertDoesNotThrow(() -> {
            InputStream input = IOUtils.openInputStream(fileA);
            app.run(new String[]{}, input, output);
        });
        String expected = joinStringsByNewline("A", "B", "C", "D", "E") + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_ValidFlagAndStdin_PrintsStdinInSerial() {
        assertDoesNotThrow(() -> {
            InputStream input = IOUtils.openInputStream(fileA);
            app.run(new String[]{"-s"}, input, output);
        });
        String expected = joinStringsByTab("A", "B", "C", "D", "E") + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_NoFlagStdinAndFile_PrintsStdinAndFileInParallel() {
        assertDoesNotThrow(() -> {
            InputStream input = IOUtils.openInputStream(fileA);
            app.run(new String[]{STDIN, fileB, STDIN}, input, output);
        });
        String expected = "A" + STRING_TAB + "1" + STRING_TAB + "B" + STRING_NEWLINE +
                "C" + STRING_TAB + "2" + STRING_TAB + "D" + STRING_NEWLINE +
                "E" + STRING_TAB + "3" + STRING_TAB + STRING_NEWLINE +
                STRING_TAB + "4" + STRING_TAB + STRING_NEWLINE +
                STRING_TAB + "5" + STRING_TAB + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_ValidFlagStdinAndFile_PrintsStdinAndFileInSerial() {
        assertDoesNotThrow(() -> {
            InputStream input = IOUtils.openInputStream(fileA);
            app.run(new String[]{STDIN, fileB, STDIN, "-s"}, input, output);
        });
        String expected = joinStringsByTab("A", "B", "C", "D", "E") + STRING_NEWLINE +
                joinStringsByTab("1", "2", "3", "4", "5") + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }
}
