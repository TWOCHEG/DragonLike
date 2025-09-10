package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import pon.main.Main;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.render.Render2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ContextMenu extends RenderArea {
    private float showFactor2 = 0;
    private boolean show = true;

    private Builder params;

    public ContextMenu(Builder params) {
        super(params.parentArea);
        this.params = params;
        this.showFactor = 0;
        this.areas.addAll(params.areas);

        for (RenderArea area : this.areas) {
            area.parentArea = this;
            area.showFactor = 0;
        }

        this.width = 80;

        this.x = params.position[0];
        this.y = params.position[1];
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

        if (!show && showFactor == 0 && params.closeTask != null) {
            params.closeTask.run();
        }

        for (RenderArea area : areas) {
            if (area instanceof ButtonArea buttonArea) {
                int buttonWidth = mc.textRenderer.getWidth(buttonArea.getName());
                if (buttonWidth + (padding * 6) > width) {
                    width = buttonWidth + (padding * 6);
                }
            } else if (area instanceof ButtonInputArea buttonArea) {
                int buttonWidth = mc.textRenderer.getWidth(buttonArea.getName());
                if (buttonWidth + (padding * 6) > width) {
                    width = buttonWidth + (padding * 6);
                }
            }
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
        showFactor = showFactor2 * (params.showFactorProvider != null ? params.showFactorProvider.get() : parentArea.showFactor);

        if (!show && showFactor == 0 && params.closeTask != null) {
            params.closeTask.run();
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

    public static class Builder {
        private List<RenderArea> areas = new ArrayList<>();
        private RenderArea context;
        private RenderArea parentArea;
        private int[] position;
        private Runnable closeTask;
        private Supplier<Float> showFactorProvider;

        public Builder(RenderArea context) {
            this.context = context;
        }
        public Builder() {}

        public ContextMenu build() {
            return new ContextMenu(this);
        }

        public Builder areas(List<RenderArea> renderAreas) {
            this.areas = renderAreas;
            return this;
        }
        public Builder areas(RenderArea[] renderAreas) {
            this.areas = Arrays.stream(renderAreas).toList();
            return this;
        }
        public Builder areas(RenderArea renderAreas) {
            return areas(List.of(renderAreas));
        }

        public Builder parentArea(RenderArea parentArea) {
            this.parentArea = parentArea;
            return this;
        }

        public Builder position(int[] position) {
            this.position = position;
            return this;
        }
        public Builder position(double[] position) {
            return position(new int[]{(int) position[0], (int) position[1]});
        }

        public Builder closeTask(Runnable closeTask) {
            this.closeTask = closeTask;
            return this;
        }

        public Builder showFactorProvider(Supplier<Float> showFactorProvider) {
            this.showFactorProvider = showFactorProvider;
            return this;
        }
    }
}
