package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.CHAR_FILE_SEP;

import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

public class MvApplicationPublicIT {

    @TempDir
    File tempDir;

    private static final String SUBFOLDER = "subfolder";
    private static final String SUBFOLDER_1 = "subfolder1";
    private static final String SUBFOLDER_2 = "subfolder2";
    private static final String SUBFOLDER_3 = "subfolder3";
    private static final String SUB_SUBFOLDER_2 = "subsubfolder2";
    private static final String SUB_SUBFOLDER_1 = "subsubfolder1";
    private static final String FILE_1_TXT = "file1.txt";
    private static final String FILE_2_TXT = "file2.txt";
    private static final String BLOCKED_FILE = "blocked";
    private static final String UNWRITABLE_FILE = "unwritable";

    private static final String FILE1_CONTENTS = "This is file1.txt content";
    private static final String FILE2_CONTENTS = "This is another file2.txt content";
    private static final String SUBFILE2_CONTENTS = "This is a subfolder1 file2.txt content";

    private MvApplication mvApplication;
    
    @BeforeEach
    void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        mvApplication = new MvApplication();
        TestEnvironmentUtil.setCurrentDirectory(tempDir.getAbsolutePath());

        new File(tempDir, SUBFOLDER_1).mkdir();
        new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUB_SUBFOLDER_1).mkdir();
        new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT).createNewFile();
        File subFile2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        FileWriter subFile2Writer = new FileWriter(subFile2);
        subFile2Writer.write(SUBFILE2_CONTENTS);
        subFile2Writer.close();

        new File(tempDir, SUBFOLDER_2).mkdir();
        new File(tempDir, SUBFOLDER_2 + CHAR_FILE_SEP + SUB_SUBFOLDER_2).mkdir();

        new File(tempDir, SUBFOLDER_3).mkdir();

        new File(tempDir, FILE_1_TXT).createNewFile();
        File file1 = new File(tempDir, FILE_1_TXT);
        FileWriter file1Writer = new FileWriter(file1);
        file1Writer.write(FILE1_CONTENTS);
        file1Writer.close();

        new File(tempDir, FILE_2_TXT).createNewFile();
        File file2 = new File(tempDir, FILE_2_TXT);
        FileWriter file2Writer = new FileWriter(file2);
        file2Writer.write(FILE2_CONTENTS);
        file2Writer.close();

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
    public void run_NullArgs_ThrowsException() {
        assertThrows(MvException.class, () -> mvApplication.run(null, System.in, System.out));
    }

    @Test
    public void run_InvalidFlag_ThrowsException() {
        String[] argList = new String[]{"-a", FILE_1_TXT, FILE_2_TXT};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_InvalidNumOfArgs_ThrowsException() {
        String[] argList = new String[]{"-n", FILE_2_TXT};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_WnwritableSrcFile_ThrowsException() {
        // no permissions to rename unwritable
        String[] argList = new String[]{UNWRITABLE_FILE, "file4.txt"};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_UnwritableDestFileWithoutFlag_ThrowsException() throws Exception {
        // no permissions to override unwritable
        String[] argList = new String[]{FILE_2_TXT, UNWRITABLE_FILE};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));

        File expectedSrcFile = new File(tempDir, FILE_2_TXT);
        File expectedDestFile = new File(tempDir, UNWRITABLE_FILE);

        assertTrue(expectedSrcFile.exists());
        assertTrue(expectedDestFile.exists());
        List<String> expectedNewFileContents = Files.readAllLines(expectedDestFile.toPath());
        assertEquals(0, expectedNewFileContents.size());
    }

    @Test
    public void run_UnwritableDestFileWithFlag_NoChange() throws Exception {
        // not overriding unwritable, so no error thrown
        String[] argList = new String[]{"-n", FILE_2_TXT, UNWRITABLE_FILE};
        mvApplication.run(argList, System.in, System.out);

        // no change
        File expectedSrcFile = new File(tempDir, FILE_2_TXT);
        File expectedDestFile = new File(tempDir, UNWRITABLE_FILE);

        assertTrue(expectedSrcFile.exists());
        assertTrue(expectedDestFile.exists());
        List<String> expectedNewFileContents = Files.readAllLines(expectedDestFile.toPath());
        assertEquals(0, expectedNewFileContents.size());
    }

    @Test
    @DisabledOnOs(WINDOWS)
    public void run_UnwritableDestFolder_ThrowsException() {
        // no permissions to move files into blocked folder
        String[] argList = new String[]{FILE_1_TXT, FILE_2_TXT, BLOCKED_FILE};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));

        File expectedRemainingFile1 = new File(tempDir, FILE_1_TXT);
        File expectedRemainingFile2 = new File(tempDir, FILE_2_TXT);

        assertTrue(expectedRemainingFile1.exists());
        assertTrue(expectedRemainingFile2.exists());
    }

    @Test
    public void run_WithoutFlag2ArgsDestExist_RemoveSrcAndOverrideFile() throws Exception {
        String[] argList = new String[]{FILE_1_TXT, FILE_2_TXT};
        mvApplication.run(argList, System.in, System.out);

        File expectedRemovedFile = new File(tempDir, FILE_1_TXT);
        File expectedNewFile = new File(tempDir, FILE_2_TXT);

        assertFalse(expectedRemovedFile.exists());
        assertTrue(expectedNewFile.exists());
        List<String> expectedNewFileContents = Files.readAllLines(expectedNewFile.toPath());
        assertEquals(FILE1_CONTENTS, expectedNewFileContents.get(0));
    }

    @Test
    public void run_WithFlag2ArgsDestFileExist_NoChange() throws Exception {
        String[] argList = new String[]{"-n", FILE_1_TXT, FILE_2_TXT};
        mvApplication.run(argList, System.in, System.out);

        File expectedOldFile = new File(tempDir, FILE_1_TXT);
        File expectedNewFile = new File(tempDir, FILE_2_TXT);

        assertTrue(expectedOldFile.exists());
        List<String> expectedOldFileContents = Files.readAllLines(expectedOldFile.toPath());
        assertEquals(FILE1_CONTENTS, expectedOldFileContents.get(0));
        assertTrue(expectedNewFile.exists());
        List<String> expectedNewFileContents = Files.readAllLines(expectedNewFile.toPath());
        assertEquals(FILE2_CONTENTS, expectedNewFileContents.get(0));
    }

    @Test
    public void run_WithoutFlags2ArgsDestFileNonExist_RenameFile() throws Exception {
        String[] argList = new String[]{FILE_2_TXT, "file4.txt"};
        mvApplication.run(argList, System.in, System.out);

        File expectedRemovedFile = new File(tempDir, FILE_2_TXT);
        File expectedNewFile = new File(tempDir, "file4.txt");

        assertFalse(expectedRemovedFile.exists());
        assertTrue(expectedNewFile.exists());
        List<String> expectedNewFileContents = Files.readAllLines(expectedNewFile.toPath());
        assertEquals(FILE2_CONTENTS, expectedNewFileContents.get(0));
    }

    @Test
    public void run_WithFlagRenameOneSubFileIntoFolder_RenameSubFile() throws Exception {
        String[] argList = new String[]{SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT, "file5.txt"};
        mvApplication.run(argList, System.in, System.out);

        File expectedRemovedFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File expectedNewFile = new File(tempDir, "file5.txt");

        assertFalse(expectedRemovedFile.exists());
        assertTrue(expectedNewFile.exists());
        List<String> expectedNewFileContents = Files.readAllLines(expectedNewFile.toPath());
        assertEquals(SUBFILE2_CONTENTS, expectedNewFileContents.get(0));
    }

    @Test
    public void run_WithFlagRenameOneSubFileIntoSubFile_RenameSubFile() throws Exception {
        String[] argList = new String[]{SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT,
                SUBFOLDER_2 + CHAR_FILE_SEP + "file5.txt"};
        mvApplication.run(argList, System.in, System.out);

        File expectedRemovedFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File expectedNewFile = new File(tempDir,SUBFOLDER_2 + CHAR_FILE_SEP + "file5.txt");

        assertFalse(expectedRemovedFile.exists());
        assertTrue(expectedNewFile.exists());
        List<String> expectedNewFileContents = Files.readAllLines(expectedNewFile.toPath());
        assertEquals(SUBFILE2_CONTENTS, expectedNewFileContents.get(0));
    }

    @Test
    public void run_WithFlags2ArgsDestFoldersNonExist_RenameFile() throws Exception {
        String[] argList = new String[]{"-n", SUBFOLDER_1, "newSubFolder"};
        mvApplication.run(argList, System.in, System.out);

        File expectedRemovedFile = new File(tempDir, SUBFOLDER_1);
        File expectedNewFile = new File(tempDir, "newSubFolder");

        assertFalse(expectedRemovedFile.exists());
        assertTrue(expectedNewFile.exists());
        assertTrue(Files.isDirectory(expectedNewFile.toPath()));
        List<String> subFiles = Arrays.stream(expectedNewFile.listFiles()).map(File::getName).collect(Collectors.toList());
        assertEquals(2, subFiles.size());
        assertTrue(subFiles.contains(FILE_2_TXT));
        assertTrue(subFiles.contains(SUB_SUBFOLDER_1));
    }

    @Test
    public void run_WithFlags2ArgsSrcFolderDestFileNonExist_RenameFile() throws Exception {
        String[] argList = new String[]{"-n", SUBFOLDER_1, "file3.txt"};
        mvApplication.run(argList, System.in, System.out);

        File expectedRemovedFile = new File(tempDir, SUBFOLDER_1);
        File expectedNewFile = new File(tempDir, "file3.txt");

        assertFalse(expectedRemovedFile.exists());
        assertTrue(expectedNewFile.exists());
        assertTrue(Files.isDirectory(expectedNewFile.toPath()));
        List<String> subFiles = Arrays.stream(expectedNewFile.listFiles()).map(File::getName).collect(Collectors.toList());
        assertEquals(2, subFiles.size());
        assertTrue(subFiles.contains(FILE_2_TXT));
        assertTrue(subFiles.contains(SUB_SUBFOLDER_1));
    }

    @Test
    public void run_WithFlags2ArgsDiffFileTypesNonExist_ConvertFolderToFile() throws Exception {
        String[] argList = new String[]{"-n", FILE_1_TXT, "file1.png"};
        mvApplication.run(argList, System.in, System.out);

        File expectedRemovedFile = new File(tempDir, FILE_1_TXT);
        File expectedNewFile = new File(tempDir, "file1.png");

        assertFalse(expectedRemovedFile.exists());
        assertTrue(expectedNewFile.exists());
        List<String> expectedNewFileContents = Files.readAllLines(expectedNewFile.toPath());
        assertEquals(FILE1_CONTENTS, expectedNewFileContents.get(0));
    }

    @Test
    public void run_WithoutFlagsSameSrcAndDestExist_NoChange() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String[] argList = new String[]{FILE_1_TXT, FILE_1_TXT};
        mvApplication.run(argList, System.in, output);
        File expectedFile = new File(tempDir, FILE_1_TXT);
        assertTrue(expectedFile.exists());
        List<String> expectedNewFileContents = Files.readAllLines(expectedFile.toPath());
        assertEquals(FILE1_CONTENTS, expectedNewFileContents.get(0));
    }

    @Test
    public void run_WithoutFlagsSameSrcToCurrFolder_ThrowException() {
        String[] argList = new String[]{FILE_1_TXT, "."};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_invalidSourceFile_ThrowException() {
        String[] argList = new String[]{"file3.txt", FILE_1_TXT};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_WithoutFlagMoveOneFileIntoFolder_MovedIntoFolder() throws Exception {
        String[] argList = new String[]{FILE_1_TXT, SUBFOLDER_1};
        mvApplication.run(argList, System.in, System.out);

        File expectedRemovedFile = new File(tempDir, FILE_1_TXT);
        File expectedNewFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_1_TXT);

        assertFalse(expectedRemovedFile.exists());
        assertTrue(expectedNewFile.exists());
        List<String> expectedNewFileContents = Files.readAllLines(expectedNewFile.toPath());
        assertEquals(FILE1_CONTENTS, expectedNewFileContents.get(0));
    }

    @Test
    public void run_WithoutFlagMoveOneSubFileIntoFolder_MovedIntoFolder() throws Exception {
        String[] argList = new String[]{SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT, SUBFOLDER_2};
        mvApplication.run(argList, System.in, System.out);

        File expectedRemovedFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File expectedNewFile = new File(tempDir, SUBFOLDER_2 + CHAR_FILE_SEP + FILE_2_TXT);

        assertFalse(expectedRemovedFile.exists());
        assertTrue(expectedNewFile.exists());
        List<String> expectedNewFileContents = Files.readAllLines(expectedNewFile.toPath());
        assertEquals(SUBFILE2_CONTENTS, expectedNewFileContents.get(0));
    }

    @Test
    public void run_WithoutFlagMoveOneSubFileIntoSubSFolder_MovedIntoSubFolder() throws Exception {
        String[] argList = new String[]{SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT,
                SUBFOLDER_2 + CHAR_FILE_SEP + SUB_SUBFOLDER_2};
        mvApplication.run(argList, System.in, System.out);

        File expectedRemovedFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File expectedNewFile = new File(tempDir, SUBFOLDER_2 + CHAR_FILE_SEP + SUB_SUBFOLDER_2 +
                CHAR_FILE_SEP + FILE_2_TXT);

        assertFalse(expectedRemovedFile.exists());
        assertTrue(expectedNewFile.exists());
        List<String> expectedNewFileContents = Files.readAllLines(expectedNewFile.toPath());
        assertEquals(SUBFILE2_CONTENTS, expectedNewFileContents.get(0));
    }

    @Test
    public void run_WithoutFlagMoveOneAbsolutePathFileIntoSubFolder_MovedIntoSubFolder() throws Exception {
        String[] argList = new String[]{tempDir.getAbsolutePath() + CHAR_FILE_SEP + SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT,
                tempDir.getAbsolutePath() + CHAR_FILE_SEP + SUBFOLDER_2 + CHAR_FILE_SEP + SUB_SUBFOLDER_2};
        mvApplication.run(argList, System.in, System.out);

        File expectedRemovedFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File expectedNewFile = new File(tempDir, SUBFOLDER_2 + CHAR_FILE_SEP +
                SUB_SUBFOLDER_2 + CHAR_FILE_SEP + FILE_2_TXT);

        assertFalse(expectedRemovedFile.exists());
        assertTrue(expectedNewFile.exists());
        List<String> expectedNewFileContents = Files.readAllLines(expectedNewFile.toPath());
        assertEquals(SUBFILE2_CONTENTS, expectedNewFileContents.get(0));
    }

    @Test
    public void run_WithoutFlagsMoveOneFolderIntoFolder_MovedIntoFolder() throws Exception {
        String[] argList = new String[]{SUBFOLDER_2, SUBFOLDER_1};
        mvApplication.run(argList, System.in, System.out);

        File expectedRemovedFile = new File(tempDir, SUBFOLDER_2);
        File expectedNewFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUBFOLDER_2);

        assertFalse(expectedRemovedFile.exists());
        assertTrue(expectedNewFile.exists());
        assertTrue(Files.isDirectory(expectedNewFile.toPath()));
        File[] subFiles = expectedNewFile.listFiles();
        assertEquals(1, subFiles.length);
        assertEquals(SUB_SUBFOLDER_2, subFiles[0].getName());
    }

    @Test
    public void run_WithoutFlagsMoveMultipleFilesIntoFolder_MovedAllIntoFolder() throws Exception {
        String[] argList = new String[]{FILE_1_TXT, SUBFOLDER_2, SUBFOLDER_1};
        mvApplication.run(argList, System.in, System.out);

        File expectedRemainingSubFile2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File expectedRemovedFile1 = new File(tempDir, FILE_1_TXT);
        File expectedRemovedSubFolder2 = new File(tempDir, SUBFOLDER_2);
        File expectedNewFile1 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_1_TXT);
        File expectedNewSubFolder2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUBFOLDER_2);

        assertTrue(expectedRemainingSubFile2.exists());
        assertFalse(expectedRemovedFile1.exists());
        assertFalse(expectedRemovedSubFolder2.exists());
        assertTrue(expectedNewFile1.exists());
        List<String> expectedNewFile1Contents = Files.readAllLines(expectedNewFile1.toPath());
        assertEquals(FILE1_CONTENTS, expectedNewFile1Contents.get(0));
        assertTrue(expectedNewSubFolder2.exists());
        assertTrue(Files.isDirectory(expectedNewSubFolder2.toPath()));
        File[] subFiles = expectedNewSubFolder2.listFiles();
        assertEquals(1, subFiles.length);
        assertEquals(SUB_SUBFOLDER_2, subFiles[0].getName());
    }

    @Test
    public void run_WithoutFlagsMoveFileWithSameNameIntoFolder_MovedIntoFolderWithOverriding() throws Exception {
        String[] argList = new String[]{FILE_1_TXT, FILE_2_TXT, SUBFOLDER_2, SUBFOLDER_1};
        mvApplication.run(argList, System.in, System.out);

        File expectedRemovedFile1 = new File(tempDir, FILE_1_TXT);
        File expectedRemovedFile2 = new File(tempDir, FILE_2_TXT);
        File expectedRemovedSubFolder2 = new File(tempDir, SUBFOLDER_2);
        File expectedNewFile1 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_1_TXT);
        File expectedNewFile2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File expectedNewSubFolder2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUBFOLDER_2);

        assertFalse(expectedRemovedFile1.exists());
        assertFalse(expectedRemovedFile2.exists());
        assertFalse(expectedRemovedSubFolder2.exists());
        assertTrue(expectedNewFile1.exists());
        List<String> expectedNewFile1Contents = Files.readAllLines(expectedNewFile1.toPath());
        assertEquals(FILE1_CONTENTS, expectedNewFile1Contents.get(0));
        assertTrue(expectedNewFile2.exists());
        List<String> expectedNewFile2Contents = Files.readAllLines(expectedNewFile2.toPath());
        assertEquals(FILE2_CONTENTS, expectedNewFile2Contents.get(0)); //override with file2.txt contents
        assertTrue(expectedNewSubFolder2.exists());
        assertTrue(Files.isDirectory(expectedNewSubFolder2.toPath()));
        File[] subFiles = expectedNewSubFolder2.listFiles();
        assertEquals(1, subFiles.length);
        assertEquals(SUB_SUBFOLDER_2, subFiles[0].getName());
    }

    @Test
    public void run_WithFlagsMoveFilesWithSameNameIntoFolder_MovedIntoFolderWithoutOverriding() throws Exception {
        String[] argList = new String[]{"-n", FILE_1_TXT, FILE_2_TXT, SUBFOLDER_2, SUBFOLDER_1};
        mvApplication.run(argList, System.in, System.out);

        File expectedRemovedFile1 = new File(tempDir, FILE_1_TXT);
        File expectedRemovedFile2 = new File(tempDir, FILE_2_TXT);
        File expectedRemovedSubFolder2 = new File(tempDir, SUBFOLDER_2);
        File expectedNewFile1 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_1_TXT);
        File expectedNewFile2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + FILE_2_TXT);
        File expectedNewSubFolder2 = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUBFOLDER_2);

        assertFalse(expectedRemovedFile1.exists());
        assertTrue(expectedRemovedFile2.exists()); // file2.txt not moved
        assertFalse(expectedRemovedSubFolder2.exists());
        assertTrue(expectedNewFile1.exists());
        List<String> expectedNewFile1Contents = Files.readAllLines(expectedNewFile1.toPath());
        assertEquals(FILE1_CONTENTS, expectedNewFile1Contents.get(0));
        assertTrue(expectedNewFile2.exists());
        List<String> expectedNewFile2Contents = Files.readAllLines(expectedNewFile2.toPath());
        assertEquals(SUBFILE2_CONTENTS, expectedNewFile2Contents.get(0)); //NOT override with file2.txt contents
        assertTrue(expectedNewSubFolder2.exists());
        assertTrue(Files.isDirectory(expectedNewSubFolder2.toPath()));
        File[] subFiles = expectedNewSubFolder2.listFiles();
        assertEquals(1, subFiles.length);
        assertEquals(SUB_SUBFOLDER_2, subFiles[0].getName());
    }

    @Test
    public void run_NonExistentDestFolder_ThrowsException() {
        String[] argList = new String[]{"-n", FILE_1_TXT, FILE_2_TXT, "nonExistentFolder"};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_ExistentNonDirDestFile_ThrowsException() {
        String[] argList = new String[]{FILE_1_TXT, SUBFOLDER_2, FILE_2_TXT};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_NonExistentNonDirDestFile_ThrowsException() {
        String[] argList = new String[]{FILE_1_TXT, SUBFOLDER_2, "f"};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));
    }

    @Test
    public void run_InvalidSrcFileFirst_ThrowsException() {
        String[] argList = new String[]{"f", SUBFOLDER_2, SUBFOLDER_1};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));

        File expectedRemainingFile = new File(tempDir, SUBFOLDER_2);
        assertTrue(expectedRemainingFile.exists());
    }

    @Test
    public void run_InvalidSrcFilesAfter_ThrowsException() {
        String[] argList = new String[]{SUBFOLDER_2, SUBFOLDER, SUBFOLDER_1};
        assertThrows(MvException.class, () -> mvApplication.run(argList, System.in, System.out));

        File expectedMovedFile = new File(tempDir, SUBFOLDER_2);
        File expectedNewFile = new File(tempDir, SUBFOLDER_1 + CHAR_FILE_SEP + SUBFOLDER_2);
        assertFalse(expectedMovedFile.exists());
        assertTrue(expectedNewFile.exists());
        assertTrue(Files.isDirectory(expectedNewFile.toPath()));
        File[] subFiles = expectedNewFile.listFiles();
        assertEquals(1, subFiles.length);
        assertEquals(SUB_SUBFOLDER_2, subFiles[0].getName());
    }
}