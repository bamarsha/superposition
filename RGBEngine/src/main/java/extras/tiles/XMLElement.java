package extras.tiles;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class XMLElement {

    final Element element;

    XMLElement(Element element) {
        this.element = element;
    }

    public <T extends XMLElement> List<T> elementList(String name, Function<Element, T> func) {
        var list = element.getElementsByTagName(name);
        var r = new ArrayList<T>(list.getLength());
        for (int i = 0; i < list.getLength(); i++) {
            r.add(func.apply((Element) list.item(i)));
        }
        return Collections.unmodifiableList(r);
    }

    public <T extends XMLElement> List<T> elementListIndirect(String name1, String name2, Function<Element, T> func) {
        var list1 = element.getElementsByTagName(name1);
        if (list1.getLength() == 0) {
            return Arrays.asList();
        }
        if (list1.getLength() != 1) {
            throw new IllegalArgumentException(String.format("Too many subelements with tag %s", name1));
        }
        var child = (Element) list1.item(0);
        var list2 = child.getElementsByTagName(name2);
        var r = new ArrayList<T>(list2.getLength());
        for (int i = 0; i < list2.getLength(); i++) {
            r.add(func.apply((Element) list2.item(i)));
        }
        return Collections.unmodifiableList(r);
    }

    public <T extends XMLElement> T elementSingle(String name, Function<Element, T> func) {
        var list = element.getElementsByTagName(name);
        if (list.getLength() != 1) {
            throw new IllegalArgumentException(String.format("Wrong number of subelements with tag %s", name));
        }
        return func.apply((Element) list.item(0));
    }

    public int intAttr(String name) {
        if (!element.hasAttribute(name)) {
            throw new IllegalArgumentException("Attribute doesn't exist");
        }
        return Integer.parseInt(element.getAttribute(name));
    }

    public int intAttrDefault(String name, int defaultValue) {
        if (!element.hasAttribute(name)) {
            return defaultValue;
        }
        return Integer.parseInt(element.getAttribute(name));
    }

    public String stringAttr(String name) {
        if (!element.hasAttribute(name)) {
            throw new IllegalArgumentException(String.format("Attribute %s doesn't exist", name));
        }
        return element.getAttribute(name);
    }

    public String stringAttrDefault(String name, String defaultValue) {
        if (!element.hasAttribute(name)) {
            return defaultValue;
        }
        return element.getAttribute(name);
    }

    public String value() {
        var children = element.getChildNodes();
        if (children.getLength() != 1) {
            throw new IllegalArgumentException("Node has the wrong number of children");
        }
        var child = children.item(0);
        if (!(child instanceof Text)) {
            throw new IllegalArgumentException("Child node is not text");
        }
        return child.getNodeValue();
    }
}
