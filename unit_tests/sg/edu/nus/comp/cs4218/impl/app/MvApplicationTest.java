package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.MvException;

class MvApplicationTest {

    private static final String PROB_MV_DEST_FILE = "mv: Problem move to destination file: ";
    private static final String PROB_MV_FOLDER = "mv: Problem move to folder: ";
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

    private String getFileNotFoundMsg(String cause, String file) {
        return String.format("%s'%s': No such file or directory", cause, file);
    }

    private String getPermissionDeniedMsg(String cause, String file) {
        return String.format("%s'%s': Permission denied", cause, file);
    }

    @BeforeEach
    void setUp() throws IOException {
        app = new MvApplication();

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
    void mvSrcFileToDestFile_SrcFileDoNotExist_ThrowsMvException() {
        String nonExistFile = tempDir.resolve("nonExistFile.txt").toString();
        MvException result = assertThrowsExactly(MvException.class, () -> {
            app.mvSrcFileToDestFile(false, nonExistFile, tempDestFile);
        });
        String expected = getFileNotFoundMsg(PROB_MV_DEST_FILE, nonExistFile);
        assertEquals(expected, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvSrcFileToDestFile_SrcFileNoPermissionToRead_ThrowsMvException() {
        boolean isSetReadable = tempSrcFilePath.toFile().setReadable(false);
        if (!isSetReadable) {
            fail("Failed to set read permission to false for test source file");
        }

        MvException result = assertThrowsExactly(MvException.class, () -> {
            app.mvSrcFileToDestFile(false, tempSrcFile, tempDestFile);
        });
        String expected = getPermissionDeniedMsg(PROB_MV_DEST_FILE, tempSrcFile);
        assertEquals(expected, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvSrcFileToDestFile_DestFileNoPermissionToWrite_ThrowsMvException() {
        boolean isSetWritable = tempDestFilePath.toFile().setWritable(false);
        if (!isSetWritable) {
            fail("Failed to set write permission to false for test destination file");
        }

        MvException result = assertThrowsExactly(MvException.class, () -> {
            app.mvSrcFileToDestFile(false, tempSrcFile, tempDestFile);
        });
        String expected = getPermissionDeniedMsg(PROB_MV_DEST_FILE, tempDestFile);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void mvSrcFileToDestFile_DestFileDoNotExist_MovedFile() {
        String content = "a b c";
        assertDoesNotThrow(() -> Files.write(tempSrcFilePath, content.getBytes()));
        List<String> expectedContent = assertDoesNotThrow(() -> Files.readAllLines(tempSrcFilePath));

        Path nonExistDestPath = tempDir.resolve("nonExistFile.txt");
        String nonExistDestFile = nonExistDestPath.toString();

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(false, tempSrcFile, nonExistDestFile));
        List<String> actualContent = assertDoesNotThrow(() -> Files.readAllLines(nonExistDestPath));

        File destFile = nonExistDestPath.toFile();
        File srcFile = tempSrcFilePath.toFile();

        assertTrue(destFile.exists()); // assert that the dest file now exists
        assertFalse(srcFile.exists()); // assert that the src file no longer exist
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void mvSrcFileToDestFile_IsOverwrite_MovedFile() {
        String srcContent = "a b c";
        assertDoesNotThrow(() -> Files.write(tempSrcFilePath, srcContent.getBytes()));
        List<String> expectedContent = assertDoesNotThrow(() -> Files.readAllLines(tempSrcFilePath));

        String destContent = "1 2 3";
        assertDoesNotThrow(() -> Files.write(tempDestFilePath, destContent.getBytes()));

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(true, tempSrcFile, tempDestFile));
        List<String> actualContent = assertDoesNotThrow(() -> Files.readAllLines(tempDestFilePath));

        File srcFile = tempSrcFilePath.toFile();

        assertFalse(srcFile.exists()); // assert that the src file no longer exist
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void mvFilesToFolder_DestFolderDoNotExist_ThrowsMvException() {
        String nonExistFolder = tempDir.resolve("nonExistFolder").toString();
        MvException result = assertThrowsExactly(MvException.class, () -> {
            app.mvFilesToFolder(false, nonExistFolder, null);
        });
        String expected = getFileNotFoundMsg(PROB_MV_FOLDER, nonExistFolder);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void mvFilesToFolder_DestIsNotADirectory_ThrowsMvException() {
        MvException result = assertThrowsExactly(MvException.class, () -> {
            app.mvFilesToFolder(false, tempDestFile, null);
        });
        String expected = String.format("%s'%s': This is a directory", PROB_MV_FOLDER, tempDestFile);
        assertEquals(expected, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvFilesToFolder_DestFolderNoPermissionToWrite_ThrowsMvException() {
        boolean isSetWritable = tempDestDirPath.toFile().setWritable(false);
        if (!isSetWritable) {
            fail("Failed to set write permission to false for test destination directory");
        }

        MvException result = assertThrowsExactly(MvException.class, () -> {
            app.mvFilesToFolder(false, tempDestDir, null);
        });
        String expected = getPermissionDeniedMsg(PROB_MV_FOLDER, tempDestDir);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void mvFilesToFolder_SrcFileDoNotExist_ReturnsErrorMessage() {
        String nonExistFile = tempDir.resolve("nonExistFile.txt").toString();
        String result = assertDoesNotThrow(() -> app.mvFilesToFolder(false, tempDestDir, nonExistFile));
        String expected = getFileNotFoundMsg(PROB_MV_FOLDER, nonExistFile);
        assertEquals(expected, result);
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvFilesToFolder_SrcFileNoPermissionToRead_ReturnsErrorMessage() {
        boolean isSetReadable = tempSrcFilePath.toFile().setReadable(false);
        if (!isSetReadable) {
            fail("Failed to set read permission to false for test source file");
        }

        String result = assertDoesNotThrow(() -> app.mvFilesToFolder(false, tempDestDir, tempSrcFile));
        String expected = getPermissionDeniedMsg(PROB_MV_FOLDER, tempSrcFile);
        assertEquals(expected, result);
    }

    @Test
    void mvFilesToFolder_SrcFileDoNotExistInDestFolder_MovedFile() {
        String result = assertDoesNotThrow(() -> app.mvFilesToFolder(false, tempDestDir, tempSrcFile));
        File movedSrcFile = tempDestDirPath.resolve(TEMP_SRC_FILE).toFile();
        File srcFile = tempSrcFilePath.toFile();

        assertNull(result);
        assertTrue(movedSrcFile.exists()); // assert that src file now exist under subdirectory
        assertFalse(srcFile.exists()); // assert that the original src file no longer exist
    }

    @Test
    void mvFilesToFolder_IsOverwrite_MovedFile() {
        String srcContent = "a b c";
        assertDoesNotThrow(() -> Files.write(tempSrcFilePath, srcContent.getBytes()));
        List<String> expectedContent = assertDoesNotThrow(() -> Files.readAllLines(tempSrcFilePath));

        String overwriteContent = "1 2 3";
        Path destFilePath = tempDestDirPath.resolve(TEMP_SRC_FILE);
        assertDoesNotThrow(() -> Files.createFile(destFilePath));
        assertDoesNotThrow(() -> Files.write(destFilePath, overwriteContent.getBytes()));

        // assert that another src file of different content exist in subdirectory
        assertTrue(destFilePath.toFile().exists());

        assertDoesNotThrow(() -> app.mvFilesToFolder(true, tempDestDir, tempSrcFile));
        List<String> actualContent = assertDoesNotThrow(() -> Files.readAllLines(destFilePath));

        File destFile = destFilePath.toFile();
        File srcFile = tempSrcFilePath.toFile();

        assertTrue(destFile.exists()); // assert that the src file in subdirectory still exist
        assertFalse(srcFile.exists()); // assert that the original src file no longer exist
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void mvFilesToFolder_MultipleSrcFilesAllExists_MovedFile() {
        Path srcFileAPath = tempDir.resolve("A");
        Path srcFileBPath = tempDir.resolve("B");
        assertDoesNotThrow(() -> Files.createFile(srcFileAPath));
        assertDoesNotThrow(() -> Files.createFile(srcFileBPath));

        assertDoesNotThrow(() -> app.mvFilesToFolder(false, tempDestDirPath.toString(),
                srcFileAPath.toString(), srcFileBPath.toString()));

        File movedSrcFileA = tempDestDirPath.resolve("A").toFile();
        File movedSrcFileB = tempDestDirPath.resolve("B").toFile();
        File srcFileA = srcFileAPath.toFile();
        File srcFileB = srcFileBPath.toFile();

        assertTrue(movedSrcFileA.exists()); // assert that the src file A exists in subdirectory
        assertTrue(movedSrcFileB.exists()); // assert that the src file B exists in subdirectory
        assertFalse(srcFileA.exists()); // assert that the original src file A no longer exists
        assertFalse(srcFileB.exists()); // assert that the original src file B no longer exists
    }

    @Test
    void mvFilesToFolder_SomeSrcFileDoNotExistsAtEnd_MovedFile() {
        Path srcFileAPath = tempDir.resolve("A");
        Path srcFileBPath = tempDir.resolve("B");
        Path srcFileCPath = tempDir.resolve("C"); // do not exist
        assertDoesNotThrow(() -> Files.createFile(srcFileAPath));
        assertDoesNotThrow(() -> Files.createFile(srcFileBPath));

        String expected = getFileNotFoundMsg(PROB_MV_FOLDER, srcFileCPath.toString());
        String actual = assertDoesNotThrow(() -> app.mvFilesToFolder(false, tempDestDirPath.toString(),
                srcFileAPath.toString(), srcFileBPath.toString(), srcFileCPath.toString()));

        File movedSrcFileA = tempDestDirPath.resolve("A").toFile();
        File movedSrcFileB = tempDestDirPath.resolve("B").toFile();
        File srcFileA = srcFileAPath.toFile();
        File srcFileB = srcFileBPath.toFile();

        assertTrue(movedSrcFileA.exists()); // assert that the src file A exists in subdirectory
        assertTrue(movedSrcFileB.exists()); // assert that the src file B exists in subdirectory
        assertFalse(srcFileA.exists()); // assert that the original src file A no longer exists
        assertFalse(srcFileB.exists()); // assert that the original src file B no longer exists
        assertEquals(expected, actual); // assert that 'C' is returned
    }

    @Test
    void mvFilesToFolder_SomeSrcFileDoNotExistsAtStart_MovedFile() {
        Path srcFileAPath = tempDir.resolve("A"); // do not exist
        Path srcFileBPath = tempDir.resolve("B");
        Path srcFileCPath = tempDir.resolve("C");
        assertDoesNotThrow(() -> Files.createFile(srcFileBPath));
        assertDoesNotThrow(() -> Files.createFile(srcFileCPath));

        String expected = getFileNotFoundMsg(PROB_MV_FOLDER, srcFileAPath.toString());
        String actual = assertDoesNotThrow(() -> app.mvFilesToFolder(false, tempDestDirPath.toString(),
                srcFileAPath.toString(), srcFileBPath.toString(), srcFileCPath.toString()));

        File movedSrcFileB = tempDestDirPath.resolve("B").toFile();
        File movedSrcFileC = tempDestDirPath.resolve("C").toFile();
        File srcFileB = srcFileBPath.toFile();
        File srcFileC = srcFileCPath.toFile();

        assertTrue(movedSrcFileB.exists()); // assert that the src file B exists in subdirectory
        assertTrue(movedSrcFileC.exists()); // assert that the src file C exists in subdirectory
        assertFalse(srcFileB.exists()); // assert that the original src file B no longer exists
        assertFalse(srcFileC.exists()); // assert that the original src file C no longer exists
        assertEquals(expected, actual); // assert that 'A' is returned
    }
}
