package sg.edu.nus.comp.cs4218.impl.app.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.app.helper.PasteApplicationHelper.checkPasteFileValidity;
import static sg.edu.nus.comp.cs4218.impl.app.helper.PasteApplicationHelper.mergeInParallel;
import static sg.edu.nus.comp.cs4218.impl.app.helper.PasteApplicationHelper.mergeInSerial;
import static sg.edu.nus.comp.cs4218.testutils.AssertUtils.assertEmptyString;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFile;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.joinStringsByNewline;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.joinStringsByTab;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.PasteException;

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

    @Test
    void checkPasteFileValidity_FileGivenAsStdin_ReturnsTrue() {
        boolean result = assertDoesNotThrow(() -> checkPasteFileValidity("-"));
        assertTrue(result);
    }

    @Test
    void checkPasteFileValidity_FileDoNotExist_ThrowsPasteException(@TempDir Path tempDir) {
        String nonExistFile = tempDir.resolve("nonExistFile.txt").toString();
        PasteException result = assertThrowsExactly(PasteException.class, () ->
                checkPasteFileValidity(nonExistFile)
        );
        String expected = "paste: 'nonExistFile.txt': No such file or directory";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void checkPasteFileValidity_FileGivenAsDirectory_ReturnsFalse(@TempDir Path tempDir) {
        boolean result = assertDoesNotThrow(() -> checkPasteFileValidity(tempDir.toString()));
        assertFalse(result);
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS)
    void checkPasteFileValidity_FileNoPermissionToRead_ThrowsPasteException() {
        Path filePath = createNewFile("file.txt", "file content");
        boolean isSetReadable = filePath.toFile().setReadable(false);
        assertTrue(isSetReadable, "Failed to set read permission to false for test source file");
        PasteException result = assertThrowsExactly(PasteException.class,
                () -> checkPasteFileValidity(filePath.toString()));
        String expected = String.format("paste: '%s': Could not read file", filePath.toFile().getName());
        assertEquals(expected, result.getMessage());
    }

    @Test
    void checkPasteFileValidity_FileExists_ReturnTrue() {
        String file = createNewFile("file.txt", "file content").toString();
        boolean result = assertDoesNotThrow(() -> checkPasteFileValidity(file));
        assertTrue(result);
    }
}
