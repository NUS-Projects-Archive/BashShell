package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import sg.edu.nus.comp.cs4218.Environment;

class RegexArgumentTest {

    private RegexArgument regexArg;

    @BeforeEach
    void setUp() {
        this.regexArg = new RegexArgument();
    }

    /**
     * Retrieves the private field named "regex" within the RegexArgument class.
     *
     * @return The String representation of the "regex" field
     */
    private String getRegexField() {
        // Use reflection to access the private field
        Field regexField = assertDoesNotThrow(() -> RegexArgument.class.getDeclaredField("regex"));
        regexField.setAccessible(true);

        // Get the value of the private field for the specific instance
        return assertDoesNotThrow(() -> regexField.get(regexArg).toString());
    }

    private void assertRegexArgument(String plaintext, String regex, Boolean isRegex) {
        assertEquals(plaintext, regexArg.toString());
        assertEquals(regex, getRegexField());
        assertEquals(isRegex, regexArg.isRegex());
    }

    private void appendRegex(String regex) {
        for (char chr : regex.toCharArray()) {
            if (chr == '*') {
                regexArg.appendAsterisk();
            } else {
                regexArg.append(chr);
            }
        }
    }

    @Test
    void constructor_EmptyParams_ReturnsCorrectRegexArgument() {
        this.regexArg = new RegexArgument();
        assertRegexArgument("", "", false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"regexStr1", "regexStr2", "regexStr3"})
    void constructor_GivenString_ReturnsCorrectRegexArgument(String str) {
        this.regexArg = new RegexArgument(str);
        assertRegexArgument(str, Pattern.quote(str), false);
    }

    @Test
    void constructor_GivenStringTextAndIsRegex_ReturnsCorrectRegexArgument() {
        String str = "example";
        String plaintext = "plaintext";

        StringBuilder regexField = new StringBuilder();
        regexField.append(".*");
        for (char chr : str.toCharArray()) {
            regexField.append(Pattern.quote(String.valueOf(chr)));
        }

        this.regexArg = new RegexArgument(str, plaintext, true);
        assertRegexArgument(plaintext, regexField.toString(), true);
    }

    @ParameterizedTest
    @ValueSource(chars = {'a', '1', '.', '*', '?', '^', '+', '$', '[', ']', ' ', '@', '-', '/'})
    void append_SingleCharacter_ReturnsCorrectRegexArgument(char chr) {
        regexArg.append(chr);
        assertRegexArgument(String.valueOf(chr), Pattern.quote(String.valueOf(chr)), false);
    }

    @Test
    void append_MultipleCharacter_ReturnsCorrectRegexArgument() {
        char[] chars = {'a', '1', '.'};
        StringBuilder expectedPlaintext = new StringBuilder();
        StringBuilder expectedRegex = new StringBuilder();
        for (char chr : chars) {
            regexArg.append(chr);
            expectedPlaintext.append(chr);
            expectedRegex.append(Pattern.quote(String.valueOf(chr)));
        }
        assertRegexArgument(expectedPlaintext.toString(), expectedRegex.toString(), false);
    }

    @Test
    void appendAsterisk_EmptyInitialRegex_ReturnsCorrectRegexArgument() {
        regexArg.appendAsterisk();
        assertRegexArgument(String.valueOf('*'), "[^" + StringUtils.fileSeparator() + "]*", true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"regexString1", "regexString2", "regexString3"})
    void merge_SingleRegexArgument_ReturnsCorrectRegexArgument(String str) {
        RegexArgument other = new RegexArgument(str);
        regexArg.merge(other);
        assertRegexArgument(str, Pattern.quote(str), false);
    }

    @Test
    void merge_MultipleRegexArgument_ReturnsCorrectRegexArgument() {
        String[] regexStrings = {"regexString1", "regexString2", "regexString3"};
        StringBuilder expectedPlaintext = new StringBuilder();
        StringBuilder expectedRegex = new StringBuilder();
        for (String regex : regexStrings) {
            RegexArgument other = new RegexArgument(regex);
            regexArg.merge(other);
            expectedPlaintext.append(regex);
            expectedRegex.append(Pattern.quote(regex));
        }
        assertRegexArgument(expectedPlaintext.toString(), expectedRegex.toString(), false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"regexString1", "regexString2", "regexString3"})
    void merge_SingleString_ReturnsCorrectRegexArgument(String str) {
        regexArg.merge(str);
        assertRegexArgument(str, Pattern.quote(str), false);
    }

    @Test
    void merge_MultipleStr_ReturnsCorrectRegexArgument() {
        List<String> regexStrings = List.of("regexString1", "regexString2", "regexString3");
        StringBuilder expectedPlaintext = new StringBuilder();
        StringBuilder expectedRegex = new StringBuilder();
        for (String regex : regexStrings) {
            regexArg.merge(regex);
            expectedPlaintext.append(regex);
            expectedRegex.append(Pattern.quote(regex));
        }
        assertRegexArgument(expectedPlaintext.toString(), expectedRegex.toString(), false);
    }

    @Test
    void globFiles_IsRegex_ReturnsGlobbedFiles(@TempDir Path tempDir) {
        // Set current working directory to the temporary dir
        Environment.currentDirectory = tempDir.toString();

        List<String> files = List.of("file1.txt", "file2.txt", "file3.txt", "invalid.txt", "file4.text");
        List<String> expected = files.subList(0, 3);

        for (String file : files) {
            Path filePath = tempDir.resolve(file);
            assertDoesNotThrow(() -> Files.createFile(filePath));
        }

        String globConditions = "file*.txt";
        appendRegex(globConditions);
        assertEquals(expected, regexArg.globFiles());
    }

    @Test
    void globFiles_IsNotRegex_ReturnsPlainText(@TempDir Path tempDir) {
        // Set current working directory to the temporary dir
        Environment.currentDirectory = tempDir.toString();
        String globConditions = "file.txt"; // will not be treated as regex
        appendRegex(globConditions);
        assertEquals(List.of(globConditions), regexArg.globFiles());
    }
}
