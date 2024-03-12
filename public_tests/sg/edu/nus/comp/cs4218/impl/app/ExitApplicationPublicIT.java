package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;

public class ExitApplicationPublicIT {

    public static final String EXIT_MSG = "exit: 0";

    @Test
    @ExpectSystemExitWithStatus(0)
    void run_NoArgument_ExitsSuccessfully() throws AbstractApplicationException {
        ExitApplication app = new ExitApplication();
        app.run(null, null, null);
    }
    @Test
    @ExpectSystemExitWithStatus(0)
    public void run_WithArguments_ExitsSuccessfully() throws AbstractApplicationException {
        ExitApplication app = new ExitApplication();
        app.run(new String[]{"anything", "here"}, System.in, System.out);
    }
}
