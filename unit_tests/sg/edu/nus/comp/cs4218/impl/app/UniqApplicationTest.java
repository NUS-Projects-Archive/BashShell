package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.testutils.AssertUtils;

class UniqApplicationTest {

    private static final String TEST_RESOURCES = "resources/uniq/";
    private static final String TEST_INPUT_FILE = TEST_RESOURCES + "input.txt";
    private static final String TEST_OUTPUT_FILE = "test-output.txt";
    private UniqApplication app;

    private static Stream<Arguments> validFlagsNoErrors() {
        return Stream.of(
                Arguments.of(false, false, false, TEST_RESOURCES + "out.txt"),
                Arguments.of(true, false, false, TEST_RESOURCES + "out-c.txt"),
                Arguments.of(false, true, false, TEST_RESOURCES + "out-d.txt"),
                Arguments.of(false, false, true, TEST_RESOURCES + "out-CapD.txt"),
                Arguments.of(true, true, false, TEST_RESOURCES + "out-cd.txt"),
                Arguments.of(false, true, true, TEST_RESOURCES + "out-dD.txt")
        );
    }

    private static Stream<Arguments> validFlagsThrowsError() {
        return Stream.of(
                Arguments.of(true, false, true),
                Arguments.of(true, true, true)
        );
    }

    @BeforeEach
    void setUp() {
        app = new UniqApplication();
    }

    @ParameterizedTest
    @MethodSource("validFlagsNoErrors")
    void uniqFromFile_VariousNoErrorFlags_FilesWithCorrectOutput(
            boolean isCount, boolean isRepeated, boolean isAllRepeated, String expectedFile, @TempDir Path target) {
        String outputFile = target.resolve(TEST_OUTPUT_FILE).toString();
        assertDoesNotThrow(() ->
                app.uniqFromFile(isCount, isRepeated, isAllRepeated, TEST_INPUT_FILE, outputFile)
        );
        AssertUtils.assertFileContentMatch(expectedFile, outputFile);
    }

    @ParameterizedTest
    @MethodSource("validFlagsThrowsError")
    void uniqFromFile_VariousThrowsErrorFlags_ThrowsUniqException(
            boolean isCount, boolean isRepeated, boolean isAllRepeated, @TempDir Path target) {
        String outputFile = target.resolve(TEST_OUTPUT_FILE).toString();
        assertThrowsExactly(UniqException.class, () ->
                app.uniqFromFile(isCount, isRepeated, isAllRepeated, TEST_INPUT_FILE, outputFile)
        );
    }

    @ParameterizedTest
    @MethodSource("validFlagsNoErrors")
    void uniqFromStdin_VariousNoErrorFlags_FilesWithCorrectOutput(
            boolean isCount, boolean isRepeated, boolean isAllRepeated, String expectedFile, @TempDir Path target) {
        try (InputStream inputStream = new FileInputStream(TEST_INPUT_FILE)) {
            String outputFile = target.resolve(TEST_OUTPUT_FILE).toString();
            assertDoesNotThrow(() ->
                    app.uniqFromStdin(isCount, isRepeated, isAllRepeated, inputStream, outputFile)
            );
            AssertUtils.assertFileContentMatch(expectedFile, outputFile);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @MethodSource("validFlagsThrowsError")
    void uniqFromStdin_VariousThrowsErrorFlags_ThrowsUniqException(
            boolean isCount, boolean isRepeated, boolean isAllRepeated, @TempDir Path target) {
        try (InputStream inputStream = new FileInputStream(TEST_INPUT_FILE)) {
            String outputFile = target.resolve(TEST_OUTPUT_FILE).toString();
            assertThrowsExactly(UniqException.class, () ->
                    app.uniqFromStdin(isCount, isRepeated, isAllRepeated, inputStream, outputFile)
            );
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
}
