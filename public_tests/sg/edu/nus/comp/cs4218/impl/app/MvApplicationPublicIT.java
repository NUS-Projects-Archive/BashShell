package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.CHAR_FILE_SEP;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

@SuppressWarnings("PMD.ClassNamingConventions")
public class MvApplicationPublicIT {

    @TempDir
    private File tempDir;

    private static final String SUBFOLDER = "subfolder";
    private static final String SUBFOLDER_1 = "subfolder1";
    private static final String SUBFOLDER_2 = "subfolder2";
    private static final String SUBFOLDER_3 = "subfolder3";
    private static final String SUB_SUBFOLDER_1 = "subsubfolder1";
    private static final String SUB_SUBFOLDER_2 = "subsubfolder2";
    private static final String FILE_1_TXT = "file1.txt";
    private static final String FILE_2_TXT = "file2.txt";
    private static final String FILE_3_TXT = "file3.txt";
    private static final String FILE_4_TXT = "file4.txt";
    private static final String FILE_5_TXT = "file5.txt";
    private static final String FILE1_CONTENTS = "This is file1.txt content";
    private static final String FILE2_CONTENTS = "This is another file2.txt content";
    private static final String SUBFILE2_CONTENTS = "This is a subfolder1 file2.txt content";
    private static final String BLOCKED_FILE = "blocked";
    private static final String UNWRITABLE_FILE = "unwritable";

    private MvApplication mvApplication;

    @BeforeEach
    void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        mvApplication = new MvApplication();
        TestEnvironmentUtil.setCurrentDirectory(tempDir.getAbsolutePath());

        // create sub-folders
        new File(tempDir, SUBFOLDER_1).mkdir();
        new File(tempDir, SUBFOLDER_2).mkdir();
        new File(tempDir, SUBFOLDER_3).mkdir();

