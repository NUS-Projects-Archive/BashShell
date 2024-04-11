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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.impl.app.SortApplication;

@SuppressWarnings({"PMD.ClassNamingConventions", "PMD.NoPackage"})
public class SortApplicationIT {

    private static final String TEST_RESOURCES = "resources/sort/";
    private static final String TEST_INPUT_FILE = "large-text.txt";
    private static final String TEST_OUT_NUMERIC = "out-numeric.txt";
    private static final String TEST_OUT_NUM_REV = "out-numeric-reverse.txt";

    private SortApplication app;
    private OutputStream stdout;

    @BeforeEach
    void setUp() {
        app = new SortApplication();
        stdout = new ByteArrayOutputStream();
    }

    @Nested
    class FileInputTests {

        @TempDir
        private Path testingDirectory;
        private InputStream mockStdin;

        @BeforeEach
        void setUp(@TempDir(cleanup = CleanupMode.ALWAYS) Path tempDir) throws IOException {
            testingDirectory = tempDir;
            final String resourceDirectory = removeTrailing(TEST_RESOURCES, "/");
            try (Stream<Path> stream = Files.walk(Paths.get(resourceDirectory))) {
                stream.forEach(source -> {
                    Path destination = Paths.get(testingDirectory.toString(),
                            source.toString().substring(resourceDirectory.length()));

                    try {
                        Files.copy(source, destination, REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException("Unable to copy test resources to temp directory.", e);
                    }
                });
            }

            // Set CWD to be the test directory
            Environment.currentDirectory = testingDirectory.toString();

            mockStdin = mock(InputStream.class);
        }

        @Test
        void run_IsFirstWordNumberAndIsCaseIndependentFlagForLargeTextFile_IsFirstWordNumberTakesPrecedence() {
            String[] args = {"-f", "-n", TEST_INPUT_FILE};
            assertDoesNotThrow(() -> app.run(args, mockStdin, stdout));
            Path expectedFilePath = Paths.get(testingDirectory.toString(), TEST_OUT_NUMERIC);
            String expectedContent = assertDoesNotThrow(() -> Files.readString(expectedFilePath));
            assertEquals(expectedContent, stdout.toString());
        }

        @Test
        void run_IsFirstWordNumberAndIsReverseAndIsCaseIndependentFlagForLargeTextFile_IsFirstWordNumberTakesPrecedence() {
            String[] args = {"-fr", "-n", TEST_INPUT_FILE};
            assertDoesNotThrow(() -> app.run(args, mockStdin, stdout));
            Path expectedFilePath = Paths.get(testingDirectory.toString(), TEST_OUT_NUM_REV);
            String expectedContent = assertDoesNotThrow(() -> Files.readString(expectedFilePath));
            assertEquals(expectedContent, stdout.toString());
        }
    }
}
