package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

public class TeeApplicationPublicTest {
    private static final String TEMP = "temp-tee";
    private static final String TEXT_A = "textA.txt";
    private static final String TEXT_B = "textB.txt";
    private static final String ONE_LINE_INPUT = "test 1";
    private static final String MULTI_LINE_INPUT = ONE_LINE_INPUT + TestStringUtils.STRING_NEWLINE + ONE_LINE_INPUT + STRING_NEWLINE;
    private static String initialDir;

    private TeeApplication teeApplication;

    @BeforeAll
    static void setUpAll() throws NoSuchFieldException, IllegalAccessException {
        initialDir = TestEnvironmentUtil.getCurrentDirectory();
    }

    private InputStream mockInputStream(String input) {
        return new ByteArrayInputStream(input.getBytes());
    }

    private String convertListOfStringToStrings(List<String> strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            stringBuilder.append(strings.get(i));
            if (i != strings.size() - 1) {
                stringBuilder.append(TestStringUtils.STRING_NEWLINE);
            }
        }
        return stringBuilder.toString() + STRING_NEWLINE;
    }

    private void writeToFile(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(ONE_LINE_INPUT + STRING_NEWLINE);
            writer.flush();
        } catch (Exception e) {
            fail();
        }
    }

    @BeforeEach
    void setup() throws Exception {
        teeApplication = new TeeApplication();
        Files.createDirectories(Paths.get(TEMP));
        TestEnvironmentUtil.setCurrentDirectory(TEMP);
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.walk(Paths.get(TEMP))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        TestEnvironmentUtil.setCurrentDirectory(initialDir);
    }

    @Test
    public void teeFromStdin_NoAppendNoFileNamesWithOneLineInput_returnsCorrectString() {
        try (InputStream inputStream = mockInputStream(ONE_LINE_INPUT)) {
            String[] arr = new String[0];
            String result = teeApplication.teeFromStdin(false, inputStream, arr);

            inputStream.close();
            assertEquals(ONE_LINE_INPUT + STRING_NEWLINE, result);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void teeFromStdin_NoAppendOneFileNameWithOneLineInput_returnsCorrectString() throws Exception {
        try (InputStream inputStream = mockInputStream(ONE_LINE_INPUT)) {
            String[] arr = {TEXT_A};
            String result = teeApplication.teeFromStdin(false, inputStream, arr);
            File file = new File(TEMP, TEXT_A);
            List<String> targetContent = Files.readAllLines(file.toPath());

            assertTrue(file.exists());
            assertEquals(ONE_LINE_INPUT + STRING_NEWLINE, result);
            assertEquals(ONE_LINE_INPUT, targetContent.get(0));
        }
    }

    @Test
    public void teeFromStdin_NoAppendNoFileNamesWithMultiLineInput_returnsCorrectString() throws Exception {
        try (InputStream inputStream = mockInputStream(MULTI_LINE_INPUT)) {
            String[] arr = new String[0];
            String result = teeApplication.teeFromStdin(false, inputStream, arr);

            assertEquals(MULTI_LINE_INPUT, result);
        }
    }

    @Test
    public void teeFromStdin_NoAppendOneFileNameWithMultipleLineInput_returnsCorrectString() throws Exception {
        try (InputStream inputStream = mockInputStream(MULTI_LINE_INPUT)) {
            String[] arr = {TEXT_A};
            File file = new File(TEMP, TEXT_A);
            String result = teeApplication.teeFromStdin(false, inputStream, arr);
            List<String> targetContent = Files.readAllLines(file.toPath());

            assertTrue(file.exists());
            assertEquals(MULTI_LINE_INPUT, result);
            assertEquals(MULTI_LINE_INPUT, convertListOfStringToStrings(targetContent));
        }
    }

    @Test
    public void teeFromStdin_AppendSingleFilenamesWithSingleLineInput_returnsCorrectString() throws Exception {
        try (InputStream inputStream = mockInputStream(ONE_LINE_INPUT)) {
            String[] arr = {TEXT_A};
            File fileA = new File(TEMP, TEXT_A);
            fileA.createNewFile();
            writeToFile(fileA);
            String result = teeApplication.teeFromStdin(true, inputStream, arr);
            List<String> targetContentA = Files.readAllLines(fileA.toPath());

            assertEquals(ONE_LINE_INPUT + STRING_NEWLINE, result);
            assertEquals(ONE_LINE_INPUT + STRING_NEWLINE + ONE_LINE_INPUT + STRING_NEWLINE,
                    convertListOfStringToStrings(targetContentA));
        }
    }

    @Test
    public void teeFromStdin_AppendMultipleFilenamesWithSingleLineInput_returnsCorrectString() throws Exception {
        try (InputStream inputStream = mockInputStream(ONE_LINE_INPUT)) {
            String[] arr = {TEXT_A, TEXT_B};
            File fileA = new File(TEMP, TEXT_A);
            fileA.createNewFile();
            writeToFile(fileA);
            File fileB = new File(TEMP, TEXT_B);
            fileB.createNewFile();
            writeToFile(fileB);

            String result = teeApplication.teeFromStdin(true, inputStream, arr);
            List<String> targetContentA = Files.readAllLines(fileA.toPath());
            List<String> targetContentB = Files.readAllLines(fileB.toPath());

            assertEquals(ONE_LINE_INPUT + STRING_NEWLINE, result);
            assertEquals(ONE_LINE_INPUT + STRING_NEWLINE + ONE_LINE_INPUT + STRING_NEWLINE, convertListOfStringToStrings(targetContentA));
            assertEquals(ONE_LINE_INPUT + STRING_NEWLINE + ONE_LINE_INPUT + STRING_NEWLINE, convertListOfStringToStrings(targetContentB));
        }
    }
}