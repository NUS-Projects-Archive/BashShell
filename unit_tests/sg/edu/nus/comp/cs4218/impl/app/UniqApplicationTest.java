package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.AssertUtils.assertFileMatch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import sg.edu.nus.comp.cs4218.exception.UniqException;

class UniqApplicationTest {

    static final String TEST_RESOURCES = "resources/uniq/";
    static final String TEST_FILE_ONE = TEST_RESOURCES + "input.txt";
    static final String STR_HELLO_WORLD = "Hello World";
    static final String STR_ALICE = "Alice";
    static final String STR_BOB = "Bob";

    UniqApplication app;
    ByteArrayOutputStream outputStream;

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
        outputStream = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        outputStream.close();
    }

    @Test
    void run_NoFlags_OnlyUniqueAdjacentLines() {
        //Given
        final String[] args = {TEST_FILE_ONE};
        final String expected = STR_HELLO_WORLD + STRING_NEWLINE +
                STR_ALICE + STRING_NEWLINE +
                STR_BOB + STRING_NEWLINE +
                STR_ALICE + STRING_NEWLINE +
                STR_BOB + STRING_NEWLINE;

        assertDoesNotThrow(() -> app.run(args, null, outputStream)); // When
        assertEquals(expected, outputStream.toString()); // Then
    }

    @Test
    void run_CountFlag_CorrectOccurrenceCounts() {
        //Given
        final String[] args = {"-c", TEST_FILE_ONE};
        final String expected = "2 Hello World" + STRING_NEWLINE +
                "2 Alice" + STRING_NEWLINE +
                "1 Bob" + STRING_NEWLINE +
                "1 Alice" + STRING_NEWLINE +
                "1 Bob" + STRING_NEWLINE;

        assertDoesNotThrow(() -> app.run(args, null, outputStream)); // When
        assertEquals(expected, outputStream.toString()); // Then
    }

    @Test
    void run_GroupDuplicatesFlag_OnlyOnePerDuplicateGroup() {
        //Given
        final String[] args = {"-d", TEST_FILE_ONE};
        final String expected = STR_HELLO_WORLD + STRING_NEWLINE +
                STR_ALICE + STRING_NEWLINE;

        assertDoesNotThrow(() -> app.run(args, null, outputStream)); // When
        assertEquals(expected, outputStream.toString()); // Then
    }

    @Test
    void run_AllDuplicatesFlag_AllDuplicateLines() {
        //Given
        final String[] args = {"-D", TEST_FILE_ONE};
        final String expected = STR_HELLO_WORLD + STRING_NEWLINE +
                STR_HELLO_WORLD + STRING_NEWLINE +
                STR_ALICE + STRING_NEWLINE +
                STR_ALICE + STRING_NEWLINE;

        assertDoesNotThrow(() -> app.run(args, null, outputStream)); // When
        assertEquals(expected, outputStream.toString()); // Then
    }

    @Test
    @DisplayName("Test -D flag's precedence over -d flag")
    void run_GroupDuplicatesAndAllDuplicatesFlags_AllDuplicateLines() {
        //Given
        final String[] args = {"-dD", TEST_FILE_ONE};
        final String expected = STR_HELLO_WORLD + STRING_NEWLINE +
                STR_HELLO_WORLD + STRING_NEWLINE +
                STR_ALICE + STRING_NEWLINE +
                STR_ALICE + STRING_NEWLINE;

        assertDoesNotThrow(() -> app.run(args, null, outputStream)); // When
        assertEquals(expected, outputStream.toString()); // Then
    }

    @Test
    void run_CountAndGroupDuplicateFlags_CorrectCountAndLines() {
        //Given
        final String[] args = {"-cd", TEST_FILE_ONE};
        final String expected = "2 Hello World" + STRING_NEWLINE +
                "2 Alice" + STRING_NEWLINE;

        assertDoesNotThrow(() -> app.run(args, null, outputStream)); // When
        assertEquals(expected, outputStream.toString()); // Then
    }

    @Test
    void run_CountAndAllDuplicatesFlags_ThrowsUniqException() {
        //Given
        final String[] args = {"-cD", TEST_FILE_ONE};
        final String expectedMessage = "uniq: printing all duplicated lines and repeat counts is meaningless";

        Throwable thrown = assertThrows(UniqException.class, () -> app.run(args, null, null)); // When
        assertEquals(expectedMessage, thrown.getMessage()); // Then
    }

    @ParameterizedTest
    @MethodSource("validFlagsNoErrors")
    void uniqFromFile_VariousNoErrorFlags_FilesWithCorrectOutput(
            boolean isCount, boolean isRepeated, boolean isAllRepeated,
            String expectedFile,
            @TempDir Path target) {

        String outputFile = target.resolve("test-output.txt").toString();
        assertDoesNotThrow(() -> app.uniqFromFile(isCount, isRepeated, isAllRepeated, TEST_FILE_ONE, outputFile));

        assertFileMatch(expectedFile, outputFile);
    }

    @ParameterizedTest
    @MethodSource("validFlagsThrowsError")
    void uniqFromFile_VariousThrowsErrorFlags_ThrowsUniqException(
            boolean isCount, boolean isRepeated, boolean isAllRepeated,
            @TempDir Path target) {

        String outputFile = target.resolve("test-output.txt").toString();
        assertThrowsExactly(UniqException.class, () ->
                app.uniqFromFile(isCount, isRepeated, isAllRepeated, TEST_FILE_ONE, outputFile)
        );
    }
}
