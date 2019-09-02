package engine.util;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;

public abstract class Resources {

    /**
     * Loads the contents of the file at the given URL into an array of bytes.
     * Throws an IOException if we failed to load the file.
     * Throws a NullPointerException if the given URL is null.
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
     * Throws a NullPointerException if the given URL is null.
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

    /**
     * Loads the contents of the file at the given URL into an XML Document.
     * Throws an IOException if we failed to load the file.
     * Throws a NullPointerException if the given URL is null.
     *
     * @param url The URL to load from
     * @return An XML Document built from the document
     */
    public static Document loadXML(URL url) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            var doc = db.parse(url.openStream());
            doc.getDocumentElement().normalize();
            return doc;
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
