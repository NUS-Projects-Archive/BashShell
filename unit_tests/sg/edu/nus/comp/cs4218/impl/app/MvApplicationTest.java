package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.MvException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.exception.MvException.PROB_MV_DEST_FILE;
import static sg.edu.nus.comp.cs4218.exception.MvException.PROB_MV_FOLDER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;

class MvApplicationTest {

    private static final String MV_EX_MSG = "mv: ";
    private static final String TEMP_SRC_FILE = "srcFile.txt";
    private static final String TEMP_DEST_FILE = "destFile.txt";
    private static final String TEMP_DEST_DIR = "subdirectory";
    private MvApplication app;

    @TempDir
    private Path tempDir;
    private Path tempSrcFilePath;
    private Path tempDestFilePath;
    private Path tempDestDirPath;

    @BeforeEach
    void setUp() throws IOException {
        this.app = new MvApplication();

        // Create temporary file
        tempSrcFilePath = tempDir.resolve(TEMP_SRC_FILE); // automatically deletes after test execution
        tempDestFilePath = tempDir.resolve(TEMP_DEST_FILE);
        tempDestDirPath = tempDir.resolve(TEMP_DEST_DIR);
        Files.createFile(tempSrcFilePath);
        Files.createFile(tempDestFilePath);
        Files.createDirectories(tempDestDirPath);
    }

