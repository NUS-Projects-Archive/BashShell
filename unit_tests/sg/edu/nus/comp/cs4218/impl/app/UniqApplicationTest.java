package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.UniqException;

class UniqApplicationTest {

    static final String TEST_FILE_ONE = "resources/uniq-1.txt";
    static final String STR_HELLO_WORLD = "Hello World";
    static final String STR_ALICE = "Alice";
    static final String STR_BOB = "Bob";

    @Test
    void run_NoFlags_OnlyUniqueAdjacentLines() {
        //Given
        final String[] args = {TEST_FILE_ONE};
        final String expected = STR_HELLO_WORLD + STRING_NEWLINE +
                STR_ALICE + STRING_NEWLINE +
                STR_BOB + STRING_NEWLINE +
                STR_ALICE + STRING_NEWLINE +
                STR_BOB + STRING_NEWLINE;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UniqApplication app = new UniqApplication();

        assertDoesNotThrow(() -> app.run(args, null, out)); // When
        assertEquals(expected, out.toString()); // Then
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
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UniqApplication app = new UniqApplication();

        assertDoesNotThrow(() -> app.run(args, null, out)); // When
        assertEquals(expected, out.toString()); // Then
    }

    @Test
    void run_GroupDuplicatesFlag_OnlyOnePerDuplicateGroup() {
        //Given
        final String[] args = {"-d", TEST_FILE_ONE};
        final String expected = STR_HELLO_WORLD + STRING_NEWLINE +
                STR_ALICE + STRING_NEWLINE;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UniqApplication app = new UniqApplication();

        assertDoesNotThrow(() -> app.run(args, null, out)); // When
        assertEquals(expected, out.toString()); // Then
    }

    @Test
    void run_AllDuplicatesFlag_AllDuplicateLines() {
        //Given
        final String[] args = {"-D", TEST_FILE_ONE};
        final String expected = STR_HELLO_WORLD + STRING_NEWLINE +
                STR_HELLO_WORLD + STRING_NEWLINE +
                STR_ALICE + STRING_NEWLINE +
                STR_ALICE + STRING_NEWLINE;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UniqApplication app = new UniqApplication();

        assertDoesNotThrow(() -> app.run(args, null, out)); // When
        assertEquals(expected, out.toString()); // Then
    }

    @Test
    @DisplayName("Test D flag taking precedence over d flag")
    void run_GroupDuplicatesAndAllDuplicatesFlag_AllDuplicateLines() {
        //Given
        final String[] args = {"-dD", TEST_FILE_ONE};
        final String expected = STR_HELLO_WORLD + STRING_NEWLINE +
                STR_HELLO_WORLD + STRING_NEWLINE +
                STR_ALICE + STRING_NEWLINE +
                STR_ALICE + STRING_NEWLINE;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UniqApplication app = new UniqApplication();

        assertDoesNotThrow(() -> app.run(args, null, out)); // When
        assertEquals(expected, out.toString()); // Then
    }

    @Test
    void run_CountAndGroupDuplicateFlags_CorrectCountAndLines() {
        //Given
        final String[] args = {"-cd", TEST_FILE_ONE};
        final String expected = "2 Hello World" + STRING_NEWLINE +
                "2 Alice" + STRING_NEWLINE;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UniqApplication app = new UniqApplication();

        assertDoesNotThrow(() -> app.run(args, null, out)); // When
        assertEquals(expected, out.toString()); // Then
    }

    @Test
    void run_CountAndAllDuplicatesFlags_ThrowsUniqException() {
        //Given
        final String[] args = {"-cD", TEST_FILE_ONE};
        final String expectedMessage = "uniq: printing all duplicated lines and repeat counts is meaningless";
        UniqApplication app = new UniqApplication();

        Throwable thrown = assertThrows(UniqException.class, () -> app.run(args, null, null)); // When
        assertEquals(expectedMessage, thrown.getMessage()); // Then
    }
}
