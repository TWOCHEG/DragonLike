package pon.purr.utils;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class BlockHighlight {
    public static void renderHighlight(WorldRenderContext context, BlockPos pos, float r, float g, float b) {
        renderHighlight(context, pos, r, g, b, 1.0f);
    }
    public static void renderHighlight(WorldRenderContext context, BlockPos pos, float r, float g, float b, float a) {
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
}
