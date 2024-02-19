package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.impl.app.UniqApplication;

class UniqApplicationTest {

    @Test
    void run_removeAdjacentDuplicates_onlyUniqueAdjacent() {
        //Given
        String[] args = { "resources/uniq-1.txt" };
        String expected =
                "Hello World" + STRING_NEWLINE +
                "Alice" + STRING_NEWLINE +
                "Bob" + STRING_NEWLINE +
                "Alice" + STRING_NEWLINE +
                "Bob" + STRING_NEWLINE;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // When
        try {
            new UniqApplication().run(args, null, out);
        } catch (Exception e) {
            fail("Unexpected Error: " + e.getMessage());
        }

        // Then
        assertEquals(expected, out.toString());
    }

    @Test
    void run_countOccurrences_correctCounts() {
        //Given
        String[] args = { "-c", "resources/uniq-1.txt" };
        String expected =
                "2 Hello World" + STRING_NEWLINE +
                "2 Alice" + STRING_NEWLINE +
                "1 Bob" + STRING_NEWLINE +
                "1 Alice" + STRING_NEWLINE +
                "1 Bob" + STRING_NEWLINE;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // When
        try {
            new UniqApplication().run(args, null, out);
        } catch (Exception e) {
            fail("Unexpected Error: " + e.getMessage());
        }

        // Then
        assertEquals(expected, out.toString());
    }

    @Test
    void run_duplicatesPerGroup_onlyOnePerDuplicateGroup() {
        //Given
        String[] args = { "-d", "resources/uniq-1.txt" };
        String expected =
                "Hello World" + STRING_NEWLINE +
                "Alice" + STRING_NEWLINE;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // When
        try {
            new UniqApplication().run(args, null, out);
        } catch (Exception e) {
            fail("Unexpected Error: " + e.getMessage());
        }

        // Then
        assertEquals(expected, out.toString());
    }

    @Test
    void run_AllDuplicates_AllDuplicateLines() {
        //Given
        String[] args = { "-D", "resources/uniq-1.txt" };
        String expected =
                "Hello World" + STRING_NEWLINE +
                "Hello World" + STRING_NEWLINE +
                "Alice" + STRING_NEWLINE +
                "Alice" + STRING_NEWLINE;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // When
        try {
            new UniqApplication().run(args, null, out);
        } catch (Exception e) {
            fail("Unexpected Error: " + e.getMessage());
        }

        // Then
        assertEquals(expected, out.toString());
    }

    @Test
    void run_countAndGroupDuplicate_correctCountAndLines() {
        //Given
        String[] args = { "-cd", "resources/uniq-1.txt" };
        String expected =
                "2 Hello World" + STRING_NEWLINE +
                "2 Alice" + STRING_NEWLINE;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // When
        try {
            new UniqApplication().run(args, null, out);
        } catch (Exception e) {
            fail("Unexpected Error: " + e.getMessage());
        }

        // Then
        assertEquals(expected, out.toString());
    }

    @Test
    void run_countAndAllDuplicates_throwsUniqException() {
        //Given
        String[] args = { "-cD", "resources/uniq-1.txt" };
        String expectedMessage = "uniq: printing all duplicated lines and repeat counts is meaningless";

        // When
        UniqException exception = assertThrows(UniqException.class,() -> {
            new UniqApplication().run(args, null, null);
        });

        // Then
        assertEquals(expectedMessage, exception.getMessage());
    }
}
