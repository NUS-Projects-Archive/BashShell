package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.exception.TeeException;

@SuppressWarnings("PMD.ClassNamingConventions")
public class TeeApplicationIT {

    private TeeApplication app;

    @BeforeEach
    void setUp() throws IOException {
        this.app = new TeeApplication();
    }

    @Test
    void run_NullOutputStream_ThrowsTeeException() {
        String expectedMsg = "tee: Null arguments";
        TeeException exception = assertThrowsExactly(TeeException.class, () -> {
            String[] args = {};
            InputStream mockedStdin = mock(InputStream.class);
            app.run(args, mockedStdin, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_OnlyInvalidArgs_ThrowsTeeException() {
        String expectedMsg = "tee: illegal option -- A";
        TeeException exception = assertThrowsExactly(TeeException.class, () -> {
            String[] args = {"-A"};
            InputStream mockedStdin = mock(InputStream.class);
            doThrow(new IOException()).when(mockedStdin).read(any(byte[].class));
            OutputStream mockedStdout = mock(OutputStream.class);
            app.run(args, mockedStdin, mockedStdout);
        });

        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_InsufficientArgs_ThrowsTeeException() {
        String expectedMsg = "tee: Insufficient arguments";
        TeeException exception = assertThrowsExactly(TeeException.class, () -> {
            String[] args = {"-a"};
            app.run(args, null, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_NoStdout_ThrowsTeeException() {
        String expectedMsg = "tee: OutputStream not provided";
        TeeException exception = assertThrowsExactly(TeeException.class, () -> {
            String[] args = {"-a"};
            InputStream mockedStdin = mock(InputStream.class);
            app.run(args, mockedStdin, null);
        });
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    void run_ValidArgs_DoesNotThrowException() {
        assertDoesNotThrow(() -> {
            String[] args = {"-a"};
            InputStream mockedStdin = mock(InputStream.class);
            OutputStream mockedStdout = mock(OutputStream.class);
            app.run(args, mockedStdin, mockedStdout);
        });
    }
}
