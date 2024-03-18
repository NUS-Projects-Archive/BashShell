package sg.edu.nus.comp.cs4218.impl.app.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.app.helper.PasteApplicationHelper.mergeInParallel;
import static sg.edu.nus.comp.cs4218.impl.app.helper.PasteApplicationHelper.mergeInSerial;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.joinStringsByNewline;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.joinStringsByTab;
import static sg.edu.nus.comp.cs4218.test.AssertUtils.assertEmptyString;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PasteApplicationHelperTest {

    private static final List<List<String>> LIST_OF_FILES = new ArrayList<>();

    @BeforeAll
    static void setUp() {
        LIST_OF_FILES.add(List.of("A", "B", "C", "D"));
        LIST_OF_FILES.add(List.of("1", "2", "3", "4"));
    }

    @Test
    void mergeInSerial_EmptyList_ReturnsEmptyString() {
        List<List<String>> emptyListOfFiles = new ArrayList<>();
        String result = mergeInSerial(emptyListOfFiles);
        assertEmptyString(result);
    }

    @Test
    void mergeInSerial_SingleList_ReturnsSerializedString() {
        List<List<String>> listOfFiles = new ArrayList<>();
        listOfFiles.add(List.of("A", "B", "C", "D"));
        String result = mergeInSerial(listOfFiles);
        String expected = joinStringsByTab("A", "B", "C", "D");
        assertEquals(expected, result);
    }

    @Test
    void mergeInSerial_MultipleList_ReturnsSerializedString() {
        String result = mergeInSerial(LIST_OF_FILES);
        String expected = joinStringsByTab("A", "B", "C", "D") +
                STRING_NEWLINE + joinStringsByTab("1", "2", "3", "4");
        assertEquals(expected, result);
    }

    @Test
    void mergeInParallel_EmptyList_ReturnsEmptyString() {
        List<List<String>> emptyListOfFiles = new ArrayList<>();
        String result = mergeInParallel(emptyListOfFiles);
        assertEmptyString(result);
    }

    @Test
    void mergeInParallel_SingleList_ReturnsParallelizedString() {
        List<List<String>> listOfFiles = new ArrayList<>();
        listOfFiles.add(List.of("A", "B", "C", "D"));
        String result = mergeInParallel(listOfFiles);
        String expected = joinStringsByNewline("A", "B", "C", "D");
        assertEquals(expected, result);
    }

    @Test
    void mergeInParallel_MultipleList_ReturnsParallelizedString() {
        String result = mergeInParallel(LIST_OF_FILES);
        String expected = joinStringsByTab("A", "1") +
                STRING_NEWLINE + joinStringsByTab("B", "2") +
                STRING_NEWLINE + joinStringsByTab("C", "3") +
                STRING_NEWLINE + joinStringsByTab("D", "4");
        assertEquals(expected, result);
    }
}
