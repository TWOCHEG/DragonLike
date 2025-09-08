package pon.main.modules.hud.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import pon.main.gui.components.CategoryArea;
import pon.main.modules.Parent;
import pon.main.modules.hud.Hud;
import pon.main.utils.ColorUtils;
import pon.main.utils.player.PlayerUtility;
import pon.main.utils.render.Render2D;

import java.awt.*;

public class CordsHud extends HudArea {
    public CordsHud(Hud hud) {
        super(hud);
    }

    @Override
    public void render(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        String coordinates;
        if (!Parent.fullNullCheck()) {
            int posX = (int) mc.player.getX();
            int posY = (int) mc.player.getY();
            int posZ = (int) mc.player.getZ();
            float nether = !PlayerUtility.isInHell() ? 0.125F : 8.0F;
            int hposX = (int) (mc.player.getX() * nether);
            int hposZ = (int) (mc.player.getZ() * nether);
            coordinates = "" + Formatting.WHITE + posX + " " + posY + " " + posZ + Formatting.WHITE + " [" + Formatting.RESET + hposX + " " + hposZ + Formatting.WHITE + "]";
        } else {
            coordinates = "world not loaded";
        }

        height = mc.textRenderer.fontHeight + (padding * 2);
        width = mc.textRenderer.getWidth(coordinates) + (padding * 2);

        Render2D.fill(
            context,
            x, y, x + width, y + height,
            CategoryArea.makeAColor((100 + (30 * draggedFactor)) * showFactor),
            bigPadding, 2
        );
        context.drawText(
            mc.textRenderer, coordinates,
            x + padding, y + padding,
            ColorUtils.fromRGB(255, 255, 255, 200 * showFactor),
            false
        );

        super.render(context, x, y, width, height, mouseX, mouseY);
    }
}
