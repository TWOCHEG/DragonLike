package pon.purr.utils;

import net.minecraft.client.font.TextRenderer;

import java.util.LinkedList;

public class Text {
    public static LinkedList<String> splitForRender(String t, int maxWidth, TextRenderer textRenderer, String splitRegex) {
        LinkedList<String> l = new LinkedList<>();
        StringBuilder s = new StringBuilder();
        for (String w : t.strip().split(splitRegex)) {
            if (textRenderer.getWidth(s + splitRegex + w) > maxWidth) {
                l.add(s.toString());
                s = new StringBuilder().append(w);
            } else {
                s.append(splitRegex).append(w);
            }
        }
        if (!s.isEmpty()) {
            l.add(s.toString());
        }
        return l;
    }
    public static LinkedList<String> splitForRender(String t, int maxWidth, TextRenderer r) {
        return splitForRender(t, maxWidth, r, " ");
    }
}
