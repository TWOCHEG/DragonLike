package pon.purr.utils;

import net.minecraft.client.gui.DrawContext;

import java.util.stream.IntStream;

public class Render {
    public enum CurveType {
        rounded,
        linear,
    }

    public static void fill(
        DrawContext context,
        int x1, int y1, int x2, int y2, int color,
        int radius,
        int steps // это покачто в разработке
    ) {
        context.fill(
            x1 + radius,
            y1,
            x2 - radius,
            y1 + radius,
            color
        );
        context.fill(
            x1,
            y1 + radius,
            x2,
            y2 - radius,
            color
        );
        context.fill(
            x1 + radius,
            y2 - radius,
            x2 - radius,
            y2,
            color
        );
    }
}
