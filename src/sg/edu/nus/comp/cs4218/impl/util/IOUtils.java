package sg.edu.nus.comp.cs4218.impl.util;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_CLOSING_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ShellException;

/**
 * IOUtils is a utility class that provides I/O methods for the shell apps to use.
 */
@SuppressWarnings("PMD.PreserveStackTrace")
public final class IOUtils {
    /**
     * Private constructor to prevent instantiation.
     */
    private IOUtils() { /* Does nothing */ }

    /**
     * Opens an inputStream based on the file name.
     *
     * @param fileName String containing file name.
     * @return InputStream of file opened.
     * @throws ShellException If file destination is inaccessible.
     */
    public static InputStream openInputStream(String fileName) throws ShellException {
        String resolvedFileName = resolveFilePath(fileName).toString();

        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(new File(resolvedFileName));
        } catch (FileNotFoundException e) {
            throw new ShellException(ERR_FILE_NOT_FOUND, e);
        }

        return fileInputStream;
    }

    /**
     * Opens an outputStream based on the file name.
     *
     * @param fileName String containing file name.
     * @return OutputStream of file opened.
     * @throws ShellException If file destination is inaccessible.
     */
    public static OutputStream openOutputStream(String fileName) throws ShellException, FileNotFoundException {
        String resolvedFileName = resolveFilePath(fileName).toString();

        FileOutputStream fileOutputStream;

        fileOutputStream = new FileOutputStream(new File(resolvedFileName));

        return fileOutputStream;
    }

    /**
     * Closes an inputStream. If inputStream provided is System.in or null, it will be ignored.
     *
     * @param inputStream InputStream to be closed.
     * @throws ShellException If inputStream cannot be closed successfully.
     */
    public static void closeInputStream(InputStream inputStream) throws ShellException {
        if (inputStream == null || inputStream.equals(System.in)) {
            return;
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            throw new ShellException(ERR_CLOSING_STREAMS, e);
        }
    }

    /**
     * Closes an outputStream. If outputStream provided is System.out or null, it will be ignored.
     *
     * @param outputStream OutputStream to be closed.
     * @throws ShellException If outputStream cannot be closed successfully.
     */
    public static void closeOutputStream(OutputStream outputStream) throws ShellException {
        if (outputStream == null || outputStream.equals(System.out)) {
            return;
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            throw new ShellException(ERR_CLOSING_STREAMS, e);
        }
    }

    public static Path resolveFilePath(String fileName) {
        Path currentDirectory = Paths.get(Environment.currentDirectory);
        return currentDirectory.resolve(fileName);
    }

    /**
     * Returns a list of lines based on the given InputStream.
     *
     * @param input InputStream containing arguments from System.in or FileInputStream
     * @throws Exception
     */
    public static List<String> getLinesFromInputStream(InputStream input) throws IOException {
        List<String> output = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        while ((line = reader.readLine()) != null) {
            output.add(line);
        }
        return output;
    }
}
