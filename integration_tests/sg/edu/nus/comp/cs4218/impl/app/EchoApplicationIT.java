package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
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

    private final String ECHO_EXCEPTION_MSG = "echo: ";
    private EchoApplication app;

    private OutputStream exceptionThrowingOutputStream;
    private OutputStream out;

    @BeforeEach
    public void setUp() throws IOException {
        this.app = new EchoApplication();
        this.exceptionThrowingOutputStream = mock(OutputStream.class);
        doThrow(new IOException()).when(exceptionThrowingOutputStream).write(any(byte[].class));
        this.out = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        this.exceptionThrowingOutputStream.close();
        this.out.close();
    }

    @Test
    void run_NullOutputStream_ThrowsEchoException() {
        Throwable result = assertThrows(EchoException.class, () -> {
            app.run(new String[]{"A", "B", "C"}, null, null);
        });
        assertEquals(ECHO_EXCEPTION_MSG + ERR_NO_OSTREAM, result.getMessage());
    }

    @Test
    void run_IOExceptionWhenWritingByteBuffer_ThrowsEchoException() {
        Throwable result = assertThrows(EchoException.class, () -> {
            app.run(new String[]{"A", "B", "C"}, null, this.exceptionThrowingOutputStream);
        });
        assertEquals(ECHO_EXCEPTION_MSG + ERR_IO_EXCEPTION, result.getMessage());
    }

    @Test
    void run_ValidByteBuffers_PrintsCorrectValues() throws EchoException {
        // Given
        String[] tokens = new String[]{"Hello", "World!"};
        // When
        this.app.run(tokens, null, this.out);
        // Then
        assertEquals("Hello World!" + STRING_NEWLINE, this.out.toString());
    }
}
