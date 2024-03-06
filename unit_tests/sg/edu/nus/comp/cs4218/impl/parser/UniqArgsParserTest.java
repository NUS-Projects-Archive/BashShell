package sg.edu.nus.comp.cs4218.impl.parser;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

class UniqArgsParserTest {
    UniqArgsParser parser;

    private static Stream<Arguments> validFlags() {
        return Stream.of(
                Arguments.of("-c", new boolean[]{true, false, false}),
                Arguments.of("-d", new boolean[]{false, true, false}),
                Arguments.of("-D", new boolean[]{false, false, true}),
                Arguments.of("-cd", new boolean[]{true, true, false}),
                Arguments.of("-cD", new boolean[]{true, false, true}),
                Arguments.of("-dD", new boolean[]{false, true, true}),
                Arguments.of("-cdD", new boolean[]{true, true, true})
        );
    }

    @BeforeEach
    void setUp() {
        parser = new UniqArgsParser();
    }

    @ParameterizedTest
    @MethodSource("validFlags")
    void parse_ValidFlags_CorrectMatchingFlags(String args, boolean... expectedResults) {
        assertDoesNotThrow(() -> parser.parse(args));
        assertArrayEquals(expectedResults, new boolean[]{
                parser.isPrefixWithOccurrencesCount(),
                parser.isPrintDuplicateOncePerGroup(),
                parser.isPrintAllDuplicate()
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"-C", "--", "-p", "-r"})
    void parse_InvalidFlags_ThrowsInvalidArgsException(String args) {
        InvalidArgsException thrown = assertThrowsExactly(InvalidArgsException.class, () -> parser.parse(args));

        String illegalFlag = args.substring(1);
        assertEquals(String.format("illegal option -- %s", illegalFlag), thrown.getMessage());
    }
}
