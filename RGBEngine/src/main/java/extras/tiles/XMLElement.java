package extras.tiles;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    public <T extends XMLElement, U> Map<U, T> elementMap(String name1, String name2, Function<Element, T> func, Function<T, U> keyFunc) {
        var list1 = directChildrenWithTagName(element, name1);
        if (list1.size() == 0) {
            return new HashMap<>();
        }
        if (list1.size() != 1) {
            throw new IllegalArgumentException(String.format("Too many subelements with tag %s", name1));
        }
        var child = (Element) list1.get(0);
        var list2 = child.getElementsByTagName(name2);
        var r = new HashMap<U, T>();
        for (int i = 0; i < list2.getLength(); i++) {
            var t = func.apply((Element) list2.item(i));
            r.put(keyFunc.apply(t), t);
        }
        return Collections.unmodifiableMap(r);
    }

    public <T extends XMLElement> T elementSingle(String name, Function<Element, T> func) {
        var list = element.getElementsByTagName(name);
        if (list.getLength() != 1) {
            throw new IllegalArgumentException(String.format("Wrong number of subelements with tag %s", name));
        }
        return func.apply((Element) list.item(0));
    }

    public double doubleAttr(String name) {
        if (!element.hasAttribute(name)) {
            throw new IllegalArgumentException("Attribute doesn't exist");
        }
        return Double.parseDouble(element.getAttribute(name));
    }

    public double doubleAttrDefault(String name, double defaultValue) {
        if (!element.hasAttribute(name)) {
            return defaultValue;
        }
        return Double.parseDouble(element.getAttribute(name));
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

    private List<Node> directChildrenWithTagName(Element element, String name) {
        return nodeStream(element.getChildNodes())
                .filter(node -> node.getNodeName().equals(name))
                .collect(Collectors.toList());
    }

    private Stream<Node> nodeStream(NodeList nodeList) {
        return StreamSupport.stream(nodeIterable(nodeList).spliterator(), false);
    }

    private Iterable<Node> nodeIterable(NodeList nodeList) {
        return () -> nodeIterator(nodeList);
    }

    private Iterator<Node> nodeIterator(NodeList nodeList) {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < nodeList.getLength();
            }

            @Override
            public Node next() {
                return nodeList.item(index++);
            }
        };
    }
}
