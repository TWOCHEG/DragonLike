package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.main.Main;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.render.Render2D;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ContextMenu extends RenderArea {
    private RenderArea context;

    private Runnable closeTask;

    private float showFactor2 = 0;
    private boolean show = true;

    private Supplier<Float> showFactorProvider;

    public ContextMenu(RenderArea context, int[] position, List<RenderArea> areas) {
        this(null, context, position, areas.toArray(new RenderArea[0]));
    }
    public ContextMenu(RenderArea parentArea, RenderArea context, int[] position, List<RenderArea> areas) {
        this(parentArea, context, position, areas.toArray(new RenderArea[0]));
    }
    public ContextMenu(RenderArea parentArea, RenderArea context, int[] position, RenderArea[] areas) {
        super(parentArea);
        this.showFactor = 0;
        this.context = context;

        Collections.addAll(this.areas, areas);

        for (RenderArea area : this.areas) {
            area.parentArea = this;
            area.showFactor = 0;
        }

        this.width = 80;

        this.x = position[0];
        this.y = position[1];
    }
    public ContextMenu(RenderArea parentArea, RenderArea context, double[] position, RenderArea[] areas) {
        this(
            parentArea,
            context,
            new int[]{(int) position[0], (int) position[1]},
            areas
        );
    }

    public ContextMenu showFactorProvider(Supplier<Float> showFactorProvider) {
        this.showFactorProvider = showFactorProvider;
        return this;
    }

    public ContextMenu closeTask(Runnable closeTask) {
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
        startX = startX <= 0 ? this.x : startX;
        startY = startY <= 0 ? this.y : startY;
        width = width <= 0 ? 80 : width;

        if (!show && showFactor == 0 && closeTask != null) {
            closeTask.run();
        }

        height = padding * 2;
        for (RenderArea area : areas) {
            height += area.height;
        }

        Render2D.fill(
            context, startX, startY,
            startX + width,
            startY + height,
            CategoryArea.makeAColor(100 * showFactor),
            bigPadding, 2
        );


        int y = startY + padding;
        for (RenderArea area : areas) {
            area.render(context, startX + padding, y, width - (padding * 2), -1, mouseX, mouseY);
            y += area.height;
        }

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        showFactor2 = AnimHelper.handle(show, showFactor2);
        showFactor = showFactor2 * (showFactorProvider != null ? showFactorProvider.get() : parentArea.showFactor);

        if (!show && showFactor == 0 && closeTask != null) {
            closeTask.run();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkHovered(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        } else {
            onProgramEnd();
            return false;
        }
    }
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (Main.cancelButtons.contains(keyCode)) {
            onProgramEnd();
            return true;
        }
        return false;
    }

    @Override
    public void onProgramEnd() {
        show = false;
    }
}
