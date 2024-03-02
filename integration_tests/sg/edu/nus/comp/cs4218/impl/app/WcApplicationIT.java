package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.WcException;

public class WcApplicationIT {
    private static final String FILE_NAME_A = "A.txt";
    private static final String FILE_NAME_B = "B.txt";
    private static final String WC_EXCEPTION_MSG = "wc: ";
    private static final String NUMBER_FORMAT = " %7d";
    private static final String STRING_FORMAT = " %s";
    private WcApplication wcApplication;
    @TempDir
    private Path wcTestDir;
    private String filePathA;
    private String filePathB;

    private static String appendString(int lineCount, int wordCount, int byteCount, String lastLine) {
        StringBuilder sb = new StringBuilder(); //NOPMD
        if (lineCount > -1) {
            sb.append(String.format(NUMBER_FORMAT, lineCount));
        }
        if (wordCount > -1) {
            sb.append(String.format(NUMBER_FORMAT, wordCount));
        }
        if (byteCount > -1) {
            sb.append(String.format(NUMBER_FORMAT, byteCount));
        }
        sb.append(lastLine);
        return sb.toString();
    }

    @BeforeEach
    void setUp() throws IOException {
        this.wcApplication = new WcApplication();

        Path pathA = wcTestDir.resolve(FILE_NAME_A);
        Path pathB = wcTestDir.resolve(FILE_NAME_B);

        filePathA = pathA.toString();
        filePathB = pathB.toString();

        String contentFileA = "This is a sample text\nTo test Wc Application\n For CS4218\n";
        String contentFileB = "Lorem Ipsum is simply\ndummy text of the printing\nand typesetting industry.\n";
        Files.write(pathA, contentFileA.getBytes(StandardCharsets.UTF_8));
        Files.write(pathB, contentFileB.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void run_NullStdin_ThrowsWcException() {
        Throwable result = assertThrows(WcException.class, () -> {
            wcApplication.run(new String[]{filePathA}, null, System.out);
        });
        assertEquals(WC_EXCEPTION_MSG + ERR_NO_ISTREAM, result.getMessage());
    }

    @Test
    void run_NullStdout_ThrowsWcException() {
        Throwable result = assertThrows(WcException.class, () -> {
            wcApplication.run(new String[]{filePathA}, System.in, null);
        });
        assertEquals(WC_EXCEPTION_MSG + ERR_NULL_STREAMS, result.getMessage());
    }
}
