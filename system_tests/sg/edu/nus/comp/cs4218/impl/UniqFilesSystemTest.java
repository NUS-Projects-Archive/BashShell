package sg.edu.nus.comp.cs4218.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.io.CleanupMode.ALWAYS;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFileInDir;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class UniqFilesSystemTest extends AbstractSystemTest {
    private static final String LOG_FILE_CONTENT = "[2023-12-23T12:13:13][I] Starting..."
            + "\n[2023-12-23T12:13:13][I] Attempt load Component A..."
            + "\n[2023-12-23T12:13:13][I] Component A loaded"
            + "\n[2023-12-23T12:13:13][I] Attempt load Component B..."
            + "\n[2023-12-23T12:13:13][W] Component B is outdated"
            + "\n[2023-12-23T12:13:13][I] Component B loaded"
            + "\n[2023-12-23T12:13:13][I] Attempt load Component C..."
            + "\n[2023-12-23T12:13:13][W] Missing file for Component C, creating new file..."
            + "\n[2023-12-23T12:13:14][W] Missing file for Component C, creating new file..."
            + "\n[2023-12-23T12:13:14][W] Missing file for Component C, creating new file..."
            + "\n[2023-12-23T12:13:14][I] Component C loaded"
            + "\n[2023-12-23T12:13:14][I] Attempt load Component D..."
            + "\n[2023-12-23T12:13:14][W] File corrupted, attempt recovery..."
            + "\n[2023-12-23T12:13:15][W] File corrupted, attempt recovery..."
            + "\n[2023-12-23T12:13:16][W] File corrupted, attempt recovery..."
            + "\n[2023-12-23T12:13:17][W] Start-up is taking too long"
            + "\n[2023-12-23T12:13:17][W] File corrupted, attempt recovery..."
            + "\n[2023-12-23T12:13:17][W] File corrupted, attempt recovery..."
            + "\n[2023-12-23T12:13:18][E] Failed to load Component D"
            + "\n[2023-12-23T12:13:18][W] Component D is disabled"
            + "\n[2023-12-23T12:13:18][I] System is ready";


    private String logFileName;

    @Override
    @BeforeEach
    void beforeEach(@TempDir(cleanup = ALWAYS) Path tempDir) {
        super.beforeEach(tempDir);

        Path logFile = createNewFileInDir(tempDir, "app.log", LOG_FILE_CONTENT);
        logFileName = logFile.toFile().getName();
    }

    @Test
    void main_FindWarningsIgnoreConsecutive_PrintWarningsCorrectly() {
        SystemTestResults actual = testMainWith(
                String.format("%s \"\\[.*\\]\\[W\\]\" %s | %s -c 26-1000 | %s",
                        GREP_APP, logFileName, CUT_APP, UNIQ_APP),
                EXIT_APP
        );
        String expected = String.join("\n",
                actual.rootPath() + "Component B is outdated",
                "Missing file for Component C, creating new file...",
                "File corrupted, attempt recovery...",
                "Start-up is taking too long",
                "File corrupted, attempt recovery...",
                "Component D is disabled"
        );
        assertEquals(expected, actual.out);
    }

    @Test
    void main_CountWarningsOccurrence_PrefixWarningsWithCount() {
        SystemTestResults actual = testMainWith(
                String.format("%s \"\\[.*\\]\\[W\\]\" %s | %s -c 26-1000 | %s -c",
                        GREP_APP, logFileName, CUT_APP, UNIQ_APP),
                EXIT_APP
        );
        String expected = String.join("\n",
                actual.rootPath() + "1 Component B is outdated",
                "3 Missing file for Component C, creating new file...",
                "3 File corrupted, attempt recovery...",
                "1 Start-up is taking too long",
                "2 File corrupted, attempt recovery...",
                "1 Component D is disabled"
        );
        assertEquals(expected, actual.out);
    }

}
