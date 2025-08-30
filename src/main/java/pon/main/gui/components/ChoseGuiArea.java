package pon.main.gui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import pon.main.Main;
import pon.main.modules.ui.Gui;
import pon.main.utils.ColorUtils;
import pon.main.utils.math.AnimHelper;
import pon.main.utils.render.Render2D;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public class ChoseGuiArea extends RenderArea {
    public Class<? extends Screen> currentGuiClass;
    public Class<? extends Screen> oldGuiClass;
    public Class<? extends Screen>[] guiClasses;

    private Class<? extends Screen> defaultGuiClass;

    public boolean show = false;

    private float delta = 1;

    public ChoseGuiArea(Class<? extends Screen>... guiClasses) {
        Class<? extends Screen> defaultClass = Arrays.stream(guiClasses).toList().getFirst();
        this.showFactor = 0;
        this.currentGuiClass = defaultClass;
        this.oldGuiClass = defaultClass;
        this.guiClasses = guiClasses;

        this.defaultGuiClass = defaultClass;

        Consumer<Object> onSetValue = (object) -> {
            onSetValue((Class<? extends Screen>) object);
        };

        for (Class<? extends Screen> c : guiClasses) {
            areas.add(new SelectValueArea(
                c, this, onSetValue,
                () -> delta,
                () -> currentGuiClass,
                () -> oldGuiClass,
                (object) -> {
                    try {
                        return createGui((Class<? extends Screen>) object).getTitle().getString();
                    } catch (Exception e) {
                        return object.toString();
                    }
                }
            ));
        }
    }

    public void onSetValue(Class<? extends Screen> guiClass) {
        if (!Objects.equals(guiClass, currentGuiClass)) {
            try {
                mc.setScreen(createGui(guiClass));
                delta = 0;
                oldGuiClass = currentGuiClass;
                currentGuiClass = guiClass;
                Main.MODULE_MANAGER.getModule(Gui.class).closeGuiAnimComponents();
            } catch (Exception ignore) {}
        }
    }

    public SelectValueArea getSVA(Object screen) {
        for (RenderArea area : areas) {
            if (area instanceof SelectValueArea sva) {
                if (sva.value.equals(screen)) return sva;
            }
        }
        return null;
    }

    public Screen createGui(Class<? extends Screen> guiClass) throws Exception {
        Constructor<? extends Screen> constructor = guiClass.getConstructor();
        return constructor.newInstance();
    }

    @Override
    public void render(
        DrawContext context,
        int startX, int startY,
        int width, int height,
        double mouseX, double mouseY
    ) {
        startY = 5;
        width += padding * 2;
        for (RenderArea area : areas) {
            width += area.width;
        }
        height += areas.getFirst().height + (padding * 2);
        startX -= width / 2;

        Render2D.fill(
            context,
            startX, startY, startX + width, startY + height,
            CategoryArea.makeAColor((100 * showFactor) / 255),
            bigPadding, 2
        );

        SelectValueArea oldSVA = getSVA(oldGuiClass);
        SelectValueArea currentSVA = getSVA(currentGuiClass);

        Render2D.fill(
            context,
            MathHelper.lerp(delta, oldSVA.x, currentSVA.x),
            MathHelper.lerp(delta, oldSVA.y, currentSVA.y),
            MathHelper.lerp(delta, oldSVA.x + oldSVA.width, currentSVA.x + currentSVA.width),
            MathHelper.lerp(delta, oldSVA.y + oldSVA.height, currentSVA.y + currentSVA.height),
            ColorUtils.fromRGB(0, 0, 0, 100 * showFactor),
            bigPadding, 2
        );
        int x = startX;
        for (RenderArea area : areas) {
            area.render(context, x + padding, y + padding, width, height, mouseX, mouseY);
            x += area.width;
        }

        super.render(context, startX, startY, width, height, mouseX, mouseY);
    }

    @Override
    public void animHandler() {
        showFactor = AnimHelper.handle(show, showFactor);
        delta = AnimHelper.handle(true, delta);
    }

    public void returnToDefault() {
        if (!Objects.equals(currentGuiClass, defaultGuiClass)) {
            delta = 0;
            oldGuiClass = currentGuiClass;
            currentGuiClass = defaultGuiClass;
        }
    }
}
