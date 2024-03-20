package sg.edu.nus.comp.cs4218.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SystemTest extends AbstractSystemTest {

    @Test
    void main_Exit_ExitWithCodeZero() {
        SystemTestResults actual = testMainWith("exit");
        assertEquals(0, actual.exitCode);
    }

    @Test
    void main_EchoToFileAndCatFile_PrintsCorrectlyToStdout() {
        SystemTestResults actual = testMainWith(
                "echo \"Welcome to CS4218!\" > hello_world.txt",
                "cat hello_world.txt",
                "exit"
        );
        String expectedStdout = actual.rootPath("")
                + actual.rootPath("")
                + "Welcome to CS4218!";
        assertEquals(expectedStdout, actual.out);
    }

    // TODO: create tests with chaining multiple commands and uses shell features

}
