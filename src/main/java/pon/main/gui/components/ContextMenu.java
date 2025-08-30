package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.render.Render2D;

import java.util.Collections;

public class ContextMenu extends RenderArea {
    private RenderArea context;

    private float showFactor2 = 0;

    public ContextMenu(RenderArea parentArea, RenderArea context, int[] position, RenderArea[] areas, int targetWidth) {
        super(parentArea);
        this.showFactor = 0;
        this.context = context;

        Collections.addAll(this.areas, areas);

        for (RenderArea area : areas) {
            area.parentArea = this;
        }

        this.width = targetWidth;

        this.x = position[0];
        this.y = position[1];
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        float showFa = showFactor * parentArea.showFactor;

        this.height = padding * 2;
        for (RenderArea area : areas) {
            this.height += area.height;
        }

        Render2D.fill(
            context, this.x, this.y,
            this.x + this.width,
            this.y + this.height,
            CategoryArea.makeAColor((100 * showFa) / 255),
            bigPadding, 2
        );
        int y = this.y + padding;
        for (RenderArea area : areas) {
            area.render(context, this.x + padding, y, 0, 0, mouseX, mouseY);
            y += area.height;
        }

        super.render(context, this.x, this.y, this.width, this.height, mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        showFactor2 = AnimHelper.handle(false, showFactor2);
        showFactor = showFactor2 * parentArea.showFactor;
    }
}
