package sg.edu.nus.comp.cs4218.impl.util;

import java.util.List;

public final class CollectionsUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private CollectionsUtils() { /* Does nothing */ }

    /**
     * Converts a {@code List<String>} to {@code String[]}
     *
     * @param list {@code List} of {@code String}
     * @return A {@code String[]} with the values of the given {@code List<String>}
     */
    public static String[] listToArray(List<String> list) {
        return list == null ? null : list.toArray(new String[0]);
    }

}
