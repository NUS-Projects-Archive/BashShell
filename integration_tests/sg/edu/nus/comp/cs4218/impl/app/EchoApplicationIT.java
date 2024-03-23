package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.EchoException;

@SuppressWarnings("PMD.ClassNamingConventions")
public class EchoApplicationIT {

    private static final String ECHO_EX_MSG = "echo: ";
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
    void run_NullOutputStream_ThrowsEchoException() {
        EchoException result = assertThrowsExactly(EchoException.class, () ->
                app.run(new String[]{"A", "B", "C"}, null, null)
        );
        assertEquals(ECHO_EX_MSG + ERR_NO_OSTREAM, result.getMessage());
    }

    @Test
    void run_IOExceptionWhenWritingByteBuffer_ThrowsEchoException() {
        EchoException result = assertThrowsExactly(EchoException.class, () ->
                app.run(new String[]{"A", "B", "C"}, null, outThrowException)
        );
        assertEquals(ECHO_EX_MSG + ERR_WRITE_STREAM, result.getMessage());
    }

    @Test
    void run_ValidByteBuffers_PrintsCorrectValues() {
        // Given
        String[] tokens = new String[]{"Hello", "World!"};
        // When
        assertDoesNotThrow(() -> app.run(tokens, null, out));
        // Then
        assertEquals("Hello World!" + STRING_NEWLINE, out.toString());
    }
}
