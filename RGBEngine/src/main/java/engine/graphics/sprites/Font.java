package engine.graphics.sprites;

import engine.graphics.opengl.BufferObject;
import static engine.graphics.opengl.GLObject.bindAll;
import engine.graphics.opengl.Shader;
import engine.graphics.opengl.Texture;
import engine.graphics.opengl.VertexArrayObject;
import engine.util.Color;
import engine.util.Resources;
import engine.util.math.Transformation;
import engine.util.math.Vec2d;
import engine.util.math.Vec3d;
import static java.lang.Integer.parseInt;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

public class Font {

    private static final Map<String, Font> FONT_CACHE = new HashMap();

    public static Font load(String fileName) {
        if (!FONT_CACHE.containsKey(fileName)) {
            Font s = new Font(fileName);
            FONT_CACHE.put(fileName, s);
        }
        return FONT_CACHE.get(fileName);
    }

    private static final Shader FONT_SHADER = Shader.load("sprite", "font");

    private Texture[] textures;
    private final Map<Integer, FontChar> charMap = new HashMap();
    private final Map<String, FontText> textMap = new HashMap();

    // See http://www.angelcode.com/products/bmfont/doc/file_format.html
    // Info
    private String face;
    private int size;
    private boolean bold;
    private boolean italic;
    private int stretchH;
    private boolean smooth;
    private int aa;
    private int paddingUp, paddingRight, paddingDown, paddingLeft;
    private int spacingHor, spacingVer;
    private int outline;
    // Common
    private int lineHeight;
    private int base;
    private int scaleW, scaleH;
    private int pages;

    private Font(String name) {
        String[] fontDesc = Resources.loadFileAsString("fonts/" + name + ".fnt").split("[\r\n]+");
        for (String line : fontDesc) {
            Map<String, String> nvp = parseNameValuePairs(line);
            switch (parseTag(line)) {
                case "info":
                    face = nvp.get("face");
                    size = parseInt(nvp.get("size"));
                    bold = nvp.get("bold").equals("1");
                    italic = nvp.get("italic").equals("1");
                    stretchH = parseInt(nvp.get("stretchH"));
                    smooth = nvp.get("smooth").equals("1");
                    aa = parseInt(nvp.get("aa"));
                    paddingUp = parseInt(nvp.get("padding").split(",")[0]);
                    paddingRight = parseInt(nvp.get("padding").split(",")[1]);
                    paddingDown = parseInt(nvp.get("padding").split(",")[2]);
                    paddingLeft = parseInt(nvp.get("padding").split(",")[3]);
                    spacingHor = parseInt(nvp.get("spacing").split(",")[0]);
                    spacingVer = parseInt(nvp.get("spacing").split(",")[1]);
                    outline = parseInt(nvp.get("outline"));
                    break;
                case "common":
                    lineHeight = parseInt(nvp.get("lineHeight"));
                    base = parseInt(nvp.get("base"));
                    scaleW = parseInt(nvp.get("scaleW"));
                    scaleH = parseInt(nvp.get("scaleH"));
                    pages = parseInt(nvp.get("pages"));
                    textures = new Texture[pages];
                    break;
                case "page":
                    int id = parseInt(nvp.get("id"));
                    String file = nvp.get("file");
                    textures[id] = Texture.load("../fonts/" + file);
                    break;
                case "chars":
                    break;
                case "char":
                    FontChar c = new FontChar();
                    c.id = parseInt(nvp.get("id"));
                    c.x = parseInt(nvp.get("x"));
                    c.y = parseInt(nvp.get("y"));
                    c.width = parseInt(nvp.get("width"));
                    c.height = parseInt(nvp.get("height"));
                    c.xoffset = parseInt(nvp.get("xoffset"));
                    c.yoffset = parseInt(nvp.get("yoffset"));
                    c.xadvance = parseInt(nvp.get("xadvance"));
                    c.page = parseInt(nvp.get("page"));
                    charMap.put(c.id, c);
                    break;
                case "kernings":
                    break;
                case "kerning":
                    int first = parseInt(nvp.get("first"));
                    int second = parseInt(nvp.get("second"));
                    int amount = parseInt(nvp.get("amount"));
                    charMap.get(first).kernings.put(second, amount);
                    break;
                default:
                    throw new RuntimeException("Unknown tag: " + parseTag(line));
            }
        }
    }

    private Map<String, String> parseNameValuePairs(String line) {
        HashMap<String, String> r = new HashMap();
        for (String pair : line.split(" +")) {
            if (pair.contains("=")) {
                String[] parts = pair.split("=");
                r.put(parts[0], parts[1].replace("\"", ""));
            }
        }
        return r;
    }

