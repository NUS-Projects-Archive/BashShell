package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewDirectory;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CdException;

@SuppressWarnings("PMD.ClassNamingConventions")
class CdApplicationIT {

    private static final String DIR_NAME = "tempDir";
    private static final String CHILD_DIR_NAME = "tempChildDir";

    @TempDir
    private static Path parentDir;
    private static String parentDirAbsPath;

    private static Path dir;
    private static String dirAbsPath;

    private static String childDirAbsPath;

    private static CdApplication app;

    private InputStream mockInputStream;
    private OutputStream mockOutputStream;

    static Stream<Arguments> validDirs() {
        return Stream.of(
                Arguments.of(".", dirAbsPath),
                Arguments.of("..", parentDirAbsPath),
                Arguments.of(String.format(".%s%s", CHAR_FILE_SEP, CHILD_DIR_NAME), childDirAbsPath),
                Arguments.of(String.format("..%s%s", CHAR_FILE_SEP, DIR_NAME), dirAbsPath),
                Arguments.of(childDirAbsPath, childDirAbsPath),
                Arguments.of(CHILD_DIR_NAME, childDirAbsPath)
        );
    }

    @BeforeAll
    static void setUpEnvironment() {
        app = new CdApplication();

        parentDirAbsPath = parentDir.toString();

        // Add tempDir to tempParentDir
        dir = createNewDirectory(parentDir, DIR_NAME);
        dirAbsPath = dir.toString();

        // Add tempChildDir to tempDir
        Path tempChildDir = createNewDirectory(dir, CHILD_DIR_NAME);
        childDirAbsPath = tempChildDir.toString();

        // Set cwd to tempDir
        Environment.currentDirectory = dirAbsPath;
    }

    @BeforeEach
    void setUp() {
        mockInputStream = mock(InputStream.class);
        mockOutputStream = mock(OutputStream.class);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Reset cwd to tempDir
        Environment.currentDirectory = dirAbsPath;
        mockInputStream.close();
        mockOutputStream.close();
    }

    /**
     * Tests run method in CdApplication with null args and expects a CdException to be thrown.
     */
    @Test
    void run_NullArgs_ThrowsErrNullArgs() {
        CdException result = assertThrowsExactly(CdException.class, () -> app.run(null, mockInputStream, mockOutputStream));
        assertEquals("cd: " + ERR_NULL_ARGS, result.getMessage());
    }

    /**
     * Tests run method in CdApplication with multiple args and expects a CdException to be thrown.
     */
    @Test
    void run_MultipleArgs_ThrowsErrTooManyArgs() {
        CdException result = assertThrowsExactly(CdException.class, () -> app.run(new String[]{"a", "b"},
                mockInputStream, mockOutputStream));
        assertEquals(String.format("cd: %s", ERR_TOO_MANY_ARGS), result.getMessage());
    }

    /**
     * Tests run method in CdApplication with no args and expects the current directory to be changed to the home.
     */
    @Test
    void run_EmptyArgs_ChangesToHomeDir() {
        String expectedDir = System.getProperty("user.dir");
        assertDoesNotThrow(() -> app.run(new String[]{}, mockInputStream, mockOutputStream));
        String currDir = Environment.currentDirectory;
        assertEquals(expectedDir, currDir);
    }

    /**
     * Tests run method in CdApplication with valid directory and expects the current directory to be changed to the
     * valid directory.
     */
    @ParameterizedTest
    @MethodSource("validDirs")
    void run_ValidDir_ChangeCurrentDirectoryToArg(String validDir, Path expectedDir) {
        assertDoesNotThrow(() -> app.run(new String[]{validDir}, mockInputStream, mockOutputStream));
        assertEquals(expectedDir.toString(), Environment.currentDirectory);
    }
}
