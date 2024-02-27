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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.exception.MvException.PROB_MV_DEST_FILE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;

class MvApplicationTest {

    private static final String MV_EX_MSG = "mv: ";
    private static final String TEMP_SRC_FILE = "srcFile.txt";
    private static final String TEMP_DEST_FILE = "destFile.txt";
    private MvApplication app;

    @TempDir
    private Path tempDir;
    private Path tempSrcFilePath;
    private Path tempDestFilePath;

    @BeforeEach
    void setUp() throws IOException {
        this.app = new MvApplication();

        // Create temporary file
        tempSrcFilePath = tempDir.resolve(TEMP_SRC_FILE); // automatically deletes after test execution
        tempDestFilePath = tempDir.resolve(TEMP_DEST_FILE);
        Files.createFile(tempSrcFilePath);
        Files.createFile(tempDestFilePath);
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
    void mvSrcFileToDestFile_DestFileDoNotExist_FileMovedSuccessfully() throws IOException, MvException {
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
    void mvSrcFileToDestFile_IsOverwrite_FileMovedSuccessfully() throws IOException, MvException {
        String content = "a b c";
        Files.write(tempSrcFilePath, content.getBytes());
        List<String> expectedContent = Files.readAllLines(tempSrcFilePath);

        app.mvSrcFileToDestFile(true, tempSrcFilePath.toString(), tempDestFilePath.toString());
        List<String> actualContent = Files.readAllLines(tempDestFilePath);

        File destFile = new File(tempDestFilePath.toString());
        File srcFile = new File(tempSrcFilePath.toString());

        assertTrue(destFile.exists()); // existing dest file should still exist
        assertFalse(srcFile.exists()); // existing src file should no longer exist
        assertEquals(expectedContent, actualContent);
    }
}