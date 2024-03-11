package sg.edu.nus.comp.cs4218.impl.app.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

/**
 * A helper class that provides functionality to cut out lines from InputStream.
 */
public final class CutApplicationHelper {

    private CutApplicationHelper() { /* Does nothing*/ }

    /**
     * Cuts selected portions from the lines read from the input stream based on the specified criteria.
     *
     * @param isCharPo Indicates whether the cut operation is performed based on character positions
     * @param isBytePo Indicates whether the cut operation is performed based on byte positions
     * @param ranges   List of integer arrays representing the start and end positions for each portion to cut
     * @param stdin    InputStream to read lines from
     * @return List of strings representing the cut portions
     * @throws IOException If an I/O error occurs while reading from the input stream
     */
    public static List<String> cutSelectedPortions(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, InputStream stdin)
            throws IOException {
        List<String> lines = IOUtils.getLinesFromInputStream(stdin);
        return lines.stream()
                .map(line -> cutLine(isCharPo, isBytePo, ranges, line))
                .collect(Collectors.toList());
    }

    /**
     * Cuts portions from the input line based on the specified criteria and ranges.
     *
     * @param isCharPo Indicates whether the cut operation is performed based on character positions
     * @param isBytePo Indicates whether the cut operation is performed based on byte positions
     * @param ranges   List of integer arrays representing the start and end positions for each portion to cut
     * @param line     The input line to cut portions from
     * @return A string representing the concatenated result of cutting portions from the input line
     */
    private static String cutLine(Boolean isCharPo, Boolean isBytePo, List<int[]> ranges, String line) {
        return ranges.stream()
                .map(range -> cutPortion(isCharPo, isBytePo, range, line))
                .collect(Collectors.joining());
    }

    /**
     * Cuts a portion from the input line based on the specified criteria and range.
     *
     * @param isCharPo Indicates whether the cut operation is performed based on character positions
     * @param isBytePo Indicates whether the cut operation is performed based on byte position
     * @param range    An integer array representing the start and end positions for the portion to cut
     * @param line     The input line to cut a portion from
     * @return A string representing the cut portion from the input line
     */
    private static String cutPortion(Boolean isCharPo, Boolean isBytePo, int[] range, String line) {
        int start = range[0] - 1; // 0-based index
        int end = range[1];

        if (start < 0 || start >= line.length()) {
            return "";
        }

        int limit = isCharPo
                ? Math.min(end, line.length())
                : Math.min(end, line.getBytes().length);

        if (isCharPo) {
            return line.substring(start, limit);
        } else if (isBytePo) {
            byte[] lineBytes = line.getBytes();
            return new String(lineBytes, start, limit - start);
        } else {
            throw new IllegalArgumentException("You must specify either cut by character or byte");
        }
    }
}
