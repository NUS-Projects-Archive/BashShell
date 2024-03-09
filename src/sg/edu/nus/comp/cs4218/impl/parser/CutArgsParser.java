package sg.edu.nus.comp.cs4218.impl.parser;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

/**
 * CutArgsParser is a class to parse the arguments for cut command.
 */
public class CutArgsParser extends ArgsParser {

    private static final char FLAG_CHAR_POS = 'c';
    private static final char FLAG_BYTE_POS = 'b';
    public static final String NO_FLAG_MSG = "You must specify either cut by character or byte";
    public static final String TOO_MANY_FLAG_MSG = "Only one flag can be selected";

    public CutArgsParser() {
        super();
        legalFlags.add(FLAG_CHAR_POS);
        legalFlags.add(FLAG_BYTE_POS);
    }

    public Boolean isCharPo() {
        return flags.contains(FLAG_CHAR_POS);
    }

    public Boolean isBytePo() {
        return flags.contains(FLAG_BYTE_POS);
    }

    public List<int[]> getRangeList() throws InvalidArgsException {
        return convertToRangeList(nonFlagArgs.get(0));
    }

    public List<String> getFileNames() {
        return nonFlagArgs.subList(1, nonFlagArgs.size());
    }

    @Override
    public void validateArgs() throws InvalidArgsException {
        super.validateArgs(); // checks for any illegal flags

        // No flags (options) is selected
        if (isNoFlagsSelected()) {
            throw new InvalidArgsException(NO_FLAG_MSG);
        }

        // More than 1 flags (options) is selected
        if (isBothFlagsSelected()) {
            throw new InvalidArgsException(TOO_MANY_FLAG_MSG);
        }

        // No range is given
        if (getRangeList().isEmpty()) {
            throw new InvalidArgsException(ERR_SYNTAX);
        }
    }

    private Boolean isNoFlagsSelected() {
        return !isCharPo() && !isBytePo();
    }

    private Boolean isBothFlagsSelected() {
        return isCharPo() && isBytePo();
    }

    /**
     * Converts the input string into a list of integer arrays representing ranges.
     * <p>
     * Assumptions:
     * - Empty characters are assumed at the start or end of a comma if there are no other characters
     * - Empty character are assumed in between two consecutive commas
     * - There cannot be more than one hyphen in between two commas
     * - The start and end values of a hyphen must be provided and cannot be empty
     *
     * @param input The input string to be converted
     * @return A list of integer arrays representing the ranges
     * @throws InvalidArgsException If there is an error converting the input
     */
    private List<int[]> convertToRangeList(String input) throws InvalidArgsException {
        List<int[]> rangeList = new ArrayList<>();

        String[] ranges = input.split(",", -1); // negative limit to not discard trailing empty fields
        for (String range : ranges) {
            // Check if the range contains only digits and hyphens
            if (!range.matches("^[0-9-]*$")) {
                throw new InvalidArgsException(String.format("invalid byte/character position: '%s'", range));
            }

            // Check if the range is empty
            if (range.isEmpty()) {
                throw new InvalidArgsException("byte/character positions are numbered from 1");
            }

            // Check if the range is equal to (only) hyphen
            if ("-".equals(range)) {
                throw new InvalidArgsException(String.format("invalid range with no endpoint: '%s'", range));
            }

            if (range.contains("-")) {
                String[] parts = range.split("-");
                if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
                    throw new InvalidArgsException(String.format("invalid range format: '%s'", range));
                }

                int start = Integer.parseInt(parts[0]);
                int end = Integer.parseInt(parts[1]);

                if (start < 1) {
                    throw new InvalidArgsException("byte/character positions are numbered from 1");
                }
                if (start > end) {
                    throw new InvalidArgsException(String.format("invalid decreasing range: '%s'", range));
                }

                rangeList.add(new int[]{start, end});
            } else {
                int singleValue = Integer.parseInt(range);
                if (Integer.parseInt(range) < 1) {
                    throw new InvalidArgsException("byte/character positions are numbered from 1");
                }
                rangeList.add(new int[]{singleValue, singleValue});
            }
        }
        rangeList.sort(Comparator.comparingInt(arr -> arr[0]));

        return rangeList;
    }
}
