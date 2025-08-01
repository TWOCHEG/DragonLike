package pon.purr.utils;

import net.minecraft.client.gui.DrawContext;

public class Render {
    public enum CurveType {
        rounded, linear;

        public int getValue(int i, int r, int steps) {
            if (this.equals(linear)) {
                return (r * i) / steps;
            } else if (this.equals(rounded)) {
                // залупа не работает le le le
                float x = (float) i / steps * r;
                float y = (float) Math.sqrt(r * r - x * x);
                return r - Math.round(y);
            }
            return 0;
        }
    }

    public static void fill(
        DrawContext context,
        int x1, int y1, int x2, int y2, int c,
        int r,
        int s
    ) {
        fill(
            context,
            x1, y1, x2, y2, c,
            r, s,
            CurveType.linear
        );
    }
    public static void fill(
        DrawContext context,
        int x1, int y1, int x2, int y2, int c,
        int r,
        int s,
        CurveType t
    ) {
        int stepSize = r / s;

        for (int i = 0; i < s; i++) {
            int offset = t.getValue(i, r, s);
            context.fill(
                x1 + offset,
                (y1 + r) - offset,
                x2 - offset,
                (y1 + r) - (offset + stepSize),
                c
            );
        }
        context.fill(
            x1,
            y1 + r,
            x2,
            y2 - r,
            c
        );
        for (int i = 0; i < s; i++) {
            int offset = t.getValue(i, r, s);
            context.fill(
                x1 + offset,
                (y2 - r) + offset,
                x2 - offset,
                (y2 - r) + (offset + stepSize),
                c
            );
        }
    }

    public static void fillPart(
        DrawContext context,
        int x1, int y1, int x2, int y2, int c,
        int s,
        boolean up
    ) {
        fillPart(
            context,
            x1, y1, x2, y2, c,
            s,
            up,
            CurveType.linear
        );
    }
    public static void fillPart(
        DrawContext context,
        int x1, int y1, int x2, int y2, int c,
        int s,
        boolean up,
        CurveType t
    ) {
        int radius = y2 - y1;
        int stepSize = radius / s;
        if (up) {
            for (int i = 0; i < s; i++) {
                int offset = t.getValue(i, radius, s);
                context.fill(
                    x1 + offset,
                    (y1 + radius) - offset,
                    x2 - offset,
                    (y1 + radius) - (offset + stepSize),
                    c
                );
            }
        } else {
            for (int i = 0; i < s; i++) {
                int offset = t.getValue(i, radius, s);
                context.fill(
                    x1 + offset,
                    (y2 - radius) + offset,
                    x2 - offset,
                    (y2 - radius) + (offset + stepSize),
                    c
                );
            }
        }
    }
}