    @Test
    void mvSrcFileToDestFile_SrcFileDoNotExist_ThrowsMvException() {
        Path nonExistFilePath = tempDir.resolve("nonExistFile.txt");
        Throwable result = assertThrows(MvException.class, () -> {
            app.mvSrcFileToDestFile(false, nonExistFilePath.toString(), tempDestFilePath.toString());
        });
        assertEquals(MV_EX_MSG + PROB_MV_DEST_FILE + ERR_FILE_NOT_FOUND, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvSrcFileToDestFile_SrcFileNoPermissionToRead_ThrowsMvException() throws IOException {
        Files.setPosixFilePermissions(tempSrcFilePath,
                new HashSet<>(Collections.singleton(PosixFilePermission.OWNER_READ)));
        Throwable result = assertThrows(MvException.class, () -> {
            app.mvSrcFileToDestFile(false, tempSrcFilePath.toString(), tempDestFilePath.toString());
        });
        assertEquals(MV_EX_MSG + PROB_MV_DEST_FILE + ERR_NO_PERM, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvSrcFileToDestFile_DestFileNoPermissionToWrite_ThrowsMvException() throws IOException {
        Files.setPosixFilePermissions(tempDestFilePath,
                new HashSet<>(Collections.singleton(PosixFilePermission.OWNER_WRITE)));
        Throwable result = assertThrows(MvException.class, () -> {
            app.mvSrcFileToDestFile(false, tempSrcFilePath.toString(), tempDestFilePath.toString());
        });
        assertEquals(MV_EX_MSG + PROB_MV_DEST_FILE + ERR_NO_PERM, result.getMessage());
    }

    @Test
    void mvSrcFileToDestFile_DestFileDoNotExist_MovedFile() throws IOException, MvException {
        String content = "a b c";
        Files.write(tempSrcFilePath, content.getBytes());
        List<String> expectedContent = Files.readAllLines(tempSrcFilePath);

        Path nonExistDestFilePath = tempDir.resolve("nonExistFile.txt");
        app.mvSrcFileToDestFile(false, tempSrcFilePath.toString(), nonExistDestFilePath.toString());
        List<String> actualContent = Files.readAllLines(nonExistDestFilePath);

        File destFile = new File(nonExistDestFilePath.toString());
        File srcFile = new File(tempSrcFilePath.toString());

        assertTrue(destFile.exists()); // non existing dest file should exist
        assertFalse(srcFile.exists()); // existing src file should no longer exist
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void mvSrcFileToDestFile_IsOverwrite_MovedFile() throws IOException, MvException {
        String content = "a b c";
        Files.write(tempSrcFilePath, content.getBytes());
        List<String> expectedContent = Files.readAllLines(tempSrcFilePath);

        app.mvSrcFileToDestFile(true, tempSrcFilePath.toString(), tempDestFilePath.toString());
        List<String> actualContent = Files.readAllLines(tempDestFilePath);

        File destFile = new File(tempDestDirPath.toString());
        File srcFile = new File(tempSrcFilePath.toString());

        assertTrue(destFile.exists()); // existing dest file should still exist
        assertFalse(srcFile.exists()); // existing src file should no longer exist
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void mvFilesToFolder_DestFolderDoNotExist_ThrowsMvException() {
        Path nonExistFolderPath = tempDir.resolve("nonExistFolder");
        Throwable result = assertThrows(MvException.class, () -> {
            app.mvFilesToFolder(false, nonExistFolderPath.toString(), null);
        });
        assertEquals(MV_EX_MSG + PROB_MV_FOLDER + ERR_FILE_NOT_FOUND, result.getMessage());
    }

    @Test
    void mvFilesToFolder_DestIsNotADirectory_ThrowsMvException() {
        Throwable result = assertThrows(MvException.class, () -> {
            app.mvFilesToFolder(false, tempDestFilePath.toString(), null);
        });
        assertEquals(MV_EX_MSG + PROB_MV_FOLDER + ERR_IS_DIR, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvFilesToFolder_DestFolderNoPermissionToWrite_ThrowsMvException() throws IOException {
        Files.setPosixFilePermissions(tempDestDirPath,
                new HashSet<>(Collections.singleton(PosixFilePermission.OWNER_WRITE)));
        Throwable result = assertThrows(MvException.class, () -> {
            app.mvFilesToFolder(false, tempDestDirPath.toString(), null);
        });
        assertEquals(MV_EX_MSG + PROB_MV_FOLDER + ERR_NO_PERM, result.getMessage());
    }

    @Test
    void mvFilesToFolder_SrcFileDoNotExist_ThrowsMvException() {
        Path nonExistFilePath = tempDir.resolve("nonExistFile.txt");
        Throwable result = assertThrows(MvException.class, () -> {
            app.mvFilesToFolder(false, tempDestDirPath.toString(), nonExistFilePath.toString());
        });
        assertEquals(MV_EX_MSG + PROB_MV_FOLDER + ERR_FILE_NOT_FOUND, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvFilesToFolder_SrcFileNoPermissionToRead_ThrowsMvException() throws IOException {
        Files.setPosixFilePermissions(tempSrcFilePath,
                new HashSet<>(Collections.singleton(PosixFilePermission.OWNER_READ)));
        Throwable result = assertThrows(MvException.class, () -> {
            app.mvFilesToFolder(false, tempDir.toString(), tempSrcFilePath.toString());
        });
        assertEquals(MV_EX_MSG + PROB_MV_FOLDER + ERR_NO_PERM, result.getMessage());
    }

    @Test
    void mvFilesToFolder_SrcFileDoNotExistUnderDestFolder_MovedFile() throws MvException {
        app.mvFilesToFolder(false, tempDestDirPath.toString(), tempSrcFilePath.toString());

        File destFolder = new File(tempDestDirPath.toString());
        File srcFile = new File(tempSrcFilePath.toString());
        Path movedSrcFilePath = tempDestDirPath.resolve(TEMP_SRC_FILE);
        File movedSrcFile = new File(movedSrcFilePath.toString());

        assertTrue(destFolder.exists()); // existing dest folder should still exist
        assertFalse(srcFile.exists()); // existing src file should no longer exist under directory: tempDir
        assertTrue(movedSrcFile.exists()); // src file should be moved under the directory: tempDir/subdirectory
    }

    @Test
    void mvFilesToFolder_IsOverwrite_MovedFile() throws IOException, MvException {
        String srcContent = "a b c";
        Files.write(tempSrcFilePath, srcContent.getBytes());
        List<String> expectedContent = Files.readAllLines(tempSrcFilePath);

        String overwrittenContent = "1 2 3";
        Path tempSrcFileUnderSubDir = tempDestDirPath.resolve(TEMP_SRC_FILE);
        Files.createFile(tempSrcFileUnderSubDir);
        Files.write(tempSrcFileUnderSubDir, overwrittenContent.getBytes());

        assertTrue(tempSrcFileUnderSubDir.toFile().exists());

        app.mvFilesToFolder(true, tempDestDirPath.toString(), tempSrcFilePath.toString());
        List<String> actualContent = Files.readAllLines(tempSrcFileUnderSubDir);

        File destFile = new File(tempSrcFileUnderSubDir.toString());
        File srcFile = new File(tempSrcFilePath.toString());

        assertTrue(destFile.exists()); // existing dest file should still exist
        assertFalse(srcFile.exists()); // existing src file should no longer exist
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void mvFilesToFolder_MultipleSrcFiles_MovedFile() throws IOException, MvException {
        Path tempSrcFileA = tempDir.resolve("A");
        Path tempSrcFileB = tempDir.resolve("B");
        Files.createFile(tempSrcFileA);
        Files.createFile(tempSrcFileB);

        String args = tempSrcFileA.toString() + " " + tempSrcFileB.toString();
        String[] argArray = args.split("\\s+");
        app.mvFilesToFolder(false, tempDestDirPath.toString(), argArray);

        File destFolder = new File(tempDestDirPath.toString());
        Path movedSrcFilePathA = tempDestDirPath.resolve("A");
        Path movedSrcFilePathB = tempDestDirPath.resolve("B");
        File movedSrcFileA = new File(movedSrcFilePathA.toString());
        File movedSrcFileB = new File(movedSrcFilePathB.toString());

        assertTrue(destFolder.exists()); // existing dest folder should still exist
        assertTrue(movedSrcFileA.exists()); // existing src file should no longer exist under directory: tempDir
        assertTrue(movedSrcFileB.exists()); // src file should be moved under the directory: tempDir/subdirectory
    }
}