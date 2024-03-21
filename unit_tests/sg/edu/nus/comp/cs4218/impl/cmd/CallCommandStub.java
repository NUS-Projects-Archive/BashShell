package sg.edu.nus.comp.cs4218.impl.cmd;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_APP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

public class CallCommandStub extends CallCommand {

    private final List<String> argsList;

    public CallCommandStub(List<String> argsList) {
        super(null, null, null); // not used
        this.argsList = argsList;
    }

    public CallCommandStub(String... args) {
        this(Arrays.asList(args));
    }

    @Override
    public void evaluate(InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException, ShellException {
        try {
            if (matchArgsListExactly("lsa")) {
                throw new ShellException("lsa: " + ERR_INVALID_APP);
            } else if (matchArgsListExactly("echo", "hello", "world")) {
                stdout.write("hello world".getBytes());
            } else if (matchArgsListExactly("paste", "ghost.txt")) {
                String content = "Line# 1" + STRING_NEWLINE + "Line# 2" + STRING_NEWLINE + "Total lines: 2";
                stdout.write(content.getBytes());
            } else if (matchArgsListExactly("grep", "Line#")) {
                mockGrep("Line#", stdin, stdout);
            } else if (matchArgsListExactly("grep", "2")) {
                mockGrep("2", stdin, stdout);
            } else if (matchArgsListExactly("cat", "nonExistFile")) {
                throw new CatException("'nonExistFile.txt': No such file or directory");
            } else {
                throw new RuntimeException("CallCommandStub: case not mocked");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void terminate() {
        // Stub: not implemented
    }

    @Override
    public List<String> getArgsList() {
        return argsList;
    }

    private boolean matchArgsListExactly(String... arguments) {
        if (argsList.size() != arguments.length) {
            return false;
        }

        for (int i = 0; i < arguments.length; i++) {
            if (argsList.get(i).compareTo(arguments[i]) != 0) {
                return false;
            }
        }

        return true;
    }

    private void mockGrep(CharSequence sequence, InputStream stdin, OutputStream stdout) {
        String line;
        StringBuilder results = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stdin))) {
            while ((line = reader.readLine()) != null) {
                if (line.contains(sequence)) {
                    results.append(line).append(STRING_NEWLINE);
                }
            }
            stdout.write(results.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
