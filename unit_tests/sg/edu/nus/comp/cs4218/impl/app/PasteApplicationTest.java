package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PasteApplicationTest {
    private PasteApplication pasteApplication;
    private static final String ROOT_PATH = Environment.currentDirectory;
    private static final String TEST_PATH = ROOT_PATH + File.separatorChar + "pasteTestFolder" + File.separatorChar;

    private static final String FILE_A = "A.txt";
    private static final String FILE_PATH_A = TEST_PATH + FILE_A;
    private static final String FILE_B = "B.txt";
    private static final String FILE_PATH_B = TEST_PATH + FILE_B;
    private static final String NON_EXISTENT_FILE = "nonExistent.txt";

    private static final String PASTE_EXCEPTION_MSG = "paste: ";

    private static final String STDIN = "-";

    private static void createFile(String filePath, String fileContent) throws IOException {
        File file = new File(filePath);
        FileWriter writer = null;

        try {
            boolean result = file.createNewFile();
            if (result) {
                writer = new FileWriter(file.getCanonicalPath());
                writer.write(fileContent);
            } else {
                System.out.println("File already exists at location: " + file.getCanonicalPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            file.delete();
        }
    }

    private static void deleteDirectory(File directory) {
        if (directory != null && directory.exists()) {
            File[] contents = directory.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    deleteDirectory(f);
                }
            }
            directory.delete();
        }
    }

    @BeforeAll
    static void setUp() throws IOException {
        deleteDirectory(new File(TEST_PATH));
        Files.createDirectories(Paths.get(TEST_PATH));
        String contentFileA = "A" + StringUtils.STRING_NEWLINE + "B" +
                StringUtils.STRING_NEWLINE + "C" +
                StringUtils.STRING_NEWLINE + "D" +
                StringUtils.STRING_NEWLINE + "E";

        createFile(FILE_PATH_A, contentFileA);

        String contentFileB = "1" + StringUtils.STRING_NEWLINE + "2" +
                StringUtils.STRING_NEWLINE + "3" +
                StringUtils.STRING_NEWLINE + "4" +
                StringUtils.STRING_NEWLINE + "5";

        createFile(FILE_PATH_B, contentFileB);
        deleteFile(NON_EXISTENT_FILE);

    }

    @BeforeEach
    void setUpEach() {
        pasteApplication = new PasteApplication();
        Environment.currentDirectory = ROOT_PATH;
    }

    @AfterAll
    static void tearDown() {
        Environment.currentDirectory = ROOT_PATH;
        deleteDirectory(new File(TEST_PATH));
    }

    @Test
    void run_nullStdin_shouldThrowPasteException() {
        Throwable result = assertThrows(PasteException.class, () -> {
            pasteApplication.run(new String[]{FILE_PATH_A}, null, System.out);
        });
        assertEquals(PASTE_EXCEPTION_MSG + ERR_NO_ISTREAM, result.getMessage());
    }

    @Test
    void run_nullStdout_shouldThrowPasteException() {
        Throwable result = assertThrows(PasteException.class, () -> {
            pasteApplication.run(new String[]{FILE_PATH_A}, System.in, null);
        });
        assertEquals(PASTE_EXCEPTION_MSG + ERR_NULL_STREAMS, result.getMessage());
    }

    @Test
    void mergeStdin_stdinWithoutFlag_shouldMergeStdinInParallel() throws Exception {
        InputStream inputStream = IOUtils.openInputStream(FILE_PATH_A);
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
    void mergeStdin_stdinWithFlag_shouldMergeStdinInSerial() throws Exception {
        InputStream inputStream = IOUtils.openInputStream(FILE_PATH_A);
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
    void mergeFile_filesWithoutFlag_shouldMergeFilesInParallel() throws Exception {
        String result = pasteApplication.mergeFile(false, FILE_PATH_A, FILE_PATH_B);
        String expected = "A" + StringUtils.STRING_TAB + "1" +
                StringUtils.STRING_NEWLINE + "B" + StringUtils.STRING_TAB + "2" +
                StringUtils.STRING_NEWLINE + "C" + StringUtils.STRING_TAB + "3" +
                StringUtils.STRING_NEWLINE + "D" + StringUtils.STRING_TAB + "4" +
                StringUtils.STRING_NEWLINE + "E" + StringUtils.STRING_TAB + "5";

        assertEquals(expected, result);
    }

    @Test
    void mergeFile_filesWithFlag_shouldMergeFilesInSerial() throws Exception {
        String result = pasteApplication.mergeFile(true, FILE_PATH_A, FILE_PATH_B);
        String expected = "A" + StringUtils.STRING_TAB + "B" +
                StringUtils.STRING_TAB + "C" + StringUtils.STRING_TAB + "D" +
                StringUtils.STRING_TAB + "E" + StringUtils.STRING_NEWLINE + "1" +
                StringUtils.STRING_TAB + "2" + StringUtils.STRING_TAB + "3" +
                StringUtils.STRING_TAB + "4" + StringUtils.STRING_TAB + "5";

        assertEquals(expected, result);
    }

    @Test
    void mergeFileAndStdin_FileAndStdinWithoutFlag_shouldMergeFileAndStdinInParallel() throws Exception {
        InputStream inputStream = IOUtils.openInputStream(FILE_PATH_A);
        String result;
        try {
            result = pasteApplication.mergeFileAndStdin(false, inputStream, STDIN, FILE_PATH_B, STDIN);
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
    void mergeFileAndStdin_FileAndStdinWithFlag_shouldMergeFileAndStdinInSerial() throws Exception {
        InputStream inputStream = IOUtils.openInputStream(FILE_PATH_A);
        String result;
        try {
            result = pasteApplication.mergeFileAndStdin(true, inputStream, STDIN, FILE_PATH_B, STDIN);
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
