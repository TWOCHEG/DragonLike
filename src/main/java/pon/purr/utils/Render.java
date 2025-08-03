package pon.purr.utils;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.LinkedList;

public class Render {
    /**
     * @rounded - неиспользуйте эта хуйня не работает
     * @linear - работает
     */
    public enum CurveType {
        rounded, linear;
        public int getValue(int i, int r, int steps) {
            if (this.equals(linear)) {
                return (r * i) / steps;
            } else if (this.equals(rounded)) {
                int s = (r * i) / steps;
                s = s * (steps / (i + 1));
                return s;
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

    public static void highlightBlock(WorldRenderContext context, BlockPos pos, float r, float g, float b) {
        highlightBlock(context, pos, r, g, b, 1.0f);
    }
    public static void highlightBlock(WorldRenderContext context, BlockPos pos, float r, float g, float b, float a) {
        MatrixStack matrices = context.matrixStack();
        if (matrices == null) return;
        VertexConsumer vertexConsumer = context.consumers().getBuffer(RenderLayer.LINES);

        Box box = new Box(pos).expand(0.001);
        Vec3d cameraPos = context.camera().getPos();

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        drawOutlinedBox(matrices, vertexConsumer, box, r, g, b, a);

        matrices.pop();
    }
    private static void drawOutlinedBox(MatrixStack matrices, VertexConsumer vertexConsumer, Box box, float r, float g, float b, float a) {
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        drawLine(matrices, vertexConsumer, x1, y1, z1, x2, y1, z1, r, g, b, a);
        drawLine(matrices, vertexConsumer, x2, y1, z1, x2, y1, z2, r, g, b, a);
        drawLine(matrices, vertexConsumer, x2, y1, z2, x1, y1, z2, r, g, b, a);
        drawLine(matrices, vertexConsumer, x1, y1, z2, x1, y1, z1, r, g, b, a);

        drawLine(matrices, vertexConsumer, x1, y2, z1, x2, y2, z1, r, g, b, a);
        drawLine(matrices, vertexConsumer, x2, y2, z1, x2, y2, z2, r, g, b, a);
        drawLine(matrices, vertexConsumer, x2, y2, z2, x1, y2, z2, r, g, b, a);
        drawLine(matrices, vertexConsumer, x1, y2, z2, x1, y2, z1, r, g, b, a);

        drawLine(matrices, vertexConsumer, x1, y1, z1, x1, y2, z1, r, g, b, a);
        drawLine(matrices, vertexConsumer, x2, y1, z1, x2, y2, z1, r, g, b, a);
        drawLine(matrices, vertexConsumer, x2, y1, z2, x2, y2, z2, r, g, b, a);
        drawLine(matrices, vertexConsumer, x1, y1, z2, x1, y2, z2, r, g, b, a);
    }
    private static void drawLine(
        MatrixStack matrices, VertexConsumer vertexConsumer,
        float x1, float y1, float z1,
        float x2, float y2, float z2,
        float r, float g, float b, float a
    ) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        vertexConsumer.vertex(matrix, x1, y1, z1)
            .color(r, g, b, Math.max(a, 0.2f))
            .normal(0, 1, 0);

        vertexConsumer.vertex(matrix, x2, y2, z2)
            .color(r, g, b, Math.max(a, 0.2f))
            .normal(0, 1, 0);
    }

    public static int drawTextWithTransfer(
        String text, DrawContext context, TextRenderer textRenderer, int x, int y, int maxWidth, int padding, int color,
        boolean centered
    ) {
        LinkedList<String> toDrawText = Text.splitForRender(text, maxWidth, s -> textRenderer.getWidth(s));
        int textY = y;
        for (String t : toDrawText) {
            context.drawText(
                textRenderer,
                t.strip(),
                centered ? x + (maxWidth / 2 - textRenderer.getWidth(t.strip()) / 2) : x,
                textY,
                color,
                false
            );
            textY += textRenderer.fontHeight + padding;
        }
        return textY - y;
    }
    public static int drawTextWithTransfer(
        String text, DrawContext context, TextRenderer textRenderer, int x, int y, int maxWidth, int padding, int color
    ) {
        return drawTextWithTransfer(text, context, textRenderer, x, y, maxWidth, padding, color, false);
    }
}
