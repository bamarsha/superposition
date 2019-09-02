package engine.util;

import java.io.IOException;
import java.net.URL;

public abstract class Resources {

    /**
     * Loads the contents of the file at the given URL into an array of bytes.
     * Throws an IOException if we failed to load the file.
     * Throws a NullPointerException if the given url is null.
     *
     * @param url The URL to load from
     * @return The array of bytes in the file
     */
    public static byte[] loadBytes(URL url) {
        try {
            return url.openStream().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the contents of the file at the given URL into a String.
     * Throws an IOException if we failed to load the file.
     * Throws a NullPointerException if the given url is null.
     *
     * @param url The URL to load from
     * @return The text in the file
     */
    public static String loadString(URL url) {
        return new String(loadBytes(url));
    }

    /**
     * Loads the contents of the file at the given URL into a String.
     * Returns null if the given URL is null.
     * Throws an IOException if we failed to load the file.
     *
     * @param url The URL to load from
     * @return The text in the file, or null if the given URL is null
     */
    public static String loadStringNullable(URL url) {
        return url == null ? null : loadString(url);
    }
}
