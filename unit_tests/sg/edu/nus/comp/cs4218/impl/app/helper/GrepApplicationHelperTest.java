package sg.edu.nus.comp.cs4218.impl.app.helper;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static sg.edu.nus.comp.cs4218.impl.app.helper.GrepApplicationHelper.getGrepArguments;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.GrepException;

class GrepApplicationHelperTest {
    private static final String FILE_NAME_ONE = "filenameone";
    private static final String FILE_NAME_TWO = "filenametwo";
    private static final String PATTERN = "pattern";
    private static final String I_FLAG = "-i";
    private static final String C_FLAG = "-c";
    private static final String H_FLAG = "-H";

    private ArrayList<String> inputFiles;
    private boolean[] flags;

    @BeforeEach
    void setUp() {
        inputFiles = new ArrayList<>();
        flags = new boolean[3];
    }

    @Test
    void getGrepArguments_NullArgs_ThrowsNullPointerException() {
        assertThrowsExactly(NullPointerException.class, () -> getGrepArguments(null, flags, new ArrayList<>()));
    }

    @Test
    void getGrepArguments_NullInputFiles_ThrowsNullPointerException() {
        String[] args = {PATTERN, FILE_NAME_ONE};
        assertThrowsExactly(NullPointerException.class, () -> getGrepArguments(args, flags, null));
    }

    @Test
    void getGrepArguments_PatternBeforeFileNames_ReturnsPatternAndParseCorrectInputFiles() {
        String[] args = {PATTERN, FILE_NAME_ONE, FILE_NAME_TWO};
        String actual = assertDoesNotThrow(() -> getGrepArguments(args, null, inputFiles));
        assertEquals(PATTERN, actual);
        assertArrayEquals(new String[]{FILE_NAME_ONE, FILE_NAME_TWO}, inputFiles.toArray());
    }

    @Test
    void getGrepArguments_PatternAfterFileNames_ReturnsWrongPatternAndParseWrongInputFiles() {
        String[] args = {FILE_NAME_ONE, PATTERN};
        String actual = assertDoesNotThrow(() -> getGrepArguments(args, null, inputFiles));
        assertNotEquals(PATTERN, actual);
        assertNotEquals(FILE_NAME_ONE, inputFiles.get(0));
    }

    @Test
    void getGrepArguments_PatternAndIFlagAndInputFiles_ReturnsPatternAndIFlagAndInputFiles() {
        String[] args = {I_FLAG, PATTERN, FILE_NAME_ONE, FILE_NAME_TWO};
        String actual = assertDoesNotThrow(() -> getGrepArguments(args, flags, inputFiles));
        assertEquals(PATTERN, actual);
        assertArrayEquals(new boolean[]{true, false, false}, new boolean[]{flags[0], flags[1], flags[2]});
        assertArrayEquals(new String[]{FILE_NAME_ONE, FILE_NAME_TWO}, inputFiles.toArray());
    }

    @Test
    void getGrepArguments_PatternAndCFlagAndInputFiles_ReturnsPatternAndCFlagAndInputFiles() {
        String[] args = {C_FLAG, PATTERN, FILE_NAME_ONE, FILE_NAME_TWO};
        String actual = assertDoesNotThrow(() -> getGrepArguments(args, flags, inputFiles));
        assertEquals(PATTERN, actual);
        assertArrayEquals(new boolean[]{false, true, false}, new boolean[]{flags[0], flags[1], flags[2]});
        assertArrayEquals(new String[]{FILE_NAME_ONE, FILE_NAME_TWO}, inputFiles.toArray());
    }

    @Test
    void getGrepArguments_PatternAndHFlagAndInputFiles_ReturnsPatternAndHFlagAndInputFiles() {
        String[] args = {H_FLAG, PATTERN, FILE_NAME_ONE, FILE_NAME_TWO};
        String actual = assertDoesNotThrow(() -> getGrepArguments(args, flags, inputFiles));
        assertEquals(PATTERN, actual);
        assertArrayEquals(new boolean[]{false, false, true}, new boolean[]{flags[0], flags[1], flags[2]});
        assertArrayEquals(new String[]{FILE_NAME_ONE, FILE_NAME_TWO}, inputFiles.toArray());
    }

    @Test
    void getGrepArguments_PatternAndAllValidFlagsAndInputFiles_ReturnsPatternAndAllFlagsAndInputFiles() {
        String[] args = {I_FLAG, H_FLAG, C_FLAG, PATTERN, FILE_NAME_ONE, FILE_NAME_TWO};
        String actual = assertDoesNotThrow(() -> getGrepArguments(args, flags, inputFiles));
        assertEquals(PATTERN, actual);
        assertArrayEquals(new boolean[]{true, true, true}, new boolean[]{flags[0], flags[1], flags[2]});
        assertArrayEquals(new String[]{FILE_NAME_ONE, FILE_NAME_TWO}, inputFiles.toArray());
    }

    @Test
    void getGrepArguments_PatternAndValidInvalidFlags_ThrowsGrepException() {
        String[] args = {I_FLAG, "-X", C_FLAG, H_FLAG, PATTERN, FILE_NAME_ONE, FILE_NAME_TWO};
        GrepException exception = assertThrowsExactly(GrepException.class, () ->
                getGrepArguments(args, flags, inputFiles));
        assertEquals("grep: Invalid syntax", exception.getMessage());
    }

    @Test
    void getGrepArguments_PatternBeforeFlagAndFiles_ParsesFlagAsFileAndUnableToSetFlag() {
        String[] args = {PATTERN, I_FLAG, FILE_NAME_ONE, FILE_NAME_TWO};
        String actual = assertDoesNotThrow(() -> getGrepArguments(args, flags, inputFiles));
        assertEquals(PATTERN, actual);
        assertArrayEquals(new boolean[]{false, false, false}, new boolean[]{flags[0], flags[1], flags[2]});
        assertArrayEquals(new String[]{I_FLAG, FILE_NAME_ONE, FILE_NAME_TWO}, inputFiles.toArray());
    }
}
