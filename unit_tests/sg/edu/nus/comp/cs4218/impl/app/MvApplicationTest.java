package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
    private MvApplication app;

    @TempDir
    private Path tempDir;
    private Path srcFilePath;
    private Path destFilePath;
    private Path destDirPath;
    private String srcFile;
    private String destFile;
    private String destDir;

    private String getFileNotFoundMsg(String file) {
        return String.format("mv: cannot find '%s': No such file or directory", file);
    }

    private String getCannotReadPermissionMsg(String file) {
        return String.format("mv: cannot read '%s': Permission denied", file);
    }

    private String getCannotWritePermissionMsg(String file) {
        return String.format("mv: cannot write '%s': Permission denied", file);
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
        if (!isSetReadable) {
            fail("Failed to set read permission to false for test source file");
        }

        MvException result = assertThrowsExactly(MvException.class, () ->
                app.mvSrcFileToDestFile(false, srcFile, destFile)
        );
        String expected = getCannotReadPermissionMsg(srcFile);
        assertEquals(expected, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvSrcFileToDestFile_DestFileNoPermissionToWrite_ThrowsMvException() {
        boolean isSetWritable = destFilePath.toFile().setWritable(false);
        if (!isSetWritable) {
            fail("Failed to set write permission to false for test destination file");
        }

        MvException result = assertThrowsExactly(MvException.class, () ->
                app.mvSrcFileToDestFile(false, srcFile, destFile)
        );
        String expected = getCannotWritePermissionMsg(destFile);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void mvSrcFileToDestFile_DestFileDoNotExist_MovesFile() {
        List<String> expectedContent = assertDoesNotThrow(() -> Files.readAllLines(srcFilePath));
        Path nonExistFilePath = tempDir.resolve("nonExistFile.txt");

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(false, srcFile, nonExistFilePath.toString()));
        List<String> actualContent = assertDoesNotThrow(() -> Files.readAllLines(nonExistFilePath));

        assertTrue(Files.exists(nonExistFilePath)); // assert that the dest file now exists
        assertFalse(Files.exists(srcFilePath)); // assert that the src file no longer exist
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void mvSrcFileToDestFile_IsOverwrite_MovesFile() {
        List<String> expectedContent = assertDoesNotThrow(() -> Files.readAllLines(srcFilePath));
        List<String> oldContent = assertDoesNotThrow(() -> Files.readAllLines(destFilePath));

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(true, srcFile, destFile));
        List<String> actualContent = assertDoesNotThrow(() -> Files.readAllLines(destFilePath));

        assertFalse(Files.exists(srcFilePath)); // assert that the src file no longer exist
        assertEquals(expectedContent, actualContent); // assert that dest file now holds the content of the src file
        assertNotEquals(oldContent, actualContent); // assert that dest file content has been overwritten
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
        String expected = String.format("mv: cannot move '%s': This is a directory", destFile);
        assertEquals(expected, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvFilesToFolder_DestFolderNoPermissionToWrite_ThrowsMvException() {
        boolean isSetWritable = destDirPath.toFile().setWritable(false);
        if (!isSetWritable) {
            fail("Failed to set write permission to false for test destination directory");
        }

        MvException result = assertThrowsExactly(MvException.class, () ->
                app.mvFilesToFolder(false, destDir, new String[0])
        );
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
        if (!isSetReadable) {
            fail("Failed to set read permission to false for test source file");
        }

        MvException result = assertThrowsExactly(MvException.class, () ->
                app.mvFilesToFolder(false, destDir, srcFile)
        );
        String expected = getCannotReadPermissionMsg(srcFile);
        assertEquals(expected, result.getMessage());
    }

    @Test
    void mvFilesToFolder_SrcFileDoNotExistInDestFolder_MovesFile() {
        String result = assertDoesNotThrow(() -> app.mvFilesToFolder(false, destDir, srcFile));
        Path movedSrcFilePath = destDirPath.resolve(SRC_FILE);

        assertNull(result);
        assertTrue(Files.exists(movedSrcFilePath)); // assert that src file now exist under subdirectory
        assertFalse(Files.exists(srcFilePath)); // assert that the original src file no longer exist
    }

    @Test
    void mvFilesToFolder_IsOverwrite_MovesFile() {
        List<String> expectedContent = assertDoesNotThrow(() -> Files.readAllLines(srcFilePath));

        String destContent = "destination file content";
        Path destFilePath = destDirPath.resolve(SRC_FILE);
        assertDoesNotThrow(() -> Files.createFile(destFilePath));
        assertDoesNotThrow(() -> Files.write(destFilePath, destContent.getBytes()));

        // assert that another src file of different content exist in subdirectory
        List<String> oldContent = assertDoesNotThrow(() -> Files.readAllLines(destFilePath));
        assertTrue(Files.exists(destFilePath));
        assertNotEquals(expectedContent, oldContent);

        assertDoesNotThrow(() -> app.mvFilesToFolder(true, destDir, srcFile));
        List<String> actualContent = assertDoesNotThrow(() -> Files.readAllLines(destFilePath));

        assertTrue(Files.exists(destFilePath)); // assert that the src file in subdirectory still exist
        assertFalse(Files.exists(srcFilePath)); // assert that the original src file no longer exist
        assertEquals(expectedContent, actualContent); // assert that dest file now holds the content of the src file
        assertNotEquals(oldContent, actualContent); // assert that dest file content has been overwritten
    }

    @Test
    void mvFilesToFolder_MultipleSrcFilesAllExists_MovesFile() {
        Path srcFileAPath = tempDir.resolve("A");
        Path srcFileBPath = tempDir.resolve("B");
        assertDoesNotThrow(() -> Files.createFile(srcFileAPath));
        assertDoesNotThrow(() -> Files.createFile(srcFileBPath));

        assertDoesNotThrow(() -> app.mvFilesToFolder(false, destDirPath.toString(),
                srcFileAPath.toString(), srcFileBPath.toString()));

        Path movedSrcFileAPath = destDirPath.resolve("A");
        Path movedSrcFileBPath = destDirPath.resolve("B");

        assertTrue(Files.exists(movedSrcFileAPath)); // assert that the src file A exists in subdirectory
        assertTrue(Files.exists(movedSrcFileBPath)); // assert that the src file B exists in subdirectory
        assertFalse(Files.exists(srcFileAPath)); // assert that the original src file A no longer exists
        assertFalse(Files.exists(srcFileBPath)); // assert that the original src file B no longer exists
    }

    @Test
    void mvFilesToFolder_SomeSrcFileDoNotExistsAtEnd_MovesFileAndThrowsMvException() {
        Path srcFileAPath = tempDir.resolve("A");
        Path srcFileBPath = tempDir.resolve("B");
        Path srcFileCPath = tempDir.resolve("C"); // do not exist
        assertDoesNotThrow(() -> Files.createFile(srcFileAPath));
        assertDoesNotThrow(() -> Files.createFile(srcFileBPath));

        MvException actual = assertThrowsExactly(MvException.class, () ->
                app.mvFilesToFolder(false, destDirPath.toString(), srcFileAPath.toString(),
                        srcFileBPath.toString(), srcFileCPath.toString())
        );

        String expected = getFileNotFoundMsg(srcFileCPath.toString());
        Path movedSrcFileAPath = destDirPath.resolve("A");
        Path movedSrcFileBPath = destDirPath.resolve("B");

        assertTrue(Files.exists(movedSrcFileAPath)); // assert that the src file A exists in subdirectory
        assertTrue(Files.exists(movedSrcFileBPath)); // assert that the src file B exists in subdirectory
        assertFalse(Files.exists(srcFileAPath)); // assert that the original src file A no longer exists
        assertFalse(Files.exists(srcFileBPath)); // assert that the original src file B no longer exists
        assertEquals(expected, actual.getMessage()); // assert that 'C' not found is returned
    }

    @Test
    void mvFilesToFolder_SomeSrcFileDoNotExistsAtStart_MovesFileAndThrowsMvException() {
        Path srcFileAPath = tempDir.resolve("A"); // do not exist
        Path srcFileBPath = tempDir.resolve("B");
        Path srcFileCPath = tempDir.resolve("C");
        assertDoesNotThrow(() -> Files.createFile(srcFileBPath));
        assertDoesNotThrow(() -> Files.createFile(srcFileCPath));

        MvException actual = assertThrowsExactly(MvException.class, () ->
                app.mvFilesToFolder(false, destDirPath.toString(), srcFileAPath.toString(),
                        srcFileBPath.toString(), srcFileCPath.toString())
        );

        String expected = getFileNotFoundMsg(srcFileAPath.toString());
        Path movedSrcFileBPath = destDirPath.resolve("B");
        Path movedSrcFileCPath = destDirPath.resolve("C");

        assertTrue(Files.exists(movedSrcFileBPath)); // assert that the src file B exists in subdirectory
        assertTrue(Files.exists(movedSrcFileCPath)); // assert that the src file C exists in subdirectory
        assertFalse(Files.exists(srcFileBPath)); // assert that the original src file B no longer exists
        assertFalse(Files.exists(srcFileCPath)); // assert that the original src file C no longer exists
        assertEquals(expected, actual.getMessage()); // assert that 'A' not found is returned
    }
}
