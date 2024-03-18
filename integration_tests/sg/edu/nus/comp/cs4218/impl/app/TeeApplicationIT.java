package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.TeeException;

@SuppressWarnings("PMD.ClassNamingConventions")
public class TeeApplicationIT {

    private TeeApplication app;

    @BeforeEach
    void setUp() throws IOException {
        app = new TeeApplication();
    }

    @Test
    void run_NullOutputStream_ThrowsTeeException() {
        String[] args = {};
        TeeException result = assertThrowsExactly(TeeException.class, () -> {
            InputStream mockedStdin = mock(InputStream.class);
            app.run(args, mockedStdin, null);
        });
        String expected = "tee: OutputStream not provided";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_NullInputStream_ThrowsTeeException() {
        String[] args = {"-a"};
        TeeException result = assertThrowsExactly(TeeException.class, () -> app.run(args, null, mock(OutputStream.class)));
        String expected = "tee: InputStream not provided";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_OnlyInvalidArgs_ThrowsTeeException() {
        String[] args = {"-A"};
        TeeException result = assertThrowsExactly(TeeException.class, () -> {
            InputStream mockedStdin = mock(InputStream.class);
            doThrow(new IOException()).when(mockedStdin).read(any(byte[].class));
            OutputStream mockedStdout = mock(OutputStream.class);
            app.run(args, mockedStdin, mockedStdout);
        });

        String expected = "tee: illegal option -- A";
        assertEquals(expected, result.getMessage());
    }

    @Test
    void run_ValidArgs_DoesNotThrowException() {
        String[] args = {"-a"};
        assertDoesNotThrow(() -> {
            InputStream mockedStdin = new ByteArrayInputStream("".getBytes());
            OutputStream mockedStdout = mock(OutputStream.class);
            app.run(args, mockedStdin, mockedStdout);
        });
    }
}
