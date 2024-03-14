package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Application;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.app.CatApplication;
import sg.edu.nus.comp.cs4218.impl.app.CdApplication;
import sg.edu.nus.comp.cs4218.impl.app.CutApplication;
import sg.edu.nus.comp.cs4218.impl.app.EchoApplication;
import sg.edu.nus.comp.cs4218.impl.app.ExitApplication;
import sg.edu.nus.comp.cs4218.impl.app.GrepApplication;
import sg.edu.nus.comp.cs4218.impl.app.LsApplication;
import sg.edu.nus.comp.cs4218.impl.app.MkdirApplication;
import sg.edu.nus.comp.cs4218.impl.app.MvApplication;
import sg.edu.nus.comp.cs4218.impl.app.PasteApplication;
import sg.edu.nus.comp.cs4218.impl.app.RmApplication;
import sg.edu.nus.comp.cs4218.impl.app.SortApplication;
import sg.edu.nus.comp.cs4218.impl.app.TeeApplication;
import sg.edu.nus.comp.cs4218.impl.app.UniqApplication;
import sg.edu.nus.comp.cs4218.impl.app.WcApplication;

class ApplicationRunnerTest {

    private ApplicationRunner appRunner;

    @BeforeEach
    void setUp() {
        appRunner = new ApplicationRunner();
    }

    @Test
    void createApp_EchoCommand_CreateEchoApplication() {
        Application app = assertDoesNotThrow(() -> appRunner.createApp("echo"));
        assertEquals(EchoApplication.class, app.getClass());
    }

    @Test
    void createApp_CdCommand_CreateCdApplication() {
        Application app = assertDoesNotThrow(() -> appRunner.createApp("cd"));
        assertEquals(CdApplication.class, app.getClass());
    }

    @Test
    void createApp_WcCommand_CreateWcApplication() {
        Application app = assertDoesNotThrow(() -> appRunner.createApp("wc"));
        assertEquals(WcApplication.class, app.getClass());
    }

    @Test
    void createApp_MkdirCommand_CreateMkdirApplication() {
        Application app = assertDoesNotThrow(() -> appRunner.createApp("mkdir"));
        assertEquals(MkdirApplication.class, app.getClass());
    }

    @Test
    void createApp_SortCommand_CreateSortApplication() {
        Application app = assertDoesNotThrow(() -> appRunner.createApp("sort"));
        assertEquals(SortApplication.class, app.getClass());
    }

    @Test
    void createApp_CatCommand_CreateCatApplication() {
        Application app = assertDoesNotThrow(() -> appRunner.createApp("cat"));
        assertEquals(CatApplication.class, app.getClass());
    }

    @Test
    void createApp_ExitCommand_CreateExitApplication() {
        Application app = assertDoesNotThrow(() -> appRunner.createApp("exit"));
        assertEquals(ExitApplication.class, app.getClass());
    }

    @Test
    void createApp_LsCommand_CreateLsApplication() {
        Application app = assertDoesNotThrow(() -> appRunner.createApp("ls"));
        assertEquals(LsApplication.class, app.getClass());
    }

    @Test
    void createApp_PasteCommand_CreatePasteApplication() {
        Application app = assertDoesNotThrow(() -> appRunner.createApp("paste"));
        assertEquals(PasteApplication.class, app.getClass());
    }

    @Test
    void createApp_UniqCommand_CreateUniqApplication() {
        Application app = assertDoesNotThrow(() -> appRunner.createApp("uniq"));
        assertEquals(UniqApplication.class, app.getClass());
    }

    @Test
    void createApp_MvCommand_CreateMvApplication() {
        Application app = assertDoesNotThrow(() -> appRunner.createApp("mv"));
        assertEquals(MvApplication.class, app.getClass());
    }

    @Test
    void createApp_CutCommand_CreateCutApplication() {
        Application app = assertDoesNotThrow(() -> appRunner.createApp("cut"));
        assertEquals(CutApplication.class, app.getClass());
    }

    @Test
    void createApp_RmCommand_CreateRmApplication() {
        Application app = assertDoesNotThrow(() -> appRunner.createApp("rm"));
        assertEquals(RmApplication.class, app.getClass());
    }

    @Test
    void createApp_TeeCommand_CreateTeeApplication() {
        Application app = assertDoesNotThrow(() -> appRunner.createApp("tee"));
        assertEquals(TeeApplication.class, app.getClass());
    }

    @Test
    void createApp_GrepCommand_CreateGrepApplication() {
        Application app = assertDoesNotThrow(() -> appRunner.createApp("grep"));
        assertEquals(GrepApplication.class, app.getClass());
    }

    @Test
    void createApp_InvalidCommand_ThrowsShellException() {
        ShellException result = assertThrowsExactly(ShellException.class, () ->
                appRunner.createApp("invalid")
        );
        assertEquals("shell: invalid: Invalid app", result.getMessage());
    }
}
