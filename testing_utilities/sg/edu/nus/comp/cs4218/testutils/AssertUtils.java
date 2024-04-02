package sg.edu.nus.comp.cs4218.testutils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class AssertUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private AssertUtils() { /* Does nothing */ }

    /**
     * Assert that file exists.
     *
     * @param actual {@code String} of path to file
     */
    public static void assertFileExists(String actual) {
        assertFileExists(Paths.get(actual));
    }

    /**
     * Assert that file exists.
     *
     * @param actual {@code Path} to file
     */
    public static void assertFileExists(Path actual) {
        assertTrue(Files.exists(actual));
    }

    /**
     * Assert that file does not exist.
     *
     * @param actual {@code String} of path to file
     */
    public static void assertFileDoNotExists(String actual) {
        assertFileDoNotExists(Paths.get(actual));
    }

    /**
     * Assert that file exists.
     *
     * @param actual {@code Path} to file
     */
    public static void assertFileDoNotExists(Path actual) {
        assertFalse(Files.exists(actual));
    }

    /**
     * Assert that the 2 files contain the same thing.
     * Data is compared line by line.
     *
     * @param expected {@code String} of path to file containing the expected data
     * @param actual   {@code String} of path to file containing the actual data
     */
    public static void assertFileContentMatch(String expected, String actual) {
        assertFileContentMatch(Paths.get(expected), Paths.get(actual));
    }

    /**
     * Assert that the 2 files contain the same thing.
     * Data is compared line by line.
     *
     * @param expected {@code Path} to file containing the expected data
     * @param actual   {@code Path} to file containing the actual data
     */
    public static void assertFileContentMatch(Path expected, Path actual) {
        try {
            List<String> expectedContent = Files.readAllLines(expected);
            List<String> actualContent = Files.readAllLines(actual);
            assertEquals(expectedContent, actualContent);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Assert that {@code actualObject} has the type {@code expectedType}.
     *
     * @param expectedType Type that the object is expected to be. Should end with {@code .class}.
     * @param actualObject Object to assert the type of
     */
    public static void assertSameType(Object expectedType, Object actualObject) {
        assertEquals(expectedType, actualObject.getClass());
    }

    /**
     * Assert that {@code actual} is equal to an empty string ("").
     *
     * @param actual The string to be checked for emptiness
     */
    public static void assertEmptyString(String actual) {
        assertTrue(actual.isEmpty());
    }

    /**
     * Asserts that two lists are equal in size and contain equal elements at corresponding positions.
     *
     * @param expected The expected list
     * @param actual   The actual list to be compared against the expected list
     */
    public static <T> void assertSameList(List<T> expected, List<T> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }
}
