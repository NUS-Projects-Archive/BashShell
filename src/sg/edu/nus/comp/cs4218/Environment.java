package sg.edu.nus.comp.cs4218;

/**
 * Environment is a class to store environment related variables.
 */
public final class Environment {

    /**
     * Java VM does not support changing the current working directory.
     * For this reason, we use Environment.currentDirectory instead.
     */
    public static volatile String currentDirectory = System.getProperty("user.dir");

    private Environment() { /* Does nothing */ }
}
