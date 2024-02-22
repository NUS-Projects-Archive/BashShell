package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.UniqException;

class UniqApplicationTest {

    static final String TEST_FILE_ONE = "resources/uniq-1.txt";
    static final String STR_HELLO_WORLD = "Hello World";
    static final String STR_ALICE = "Alice";
    static final String STR_BOB = "Bob";

    @Test
    void run_removeAdjacentDuplicates_onlyUniqueAdjacent() {
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
    void run_countOccurrences_correctCounts() {
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
    void run_duplicatesPerGroup_onlyOnePerDuplicateGroup() {
        //Given
        final String[] args = {"-d", TEST_FILE_ONE};
        final String expected = "Hello World" + STRING_NEWLINE +
                "Alice" + STRING_NEWLINE;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UniqApplication app = new UniqApplication();

        assertDoesNotThrow(() -> app.run(args, null, out)); // When
        assertEquals(expected, out.toString()); // Then
    }

    @Test
    void run_AllDuplicates_AllDuplicateLines() {
        //Given
        final String[] args = {"-D", TEST_FILE_ONE};
        final String expected = "Hello World" + STRING_NEWLINE +
                "Hello World" + STRING_NEWLINE +
                "Alice" + STRING_NEWLINE +
                "Alice" + STRING_NEWLINE;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UniqApplication app = new UniqApplication();

        assertDoesNotThrow(() -> app.run(args, null, out)); // When
        assertEquals(expected, out.toString()); // Then
    }

    @Test
    void run_countAndGroupDuplicate_correctCountAndLines() {
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
    void run_countAndAllDuplicates_throwsUniqException() {
        //Given
        final String[] args = {"-cD", TEST_FILE_ONE};
        final String expectedMessage = "uniq: printing all duplicated lines and repeat counts is meaningless";
        UniqApplication app = new UniqApplication();

        // When
        UniqException exception = assertThrows(UniqException.class, () -> {
            app.run(args, null, null);
        });

        // Then
        assertEquals(expectedMessage, exception.getMessage());
    }
}
