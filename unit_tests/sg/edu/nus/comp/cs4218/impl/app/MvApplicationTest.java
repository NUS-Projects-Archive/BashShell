package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.MvException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class MvApplicationTest {

    private static final String TEMP_SRC_FILE = "srcFile.txt";
    private static final String TEMP_DEST_FILE = "destFile.txt";
    private static final String TEMP_DEST_DIR = "subdirectory";
    private MvApplication app;

    @TempDir
    private Path tempDir;
    private Path tempSrcFilePath;
    private Path tempDestFilePath;
    private Path tempDestDirPath;
    private String tempSrcFile;
    private String tempDestFile;
    private String tempDestDir;

    @BeforeEach
    void setUp() throws IOException {
        this.app = new MvApplication();

        // Create temporary file and subdirectory, automatically deletes after test execution
        tempSrcFilePath = tempDir.resolve(TEMP_SRC_FILE);
        tempSrcFile = tempSrcFilePath.toString();

        tempDestFilePath = tempDir.resolve(TEMP_DEST_FILE);
        tempDestFile = tempDestFilePath.toString();

        tempDestDirPath = tempDir.resolve(TEMP_DEST_DIR);
        tempDestDir = tempDestDirPath.toString();

        Files.createFile(tempSrcFilePath);
        Files.createFile(tempDestFilePath);
        Files.createDirectories(tempDestDirPath);
    }

    @Test
    void run_NullArgs_ThrowsMvException() {
        String expectedMsg = "mv: Missing Argument";
        MvException exception = assertThrowsExactly(MvException.class, () -> {
            app.run(null, null, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_EmptyArgsArray_ThrowsMvException() {
        String expectedMsg = "mv: Missing Argument";
        MvException exception = assertThrowsExactly(MvException.class, () -> {
            String[] args = {};
            app.run(args, null, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "1", "$", "."})
    void run_InsufficientArgs_ThrowsMvException(String args) {
        String expectedMsg = "mv: Insufficient arguments";
        MvException exception = assertThrowsExactly(MvException.class, () -> {
            app.run(args.split("\\s+"), null, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void mvSrcFileToDestFile_SrcFileDoNotExist_ThrowsMvException() {
        Path nonExistFilePath = tempDir.resolve("nonExistFile.txt");
        String nonExistFile = nonExistFilePath.toString();
        String expectedMsg = "mv: Problem move to destination file: No such file or directory";
        MvException exception = assertThrowsExactly(MvException.class, () -> {
            app.mvSrcFileToDestFile(false, nonExistFile, tempDestFile);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvSrcFileToDestFile_SrcFileNoPermissionToRead_ThrowsMvException() {
        boolean isReadable = tempSrcFilePath.toFile().setReadable(false);
        if (isReadable) {
            fail("Failed to set read permission to false for test source file");
        }

        String expectedMsg = "mv: Problem move to destination file: Permission denied";
        MvException exception = assertThrowsExactly(MvException.class, () -> {
            app.mvSrcFileToDestFile(false, tempSrcFile, tempDestFile);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvSrcFileToDestFile_DestFileNoPermissionToWrite_ThrowsMvException() {
        boolean isWritable = tempDestFilePath.toFile().setWritable(false);
        if (isWritable) {
            fail("Failed to set write permission to false for test destination file");
        }

        String expectedMsg = "mv: Problem move to destination file: Permission denied";
        MvException exception = assertThrowsExactly(MvException.class, () -> {
            app.mvSrcFileToDestFile(false, tempSrcFile, tempDestFile);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void mvSrcFileToDestFile_DestFileDoNotExist_MovedFile() throws IOException, MvException {
        String content = "a b c";
        Files.write(tempSrcFilePath, content.getBytes());
        List<String> expectedContent = Files.readAllLines(tempSrcFilePath);

        Path nonExistDestPath = tempDir.resolve("nonExistFile.txt");
        String nonExistDestFile = nonExistDestPath.toString();

        app.mvSrcFileToDestFile(false, tempSrcFile, nonExistDestFile);
        List<String> actualContent = Files.readAllLines(nonExistDestPath);

        File destFile = nonExistDestPath.toFile();
        File srcFile = tempSrcFilePath.toFile();

        assertTrue(destFile.exists()); // assert that the dest file now exists
        assertFalse(srcFile.exists()); // assert that the src file no longer exist
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void mvSrcFileToDestFile_IsOverwrite_MovedFile() throws IOException, MvException {
        String srcContent = "a b c";
        Files.write(tempSrcFilePath, srcContent.getBytes());
        List<String> expectedContent = Files.readAllLines(tempSrcFilePath);

        String destContent = "1 2 3";
        Files.write(tempDestFilePath, destContent.getBytes());

        app.mvSrcFileToDestFile(true, tempSrcFile, tempDestFile);
        List<String> actualContent = Files.readAllLines(tempDestFilePath);

        File srcFile = tempSrcFilePath.toFile();

        assertFalse(srcFile.exists()); // assert that the src file no longer exist
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void mvFilesToFolder_DestFolderDoNotExist_ThrowsMvException() {
        String nonExistFolder = tempDir.resolve("nonExistFolder").toString();
        String expectedMsg = "mv: Problem move to folder: No such file or directory";
        MvException exception = assertThrowsExactly(MvException.class, () -> {
            app.mvFilesToFolder(false, nonExistFolder, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void mvFilesToFolder_DestIsNotADirectory_ThrowsMvException() {
        String expectedMsg = "mv: Problem move to folder: This is a directory";
        MvException exception = assertThrowsExactly(MvException.class, () -> {
            app.mvFilesToFolder(false, tempDestFile, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvFilesToFolder_DestFolderNoPermissionToWrite_ThrowsMvException() {
        boolean isWritable = tempDestDirPath.toFile().setWritable(false);
        if (isWritable) {
            fail("Failed to set write permission to false for test destination directory");
        }

        String expectedMsg = "mv: Problem move to folder: Permission denied";
        MvException exception = assertThrowsExactly(MvException.class, () -> {
            app.mvFilesToFolder(false, tempDestDir, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void mvFilesToFolder_SrcFileDoNotExist_ThrowsMvException() {
        String nonExistFile = tempDir.resolve("nonExistFile.txt").toString();
        String expectedMsg = "mv: Problem move to folder: No such file or directory";
        MvException exception = assertThrowsExactly(MvException.class, () -> {
            app.mvFilesToFolder(false, tempDestDir, nonExistFile);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvFilesToFolder_SrcFileNoPermissionToRead_ThrowsMvException() {
        boolean isReadable = tempSrcFilePath.toFile().setReadable(false);
        if (isReadable) {
            fail("Failed to set read permission to false for test source file");
        }

        String expectedMsg = "mv: Problem move to folder: Permission denied";
        MvException exception = assertThrowsExactly(MvException.class, () -> {
            app.mvFilesToFolder(false, tempDestDir, tempSrcFile);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void mvFilesToFolder_SrcFileDoNotExistInDestFolder_MovedFile() throws MvException {
        app.mvFilesToFolder(false, tempDestDir, tempSrcFile);
        File srcFile = tempSrcFilePath.toFile();
        File movedSrcFile = tempDestDirPath.resolve(TEMP_SRC_FILE).toFile();

        assertTrue(movedSrcFile.exists()); // assert that src file now exist under subdirectory
        assertFalse(srcFile.exists()); // assert that the original src file no longer exist
    }

    @Test
    void mvFilesToFolder_IsOverwrite_MovedFile() throws IOException, MvException {
        String srcContent = "a b c";
        Files.write(tempSrcFilePath, srcContent.getBytes());
        List<String> expectedContent = Files.readAllLines(tempSrcFilePath);

        String overwriteContent = "1 2 3";
        Path destFilePath = tempDestDirPath.resolve(TEMP_SRC_FILE);
        Files.createFile(destFilePath);
        Files.write(destFilePath, overwriteContent.getBytes());

        // assert that another src file of different content exist in subdirectory
        assertTrue(destFilePath.toFile().exists());

        app.mvFilesToFolder(true, tempDestDir, tempSrcFile);
        List<String> actualContent = Files.readAllLines(destFilePath);

        File destFile = destFilePath.toFile();
        File srcFile = tempSrcFilePath.toFile();

        assertTrue(destFile.exists()); // assert that the src file in subdirectory still exist
        assertFalse(srcFile.exists()); // assert that the original src file no longer exist
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void mvFilesToFolder_MultipleSrcFiles_MovedFile() throws IOException, MvException {
        Path srcFileAPath = tempDir.resolve("A");
        Path srcFileBPath = tempDir.resolve("B");
        Files.createFile(srcFileAPath);
        Files.createFile(srcFileBPath);

        app.mvFilesToFolder(false, tempDestDirPath.toString(), srcFileAPath.toString(), srcFileBPath.toString());

        File movedSrcFileA = tempDestDirPath.resolve("A").toFile();
        File movedSrcFileB = tempDestDirPath.resolve("B").toFile();
        File srcFileA = srcFileAPath.toFile();
        File srcFileB = srcFileBPath.toFile();

        assertTrue(movedSrcFileA.exists()); // assert that the src file A exists in subdirectory
        assertTrue(movedSrcFileB.exists()); // assert that the src file B exists in subdirectory
        assertFalse(srcFileA.exists()); // assert that the original src file A no longer exists
        assertFalse(srcFileB.exists()); // assert that the original src file B no longer exists
    }
}