package pon.purr.utils;

import net.minecraft.client.font.TextRenderer;

import java.util.LinkedList;
import java.util.function.Function;

public class Text {
    public static LinkedList<String> splitForRender(String text, int maxWidth, Function<String, Integer> widthCalculator, String splitRegex) {
        LinkedList<String> lines = new LinkedList<>();
        StringBuilder currentLine = new StringBuilder();

        for (String word : text.strip().split(splitRegex)) {
            boolean fits;
            if (currentLine.isEmpty()) {
                fits = widthCalculator.apply(word) <= maxWidth;
            } else {
                fits = widthCalculator.apply(currentLine + splitRegex + word) <= maxWidth;
            }

            if (!fits) {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                if (widthCalculator.apply(word) > maxWidth) {
                    for (char c : word.toCharArray()) {
                        String charStr = String.valueOf(c);
                        if (widthCalculator.apply(currentLine + charStr) > maxWidth && !currentLine.isEmpty()) {
                            lines.add(currentLine.toString());
                            currentLine = new StringBuilder();
                        }
                        currentLine.append(c);
                    }
                } else {
                    currentLine.append(word);
                }
            } else {
                if (!currentLine.isEmpty()) {
                    currentLine.append(splitRegex);
                }
                currentLine.append(word);
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
    public static LinkedList<String> splitForRender(String t, int maxWidth, Function<String, Integer> widthCalculator) {
        return splitForRender(t, maxWidth, widthCalculator, " ");
    }
}
