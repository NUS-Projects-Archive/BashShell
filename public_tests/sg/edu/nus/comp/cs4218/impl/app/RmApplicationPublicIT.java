package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.testutils.AssertUtils.assertFileDoNotExists;
import static sg.edu.nus.comp.cs4218.testutils.AssertUtils.assertFileExists;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

@SuppressWarnings("PMD.ClassNamingConventions")
public class RmApplicationPublicIT {
    private static final String TEMP = "temp-rm";
    private static final Deque<Path> FILES = new ArrayDeque<>();
    private static Path tempPath;

    private RmApplication rmApplication;

    @BeforeAll
    static void createTemp() throws IOException, NoSuchFieldException, IllegalAccessException {
        tempPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP);
        Files.createDirectories(tempPath);
    }

    @AfterAll
    static void deleteTemp() throws IOException {
        for (Path file : FILES) {
            Files.deleteIfExists(file);
        }
        Files.delete(tempPath);
    }

    @BeforeEach
    void setUp() {
        rmApplication = new RmApplication();
    }

    private Path createFile(String name) throws IOException {
        return createFile(name, tempPath);
    }

    private Path createDirectory(String folder) throws IOException {
        return createDirectory(folder, tempPath);
    }

    private Path createFile(String name, Path inPath) throws IOException {
        Path path = inPath.resolve(name);
        Files.createFile(path);
        FILES.push(path);
        return path;
    }

    private Path createDirectory(String folder, Path inPath) throws IOException {
        Path path = inPath.resolve(folder);
        Files.createDirectory(path);
        FILES.push(path);
        return path;
    }

    private String[] toArgs(String flag, String... files) {
        List<String> args = new ArrayList<>();
        if (!flag.isEmpty()) {
            args.add("-" + flag);
        }
        for (String file : files) {
            args.add(Paths.get(TEMP, file).toString());
        }
        return args.toArray(new String[0]);
    }

    @Test
    void run_SingleFile_DeletesFile() throws Exception {
        Path fileA = createFile("a.txt");
        Path fileB = createFile("bobby");
        rmApplication.run(toArgs("", "a.txt"), System.in, System.out);
        assertFileDoNotExists(fileA);
        assertFileExists(fileB);
    }

    @Test
    void run_SpaceInName_DeletesFile() throws Exception {
        Path fileC = createFile("c   c");
        rmApplication.run(toArgs("", "c   c"), System.in, System.out);
        assertFileDoNotExists(fileC);
    }

    @Test
    void run_MultipleFiles_DeletesFiles() throws Exception {
        Path fileD = createFile("d.txt");
        Path fileE = createFile("eerie");
        rmApplication.run(toArgs("", "d.txt", "eerie"), System.in, System.out);
        assertFileDoNotExists(fileD);
        assertFileDoNotExists(fileE);
    }

    @Test
    void run_EmptyDirectory_DeletesDirectory() throws Exception {
        Path folder = createDirectory("folder");
        rmApplication.run(toArgs("d", "folder"), System.in, System.out);
        assertFileDoNotExists(folder);
    }

    @Test
    void run_MultipleFilesEmptyDirectories_DeletesAll() throws Exception {
        Path fileG = createFile("g.txt");
        Path fileH = createFile("high");
        Path directoryA = createDirectory("directoryA");
        Path directoryB = createDirectory("directoryB");
        rmApplication.run(toArgs("d", "g.txt", "high", "directoryA", "directoryB"), System.in, System.out);
        assertFileDoNotExists(fileG);
        assertFileDoNotExists(fileH);
        assertFileDoNotExists(directoryA);
        assertFileDoNotExists(directoryB);
    }

    @Test
    void run_DirectoryWithFiles_DeletesDirectory() throws Exception {
        Path directory = createDirectory("directory");
        createFile("dwf.txt", directory);
        createFile("dwf2.txt", directory);
        rmApplication.run(toArgs("r", "directory"), System.in, System.out);
        assertFileDoNotExists(directory);
    }

    @Test
    void run_DirectoryInDirectory_DeletesDirectory() throws Exception {
        Path directoryC = createDirectory("directoryC");
        createFile("did.txt", directoryC);
        Path directory = createDirectory("directoryDid", directoryC);
        Path inner = createDirectory("directoryDid", directory);
        createFile("did.txt", inner);
        createFile("did2.txt", inner);
        rmApplication.run(toArgs("r", "directoryC"), System.in, System.out);
        assertFileDoNotExists(directoryC);
    }

    @Test
    void run_MultipleFilesDirectories_DeletesAll() throws Exception {
        Path directoryD = createDirectory("directoryD");
        createFile("mfd.txt", directoryD);
        Path directory = createDirectory("directoryMfd", directoryD);
        Path inner = createDirectory("directoryMfd", directory);
        createFile("mfd.txt", inner);
        createFile("mfd2.txt", inner);
        Path empty = createDirectory("empty");
        Path fileI = createFile("ii");
        Path fileJ = createFile("jar");
        rmApplication.run(toArgs("r", "directoryD", "empty", "ii", "jar"), System.in, System.out);
        assertFileDoNotExists(directoryD);
        assertFileDoNotExists(empty);
        assertFileDoNotExists(fileI);
        assertFileDoNotExists(fileJ);
    }

    @Test
    void run_AbsolutePath_DeletesDirectory() throws Exception {
        Path directory = createDirectory("directoryAbs");
        createDirectory("innerAbs", directory);
        rmApplication.run(new String[]{"-r", tempPath.resolve("directoryAbs").toString()}, System.in, System.out);
        assertFileDoNotExists(directory);
    }

    @Test
    void run_ZeroArguments_Throws() {
        assertThrows(RmException.class, () -> rmApplication.run(toArgs(""), System.in, System.out));
    }

    @Test
    void run_FlagOnly_Throws() {
        assertThrows(RmException.class, () -> rmApplication.run(toArgs("d"), System.in, System.out));
    }

    @Test
    void run_UnknownFlag_Throws() throws IOException {
        Path fileK = createFile("kick");
        assertThrows(RmException.class, () -> rmApplication.run(toArgs("x", "kick"), System.in, System.out));
        assertFileExists(fileK);
    }

    @Test
    void run_NonexistentFile_Throws() {
        assertThrows(RmException.class, () -> rmApplication.run(toArgs("", "not exist"), System.in, System.out));
    }

    @Test
    void run_DirectoryWithoutFlag_Throws() throws IOException {
        createDirectory("directoryF");
        assertThrows(RmException.class, () -> rmApplication.run(toArgs("", "directoryF"), System.in, System.out));
    }

    @Test
    void run_NonemptyDirectoryWithDFlag_Throws() throws IOException {
        Path directory = createDirectory("directoryG");
        createFile("a.txt", directory);
        assertThrows(RmException.class, () -> rmApplication.run(toArgs("d", "directoryG"), System.in, System.out));
    }
}
