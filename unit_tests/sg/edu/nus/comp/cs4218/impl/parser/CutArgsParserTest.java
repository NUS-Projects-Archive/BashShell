package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;

import java.util.Set;

public class CutArgsParserTest {

    private final Set<Character> VALID_FLAGS = Set.of('c', 'b');
    private CutArgsParser cutArgsParser;

    @BeforeEach
    void setUp() {
        this.cutArgsParser = new CutArgsParser();
    }
}
