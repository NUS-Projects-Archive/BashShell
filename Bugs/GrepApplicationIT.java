import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.removeTrailing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.impl.app.GrepApplication;

@SuppressWarnings({"PMD.ClassNamingConventions", "PMD.NoPackage"})
class GrepApplicationIT {

    private static final String DASH = "-";
    private static final String TEST_INPUT_FILE = "c.md";
    private static final String TEST_OUTPUT_FILE = "expected_c.md";
    private static final String TEST_RESOURCES = "resources/grep";

    @TempDir
    private Path tempDir;

    private GrepApplication app;
    private InputStream stdin;
    private OutputStream stdout;

    @BeforeEach
    void setUp(@TempDir(cleanup = CleanupMode.ALWAYS) Path setUpTempDir) throws IOException {
        tempDir = setUpTempDir;
        final String resourceDirectory = removeTrailing(TEST_RESOURCES, "/");
        try (Stream<Path> stream = Files.walk(Paths.get(resourceDirectory))) {
            stream.forEach(source -> {
                Path destination = Paths.get(tempDir.toString(),
                        source.toString().substring(resourceDirectory.length()));

                try {
                    Files.copy(source, destination, REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException("Unable to copy test resources to temp directory.", e);
                }
            });
        }

        // Set CWD to be the test directory
        Environment.currentDirectory = tempDir.toString();

        app = new GrepApplication();
        stdin = mock(InputStream.class);
        stdout = new ByteArrayOutputStream();
    }

    // Disabled as this test case is written for a Hackathon valid filed bug (Issue #220) that is not fixed.
    @Test
    @Disabled
    void run_GetInputFromValidFileNameWithValidDashPattern_ReturnsMatchingLinesFromFile() {
        // Given
        String[] args = new String[]{DASH, TEST_INPUT_FILE};

        // When
        assertDoesNotThrow(() -> app.run(args, stdin, stdout));

        // Then
        Path expectedFilePath = Paths.get(tempDir.toString(), TEST_OUTPUT_FILE);
        String expectedContent = assertDoesNotThrow(() -> Files.readString(expectedFilePath));
        assertEquals(expectedContent, stdout.toString());
    }
}
