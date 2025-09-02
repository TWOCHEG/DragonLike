package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.render.Render2D;

import java.util.Collections;

public class ContextMenu extends RenderArea {
    private RenderArea context;

    private Runnable closeTask;

    private float showFactor2 = 0;
    private boolean show = true;
    private ButtonArea clickedButton;

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

    public ContextMenu setCloseTask(Runnable closeTask) {
        this.closeTask = closeTask;
        return this;
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        if (!show && showFactor == 0 && closeTask != null) {
            closeTask.run();
        }

        this.height = padding * 2;
        for (RenderArea area : areas) {
            this.height += area.height;
        }

        Render2D.fill(
            context, this.x, this.y,
            this.x + this.width,
            this.y + this.height,
            CategoryArea.makeAColor(100 * showFactor),
            bigPadding, 2
        );

        int clickedButtonY = -1;
        if (clickedButton != null) {
            int tempY = this.y + padding;
            for (RenderArea area : areas) {
                if (area.equals(clickedButton)) {
                    clickedButtonY = tempY;
                    break;
                }
                tempY += area.height;
            }
        }

        int y = this.y + padding;
        for (RenderArea area : areas) {
            int renderY;
            if (clickedButton != null && !area.equals(clickedButton) && clickedButtonY != -1) {
                renderY = y + (int) ((clickedButtonY - y) * (1 - showFactor));
            } else {
                renderY = y;
            }

            area.render(context, this.x + padding, renderY, 0, 0, mouseX, mouseY);
            y += area.height;
        }

        super.render(context, this.x, this.y, this.width, this.height, mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        showFactor2 = AnimHelper.handle(show, showFactor2);
        showFactor = showFactor2 * parentArea.showFactor;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (clickedButton == null && super.mouseClicked(mouseX, mouseY, button) && closeTask != null) {
            clickedButton = (ButtonArea) getAreaFromPos(mouseX, mouseY);
            show = false;
            return true;
        }
        if (closeTask != null) {
            show = false;
        }
        return false;
    }
}
