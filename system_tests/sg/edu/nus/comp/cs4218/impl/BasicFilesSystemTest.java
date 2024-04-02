package sg.edu.nus.comp.cs4218.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.io.CleanupMode.ALWAYS;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewDirectory;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFileInDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BasicFilesSystemTest extends AbstractSystemTest {
    private static final String FILE_CONTENT = "line1\nline2\nline3\nabc\nline4\nabc\nline5\n";
    private static final String DIR_NAME = "testDir";

    // String Literals
    private static final String LIN = "lin";
    private static final String ABC = "abc";

    private static Path file;
    private static Path dir;
    private static Path nestedFile;
    private static String fileName = "<set during runtime>";
    private static String nestedFileName = "<set during runtime>";

    @Override
    @BeforeEach
    void beforeEach(@TempDir(cleanup = ALWAYS) Path tempDir) {
        super.beforeEach(tempDir);

        /* Creates the following file structure:
         *
         * testFile.txt
         * testDir/
         *     nestedFile.txt
         */

        file = createNewFileInDir(tempDir, "testFile.txt", FILE_CONTENT);
        fileName = file.toFile().getName();
        dir = createNewDirectory(tempDir, DIR_NAME);
        nestedFile = createNewFileInDir(dir, "nestedFile.txt", FILE_CONTENT);
        nestedFileName = nestedFile.toFile().getName();
    }

    @Test
    void main_RmFile_RmSuccessfully() {
        SystemTestResults actual = testMainWith(
                LS_APP,
                EXIT_APP
        );
        assertTrue(actual.out.contains(fileName));

        actual = testMainWith(
                RM_APP + " " + fileName,
                LS_APP,
                EXIT_APP
        );
        assertFalse(actual.out.contains(fileName));
    }

    @Test
    void main_RmDirWithContents_RmSuccessfully() {
        SystemTestResults actual = testMainWith(
                LS_APP,
                EXIT_APP
        );
        assertTrue(actual.out.contains(DIR_NAME));

        actual = testMainWith(
                RM_APP + " " + DIR_NAME,
                LS_APP,
                EXIT_APP
        );
        assertTrue(actual.out.contains(DIR_NAME));

        actual = testMainWith(
                RM_APP + " -d " + DIR_NAME,
                LS_APP,
                EXIT_APP
        );
        assertTrue(actual.out.contains(DIR_NAME));

        actual = testMainWith(
                RM_APP + " -r " + DIR_NAME,
                LS_APP,
                EXIT_APP
        );
        assertFalse(actual.out.contains(DIR_NAME));
    }

    @Test
    void main_SortFile_PrintsSortedFileContents() {
        SystemTestResults actual = testMainWith(
                SORT_APP + " " + fileName,
                EXIT_APP
        );
        String expected = String.join("\n",
                actual.rootPath() + ABC,
                ABC,
                "line1",
                "line2",
                "line3",
                "line4",
                "line5"
        );
        assertEquals(expected, actual.out);
    }

    @Test
    void main_MvAllFilesInChildDirToParentDirConnectedUsingSemicolon_MvSuccessfully() {
        SystemTestResults actual = testMainWith(
                CD_APP + " " + DIR_NAME + "; " + MV_APP + " * ..",
                LS_APP + "; " + CD_APP + " ..; " + LS_APP,
                EXIT_APP
        );
        String expected = String.join("\n",
                actual.rootPath() + actual.rootPath(DIR_NAME),
                nestedFileName,
                DIR_NAME,
                fileName
        );
        assertEquals(expected, actual.out);
    }

    @Test
    void main_PasteFileAndPipeToCut_PrintsCutContents() {
        SystemTestResults actual = testMainWith(
                PASTE_APP + " " + fileName + " | " + CUT_APP + " -c 1-3",
                EXIT_APP
        );
        String expected = String.join("\n",
                actual.rootPath() + LIN,
                LIN,
                LIN,
                ABC,
                LIN,
                ABC,
                LIN
        );
        assertEquals(expected, actual.out);
    }

    @Test
    void main_GrepFileAndPipeToWcWithFlags_PrintsCorrectCount() {
        SystemTestResults actual = testMainWith(
                GREP_APP + " abc - " + fileName + " | " + WC_APP + " -l -w",
                "abcde",
                EXIT_APP
        );

        String expected = String.format("%s %7d %7d", actual.rootPath(), 3, 7);
        assertEquals(expected, actual.out);
    }

    @Test
    void main_GrepFileAndRedirectToAnotherFileAndCatTheFile_PrintsCorrectGrepStringsStoredInFile() {
        String outputFileName = "result.txt";
        SystemTestResults actual = testMainWith(
                GREP_APP + " abc < " + fileName + " > " + outputFileName + "; " + CAT_APP + " " + outputFileName,
                EXIT_APP
        );
        String expected = actual.rootPath() + "abc\nabc";
        assertEquals(expected, actual.out);
    }

    @Test
    void main_UseEchoToTeeToExistingFile_FileHasExistingAndNewContent() {
        SystemTestResults actual = testMainWith(
                String.format("%s \"%s\" | %s -a %s", ECHO_APP, "line6", TEE_APP, fileName),
                EXIT_APP
        );

        String expected = actual.rootPath() + "line6";
        assertEquals(expected, actual.out);

        List<String> expectedContent = List.of(
                "line1", "line2", "line3", "abc", "line4", "abc", "line5", "line6"
        );
        try {
            List<String> actualContent = Files.readAllLines(file);
            assertEquals(expectedContent, actualContent);
        } catch (IOException e) {
            fail(e);
        }
    }
}
