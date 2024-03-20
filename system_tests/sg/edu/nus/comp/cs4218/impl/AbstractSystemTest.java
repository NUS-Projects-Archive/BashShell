package sg.edu.nus.comp.cs4218.impl;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErrNormalized;
import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOutNormalized;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withTextFromSystemIn;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.io.CleanupMode.ALWAYS;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class AbstractSystemTest {

    static String rootDirectory = System.getProperty("user.dir");
    final static String CAT_APP = "cat";
    final static String CD_APP = "cd";
    final static String ECHO_APP = "echo";
    final static String EXIT_APP = "exit";
    final static String LS_APP = "ls";
    final static String MKDIR_APP = "mkdir";
    final static String RM_APP = "rm";

    /**
     * Test {@code ShellImpl::main} using the given inputs.
     *
     * @param inputs Strings acting as stdin
     * @return {@code SystemTestResults} of the test
     */
    static SystemTestResults testMainWith(String... inputs) {
        SystemTestResults res = new SystemTestResults();
        StringBuilder exitingDirectory = new StringBuilder();

        assertDoesNotThrow(() -> {
            res.exitCode = catchSystemExit(() -> {
                res.err = tapSystemErrNormalized(() -> {
                    res.out = tapSystemOutNormalized(() -> {
                        withTextFromSystemIn(inputs).execute(() -> {
                            Environment.currentDirectory = rootDirectory;
                            res.rootDirectory = Environment.currentDirectory;
                            ShellImpl.main();
                            exitingDirectory.append(Environment.currentDirectory);
                        });
                    });
                });
            });
        });

        final String extraClosingLine = "\n" + exitingDirectory + "$ ";
        res.out = StringUtils.removeTrailingOnce(res.out, extraClosingLine);
        res.err = StringUtils.removeTrailingOnce(res.err, "null\n");

        return res;
    }

    /**
     * Set up to be done before each test.
     *
     * @param tempDir Path of a temp directory for the test
     */
    @BeforeEach
    void beforeEach(@TempDir(cleanup = ALWAYS) Path tempDir) {
        rootDirectory = tempDir.toAbsolutePath().toString();
        Environment.currentDirectory = rootDirectory;
    }

    /**
     * Object holding out, err and exit code of a system test.
     * Also holds the "root" directory of the test environment.
     */
    static class SystemTestResults {
        String out;
        String err;
        String rootDirectory;
        int exitCode;

        String rootPath(String children) {
            return rootDirectory + children + "$ ";
        }
    }
}
