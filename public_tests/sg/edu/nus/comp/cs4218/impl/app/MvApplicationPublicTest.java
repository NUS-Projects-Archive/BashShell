package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MvApplicationPublicTest {
    private MvApplication application;
    private static final String TEMP = "temp-mv" + File.separator;
    private static final String TEXT_A = "textA.txt";
    private static final String TEXT_A_PATH = TEMP + TEXT_A;
    private static final String TEXT_B = "textB.txt";
    private static final String TEXT_B_PATH = TEMP + TEXT_B;
    private static final String FOLDER = "folder" + File.separator;
    private static final String FOLDER_PATH = TEMP + FOLDER;
    private static final String MOVED_TEXT_TXT = "movedText.txt";
    private static final String MOVED_TXT_PATH = TEMP + MOVED_TEXT_TXT;
    private static final String TARGET_FOLDER = "targetFolder" + File.separator;
    private static final String TARGET_FDR_PATH = TEMP + TARGET_FOLDER;

    void createAndWriteFile(String filePath) throws Exception {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Content inside " + filePath);
        }
    }

    void createFolder(String folderPath) throws Exception {
        Path path = Paths.get(folderPath);
        Files.createDirectories(path);
    }

    void createMvResourcesAndFolders() throws Exception {
        createFolder(TEMP);
        createFolder(TARGET_FDR_PATH);
        createFolder(FOLDER_PATH);
    }

    @BeforeEach
    void setup() throws Exception {
        application = new MvApplication();
        createMvResourcesAndFolders();
        createAndWriteFile(TEXT_A_PATH);
        createAndWriteFile(TEXT_B_PATH);
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.walk(Paths.get(TEMP))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    void moveSrcFileToDestFile_ValidFile_FileRenamed() throws Exception {
        File source = new File(TEXT_A_PATH);
        List<String> sourceContent = Files.readAllLines(source.toPath());

        application.mvSrcFileToDestFile(true, TEXT_A_PATH, MOVED_TXT_PATH);
        File target = new File(MOVED_TXT_PATH);
        List<String> targetContent = Files.readAllLines(target.toPath());

        assertTrue(target.exists());
        assertFalse(source.exists());
        assertEquals(sourceContent, targetContent);
    }

    @Test
    void moveSrcFileToDestFile_ValidFolder_FolderRenamed() throws Exception {
        File source = new File(FOLDER_PATH);

        application.mvSrcFileToDestFile(true, FOLDER_PATH, MOVED_TXT_PATH);
        File target = new File(MOVED_TXT_PATH);

        assertTrue(target.exists());
        assertTrue(target.isDirectory());
        assertFalse(source.exists());
    }

    @Test
    void mvFilesToFolder_SourceFileNotExisting_FileMoved() throws Exception {
        File source = new File(TEXT_A_PATH);
        List<String> sourceContent = Files.readAllLines(source.toPath());

        application.mvFilesToFolder(true, TARGET_FDR_PATH, TEXT_A_PATH);
        File target = new File(TARGET_FDR_PATH + TEXT_A);
        List<String> targetContent = Files.readAllLines(target.toPath());

        assertTrue(target.exists());
        assertFalse(source.exists());
        assertEquals(sourceContent, targetContent);
    }

    @Test
    void mvFilesToFolder_SourceFolderNotExisting_FolderMoved() throws Exception {
        File source = new File(FOLDER_PATH);

        application.mvFilesToFolder(true, TARGET_FDR_PATH, FOLDER_PATH);
        File target = new File(TARGET_FDR_PATH + FOLDER);

        assertTrue(target.exists());
        assertTrue(target.isDirectory());
        assertFalse(source.exists());
    }

    @Test
    void mvFilesToFolder_MultipleSourceFile_FilesMoved() throws Exception {
        File sourceA = new File(TEXT_A_PATH);
        List<String> sourceAContent = Files.readAllLines(sourceA.toPath());
        File sourceB = new File(TEXT_B_PATH);
        List<String> sourceBContent = Files.readAllLines(sourceB.toPath());

        application.mvFilesToFolder(true, TARGET_FDR_PATH, TEXT_A_PATH, TEXT_B_PATH);
        File targetA = new File(TARGET_FDR_PATH + TEXT_A);
        List<String> targetAContent = Files.readAllLines(targetA.toPath());
        File targetB = new File(TARGET_FDR_PATH + TEXT_B);
        List<String> targetBContent = Files.readAllLines(targetB.toPath());

        assertTrue(targetA.exists());
        assertTrue(targetB.exists());
        assertFalse(sourceA.exists());
        assertFalse(sourceB.exists());
        assertEquals(sourceAContent, targetAContent);
        assertEquals(sourceBContent, targetBContent);
    }

}
