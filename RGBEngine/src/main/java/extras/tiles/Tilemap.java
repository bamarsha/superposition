package extras.tiles;

import engine.util.Resources;
import org.w3c.dom.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Tilemap extends XMLElement {

    public final int width = intAttr("width");
    public final int height = intAttr("height");
    public final int tileWidth = intAttr("tilewidth");
    public final int tileHeight = intAttr("tileheight");
    public final List<Tileset> tilesets = elementList("tileset", Tileset::new);
    public final List<Layer> layers = elementList("layer", Layer::new);
    public final List<ObjectGroup> objectGroups = elementList("objectgroup", ObjectGroup::new);

    private Tilemap(Element element) {
        super(element);
    }

    public static Tilemap load(URL url) {
        var doc = Resources.loadXML(url);
        var root = doc.getDocumentElement();
        return new Tilemap(root);
    }

    public class Tileset extends XMLElement {
        public final int firstGid = intAttr("firstgid");
        public final int tileCount = intAttr("tilecount");
        public final int columns = intAttr("columns");
        public final List<Image> images = elementList("image", Image::new);

        private Tileset(Element element) {
            super(element);
        }

        public class Image extends XMLElement {
            public final String source = stringAttr("source");
            public final int width = intAttr("width");
            public final int height = intAttr("height");

            private Image(Element element) {
                super(element);
            }
        }
    }

    public class Layer extends XMLElement {
        public final int id = intAttr("id");
        public final String name = stringAttr("name");
        public final int width = intAttr("width");
        public final int height = intAttr("height");
        public final int offsetX = intAttrDefault("offsetx", 0);
        public final int offsetY = intAttrDefault("offsety", 0);
        public final Map<String, Property> properties = elementMap("properties", "property", Property::new, p -> p.name);
        public final Data data = elementSingle("data", Data::new);

        private Layer(Element element) {
            super(element);
        }

        public class Property extends XMLElement {
            public final String name = stringAttr("name");
            public final String type = stringAttr("type");
            public final String value = stringAttr("value");

            private Property(Element element) {
                super(element);
            }
        }

        public class Data extends XMLElement {
            public final String encoding = stringAttr("encoding");
            public final int[][] tiles;

            private Data(Element element) {
                super(element);
                if (encoding.equals("csv")) {
                    tiles = new int[width][height];
                    String[] rows = value().substring(1).split("\n");
                    if (rows.length != height) {
                        throw new IllegalStateException("Data is badly formatted");
                    }
                    for (int y = 0; y < height; y++) {
                        String[] row = rows[height - 1 - y].split(",");
                        if (row.length != width) {
                            throw new IllegalStateException("Data is badly formatted");
                        }
                        for (int x = 0; x < width; x++) {

                            tiles[x][y] = (int) Long.parseLong(row[x]);
                        }
                    }
                } else {
                    throw new IllegalArgumentException(String.format("Unknown encoding %s", encoding));
                }
            }
        }
    }

    public class ObjectGroup extends XMLElement {
        public final int id = intAttr("id");
        public final String name = stringAttr("name");
        public final List<Object> objects = elementList("object", Object::new);

        private ObjectGroup(Element element) {
            super(element);
        }

        public class Object extends XMLElement {
            public final int id = intAttr("id");
            public final String name = stringAttr("name");
            public final String type = stringAttr("type");
            public final double x = doubleAttr("x");
            public final double y = doubleAttr("y");
            public final Map<String, Property> properties = elementMap("properties", "property", Property::new, p -> p.name);

            private Object(Element element) {
                super(element);
            }

            public class Property extends XMLElement {
                public final String name = stringAttr("name");
                public final String value = stringAttr("value");

                private Property(Element element) {
                    super(element);
                }
            }
        }
    }
}
