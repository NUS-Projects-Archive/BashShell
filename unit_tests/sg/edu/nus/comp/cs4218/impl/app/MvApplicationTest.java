package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.testutils.AssertUtils.assertFileDoNotExists;
import static sg.edu.nus.comp.cs4218.testutils.AssertUtils.assertFileExists;

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

    private static final String SRC_FILE = "srcFile.txt";
    private static final String DEST_FILE = "destFile.txt";
    private static final String DEST_DIR = "subdirectory";

    @TempDir
    private Path tempDir;
    private Path srcFilePath;
    private Path destFilePath;
    private Path destDirPath;
    private String srcFile;
    private String destFile;
    private String destDir;
    private MvApplication app;

    private String getFileNotFoundMsg(String file) {
        return String.format("mv: '%s': No such file or directory", file);
    }

    private String getCannotReadPermissionMsg(String file) {
        return String.format("mv: '%s': Could not read file", file);
    }

    private String getCannotWritePermissionMsg(String file) {
        return String.format("mv: '%s': Permission denied", file);
    }

    @BeforeEach
    void setUp() throws IOException {
        app = new MvApplication();

        // Create temporary file and subdirectory, automatically deletes after test execution
        srcFilePath = tempDir.resolve(SRC_FILE);
        srcFile = srcFilePath.toString();

        destFilePath = tempDir.resolve(DEST_FILE);
        destFile = destFilePath.toString();

        destDirPath = tempDir.resolve(DEST_DIR);
        destDir = destDirPath.toString();

        Files.createFile(srcFilePath);
        Files.createFile(destFilePath);
        Files.createDirectories(destDirPath);

        String srcContent = "source file content";
        String destContent = "destination file content";
        assertDoesNotThrow(() -> Files.write(srcFilePath, srcContent.getBytes()));
        assertDoesNotThrow(() -> Files.write(destFilePath, destContent.getBytes()));
    }

    @Test
    void mvSrcFileToDestFile_SrcFileDoNotExist_ThrowsMvException() {
        String nonExistFile = tempDir.resolve("nonExistFile.txt").toString();
        MvException result = assertThrowsExactly(MvException.class, () ->
                app.mvSrcFileToDestFile(false, nonExistFile, destFile)
        );
        String expected = getFileNotFoundMsg(nonExistFile);
        assertEquals(expected, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvSrcFileToDestFile_SrcFileNoPermissionToRead_ThrowsMvException() {
        boolean isSetReadable = srcFilePath.toFile().setReadable(false);
        assertTrue(isSetReadable, "Failed to set read permission to false for test source file");
        MvException result = assertThrowsExactly(MvException.class, () -> app.mvSrcFileToDestFile(false, srcFile, destFile));
        String expected = getCannotReadPermissionMsg(srcFile);
        assertEquals(expected, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvSrcFileToDestFile_DestFileNoPermissionToWrite_ThrowsMvException() {
        boolean isSetWritable = destFilePath.toFile().setWritable(false);
        assertTrue(isSetWritable, "Failed to set write permission to false for test destination file");
        MvException result = assertThrowsExactly(MvException.class, () -> app.mvSrcFileToDestFile(false, srcFile, destFile));
        String expected = getCannotWritePermissionMsg(destFile);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void mvSrcFileToDestFile_DestFileDoNotExist_MovesFile() {
        List<String> expectedContent = assertDoesNotThrow(() -> Files.readAllLines(srcFilePath));
        Path nonExistFilePath = tempDir.resolve("nonExistFile.txt");
        String nonExistFile = nonExistFilePath.toString();

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(false, srcFile, nonExistFile));
        List<String> actualContent = assertDoesNotThrow(() -> Files.readAllLines(nonExistFilePath));

        assertFileExists(nonExistFile);
        assertFileDoNotExists(srcFile);
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void mvSrcFileToDestFile_IsOverwrite_MovesFile() {
        List<String> expectedContent = assertDoesNotThrow(() -> Files.readAllLines(srcFilePath));
        List<String> oldContent = assertDoesNotThrow(() -> Files.readAllLines(destFilePath));

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(true, srcFile, destFile));
        List<String> actualContent = assertDoesNotThrow(() -> Files.readAllLines(destFilePath));

        assertFileDoNotExists(srcFile); // src file no longer exist
        assertEquals(expectedContent, actualContent); // dest file now holds the content of the src file
        assertNotEquals(oldContent, actualContent);   // dest file content has been overwritten
    }

    @Test
    void mvFilesToFolder_DestFolderDoNotExist_ThrowsMvException() {
        String nonExistFolder = tempDir.resolve("nonExistFolder").toString();
        MvException result = assertThrowsExactly(MvException.class, () ->
                app.mvFilesToFolder(false, nonExistFolder, new String[0])
        );
        String expected = getFileNotFoundMsg(nonExistFolder);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void mvFilesToFolder_DestIsNotADirectory_ThrowsMvException() {
        MvException result = assertThrowsExactly(MvException.class, () ->
                app.mvFilesToFolder(false, destFile, new String[0])
        );
        String expected = String.format("mv: '%s': Not a directory", destFile);
        assertEquals(expected, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvFilesToFolder_DestFolderNoPermissionToWrite_ThrowsMvException() {
        boolean isSetWritable = destDirPath.toFile().setWritable(false);
        assertTrue(isSetWritable, "Failed to set write permission to false for test destination directory");
        MvException result = assertThrowsExactly(MvException.class, () -> app.mvFilesToFolder(false, destDir, new String[0]));
        String expected = getCannotWritePermissionMsg(destDir);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void mvFilesToFolder_SrcFileDoNotExist_ThrowsMvException() {
        String nonExistFile = tempDir.resolve("nonExistFile.txt").toString();
        MvException result = assertThrowsExactly(MvException.class, () ->
                app.mvFilesToFolder(false, destDir, nonExistFile)
        );
        String expected = getFileNotFoundMsg(nonExistFile);
        assertEquals(expected, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvFilesToFolder_SrcFileNoPermissionToRead_ThrowsMvException() {
        boolean isSetReadable = srcFilePath.toFile().setReadable(false);
        assertTrue(isSetReadable, "Failed to set read permission to false for test source file");
        MvException result = assertThrowsExactly(MvException.class, () -> app.mvFilesToFolder(false, destDir, srcFile));
        String expected = getCannotReadPermissionMsg(srcFile);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void mvFilesToFolder_SrcFileDoNotExistInDestFolder_MovesFile() {
        String result = assertDoesNotThrow(() -> app.mvFilesToFolder(false, destDir, srcFile));
        String movedSrcFile = destDirPath.resolve(SRC_FILE).toString();

        assertNull(result);
        assertFileExists(movedSrcFile); // src file now exist under subdirectory
        assertFileDoNotExists(srcFile); // original src file no longer exist
    }

    @Test
    void mvFilesToFolder_IsOverwrite_MovesFile() {
        String destContent = "destination file content";
        Path destFilePath = destDirPath.resolve(SRC_FILE);
        String destFile = destFilePath.toString();
        assertDoesNotThrow(() -> Files.createFile(destFilePath));
        assertDoesNotThrow(() -> Files.write(destFilePath, destContent.getBytes()));
        List<String> expectedContent = assertDoesNotThrow(() -> Files.readAllLines(srcFilePath));

        // assert that another src file of different content exist in subdirectory
        List<String> oldContent = assertDoesNotThrow(() -> Files.readAllLines(destFilePath));
        assertFileExists(destFile);
        assertNotEquals(expectedContent, oldContent);

        assertDoesNotThrow(() -> app.mvFilesToFolder(true, destDir, srcFile));
        List<String> actualContent = assertDoesNotThrow(() -> Files.readAllLines(destFilePath));

        assertFileExists(destFile);     //src file in subdirectory still exist
        assertFileDoNotExists(srcFile); // original src file no longer exist
        assertEquals(expectedContent, actualContent); // dest file now holds the content of the src file
        assertNotEquals(oldContent, actualContent);   // dest file content has been overwritten
    }

    @Test
    void mvFilesToFolder_MultipleSrcFilesAllExists_MovesFile() {
        Path srcFileAPath = tempDir.resolve("A");
        Path srcFileBPath = tempDir.resolve("B");
        String srcFileA = srcFileAPath.toString();
        String srcFileB = srcFileBPath.toString();
        assertDoesNotThrow(() -> Files.createFile(srcFileAPath));
        assertDoesNotThrow(() -> Files.createFile(srcFileBPath));

        assertDoesNotThrow(() -> app.mvFilesToFolder(false, destDir, srcFileA, srcFileB));
        String movedSrcFileA = destDirPath.resolve("A").toString();
        String movedSrcFileB = destDirPath.resolve("B").toString();

        assertFileExists(movedSrcFileA); // src file A exists in subdirectory
        assertFileExists(movedSrcFileB); // src file B exists in subdirectory
        assertFileDoNotExists(srcFileA); // original src file A no longer exists
        assertFileDoNotExists(srcFileB); // original src file B no longer exists
    }

    @Test
    void mvFilesToFolder_SomeSrcFileDoNotExistsAtEnd_MovesFileAndThrowsMvException() {
        Path srcFileAPath = tempDir.resolve("A");
        Path srcFileBPath = tempDir.resolve("B");
        Path srcFileCPath = tempDir.resolve("C"); // do not exist
        String srcFileA = srcFileAPath.toString();
        String srcFileB = srcFileBPath.toString();
        String srcFileC = srcFileCPath.toString();
        assertDoesNotThrow(() -> Files.createFile(srcFileAPath));
        assertDoesNotThrow(() -> Files.createFile(srcFileBPath));

        MvException actual = assertThrowsExactly(MvException.class, () ->
                app.mvFilesToFolder(false, destDir, srcFileA, srcFileB, srcFileC)
        );
        String movedSrcFileA = destDirPath.resolve("A").toString();
        String movedSrcFileB = destDirPath.resolve("B").toString();
        String movedSrcFileC = destDirPath.resolve("C").toString(); // do not exist
        String expected = getFileNotFoundMsg(srcFileC);

        assertFileExists(movedSrcFileA);      // src file A exists in subdirectory
        assertFileExists(movedSrcFileB);      // src file B exists in subdirectory
        assertFileDoNotExists(movedSrcFileC); // src file C do not exist in subdirectory
        assertFileDoNotExists(srcFileA); // original src file A no longer exists
        assertFileDoNotExists(srcFileB); // original src file B no longer exists
        assertFileDoNotExists(srcFileC); // original src file C do not exist
        assertEquals(expected, actual.getMessage()); // 'C' not found is returned
    }

    @Test
    void mvFilesToFolder_SomeSrcFileDoNotExistsAtStart_MovesFileAndThrowsMvException() {
        Path srcFileAPath = tempDir.resolve("A"); // do not exist
        Path srcFileBPath = tempDir.resolve("B");
        Path srcFileCPath = tempDir.resolve("C");
        String srcFileA = srcFileAPath.toString();
        String srcFileB = srcFileBPath.toString();
        String srcFileC = srcFileCPath.toString();
        assertDoesNotThrow(() -> Files.createFile(srcFileBPath));
        assertDoesNotThrow(() -> Files.createFile(srcFileCPath));

        MvException actual = assertThrowsExactly(MvException.class, () ->
                app.mvFilesToFolder(false, destDir, srcFileA, srcFileB, srcFileC)
        );

        String movedSrcFileA = destDirPath.resolve("A").toString(); // do not exist
        String movedSrcFileB = destDirPath.resolve("B").toString();
        String movedSrcFileC = destDirPath.resolve("C").toString();
        String expected = getFileNotFoundMsg(srcFileA);

        assertFileDoNotExists(movedSrcFileA); // src file A do not exist in subdirectory
        assertFileExists(movedSrcFileB);      // src file B exists in subdirectory
        assertFileExists(movedSrcFileC);      // src file C exists in subdirectory
        assertFileDoNotExists(srcFileA); // original src file A do not exist
        assertFileDoNotExists(srcFileB); // original src file B no longer exists
        assertFileDoNotExists(srcFileC); // original src file C no longer exists
        assertEquals(expected, actual.getMessage()); // 'A' not found is returned
    }
}
