package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.CatInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FLAG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;

public class CatApplication implements CatInterface {
    public static final String ERR_WRITE_STREAM = "Could not write to output stream";

    /**
     * Runs the cat application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws CatException If the file(s) specified do not exist or are unreadable.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CatException {
        final CatArgsParser parser = new CatArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new CatException(ERR_INVALID_FLAG + " " + e.getMessage());
        }
        final Boolean isLineNumber = parser.isLineNumber();
        final List<String> nonFlagArgs = parser.getNonFlagArgs();
        String output;

        if (nonFlagArgs.isEmpty()) {
            // cat, cat -n
            output = catStdin(isLineNumber, stdin);
            try {
                stdout.write(output.getBytes());
            } catch (IOException e) {
                throw new CatException(ERR_WRITE_STREAM + " " + e.getMessage());
            }
        }  else {
            // all other cases
            IORedirectionHandler redirHandler = new IORedirectionHandler(nonFlagArgs, stdin, stdout, new ArgumentResolver());
            try {
                redirHandler.extractRedirOptions();
            } catch (ShellException | FileNotFoundException | AbstractApplicationException e) {
                throw new CatException(e.getMessage());
            }
            List<String> noRedirArgsList = redirHandler.getNoRedirArgsList();
            String[] noRedirArgsArray = noRedirArgsList.toArray(String[]::new);
            try {
                if (noRedirArgsList.size() > 0) {
                    // Input redirect is not relevant
                    if (nonFlagArgs.contains("-")) {
                        output = catFileAndStdin(isLineNumber, stdin, noRedirArgsArray);
                    } else {
                        output = catFiles(isLineNumber, noRedirArgsArray);
                    }
                } else {
                    // Input redirect cannot be ignored, get from redirect handler
                    output = catStdin(isLineNumber, redirHandler.getInputStream());
                }
                byte[] bytes = output.getBytes();
                if (nonFlagArgs.contains(">")) {
                    // write to file
                    redirHandler.getOutputStream().write(bytes);
                } else {
                    // no Output redirect, write to shell
                    stdout.write(bytes);
                }
            } catch (IOException e) {
                throw new CatException(ERR_WRITE_STREAM + " " + e.getMessage());
            }
        }
    }

    @Override
    public String catFiles(Boolean isLineNumber, String... fileNames) throws CatException {
        StringBuilder result = new StringBuilder();
        for (String fileName : fileNames) {
            try {
                if (fileName.contains("*")) {
                    List<String> globbedFiles = search(Paths.get(Environment.currentDirectory), fileName);
                    for (String globbedFile : globbedFiles) {
                        result.append(readFile(isLineNumber, new File(globbedFile))).append(StringUtils.STRING_NEWLINE);
                    }
                } else {
                    result.append(readFile(isLineNumber, new File(fileName)));
                }
            } catch (IOException e) {
                throw new CatException(ERR_IO_EXCEPTION + " " + e.getMessage());
            }
        }
        return result.toString();
    }

    @Override
    public String catStdin(Boolean isLineNumber, InputStream stdin) throws CatException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stdin));
        return readStdIn(isLineNumber, bufferedReader);
    }

    @Override
    public String catFileAndStdin(Boolean isLineNumber, InputStream stdin, String... fileNames) throws CatException {
        StringBuilder result = new StringBuilder();
        for (String fileName : fileNames) {
            try {
                if (fileName.equals("-")) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stdin));
                    result.append(readStdIn(isLineNumber, bufferedReader));
                } else if (fileName.contains("*")) {
                    List<String> globbedFiles = search(Paths.get(Environment.currentDirectory), fileName);
                    for (String globbedFile : globbedFiles) {
                        result.append(readFile(isLineNumber, new File(globbedFile))).append(StringUtils.STRING_NEWLINE);
                    }
                } else {
                    result.append(readFile(isLineNumber, new File(fileName)));
                }
            } catch (IOException e) {
                throw new CatException(ERR_IO_EXCEPTION + " " + e.getMessage());
            }
        }
        return result.toString();
    }

    public String readStdIn(Boolean isLineNumber, BufferedReader bufferedReader) throws CatException {
        StringBuilder userInput = new StringBuilder();
        String line;
        int lineNumber = 1;
        // Read lines from stdin until an empty line is encountered
        while (true) {
            try {
                if (!((line = bufferedReader.readLine()) != null && !line.isEmpty())) {
                    break;
                } else if (userInput.length() > 0) {
                    userInput.append(StringUtils.STRING_NEWLINE);
                }
            } catch (IOException e) {
                throw new CatException(ERR_IO_EXCEPTION);
            }
            if (isLineNumber) {
                userInput.append(lineNumber).append(' ');
                lineNumber++;
            }
            userInput.append(line);
        }
        return userInput.append(StringUtils.STRING_NEWLINE).toString();
    }

    public String readFile(Boolean isLineNumber, File file) throws IOException, CatException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                if (isLineNumber) {
                    content.append(lineNumber).append(' ');
                    lineNumber++;
                }
                content.append(line).append(StringUtils.STRING_NEWLINE);
            }
        } catch (FileNotFoundException e) {
            throw new CatException(ERR_FILE_NOT_FOUND);
        }
        return content.toString();
    }

    public List<String> search(Path rootDir, String pattern) throws IOException, CatException {
        List<String> matchesList = new ArrayList<>();
        FileVisitor<Path> matcherVisitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attribs) {
                FileSystem fileSystem = FileSystems.getDefault();
                PathMatcher matcher = fileSystem.getPathMatcher(pattern);
                Path name = file.getFileName();
                if (matcher.matches(name)) {
                    matchesList.add(name.toString());
                }
                return FileVisitResult.CONTINUE;
            }
        };
        try {
            Files.walkFileTree(rootDir, matcherVisitor);
        } catch (SecurityException e) {
            throw new CatException(ERR_NO_PERM + " " + e.getMessage());
        }
        return matchesList;
    }
}