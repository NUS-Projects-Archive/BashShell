package sg.edu.nus.comp.cs4218.impl.parser;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.skeleton.parser.RmArgsParser;

public class RmArgsParserTest {
    RmArgsParser parser;

    private static Stream<Arguments> validFlags() {
        return Stream.of(
                Arguments.of("-r", new boolean[]{true, false}),
                Arguments.of("-d", new boolean[]{false, true}),
                Arguments.of("-rd", new boolean[]{true, true})
        );
    }

    @BeforeEach
    void setUp() {
        parser = new RmArgsParser();
    }

    @ParameterizedTest
    @MethodSource("validFlags")
    void parse_ValidFlags_CorrectMatchingFlags(String args, boolean... expectedResults) {
        assertDoesNotThrow(() -> parser.parse(args));
        assertArrayEquals(expectedResults, new boolean[]{
                parser.isRecursive(),
                parser.isRecursive()
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"-R", "-D"})
    void parse_InvalidFlags_ThrowsInvalidArgsException(String args) {
        Throwable thrown = assertThrowsExactly(InvalidArgsException.class, () -> parser.parse(args));
        String illegalFlag = args.substring(1);
        assertEquals(String.format("illegal option -- %s", illegalFlag), thrown.getMessage());
    }
}
