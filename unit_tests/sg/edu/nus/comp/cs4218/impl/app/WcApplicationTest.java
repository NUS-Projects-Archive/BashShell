package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class WcApplicationTest {

    private static final String FILE_NAME_A = "A.txt";
    private static final String FILE_NAME_B = "B.txt";
    private static final String NON_EXISTENT_FILE = "nonExistentFile.txt";
    private static final String WC_EXCEPTION_MSG = "wc: ";
    private static final String NUMBER_FORMAT = " %7d";
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
        wcTestDir = Files.createTempDirectory("wcTestDir");

        Path pathA = wcTestDir.resolve(FILE_NAME_A);
        Path pathB = wcTestDir.resolve(FILE_NAME_B);

        filePathA = pathA.toString();
        filePathB = pathB.toString();

        String contentFileA = "This is a sample text\nTo test Wc Application\n For CS4218";
        Files.write(pathA, Arrays.asList(contentFileA.split("\n")));

        String contentFileB = "Lorem Ipsum is simply\ndummy text of the printing\nand typesetting industry.";
        Files.write(pathB, Arrays.asList(contentFileB.split("\n")));
    }

    @Test
    void run_NullStdin_ShouldThrowWcException() {
        Throwable result = assertThrows(WcException.class, () -> {
            wcApplication.run(new String[]{filePathA}, null, System.out);
        });
        assertEquals(WC_EXCEPTION_MSG + ERR_NO_ISTREAM, result.getMessage());
    }

    @Test
    void run_NullStdout_ShouldThrowWcException() {
        Throwable result = assertThrows(WcException.class, () -> {
            wcApplication.run(new String[]{filePathA}, System.in, null);
        });
        assertEquals(WC_EXCEPTION_MSG + ERR_NULL_STREAMS, result.getMessage());
    }

    @Test
    void countFromFiles_NonExistentFile_ShouldReturnFileNotFoundError() throws Exception {
        String result = wcApplication.countFromFiles(true, true, true, NON_EXISTENT_FILE);
        assertEquals(WC_EXCEPTION_MSG + ERR_FILE_NOT_FOUND, result);
    }

    @Test
    void countFromFiles_CountFromFilesWithAllFlags_ShouldReturnAllCounts() throws Exception {
        String result = wcApplication.countFromFiles(true, true, true, filePathA, filePathB);
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, 57, String.format(" %s", filePathA)));
        expectedList.add(appendString(3, 12, 75, String.format(" %s", filePathB)));
        expectedList.add(appendString(6, 23, 132, " total"));
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertEquals(expected, result);
    }

    @Test
    void countFromFiles_CountFromFilesWithOnlyOneFlag_ShouldReturnOnlyCount() throws Exception {
        String result = wcApplication.countFromFiles(true, false, false, filePathA, filePathB);
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(-1, -1, 57, String.format(" %s", filePathA)));
        expectedList.add(appendString(-1, -1, 75, String.format(" %s", filePathB)));
        expectedList.add(appendString(-1, -1, 132, " total"));
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertEquals(expected, result);
    }

    @Test
    void countFromFiles_CountFromFilesWithTwoFlags_ShouldReturnTwoCounts() throws Exception {
        String result = wcApplication.countFromFiles(false, true, true, filePathA, filePathB);
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, -1, String.format(" %s", filePathA)));
        expectedList.add(appendString(3, 12, -1, String.format(" %s", filePathB)));
        expectedList.add(appendString(6, 23, -1, " total"));
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertEquals(expected, result);
    }

    @Test
    void countFromStdin_CountFromStdinWithAllFlags_ShouldReturnAllCounts() throws Exception {
        InputStream inputStream = IOUtils.openInputStream(filePathA);
        String result;
        try {
            result = wcApplication.countFromStdin(true, true, true, inputStream);
        } finally {
            inputStream.close();
        }
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, 57, ""));
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertEquals(expected, result);
    }

    @Test
    void countFromStdin_CountFromStdinWithOneFlag_ShouldReturnAllCounts() throws Exception {
        InputStream inputStream = IOUtils.openInputStream(filePathA);
        String result;
        try {
            result = wcApplication.countFromStdin(true, false, false, inputStream);
        } finally {
            inputStream.close();
        }
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(-1, -1, 57, ""));
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertEquals(expected, result);
    }

    @Test
    void countFromStdin_CountFromStdinWithTwoFlags_ShouldReturnTwoCounts() throws Exception {
        InputStream inputStream = IOUtils.openInputStream(filePathA);
        String result;
        try {
            result = wcApplication.countFromStdin(false, true, true, inputStream);
        } finally {
            inputStream.close();
        }
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, -1, ""));
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertEquals(expected, result);
    }

    @Test
    void countFromFileAndStdin_CountFromFileAndStdinWithAllFlags_ShouldReturnAllCounts() throws Exception {
        InputStream inputStream = IOUtils.openInputStream(filePathA);
        String result;
        try {
            result = wcApplication.countFromFileAndStdin(true, true, true, inputStream, "-", filePathB);
        } finally {
            inputStream.close();
        }
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, 11, 57, " -"));
        expectedList.add(appendString(3, 12, 75, String.format(" %s", filePathB)));
        expectedList.add(appendString(6, 23, 132, " total"));
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertEquals(expected, result);
    }

    @Test
    void countFromFileAndStdin_CountFromFileAndStdinWithOneFlag_ShouldReturnOneCount() throws Exception {
        InputStream inputStream = IOUtils.openInputStream(filePathA);
        String result;
        try {
            result = wcApplication.countFromFileAndStdin(true, false, false, inputStream, "-", filePathB);
        } finally {
            inputStream.close();
        }
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(-1, -1, 57, " -"));
        expectedList.add(appendString(-1, -1, 75, String.format(" %s", filePathB)));
        expectedList.add(appendString(-1, -1, 132, " total"));
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertEquals(expected, result);
    }

    @Test
    void countFromFileAndStdin_CountFromFileAndStdinWithTwoFlags_ShouldReturnTwoCounts() throws Exception {
        InputStream inputStream = IOUtils.openInputStream(filePathA);
        String result;
        try {
            result = wcApplication.countFromFileAndStdin(true, true, false, inputStream, "-", filePathB);
        } finally {
            inputStream.close();
        }
        List<String> expectedList = new ArrayList<>();
        expectedList.add(appendString(3, -1, 57, " -"));
        expectedList.add(appendString(3, -1, 75, String.format(" %s", filePathB)));
        expectedList.add(appendString(6, -1, 132, " total"));
        String expected = String.join(STRING_NEWLINE, expectedList);
        assertEquals(expected, result);
    }

}
