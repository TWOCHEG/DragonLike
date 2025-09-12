package pon.main.modules.hud.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import pon.main.Main;
import pon.main.gui.components.CategoryArea;
import pon.main.gui.components.RenderArea;
import pon.main.managers.Managers;
import pon.main.modules.Parent;
import pon.main.modules.hud.HudModule;
import pon.main.utils.ColorUtils;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.render.Render2D;

public class ArrayListHud extends HudArea {
    public ArrayListHud() {
        super();
    }

    @Override
    public void render(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
        if (areas.isEmpty()) {
            for (Parent module : Managers.MODULE_MANAGER.getModules()) {
                if (module.getName() == null || module.getName().isEmpty() || !module.isToggleable()) continue;
                areas.add(new ModuleNameArea(this, module));
            }
        }

        for (RenderArea area : areas) {
            if (area.width > width) {
                width = area.width;
            }
        }

        int renderY = y;
        for (RenderArea area : areas) {
            area.render(context, x, renderY, width, -1, mouseX, mouseY);
            renderY += area.height;
        }
        height = renderY - y;

        super.render(context, x, y, width, height, mouseX, mouseY);
    }

    public class ModuleNameArea extends RenderArea {
        private Parent module;

        private int nameWidth;
        private float draggedFactor = 0;

        public ModuleNameArea(RenderArea parentArea, Parent module) {
            super(parentArea);
            this.module = module;
            this.showFactor = 0;
            this.nameWidth = mc.textRenderer.getWidth(module.getName()) + (padding * 2);
        }

        @Override
        public void render(DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY) {
            height = mc.textRenderer.fontHeight + (padding * 2);
            boolean right = x < context.getScaledWindowWidth() / 2;

            Render2D.fill(
                context,
                right ? x : x + width - nameWidth, y,
                right ? x + nameWidth : x + width, y + height,
                CategoryArea.makeAColor((100 + (30 * draggedFactor)) * showFactor),
                bigPadding, 2
            );
            context.drawText(
                mc.textRenderer, module.getName(),
                right ? x + padding : x + width - nameWidth + padding,
                y + padding,
                ColorUtils.fromRGB(255, 255, 255, 200 * showFactor),
                false
            );
            width = nameWidth;
            super.render(context, x, y, width, (int) (height * showFactor), mouseX, mouseY);
        }

        @Override
        public void animHandler() {
            showFactor = AnimHelper.handle(module.getEnable(), showFactor);
            if (parentArea instanceof HudArea hudArea) {
                this.draggedFactor = hudArea.draggedFactor;
            }
        }
    }
}
