package sg.edu.nus.comp.cs4218.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_APP;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import sg.edu.nus.comp.cs4218.Environment;

public class NoSetUpSystemTest extends AbstractSystemTest {

    static String[] validQuotes() {
        return new String[]{
                " \"Hello World\"",
                " 'Hello World'",
                " `echo Hello` World",
        };
    }

    static String[] invalidQuotes() {
        return new String[]{
                " \"`echo Hello World\"",
                " \"`echo Hel`lo`World\"",
                " \"`echo Hello`World",
                " \"\"`echo Hello`World\"",
        };
    }

    @Test
    void main_EOF_ExitWithCodeZero() {
        SystemTestResults actual = testMainWith("");
        assertEquals(0, actual.exitCode);
    }

    @Test
    void main_Exit_ExitWithCodeZero() {
        SystemTestResults actual = testMainWith(EXIT_APP);
        assertEquals(0, actual.exitCode);
    }

    @Test
    void main_EchoToFileAndCatFile_PrintsCorrectlyToStdout() {
        SystemTestResults actual = testMainWith(
                ECHO_APP + " \"Welcome to CS4218!\" > hello_world.txt",
                CAT_APP + " hello_world.txt",
                EXIT_APP
        );
        String expectedStdout = actual.rootPath()
                + actual.rootPath()
                + "Welcome to CS4218!";
        assertEquals(expectedStdout, actual.out);
    }

    @Test
    void main_MkdirDirAndNestedDirAndCdIntoParentDirAndLsAndRmChildDir_RmChildDirSuccessfully() {
        String parentDirName = "parent_dir";
        String childDirName = "child_dir";
        String dirOneName = parentDirName + CHAR_FILE_SEP + childDirName;
        String dirTwoName = "dir_two";

        SystemTestResults actual = testMainWith(
                MKDIR_APP + " -p " + dirOneName + " " + dirTwoName,
                LS_APP,
                EXIT_APP
        );
        assertTrue(actual.out.contains(parentDirName) && actual.out.contains(dirTwoName));

        actual = testMainWith(
                CD_APP + " " + parentDirName,
                RM_APP + " " + childDirName,
                LS_APP,
                EXIT_APP
        );
        assertTrue(actual.out.contains(childDirName));
        String expectedCwd = actual.rootDirectory + CHAR_FILE_SEP + parentDirName;
        assertEquals(expectedCwd, Environment.currentDirectory);

        actual = testMainWith(
                CD_APP + " " + parentDirName,
                RM_APP + " -d " + childDirName,
                LS_APP,
                EXIT_APP
        );
        assertFalse(actual.out.contains(childDirName));
    }

    @ParameterizedTest
    @MethodSource("validQuotes")
    void main_ValidQuotingAndCommandSubstitution_PrintsCorrectlyToStdout(String validQuote) {
        SystemTestResults actual = testMainWith(
                ECHO_APP + validQuote,
                EXIT_APP
        );
        String expected = actual.rootPath() + "Hello World";
        assertEquals(expected, actual.out);
    }

    @ParameterizedTest
    @MethodSource("invalidQuotes")
    void main_InvalidQuotingAndCommandSubstitution_PrintsInvalidSyntax(String invalidQuote) {
        SystemTestResults actual = testMainWith(
                ECHO_APP + invalidQuote,
                EXIT_APP
        );
        String expected = String.format("%sshell: %s", actual.rootPath(), ERR_SYNTAX);
        assertEquals(expected, actual.out);
    }

    @Test
    void main_ValidQuotingAndFailedCommandSubstitution_PrintsInvalidApp() {
        String invalidApp = "echso";
        SystemTestResults actual = testMainWith(
                ECHO_APP + String.format(" `%s Hello`", invalidApp),
                EXIT_APP
        );
        String expected = String.format("%sshell: %s: %s", actual.rootPath(), invalidApp, ERR_INVALID_APP);
        assertEquals(expected, actual.out);
    }
}