    private String parseTag(String line) {
        return line.substring(0, line.indexOf(" "));
    }

    public FontText renderText(String text) {
        if (text == null || text.isEmpty()) {
            throw new RuntimeException("Invalid text");
        }
        if (!textMap.containsKey(text)) {
            List<float[]>[] data = new List[pages];
            FontChar prev = null;
            Vec2d cursor = new Vec2d(0, 0);
            for (int c : text.toCharArray()) {
                if (prev != null) {
                    cursor = cursor.add(new Vec2d(prev.getAdvance(c), 0));
                }
                FontChar fc = charMap.get(c);
                if (fc == null) {
                    throw new RuntimeException("Missing character: " + (char) c);
                }
                if (data[fc.page] == null) {
                    data[fc.page] = new LinkedList();
                }
                float x = (float) cursor.x + fc.xoffset;
                float y = (float) cursor.y + lineHeight / 2f - fc.yoffset - fc.height;
                data[fc.page].add(new float[]{
                    x, y, 0, fc.x / 256f, 1 - (fc.y + fc.height) / 256f,
                    x + fc.width, y, 0, (fc.x + fc.width) / 256f, 1 - (fc.y + fc.height) / 256f,
                    x + fc.width, y + fc.height, 0, (fc.x + fc.width) / 256f, 1 - fc.y / 256f,
                    x, y + fc.height, 0, fc.x / 256f, 1 - fc.y / 256f,});
                prev = fc;
            }

            FontText ft = new FontText();
            ft.width = cursor.x + prev.xadvance;
            for (int i = 0; i < pages; i++) {
                if (data[i] != null) {
                    ft.numChars[i] = data[i].size();
                    float[] vertices = new float[ft.numChars[i] * 20];
                    int[] indices = new int[ft.numChars[i] * 6];
                    for (int j = 0; j < ft.numChars[i]; j++) {
                        System.arraycopy(data[i].get(j), 0, vertices, j * 20, 20);
                        System.arraycopy(new int[]{
                            0 + 4 * j, 1 + 4 * j, 2 + 4 * j,
                            0 + 4 * j, 2 + 4 * j, 3 + 4 * j
                        }, 0, indices, j * 6, 6);
                    }
                    ft.vaoArray[i] = VertexArrayObject.createVAO(() -> {
                        BufferObject vbo = new BufferObject(GL_ARRAY_BUFFER, vertices);
                        BufferObject ebo = new BufferObject(GL_ELEMENT_ARRAY_BUFFER, indices);
                        glVertexAttribPointer(0, 3, GL_FLOAT, false, 20, 0);
                        glEnableVertexAttribArray(0);
                        glVertexAttribPointer(1, 2, GL_FLOAT, false, 20, 12);
                        glEnableVertexAttribArray(1);
                    });
                }
            }
            textMap.put(text, ft);
        }
        return textMap.get(text);
    }

    private static class FontChar {

        private int id;
        private int x;
        private int y;
        private int width;
        private int height;
        private int xoffset;
        private int yoffset;
        private int xadvance;
        private int page;
        private final HashMap<Integer, Integer> kernings = new HashMap();

        private int getAdvance(int next) {
            if (kernings.containsKey(next)) {
                return xadvance + kernings.get(next);
            }
            return xadvance;
        }
    }

    public class FontText {

        private double width;
        private VertexArrayObject[] vaoArray = new VertexArrayObject[pages];
        private int[] numChars = new int[pages];

        public void draw2d(Transformation t, Color color, Color outlineColor) {
            FONT_SHADER.setMVP(t);
            if (outlineColor != null) {
                FONT_SHADER.setUniform("color", outlineColor);
                FONT_SHADER.setUniform("outline", true);
                for (int i = 0; i < pages; i++) {
                    int nc = numChars[i];
                    if (nc > 0) {
                        bindAll(textures[i], FONT_SHADER, vaoArray[i]);
                        glDrawElements(GL_TRIANGLES, 6 * nc, GL_UNSIGNED_INT, 0);
                    }
                }
            }
            FONT_SHADER.setUniform("color", color);
            FONT_SHADER.setUniform("outline", false);
            for (int i = 0; i < pages; i++) {
                int nc = numChars[i];
                if (nc > 0) {
                    bindAll(textures[i], FONT_SHADER, vaoArray[i]);
                    glDrawElements(GL_TRIANGLES, 6 * nc, GL_UNSIGNED_INT, 0);
                }
            }
        }

        public void draw2dCentered(Transformation t, Color color, Color outlineColor) {
            draw2d(t.translate(new Vec3d(-width / 2, 0, 0)), color, outlineColor);
        }
    }
}