        // create sub-sub-folders
        new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUB_SUBFOLDER_1).mkdir();
        new File(tempDir, SUBFOLDER_2 + CHAR_FILE_SEP + SUB_SUBFOLDER_2).mkdir();

        // create files with contents
        new File(tempDir, FILE_1_TXT).createNewFile();
        File file1 = new File(tempDir, FILE_1_TXT);
        try (FileWriter file1Writer = new FileWriter(file1)) {
            file1Writer.write(FILE1_CONTENTS);
        }

        new File(tempDir, FILE_2_TXT).createNewFile();
        File file2 = new File(tempDir, FILE_2_TXT);
        try (FileWriter file2Writer = new FileWriter(file2)) {
            file2Writer.write(FILE2_CONTENTS);
        }

        new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT).createNewFile();
        File subFile2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        try (FileWriter subFile2Writer = new FileWriter(subFile2)) {
            subFile2Writer.write(SUBFILE2_CONTENTS);
        }

        File blockedFolder = new File(tempDir, BLOCKED_FILE);
        blockedFolder.mkdir();
        blockedFolder.setWritable(false);

        File unwritableFile = new File(tempDir, UNWRITABLE_FILE);
        unwritableFile.createNewFile();
        unwritableFile.setWritable(false);
    }

    @AfterEach
    void tearDown() {
        // set files and folders to be writable to enable clean up
        File blockedFolder = new File(tempDir, BLOCKED_FILE);
        blockedFolder.setWritable(true);
        File unwritableFile = new File(tempDir, UNWRITABLE_FILE);
        unwritableFile.setWritable(true);
    }

    @AfterAll
    static void tearDownAll() throws NoSuchFieldException, IllegalAccessException {
        TestEnvironmentUtil.setCurrentDirectory(System.getProperty("user.dir"));
    }

    @Test
    public void run_NullArgs_ThrowsMvException() {
        assertThrows(MvException.class, () -> mvApplication.run(null, System.in, System.out));
    }

    @Test
    public void run_InvalidFlag_ThrowsMvException() {
        String[] argList = new String[]{"-a", FILE_1_TXT, FILE_2_TXT};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_InvalidNumOfArgs_ThrowsMvException() {
        String[] argList = new String[]{"-n", FILE_2_TXT};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_UnwritableSrcFile_ThrowsMvException() {
        // no permissions to rename unwritable
        String[] argList = new String[]{UNWRITABLE_FILE, FILE_4_TXT};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_UnwritableDestFileWithoutFlag_ThrowsMvException() {
        // no permissions to override unwritable
        String[] argList = new String[]{FILE_2_TXT, UNWRITABLE_FILE};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));

        File srcFile = new File(tempDir, FILE_2_TXT);
        File destFile = new File(tempDir, UNWRITABLE_FILE);

        assertTrue(srcFile.exists());
        assertTrue(destFile.exists());
        List<String> destFileContents = assertDoesNotThrow(() -> Files.readAllLines(destFile.toPath()));
        assertEquals(0, destFileContents.size());
    }

    @Test
    public void run_UnwritableDestFileWithFlag_ThrowsMvException() {
        // not overriding unwritable, so no error thrown
        String[] argList = new String[]{"-n", FILE_2_TXT, UNWRITABLE_FILE};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));

        // no change
        File srcFile = new File(tempDir, FILE_2_TXT);
        File destFile = new File(tempDir, UNWRITABLE_FILE);

        assertTrue(srcFile.exists());
        assertTrue(destFile.exists());
        List<String> destFileContents = assertDoesNotThrow(() -> Files.readAllLines(destFile.toPath()));
        assertEquals(0, destFileContents.size());
    }

    @Test
    @DisabledOnOs(WINDOWS)
    public void run_UnwritableDestFolder_ThrowsMvException() {
        // no permissions to move files into blocked folder
        String[] argList = new String[]{FILE_1_TXT, FILE_2_TXT, BLOCKED_FILE};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));

        File remainingSrcFile1 = new File(tempDir, FILE_1_TXT);
        File remainingSrcFile2 = new File(tempDir, FILE_2_TXT);

        assertTrue(remainingSrcFile1.exists());
        assertTrue(remainingSrcFile2.exists());
    }

    @Test
    public void run_WithoutFlag2ArgsDestExist_RemoveSrcAndOverrideFile() {
        String[] argList = new String[]{FILE_1_TXT, FILE_2_TXT};
        assertDoesNotThrow(() -> mvApplication.run(argList, System.in, System.out));

        File srcFile = new File(tempDir, FILE_1_TXT);
        File destFile = new File(tempDir, FILE_2_TXT);

        assertFalse(srcFile.exists());
        assertTrue(destFile.exists());
        List<String> destFileContents = assertDoesNotThrow(() -> Files.readAllLines(destFile.toPath()));
        assertEquals(FILE1_CONTENTS, destFileContents.get(0));
    }

    @Test
    public void run_WithFlag2ArgsDestFileExist_NoChange() {
        String[] argList = new String[]{"-n", FILE_1_TXT, FILE_2_TXT};
        assertDoesNotThrow(() -> mvApplication.run(argList, System.in, System.out));

        File srcFile = new File(tempDir, FILE_1_TXT);
        File destFile = new File(tempDir, FILE_2_TXT);

        assertTrue(srcFile.exists());
        List<String> srcFileContents = assertDoesNotThrow(() -> Files.readAllLines(srcFile.toPath()));
        assertEquals(FILE1_CONTENTS, srcFileContents.get(0));
        assertTrue(destFile.exists());
        List<String> destFileContents = assertDoesNotThrow(() -> Files.readAllLines(destFile.toPath()));
        assertEquals(FILE2_CONTENTS, destFileContents.get(0));
    }

    @Test
    public void run_WithoutFlags2ArgsDestFileNonExist_RenameFile() {
        String[] argList = new String[]{FILE_2_TXT, FILE_4_TXT};
        assertDoesNotThrow(() -> mvApplication.run(argList, System.in, System.out));

        File srcFile = new File(tempDir, FILE_2_TXT);
        File destFile = new File(tempDir, FILE_4_TXT);

        assertFalse(srcFile.exists());
        assertTrue(destFile.exists());
        List<String> destFileContents = assertDoesNotThrow(() -> Files.readAllLines(destFile.toPath()));
        assertEquals(FILE2_CONTENTS, destFileContents.get(0));
    }

    @Test
    public void run_WithFlagRenameOneSubFileIntoFolder_RenameSubFile() {
        String[] argList = new String[]{SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT, FILE_5_TXT};
        assertDoesNotThrow(() -> mvApplication.run(argList, System.in, System.out));

        File srcFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File destFile = new File(tempDir, FILE_5_TXT);

        assertFalse(srcFile.exists());
        assertTrue(destFile.exists());
        List<String> destFileContents = assertDoesNotThrow(() -> Files.readAllLines(destFile.toPath()));
        assertEquals(SUBFILE2_CONTENTS, destFileContents.get(0));
    }

    @Test
    public void run_WithFlagRenameOneSubFileIntoSubFile_RenameSubFile() {
        String[] argList = new String[]{SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT,
                SUBFOLDER_2 + CHAR_FILE_SEP + FILE_5_TXT};
        assertDoesNotThrow(() -> mvApplication.run(argList, System.in, System.out));

        File srcFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File destFile = new File(tempDir, SUBFOLDER_2 + CHAR_FILE_SEP + FILE_5_TXT);

        assertFalse(srcFile.exists());
        assertTrue(destFile.exists());
        List<String> destFileContents = assertDoesNotThrow(() -> Files.readAllLines(destFile.toPath()));
        assertEquals(SUBFILE2_CONTENTS, destFileContents.get(0));
    }

    @Test
    public void run_WithFlags2ArgsDestFoldersNonExist_RenameFile() {
        String[] argList = new String[]{"-n", SUBFOLDER_1, "newSubFolder"};
        assertDoesNotThrow(() -> mvApplication.run(argList, System.in, System.out));

        File srcFile = new File(tempDir, SUBFOLDER_1);
        File destFile = new File(tempDir, "newSubFolder");

        assertFalse(srcFile.exists());
        assertTrue(destFile.exists());
        assertTrue(Files.isDirectory(destFile.toPath()));
        List<String> subFiles = Arrays.stream(destFile.listFiles()).map(File::getName).collect(Collectors.toList());
        assertEquals(2, subFiles.size());
        assertTrue(subFiles.contains(FILE_2_TXT));
        assertTrue(subFiles.contains(SUB_SUBFOLDER_1));
    }

    @Test
    public void run_WithFlags2ArgsSrcFolderDestFileNonExist_RenameFile() {
        String[] argList = new String[]{"-n", SUBFOLDER_1, FILE_3_TXT};
        assertDoesNotThrow(() -> mvApplication.run(argList, System.in, System.out));

        File srcFile = new File(tempDir, SUBFOLDER_1);
        File destFile = new File(tempDir, FILE_3_TXT);

        assertFalse(srcFile.exists());
        assertTrue(destFile.exists());
        assertTrue(Files.isDirectory(destFile.toPath()));
        List<String> subFiles = Arrays.stream(destFile.listFiles()).map(File::getName).collect(Collectors.toList());
        assertEquals(2, subFiles.size());
        assertTrue(subFiles.contains(FILE_2_TXT));
        assertTrue(subFiles.contains(SUB_SUBFOLDER_1));
    }

    @Test
    public void run_WithFlags2ArgsDiffFileTypesNonExist_ConvertFolderToFile() {
        String[] argList = new String[]{"-n", FILE_1_TXT, "file1.png"};
        assertDoesNotThrow(() -> mvApplication.run(argList, System.in, System.out));

        File srcFile = new File(tempDir, FILE_1_TXT);
        File destFile = new File(tempDir, "file1.png");

        assertFalse(srcFile.exists());
        assertTrue(destFile.exists());
        List<String> destFileContents = assertDoesNotThrow(() -> Files.readAllLines(destFile.toPath()));
        assertEquals(FILE1_CONTENTS, destFileContents.get(0));
    }

    @Test
    public void run_WithoutFlagsSameSrcAndDestExist_NoChange() {
        String[] argList = new String[]{FILE_1_TXT, FILE_1_TXT};
        assertDoesNotThrow(() -> mvApplication.run(argList, System.in, System.out));
        File srcFile = new File(tempDir, FILE_1_TXT);
        assertTrue(srcFile.exists());
        List<String> actualContents = assertDoesNotThrow(() -> Files.readAllLines(srcFile.toPath()));
        assertEquals(FILE1_CONTENTS, actualContents.get(0));
    }

    @Test
    public void run_invalidSourceFile_ThrowMvException() {
        String[] argList = new String[]{FILE_3_TXT, FILE_1_TXT};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_WithoutFlagMoveOneFileIntoFolder_MovedIntoFolder() {
        String[] argList = new String[]{FILE_1_TXT, SUBFOLDER_1};
        assertDoesNotThrow(() -> mvApplication.run(argList, System.in, System.out));

        File srcFile = new File(tempDir, FILE_1_TXT);
        File destFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_1_TXT);

        assertFalse(srcFile.exists());
        assertTrue(destFile.exists());
        List<String> destFileContents = assertDoesNotThrow(() -> Files.readAllLines(destFile.toPath()));
        assertEquals(FILE1_CONTENTS, destFileContents.get(0));
    }

    @Test
    public void run_WithoutFlagMoveOneSubFileIntoFolder_MovedIntoFolder() {
        String[] argList = new String[]{SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT, SUBFOLDER_2};
        assertDoesNotThrow(() -> mvApplication.run(argList, System.in, System.out));

        File srcFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File destFile = new File(tempDir, SUBFOLDER_2 + CHAR_FILE_SEP + FILE_2_TXT);

        assertFalse(srcFile.exists());
        assertTrue(destFile.exists());
        List<String> destFileContents = assertDoesNotThrow(() -> Files.readAllLines(destFile.toPath()));
        assertEquals(SUBFILE2_CONTENTS, destFileContents.get(0));
    }

    @Test
    public void run_WithoutFlagMoveOneSubFileIntoSubSFolder_MovedIntoSubFolder() {
        String[] argList = new String[]{SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT,
                SUBFOLDER_2 + CHAR_FILE_SEP + SUB_SUBFOLDER_2};
        assertDoesNotThrow(() -> mvApplication.run(argList, System.in, System.out));

        File srcFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File destFile = new File(tempDir, SUBFOLDER_2 + CHAR_FILE_SEP + SUB_SUBFOLDER_2 +
                CHAR_FILE_SEP + FILE_2_TXT);

        assertFalse(srcFile.exists());
        assertTrue(destFile.exists());
        List<String> destFileContents = assertDoesNotThrow(() -> Files.readAllLines(destFile.toPath()));
        assertEquals(SUBFILE2_CONTENTS, destFileContents.get(0));
    }

    @Test
    public void run_WithoutFlagMoveOneAbsolutePathFileIntoSubFolder_MovedIntoSubFolder() {
        String[] argList = new String[]{tempDir.getAbsolutePath() + CHAR_FILE_SEP + SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT,
                tempDir.getAbsolutePath() + CHAR_FILE_SEP + SUBFOLDER_2 + CHAR_FILE_SEP + SUB_SUBFOLDER_2};
        assertDoesNotThrow(() -> mvApplication.run(argList, System.in, System.out));

        File srcFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File destFile = new File(tempDir, SUBFOLDER_2 + CHAR_FILE_SEP +
                SUB_SUBFOLDER_2 + CHAR_FILE_SEP + FILE_2_TXT);

        assertFalse(srcFile.exists());
        assertTrue(destFile.exists());
        List<String> destFileContents = assertDoesNotThrow(() -> Files.readAllLines(destFile.toPath()));
        assertEquals(SUBFILE2_CONTENTS, destFileContents.get(0));
    }

    @Test
    public void run_WithoutFlagsMoveOneFolderIntoFolder_MovedIntoFolder() {
        String[] argList = new String[]{SUBFOLDER_2, SUBFOLDER_1};
        assertDoesNotThrow(() -> mvApplication.run(argList, System.in, System.out));

        File srcFile = new File(tempDir, SUBFOLDER_2);
        File destFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUBFOLDER_2);

        assertFalse(srcFile.exists());
        assertTrue(destFile.exists());
        assertTrue(Files.isDirectory(destFile.toPath()));
        File[] subFiles = destFile.listFiles();
        assertEquals(1, subFiles.length);
        assertEquals(SUB_SUBFOLDER_2, subFiles[0].getName());
    }

    @Test
    public void run_WithoutFlagsMoveMultipleFilesIntoFolder_MovedAllIntoFolder() {
        String[] argList = new String[]{FILE_1_TXT, SUBFOLDER_2, SUBFOLDER_1};
        assertDoesNotThrow(() -> mvApplication.run(argList, System.in, System.out));

        File remainingSubFile2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File removedFile1 = new File(tempDir, FILE_1_TXT);
        File removedSubFolder2 = new File(tempDir, SUBFOLDER_2);
        File newFile1 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_1_TXT);
        File newSubFolder2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUBFOLDER_2);

        assertTrue(remainingSubFile2.exists());
        assertFalse(removedFile1.exists());
        assertFalse(removedSubFolder2.exists());
        assertTrue(newFile1.exists());
        List<String> newFile1Contents = assertDoesNotThrow(() -> Files.readAllLines(newFile1.toPath()));
        assertEquals(FILE1_CONTENTS, newFile1Contents.get(0));
        assertTrue(newSubFolder2.exists());
        assertTrue(Files.isDirectory(newSubFolder2.toPath()));
        File[] subFiles = newSubFolder2.listFiles();
        assertEquals(1, subFiles.length);
        assertEquals(SUB_SUBFOLDER_2, subFiles[0].getName());
    }

    @Test
    public void run_WithoutFlagsMoveFileWithSameNameIntoFolder_MovedIntoFolderWithOverriding() {
        String[] argList = new String[]{FILE_1_TXT, FILE_2_TXT, SUBFOLDER_2, SUBFOLDER_1};
        assertDoesNotThrow(() -> mvApplication.run(argList, System.in, System.out));

        File removedFile1 = new File(tempDir, FILE_1_TXT);
        File removedFile2 = new File(tempDir, FILE_2_TXT);
        File removedSubFolder2 = new File(tempDir, SUBFOLDER_2);
        File newFile1 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_1_TXT);
        File newFile2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File newSubFolder2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUBFOLDER_2);

        assertFalse(removedFile1.exists());
        assertFalse(removedFile2.exists());
        assertFalse(removedSubFolder2.exists());
        assertTrue(newFile1.exists());
        List<String> newFile1Contents = assertDoesNotThrow(() -> Files.readAllLines(newFile1.toPath()));
        assertEquals(FILE1_CONTENTS, newFile1Contents.get(0));
        assertTrue(newFile2.exists());
        List<String> newFile2Contents = assertDoesNotThrow(() -> Files.readAllLines(newFile2.toPath()));
        assertEquals(FILE2_CONTENTS, newFile2Contents.get(0)); //override with file2.txt contents
        assertTrue(newSubFolder2.exists());
        assertTrue(Files.isDirectory(newSubFolder2.toPath()));
        File[] subFiles = newSubFolder2.listFiles();
        assertEquals(1, subFiles.length);
        assertEquals(SUB_SUBFOLDER_2, subFiles[0].getName());
    }

    @Test
    public void run_WithFlagsMoveFilesWithSameNameIntoFolder_MovedIntoFolderWithoutOverriding() {
        String[] argList = new String[]{"-n", FILE_1_TXT, FILE_2_TXT, SUBFOLDER_2, SUBFOLDER_1};
        assertDoesNotThrow(() -> mvApplication.run(argList, System.in, System.out));

        File removedFile1 = new File(tempDir, FILE_1_TXT);
        File removedFile2 = new File(tempDir, FILE_2_TXT);
        File removedSubFolder2 = new File(tempDir, SUBFOLDER_2);
        File newFile1 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_1_TXT);
        File newFile2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File newSubFolder2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUBFOLDER_2);

        assertFalse(removedFile1.exists());
        assertTrue(removedFile2.exists()); // file2.txt not moved
        assertFalse(removedSubFolder2.exists());
        assertTrue(newFile1.exists());
        List<String> newFile1Contents = assertDoesNotThrow(() -> Files.readAllLines(newFile1.toPath()));
        assertEquals(FILE1_CONTENTS, newFile1Contents.get(0));
        assertTrue(newFile2.exists());
        List<String> newFile2Contents = assertDoesNotThrow(() -> Files.readAllLines(newFile2.toPath()));
        assertEquals(SUBFILE2_CONTENTS, newFile2Contents.get(0)); //NOT override with file2.txt contents
        assertTrue(newSubFolder2.exists());
        assertTrue(Files.isDirectory(newSubFolder2.toPath()));
        File[] subFiles = newSubFolder2.listFiles();
        assertEquals(1, subFiles.length);
        assertEquals(SUB_SUBFOLDER_2, subFiles[0].getName());
    }

    @Test
    public void run_NonExistentDestFolder_ThrowsMvException() {
        String[] argList = new String[]{"-n", FILE_1_TXT, FILE_2_TXT, "nonExistentFolder"};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_ExistentNonDirDestFile_ThrowsMvException() {
        String[] argList = new String[]{FILE_1_TXT, SUBFOLDER_2, FILE_2_TXT};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_NonExistentNonDirDestFile_ThrowsMvException() {
        String[] argList = new String[]{FILE_1_TXT, SUBFOLDER_2, "f"};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_InvalidSrcFileFirst_ThrowsMvException() {
        String[] argList = new String[]{"f", SUBFOLDER_2, SUBFOLDER_1};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));

        File expectedNewFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUBFOLDER_2);
        assertTrue(expectedNewFile.exists());
    }

    @Test
    public void run_InvalidSrcFilesAfter_ThrowsMvException() {
        String[] argList = new String[]{SUBFOLDER_2, SUBFOLDER, SUBFOLDER_1};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));

        File movedFile = new File(tempDir, SUBFOLDER_2);
        File newFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUBFOLDER_2);
        assertFalse(movedFile.exists());
        assertTrue(newFile.exists());
        assertTrue(Files.isDirectory(newFile.toPath()));
        File[] subFiles = newFile.listFiles();
        assertEquals(1, subFiles.length);
        assertEquals(SUB_SUBFOLDER_2, subFiles[0].getName());
    }
}
