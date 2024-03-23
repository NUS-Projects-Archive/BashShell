package sg.edu.nus.comp.cs4218.impl.util;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_ASTERISK;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import sg.edu.nus.comp.cs4218.Environment;

/**
 * RegexArgument is a class to handle regex matching and globbing.
 */
@SuppressWarnings("PMD.AvoidStringBufferField")
public final class RegexArgument {
    private final StringBuilder plaintext;
    private final StringBuilder regex;
    private boolean hasAsterisk;

    /**
     * Constructor for RegexArgument without arguments. Initializes plaintext, regex, and hasAsterisk.
     */
    public RegexArgument() {
        this.plaintext = new StringBuilder();
        this.regex = new StringBuilder();
        this.hasAsterisk = false;
    }

    /**
     * Constructor for RegexArgument with a string argument. Initializes plaintext, regex, and hasAsterisk.
     * @param str String as the starting pattern of regex
     */
    public RegexArgument(String str) {
        this();
        merge(str);
    }

    /**
     * Constructor for RegexArgument with two string and a boolean argument. Initializes plaintext, regex, and hasAsterisk.
     * Used for `find` command. `text` here corresponds to the folder that we want to look in.
     * 
     * @param str           The string to match
     * @param text          The folder to look in
     * @param hasAsterisk   Boolean to indicate if the string contains an asterisk
     */
    public RegexArgument(String str, String text, boolean hasAsterisk) {
        this();
        this.plaintext.append(text);
        this.hasAsterisk = hasAsterisk;
        this.regex.append(".*"); // We want to match filenames
        for (char c : str.toCharArray()) {
            if (c == CHAR_ASTERISK) {
                this.regex.append("[^").append(StringUtils.fileSeparator()).append("]*");
            } else {
                this.regex.append(Pattern.quote(String.valueOf(c)));
            }
        }
    }

    /**
     * Appends a character to the plaintext and regex.
     * 
     * @param chr  The character to append
     */
    public void append(char chr) {
        plaintext.append(chr);
        regex.append(Pattern.quote(String.valueOf(chr)));
    }

    /**
     * Appends an asterisk to the plaintext and regex.
     * Updates hasAsterisk to true.
     */
    public void appendAsterisk() {
        plaintext.append(CHAR_ASTERISK);
        regex.append("[^").append(StringUtils.fileSeparator()).append("]*");
        hasAsterisk = true;
    }

    /**
     * Merges the given RegexArgument with the current plaintext and regex.
     * Updates hasAsterisk to true if either the current or the given RegexArgument has an asterisk.
     * 
     * @param other The RegexArgument to merge with
     */
    public void merge(RegexArgument other) {
        plaintext.append(other.plaintext);
        regex.append(other.regex);
        hasAsterisk = this.hasAsterisk || other.hasAsterisk;
    }

    /**
     * Merges the given string with the current plaintext and regex.
     * 
     * @param str The string to merge with
     */
    public void merge(String str) {
        plaintext.append(str);
        regex.append(Pattern.quote(str));
    }

    /**
     * Returns a list of matching file paths if the string contains an asterisk, 
     * or the plaintext if no files match or the string does not contain an asterisk.
     *
     * @return A List of files that matches the file path
     */
    public List<String> globFiles() {
        List<String> globbedFiles = new LinkedList<>();

        if (hasAsterisk) {
            Pattern regexPattern = Pattern.compile(regex.toString());
            String dir;
            String[] tokens = plaintext.toString().replaceAll("\\\\", "/").split("/");
            StringBuilder dirBuilder = new StringBuilder();
            for (int i = 0; i < tokens.length - 1; i++) {
                dirBuilder.append(tokens[i]).append(File.separator);
            }
            dir = dirBuilder.toString();

            File currentDir = Paths.get(Environment.currentDirectory + File.separator + dir).toFile();
            String[] files = currentDir.list();

            if (files != null) {
                for (String candidate : files) {
                    // Replace any platform-specific File.separator with forward slash '/'
                    // Ensure compatibility with regexPattern, which matches directory separators using forward slash '/'
                    candidate = (dir + candidate).replace(File.separator, "/");
                    if (regexPattern.matcher(candidate).matches()) {
                        globbedFiles.add(candidate);
                    }
                }
            }

            Collections.sort(globbedFiles);
        }

        if (globbedFiles.isEmpty()) {
            globbedFiles.add(plaintext.toString());
        }

        return globbedFiles;
    }

    /**
     * Returns whether the string is a regex.
     * 
     * @return True if the string is a regex; otherwise false
     */
    public boolean isRegex() {
        return hasAsterisk;
    }

    /**
     * Returns whether the string is empty.
     * 
     * @return True if the string is empty; otherwise false
     */
    public boolean isEmpty() {
        return plaintext.length() == 0;
    }

    /**
     * Returns the plaintext.
     * 
     * @return {code plaintext of this {@code RegexArgument}
     */
    public String toString() {
        return plaintext.toString();
    }
}
