package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.CatException;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;

class CatApplicationTest {

    // run
    // catstdin
    // catfiles
    // catfileandstdin
    // readfile
    // readstdin
    // search
    private CatApplication app;

    @BeforeEach
    void setUp() {
        this.app = new CatApplication();
    }
    @Test
    void readFile_FileNotFound_ThrowsCatException() {
    }

    @Test
    void readFile_NoPermissionToAccessFile_ThrowsCatException() {
    }

    @Test
    void readFile_ValidFileNoLineNumber_ReturnsFileContent() {
    }

    @Test
    void readFile_ValidFileHasLineNumber_ReturnsLineNumberedFileContent() {
    }

    @Test
    void readStdin_IOExceptionWhenReadingInput_ThrowsCatException() {
    }

    @Test
    void readStdin_ValidInputStreamNoLineNumber_ReturnsUserInput() {
    }

    @Test
    void readStdin_ValidInputStreamHasLineNumber_ReturnsLineNumberedUserInput() {
    }

    @Test
    void catStdin_IOExceptionWhenReadingInput_ThrowsCatException() {
    }

    @Test
    void catStdin_ValidInputStreamNoLineNumber_ReturnsUserInput() {
    }

    @Test
    void catStdin_ValidInputStreamHasLineNumber_ReturnsLineNumberedUserInput() {
    }

    @Test
    void catFiles_NoFiles_ReturnsEmptyString() {
    }

    @Test
    void catFiles_OneFileNoLineNumber_ReturnsFileContent() {
    }

    @Test
    void catFiles_OneFileHasLineNumber_ReturnsLineNumberedFileContent() {
    }

    @Test
    void catFiles_MultipleFilesNoLineNumber_ReturnsConcatenatedFileContent() {
    }

    @Test
    void catFiles_MultipleFilesHasLineNumber_ReturnsLineNumberedConcatenatedFileContent() {
    }

    @Test
    void catFileAndStdin_StdinOnlyNoLineNumber_ReturnsUserInput() {
    }

    @Test
    void catFileAndStdin_StdinOnlyHasLineNumber_ReturnsLineNumberedUserInput() {
    }

    @Test
    void catFileAndStdin_FileOnlyNoLineNumber_ReturnsFileContent() {
    }

    @Test
    void catFileAndStdin_FileOnlyHasLineNumber_ReturnsLineNumberedFileContent() {
    }

    @Test
    void catFileAndStdin_FileAndStdInNoLineNumber_ReturnsConcatenatedFileAndStdin() {
    }

    @Test
    void catFileAndStdin_FileAndStdInHasLineNumber_ReturnsLineNumberedConcatenatedFileAndStdin() {
    }
}