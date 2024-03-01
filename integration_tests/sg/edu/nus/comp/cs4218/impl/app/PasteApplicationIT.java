package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.PasteException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;

@SuppressWarnings("PMD.ClassNamingConventions")
public class PasteApplicationIT {
    private static final String FILE_NAME_A = "A.txt";
    private static final String FILE_NAME_B = "B.txt";
    private static final String PASTE_EXCEPTION_MSG = "paste: ";
    private static final String STDIN = "-";
    private PasteApplication pasteApplication;
    @TempDir
    private Path pasteTestDir;
    private String filePathA;
    private String filePathB;

    @BeforeEach
    void setUp() throws IOException {
        this.pasteApplication = new PasteApplication();
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
    void run_NullStdin_ShouldThrowPasteException() {
        Throwable result = assertThrows(PasteException.class, () -> {
            pasteApplication.run(new String[]{filePathA}, null, System.out);
        });
        assertEquals(PASTE_EXCEPTION_MSG + ERR_NO_ISTREAM, result.getMessage());
    }

    @Test
    void run_NullStdout_ShouldThrowPasteException() {
        Throwable result = assertThrows(PasteException.class, () -> {
            pasteApplication.run(new String[]{filePathA}, System.in, null);
        });
        assertEquals(PASTE_EXCEPTION_MSG + ERR_NULL_STREAMS, result.getMessage());
    }

}
