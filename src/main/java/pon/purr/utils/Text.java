package pon.purr.utils;

import net.minecraft.client.font.TextRenderer;

import java.util.LinkedList;

public class Text {
    public static LinkedList<String> splitForRender(String t, int maxWidth, TextRenderer textRenderer, String splitRegex, float scale) {
        LinkedList<String> f = new LinkedList<>();
        StringBuilder s = new StringBuilder();
        if (t.contains(splitRegex)) {
            for (String w : t.strip().split(splitRegex)) {
                if (textRenderer.getWidth(s + splitRegex + w) * scale > maxWidth) {
                    f.add(s.toString());
                    s = new StringBuilder().append(w);
                } else {
                    s.append(splitRegex).append(w);
                }
            }
        } else {
            for (char l : t.strip().toCharArray()) {
                if (textRenderer.getWidth(s + splitRegex + l) > maxWidth) {
                    f.add(s.toString());
                    s = new StringBuilder().append(l);
                } else {
                    s.append(l);
                }
            }
        }
        if (!s.isEmpty()) {
            f.add(s.toString());
        }
        return f;
    }
    public static LinkedList<String> splitForRender(String t, int maxWidth, TextRenderer r) {
        return splitForRender(t, maxWidth, r, " ", 1);
    }
    public static LinkedList<String> splitForRender(String t, int maxWidth, TextRenderer r, float scale) {
        return splitForRender(t, maxWidth, r, " ", scale);
    }
}
