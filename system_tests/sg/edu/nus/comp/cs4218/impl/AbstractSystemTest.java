package sg.edu.nus.comp.cs4218.impl;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErrNormalized;
import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOutNormalized;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withTextFromSystemIn;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.io.CleanupMode.ALWAYS;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.removeTrailingOnce;

import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.Environment;

@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class AbstractSystemTest {

    final static String CAT_APP = "cat";
    final static String CD_APP = "cd";
    final static String CUT_APP = "cut";
    final static String ECHO_APP = "echo";
    final static String EXIT_APP = "exit";
    final static String GREP_APP = "grep";
    final static String LS_APP = "ls";
    final static String MKDIR_APP = "mkdir";
    final static String MV_APP = "mv";
    final static String PASTE_APP = "paste";
    final static String RM_APP = "rm";
    final static String SORT_APP = "sort";
    final static String TEE_APP = "tee";
    final static String UNIQ_APP = "uniq";
    final static String WC_APP = "wc";

    static String rootDirectory = System.getProperty("user.dir");

    /**
     * Tests {@code ShellImpl::main} using the given inputs.
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
        res.out = removeTrailingOnce(res.out, extraClosingLine);
        res.err = removeTrailingOnce(res.err, "null\n");

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

        private static final String TERMINATOR = "$ ";

        String out;
        String err;
        String rootDirectory;
        int exitCode;

        /**
         * Get a string representation of the root directory.
         *
         * @return String representation of root directory
         */
        String rootPath() {
            return rootDirectory + TERMINATOR;
        }

        /**
         * Get a string representation of a current-working-directory,
         * where {@code relativePath} represents the folder path from the root directory.
         *
         * @param relativePath String representation the folder path from root directory to current-working-directory
         * @return String representation of a current-working-directory
         */
        String rootPath(String relativePath) {
            if (relativePath == null || relativePath.isBlank()) {
                return rootPath();
            }

            return rootDirectory + CHAR_FILE_SEP + relativePath + TERMINATOR;
        }

        /**
         * Get a string representation of a current-working-directory,
         * where {@code relativeFolders} represents list of folder "inside" of root directory.
         * Blank {@code relativeFolders} are ignored.
         *
         * @param relativeFolders String of individual folder names, to go from root directory to
         *                        current-working-directory
         * @return String representation of a current-working-directory
         */
        String rootPath(String... relativeFolders) {
            String[] folders = Stream.of(relativeFolders)
                    .map(String::trim)
                    .filter(((Predicate<String>) String::isBlank).negate())
                    .toArray(String[]::new);
            return rootPath(String.join(String.valueOf(CHAR_FILE_SEP), folders));
        }
    }


}
