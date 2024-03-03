package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class AssertUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private AssertUtils() { /* Does nothing */ }

    /**
     * Assert that the 2 files contain the same thing.
     * Data is compared character by character.
     *
     * @param expected {@code Path} to file containing the expected data
     * @param actual   {@code Path} to file containing the actual data
     */
    public static void assertFileMatch(Path expected, Path actual) {
        int expectedChar, actualChar;
        BufferedInputStream streamOfExpected = null;
        BufferedInputStream streamOfActual = null;

        try {
            streamOfExpected = new BufferedInputStream(new FileInputStream(expected.toFile()));
            streamOfActual = new BufferedInputStream(new FileInputStream(actual.toFile()));

            do {
                expectedChar = streamOfExpected.read();
                actualChar = streamOfActual.read();

                if (expectedChar != actualChar) {
                    break;
                }
            } while (expectedChar != -1);

            if (expectedChar != actualChar) {
                fail(String.format("Expected '%c' but got '%c'", expectedChar, actualChar));
            }
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            try {
                if (streamOfExpected != null) {
                    streamOfExpected.close();
                }
                if (streamOfActual != null) {
                    streamOfActual.close();
                }
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }
    }

    /**
     * Assert that the 2 files contain the same thing.
     * Data is compared character by character.
     *
     * @param expected {@code String} of path to file containing the expected data
     * @param actual   {@code String} of path to file containing the actual data
     */
    public static void assertFileMatch(String expected, String actual) {
        assertFileMatch(Paths.get(expected), Paths.get(actual));
    }

    /**
     * Assert that {@code actualObject} has the type {@code expectedType}.
     *
     * @param expectedType Type that the object is expected to be. Should end with {@code .class}.
     * @param actualObject Object to assert the type of
     */
    public static void assertSameType(Object expectedType, Object actualObject) {
        assertEquals(actualObject.getClass(), expectedType);
    }
}
