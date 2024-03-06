package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.EchoException;

class EchoApplicationTest {

    private static final String ECHO_EXCEPTION = "echo: ";
    private EchoApplication app;

    private OutputStream outThrowException;
    private OutputStream out;

    @BeforeEach
    public void setUp() throws IOException {
        app = new EchoApplication();
        outThrowException = mock(OutputStream.class);
        doThrow(new IOException()).when(outThrowException).write(any(byte[].class));
        out = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        outThrowException.close();
        out.close();
    }

    @Test
    void constructResult_NullArgs_ThrowsEchoException() {
        EchoException result = assertThrowsExactly(EchoException.class, () -> app.constructResult(null));
        assertEquals(ECHO_EXCEPTION + ERR_NULL_ARGS, result.getMessage());
    }

    @Test
    void constructResult_NoArgs_ReturnsNewLine() {
        String result = assertDoesNotThrow(() -> app.constructResult(new String[0]));
        assertEquals(STRING_NEWLINE, result);
    }

    @Test
    void constructResult_OneArg_JoinsArgAndNewLine() {
        String result = assertDoesNotThrow(() -> app.constructResult(new String[]{"OneArg"}));
        assertEquals("OneArg" + STRING_NEWLINE, result);
    }

    @Test
    void constructResult_ManyArgs_JoinsArgsAndNewLine() {
        String result = assertDoesNotThrow(() -> app.constructResult(new String[]{"Many", "Args"}));
        assertEquals("Many" + " " + "Args" + STRING_NEWLINE, result);
    }
}
