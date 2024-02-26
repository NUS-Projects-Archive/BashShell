package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MvArgsParserTest {

    private final Set<Character> VALID_FLAGS = Set.of('n');
    private MvArgsParser mvArgsParser;

    @BeforeEach
    void setUp() {
        this.mvArgsParser = new MvArgsParser();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n"})
    void parse_EmptyString_ReturnsEmptyFlagsAndNonFlagArgsContainsInput(String args)
            throws InvalidArgsException {
        mvArgsParser.parse(args);
        assertTrue(mvArgsParser.flags.isEmpty());
        assertTrue(mvArgsParser.nonFlagArgs.contains(args));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-n"})
    void parse_ValidFlag_ShouldMatchGivenFlags(String args) throws InvalidArgsException {
        mvArgsParser.parse(args);

        // Retain only the common elements between sortArgsParser.flags and VALID_FLAGS
        mvArgsParser.flags.retainAll(VALID_FLAGS);

        // Check if there's at least one common element
        assertFalse(mvArgsParser.flags.isEmpty());
    }

    @Test
    void parse_ValidFlag_ReturnsGivenMatchingFlag() throws InvalidArgsException {
        mvArgsParser.parse("-n");
        assertEquals(VALID_FLAGS, mvArgsParser.flags, "Flags do not match");
    }
}