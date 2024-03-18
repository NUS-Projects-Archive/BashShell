package sg.edu.nus.comp.cs4218.impl.app.helper;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_TAB;

import java.util.ArrayList;
import java.util.List;

public final class PasteApplicationHelper {

    private PasteApplicationHelper() { /* Does nothing*/ }

    /**
     * Takes in a List of Lists of Strings and merges the lists in serial.
     * Each inner list represents data from a file, where each element represents a line of data.
     * Columns within a row are separated by a tab character ('\t'), and rows are separated by a newline character ('\n').
     *
     * @param listOfFiles List of Lists of Strings representing the data from multiple files to be merged
     * @return Merged data as a single String where columns within a row are separated by a
     * tab character ('\t') and rows are separated by a newline character ('\n')
     */
    public static String mergeInSerial(List<List<String>> listOfFiles) {
        List<String> mergedLines = new ArrayList<>();
        for (List<String> files : listOfFiles) {
            mergedLines.add(String.join(STRING_TAB, files));
        }
        return String.join(STRING_NEWLINE, mergedLines);
    }

    /**
     * Merges lists in parallel, where each sublist corresponds to a column in the merged result.
     * If a sublist does not have an element at a particular index, an empty string is inserted.
     *
     * @param listOfFiles A List of Lists of Strings representing the data to be merged in parallel
     * @return A String representing the merged data with elements separated by tabs and rows separated by newlines
     */
    public static String mergeInParallel(List<List<String>> listOfFiles) {
        int maxFileLength = listOfFiles.stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);

        List<List<String>> parallelizedFiles = new ArrayList<>();
        for (int i = 0; i < maxFileLength; i++) {
            List<String> currIndexList = new ArrayList<>();
            for (List<String> file : listOfFiles) {
                currIndexList.add(i < file.size() ? file.get(i) : ""); // Empty string if the list do not have an element at index i
            }
            parallelizedFiles.add(currIndexList);
        }

        List<String> mergedLines = new ArrayList<>();
        for (List<String> files : parallelizedFiles) {
            mergedLines.add(String.join(STRING_TAB, files));
        }
        return String.join(STRING_NEWLINE, mergedLines);
    }
}
