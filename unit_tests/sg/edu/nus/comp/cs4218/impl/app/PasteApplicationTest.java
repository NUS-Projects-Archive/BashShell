package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;

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

public class PasteApplicationTest {
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

    @Test
    void mergeStdin_StdinWithoutFlag_ShouldMergeStdinInParallel() throws Exception {
        InputStream inputStream = IOUtils.openInputStream(filePathA);
        String result;
        try {
            result = pasteApplication.mergeStdin(false, inputStream);
        } finally {
            inputStream.close();
        }
        String expected = "A" + StringUtils.STRING_NEWLINE + "B" +
                StringUtils.STRING_NEWLINE + "C" +
                StringUtils.STRING_NEWLINE + "D" +
                StringUtils.STRING_NEWLINE + "E";

        assertEquals(expected, result);
    }

    @Test
    void mergeStdin_StdinWithFlag_ShouldMergeStdinInSerial() throws Exception {
        InputStream inputStream = IOUtils.openInputStream(filePathA);
        String result;
        try {
            result = pasteApplication.mergeStdin(true, inputStream);
        } finally {
            inputStream.close();
        }
        String expected = "A" + StringUtils.STRING_TAB + "B" +
                StringUtils.STRING_TAB + "C" +
                StringUtils.STRING_TAB + "D" +
                StringUtils.STRING_TAB + "E";

        assertEquals(expected, result);
    }

    @Test
    void mergeFile_FilesWithoutFlag_ShouldMergeFilesInParallel() throws Exception {
        String result = pasteApplication.mergeFile(false, filePathA, filePathB);
        String expected = "A" + StringUtils.STRING_TAB + "1" +
                StringUtils.STRING_NEWLINE + "B" + StringUtils.STRING_TAB + "2" +
                StringUtils.STRING_NEWLINE + "C" + StringUtils.STRING_TAB + "3" +
                StringUtils.STRING_NEWLINE + "D" + StringUtils.STRING_TAB + "4" +
                StringUtils.STRING_NEWLINE + "E" + StringUtils.STRING_TAB + "5";

        assertEquals(expected, result);
    }

    @Test
    void mergeFile_FilesWithFlag_ShouldMergeFilesInSerial() throws Exception {
        String result = pasteApplication.mergeFile(true, filePathA, filePathB);
        String expected = "A" + StringUtils.STRING_TAB + "B" +
                StringUtils.STRING_TAB + "C" + StringUtils.STRING_TAB + "D" +
                StringUtils.STRING_TAB + "E" + StringUtils.STRING_NEWLINE + "1" +
                StringUtils.STRING_TAB + "2" + StringUtils.STRING_TAB + "3" +
                StringUtils.STRING_TAB + "4" + StringUtils.STRING_TAB + "5";

        assertEquals(expected, result);
    }

    @Test
    void mergeFileAndStdin_FileAndStdinWithoutFlag_ShouldMergeFileAndStdinInParallel() throws Exception {
        InputStream inputStream = IOUtils.openInputStream(filePathA);
        String result;
        try {
            result = pasteApplication.mergeFileAndStdin(false, inputStream, STDIN, filePathB, STDIN);
        } finally {
            inputStream.close();
        }

        String expected = "A" + StringUtils.STRING_TAB + "1" + StringUtils.STRING_TAB + "B" +
                StringUtils.STRING_NEWLINE + "C" + StringUtils.STRING_TAB + "2" + StringUtils.STRING_TAB + "D" +
                StringUtils.STRING_NEWLINE + "E" + StringUtils.STRING_TAB + "3" +
                StringUtils.STRING_TAB + StringUtils.STRING_NEWLINE + StringUtils.STRING_TAB + "4" +
                StringUtils.STRING_TAB + StringUtils.STRING_NEWLINE + StringUtils.STRING_TAB + "5" +
                StringUtils.STRING_TAB;

        assertEquals(expected, result);
    }

    @Test
    void mergeFileAndStdin_FileAndStdinWithFlag_ShouldMergeFileAndStdinInSerial() throws Exception {
        InputStream inputStream = IOUtils.openInputStream(filePathA);
        String result;
        try {
            result = pasteApplication.mergeFileAndStdin(true, inputStream, STDIN, filePathB, STDIN);
        } finally {
            inputStream.close();
        }

        String expected = "A" + StringUtils.STRING_TAB + "B" +
                StringUtils.STRING_TAB + "C" + StringUtils.STRING_TAB + "D" +
                StringUtils.STRING_TAB + "E" + StringUtils.STRING_NEWLINE + "1" +
                StringUtils.STRING_TAB + "2" + StringUtils.STRING_TAB + "3" +
                StringUtils.STRING_TAB + "4" + StringUtils.STRING_TAB + "5" + StringUtils.STRING_NEWLINE;

        assertEquals(expected, result);
    }
}
