package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import pon.main.modules.settings.Group;
import pon.main.modules.settings.Setting;
import pon.main.utils.ColorUtils;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.render.Render2D;

import java.util.*;
import java.util.function.Consumer;

public class SetsHGroupArea extends RenderArea {
    private final Group group;

    public float showFa = 0;

    private float delta = 1;
    private Setting oldSetting;

    RenderArea currentArea;

    public SetsHGroupArea(Group group, RenderArea parentArea) {
        super(parentArea);
        this.showFactor = group.getVisible() ? 1 : 0;
        this.group = group;

        Consumer<Object> onSetValue = (object) -> {
            if (object instanceof Setting<?> set) {
                onSetValue(set);
            }
        };

        for (Setting s : group.getOptions()) {
            areas.add(new SelectValueArea(
                s, this, onSetValue, () -> delta, group::getValue, () -> oldSetting
            ));
        }
        for (RenderArea area : ModuleArea.getAreas(group.getOptions(), this)) {
            areas.add(area);
        }
        oldSetting = group.getValue();
        currentArea = getArea(group.getValue());
    }

    public RenderArea getArea(Setting setting) {
        List<RenderArea> clearList = new ArrayList<>();
        for (RenderArea area : areas) {
            if (!(area instanceof SelectValueArea)) {
                clearList.add(area);
            }
        }
        return clearList.get(group.getOptions().indexOf(setting));
    }
    private SelectValueArea getSelectArea(Setting setting) {
        for (RenderArea area : areas) {
            if (area instanceof SelectValueArea ssa) {
                if (ssa.value.equals(setting)) {
                    return ssa;
                }
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (RenderArea area : areas) {
            if (area instanceof SelectValueArea || area.equals(currentArea)) {
                if (area.mouseClicked(mouseX, mouseY, button)) return true;
            }
        }
        return false;
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (RenderArea area : areas) {
            if (area instanceof SelectValueArea || area.equals(currentArea)) {
                if (area.mouseReleased(mouseX, mouseY, button)) return true;
            }
        }
        return false;
    }
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (RenderArea area : areas) {
            if (area instanceof SelectValueArea || area.equals(currentArea)) {
                if (area.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
            }
        }
        return false;
    }

    public void onSetValue(Setting setting) {
        if (!Objects.equals(setting, group.getValue())) {
            this.delta = 0;
            this.oldSetting = group.getValue();
            group.setValue(setting);
        }
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        showFa = showFactor * parentArea.showFactor;

        SelectValueArea currentSVA = getSelectArea(group.getValue());
        SelectValueArea oldSVA = getSelectArea(oldSetting);
        Render2D.fill(
            context,
            MathHelper.lerp(delta, oldSVA.x, currentSVA.x),
            MathHelper.lerp(delta, oldSVA.y, currentSVA.y),
            MathHelper.lerp(delta, oldSVA.x + oldSVA.width, currentSVA.x + currentSVA.width),
            MathHelper.lerp(delta, oldSVA.y + oldSVA.height, currentSVA.y + currentSVA.height),
            ColorUtils.fromRGB(0, 0, 0, 100 * showFa),
            bigPadding, 2
        );

        List<SelectValueArea> clearList = new ArrayList<>();
        for (RenderArea area : areas) {
            if (area instanceof SelectValueArea sva) {
                clearList.add(sva);
            }
        }

        height += currentSVA.height;
        int x = 0;
        int y = 0;
        for (SelectValueArea sva : clearList) {
            if (x + sva.width > width) {
                y += sva.height;
                x = 0;
            }
            sva.render(context, startX + x, startY + height - sva.height + y, width, 0, mouseX, mouseY);
            x += sva.width + padding;
        }
        height += y + padding;

        currentArea = getArea(group.getValue());
        currentArea.render(
            context,
            (int) (startX + ((width + bigPadding) * (clearList.indexOf(oldSVA) > clearList.indexOf(currentSVA) ? -1 : 1) * (1 - delta))),
            startY + height,
            width, 0,
            mouseX, mouseY
        );
        RenderArea oldArea = getArea(oldSetting);
        if (delta != 1) {
            oldArea.render(
                context,
                (int) (startX + ((width + bigPadding) * (clearList.indexOf(oldSVA) > clearList.indexOf(currentSVA) ? 1 : -1) * delta)),
                startY + height,
                width, 0,
                mouseX, mouseY
            );
        }

        height += MathHelper.lerp(delta, oldArea.height, currentArea.height);

        super.render(context, startX, startY, width, (int) (height * showFactor), mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        showFactor = AnimHelper.handle(group.getVisible(), showFactor);
        delta = AnimHelper.handle(true, delta);
    }
}
