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
import java.nio.file.attribute.PosixFilePermission;
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
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_MISSING_ARG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;
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
        Throwable result = assertThrows(MvException.class, () -> {
            app.run(null, null, null);
        });
        assertEquals(MV_EX_MSG + ERR_MISSING_ARG, result.getMessage());
    }

    @Test
    void run_EmptyArgsArray_ThrowsMvException() {
        Throwable result = assertThrows(MvException.class, () -> {
            String[] args = {};
            app.run(args, null, null);
        });
        assertEquals(MV_EX_MSG + ERR_MISSING_ARG, result.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "1", "$", "."})
    void run_InsufficientArgs_ThrowsMvException(String args) {
        Throwable result = assertThrows(MvException.class, () -> {
            app.run(args.split("\\s+"), null, null);
        });
        assertEquals(MV_EX_MSG + ERR_NO_ARGS, result.getMessage());
    }

    @Test
    void mvSrcFileToDestFile_SrcFileDoNotExist_ThrowsMvException() {
        Path nonExistFilePath = tempDir.resolve("nonExistFile.txt");
        String nonExistFile = nonExistFilePath.toString();
        Throwable result = assertThrows(MvException.class, () -> {
            app.mvSrcFileToDestFile(false, nonExistFile, tempDestFile);
        });
        assertEquals(MV_EX_MSG + PROB_MV_DEST_FILE + ERR_FILE_NOT_FOUND, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvSrcFileToDestFile_SrcFileNoPermissionToRead_ThrowsMvException() throws IOException {
        Files.setPosixFilePermissions(tempSrcFilePath,
                new HashSet<>(Collections.singleton(PosixFilePermission.OWNER_READ)));
        Throwable result = assertThrows(MvException.class, () -> {
            app.mvSrcFileToDestFile(false, tempSrcFile, tempDestFile);
        });
        assertEquals(MV_EX_MSG + PROB_MV_DEST_FILE + ERR_NO_PERM, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvSrcFileToDestFile_DestFileNoPermissionToWrite_ThrowsMvException() throws IOException {
        Files.setPosixFilePermissions(tempDestFilePath,
                new HashSet<>(Collections.singleton(PosixFilePermission.OWNER_WRITE)));
        Throwable result = assertThrows(MvException.class, () -> {
            app.mvSrcFileToDestFile(false, tempSrcFile, tempDestFile);
        });
        assertEquals(MV_EX_MSG + PROB_MV_DEST_FILE + ERR_NO_PERM, result.getMessage());
    }

    @Test
    void mvSrcFileToDestFile_DestFileDoNotExist_MovedFile() throws IOException, MvException {
        String content = "a b c";
        Files.write(tempSrcFilePath, content.getBytes());
        List<String> expectedContent = Files.readAllLines(tempSrcFilePath);

        Path nonExistDestFilePath = tempDir.resolve("nonExistFile.txt");
        String nonExistDestFile = nonExistDestFilePath.toString();

        app.mvSrcFileToDestFile(false, tempSrcFile, nonExistDestFile);
        List<String> actualContent = Files.readAllLines(nonExistDestFilePath);

        File destFile = nonExistDestFilePath.toFile();
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
        Throwable result = assertThrows(MvException.class, () -> {
            app.mvFilesToFolder(false, nonExistFolder, null);
        });
        assertEquals(MV_EX_MSG + PROB_MV_FOLDER + ERR_FILE_NOT_FOUND, result.getMessage());
    }

    @Test
    void mvFilesToFolder_DestIsNotADirectory_ThrowsMvException() {
        Throwable result = assertThrows(MvException.class, () -> {
            app.mvFilesToFolder(false, tempDestFile, null);
        });
        assertEquals(MV_EX_MSG + PROB_MV_FOLDER + ERR_IS_DIR, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvFilesToFolder_DestFolderNoPermissionToWrite_ThrowsMvException() throws IOException {
        Files.setPosixFilePermissions(tempDestDirPath,
                new HashSet<>(Collections.singleton(PosixFilePermission.OWNER_WRITE)));
        Throwable result = assertThrows(MvException.class, () -> {
            app.mvFilesToFolder(false, tempDestDir, null);
        });
        assertEquals(MV_EX_MSG + PROB_MV_FOLDER + ERR_NO_PERM, result.getMessage());
    }

    @Test
    void mvFilesToFolder_SrcFileDoNotExist_ThrowsMvException() {
        String nonExistFile = tempDir.resolve("nonExistFile.txt").toString();
        Throwable result = assertThrows(MvException.class, () -> {
            app.mvFilesToFolder(false, tempDestDir, nonExistFile);
        });
        assertEquals(MV_EX_MSG + PROB_MV_FOLDER + ERR_FILE_NOT_FOUND, result.getMessage());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void mvFilesToFolder_SrcFileNoPermissionToRead_ThrowsMvException() throws IOException {
        Files.setPosixFilePermissions(tempSrcFilePath,
                new HashSet<>(Collections.singleton(PosixFilePermission.OWNER_READ)));
        Throwable result = assertThrows(MvException.class, () -> {
            app.mvFilesToFolder(false, tempDestDir, tempSrcFile);
        });
        assertEquals(MV_EX_MSG + PROB_MV_FOLDER + ERR_NO_PERM, result.getMessage());
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
        Path srcFileInSubDirPath = tempDestDirPath.resolve(TEMP_SRC_FILE);
        Files.createFile(srcFileInSubDirPath);
        Files.write(srcFileInSubDirPath, overwriteContent.getBytes());

        // assert that another src file of different content exist in subdirectory
        assertTrue(srcFileInSubDirPath.toFile().exists());

        app.mvFilesToFolder(true, tempDestDir, tempSrcFile);
        List<String> actualContent = Files.readAllLines(srcFileInSubDirPath);

        File destFile = srcFileInSubDirPath.toFile();
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