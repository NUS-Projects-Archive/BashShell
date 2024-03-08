package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

@SuppressWarnings("PMD.ClassNamingConventions")
public class PasteApplicationIT {

    private static final String FILE_NAME_A = "A.txt";
    private static final String FILE_NAME_B = "B.txt";
    private static final String PASTE_EX_MSG = "paste: ";
    private static final String STDIN = "-";
    private PasteApplication app;
    private ByteArrayOutputStream outputStream;

    @TempDir
    private Path pasteTestDir;
    private String filePathA;
    private String filePathB;

    @BeforeEach
    void setUp() throws IOException {
        app = new PasteApplication();
        outputStream = new ByteArrayOutputStream();

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
    void run_NullStdin_ThrowsPasteException() {
        PasteException result = assertThrowsExactly(PasteException.class, () -> app.run(new String[]{filePathA}, null, System.out));
        assertEquals(PASTE_EX_MSG + ERR_NO_ISTREAM, result.getMessage());
    }

    @Test
    void run_NullStdout_ThrowsPasteException() {
        PasteException result = assertThrowsExactly(PasteException.class, () -> app.run(new String[]{filePathA}, System.in, null));
        assertEquals(PASTE_EX_MSG + ERR_NULL_STREAMS, result.getMessage());
    }

    @Test
    void run_NoFlagsAndOneFile_PrintsFileInParallel() {
        assertDoesNotThrow(() -> app.run(new String[]{filePathA}, System.in, outputStream));
        String expected = "A" + StringUtils.STRING_NEWLINE + "B" +
                StringUtils.STRING_NEWLINE + "C" +
                StringUtils.STRING_NEWLINE + "D" +
                StringUtils.STRING_NEWLINE + "E" + StringUtils.STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    @Test
    void run_NoFlagsAndMultipleFiles_PrintsFilesInParallel() {
        assertDoesNotThrow(() -> app.run(new String[]{filePathA, filePathB}, System.in, outputStream));
        String expected = "A" + StringUtils.STRING_TAB + "1" +
                StringUtils.STRING_NEWLINE + "B" + StringUtils.STRING_TAB + "2" +
                StringUtils.STRING_NEWLINE + "C" + StringUtils.STRING_TAB + "3" +
                StringUtils.STRING_NEWLINE + "D" + StringUtils.STRING_TAB + "4" +
                StringUtils.STRING_NEWLINE + "E" + StringUtils.STRING_TAB + "5" + StringUtils.STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }


    @Test
    void run_SerialFlagAndOneFile_PrintsFileInSerial() {
        assertDoesNotThrow(() -> app.run(new String[]{filePathA, "-s"}, System.in, outputStream));
        String expected = "A" + StringUtils.STRING_TAB + "B" +
                StringUtils.STRING_TAB + "C" +
                StringUtils.STRING_TAB + "D" +
                StringUtils.STRING_TAB + "E" + StringUtils.STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    @Test
    void run_SerialFlagAndMultipleFiles_PrintsFilesInSerial() {
        assertDoesNotThrow(() -> app.run(new String[]{filePathA, filePathB, "-s"}, System.in, outputStream));
        String expected = "A" + StringUtils.STRING_TAB + "B" +
                StringUtils.STRING_TAB + "C" + StringUtils.STRING_TAB + "D" +
                StringUtils.STRING_TAB + "E" + StringUtils.STRING_NEWLINE + "1" +
                StringUtils.STRING_TAB + "2" + StringUtils.STRING_TAB + "3" +
                StringUtils.STRING_TAB + "4" + StringUtils.STRING_TAB + "5" + StringUtils.STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    @Test
    void run_NoFlagAndStdin_PrintsStdinInParallel() {
        assertDoesNotThrow(() -> {
            InputStream inputStream = IOUtils.openInputStream(filePathA);
            app.run(new String[]{}, inputStream, outputStream);
        });
        String expected = "A" + StringUtils.STRING_NEWLINE + "B" +
                StringUtils.STRING_NEWLINE + "C" +
                StringUtils.STRING_NEWLINE + "D" +
                StringUtils.STRING_NEWLINE + "E" + StringUtils.STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    @Test
    void run_ValidFlagAndStdin_PrintsStdinInSerial() {
        assertDoesNotThrow(() -> {
            InputStream inputStream = IOUtils.openInputStream(filePathA);
            app.run(new String[]{"-s"}, inputStream, outputStream);
        });
        String expected = "A" + StringUtils.STRING_TAB + "B" +
                StringUtils.STRING_TAB + "C" +
                StringUtils.STRING_TAB + "D" +
                StringUtils.STRING_TAB + "E" + StringUtils.STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    @Test
    void run_NoFlagStdinAndFile_PrintsStdinAndFileInParallel() {
        assertDoesNotThrow(() -> {
            InputStream inputStream = IOUtils.openInputStream(filePathA);
            app.run(new String[]{STDIN, filePathB, STDIN}, inputStream, outputStream);
        });
        String expected = "A" + StringUtils.STRING_TAB + "1" + StringUtils.STRING_TAB + "B" +
                StringUtils.STRING_NEWLINE + "C" + StringUtils.STRING_TAB + "2" + StringUtils.STRING_TAB + "D" +
                StringUtils.STRING_NEWLINE + "E" + StringUtils.STRING_TAB + "3" +
                StringUtils.STRING_TAB + StringUtils.STRING_NEWLINE + StringUtils.STRING_TAB + "4" +
                StringUtils.STRING_TAB + StringUtils.STRING_NEWLINE + StringUtils.STRING_TAB + "5" +
                StringUtils.STRING_TAB + StringUtils.STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }

    @Test
    void run_ValidFlagStdinAndFile_PrintsStdinAndFileInSerial() {
        assertDoesNotThrow(() -> {
            InputStream inputStream = IOUtils.openInputStream(filePathA);
            app.run(new String[]{STDIN, filePathB, STDIN, "-s"}, inputStream, outputStream);
        });
        String expected = "A" + StringUtils.STRING_TAB + "B" +
                StringUtils.STRING_TAB + "C" + StringUtils.STRING_TAB + "D" +
                StringUtils.STRING_TAB + "E" + StringUtils.STRING_NEWLINE + "1" +
                StringUtils.STRING_TAB + "2" + StringUtils.STRING_TAB + "3" +
                StringUtils.STRING_TAB + "4" + StringUtils.STRING_TAB + "5" + StringUtils.STRING_NEWLINE +
                StringUtils.STRING_NEWLINE;
        assertEquals(expected, outputStream.toString());
    }
}
