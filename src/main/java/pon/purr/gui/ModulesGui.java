package pon.purr.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import pon.purr.gui.components.*;
import pon.purr.modules.ui.Gui;
import pon.purr.utils.GetAnimDiff;
import pon.purr.utils.RGB;
import pon.purr.utils.math.AnimHelper;

import java.util.*;

public class ModulesGui extends Screen {
    private int frameCounter = 0;
    private final Gui guiModule;
    private final Screen previous;

    private boolean mouseDown = false;
    private int mouseDownCounter = 0;
    private float mouseDownPercent = 0f;
    private boolean mouseUp = false;
    private int mouseUpCounter = 0;
    private float mouseUpPercent = 0f;

    private float upPercent = 0f;
    private float downPercent = 0f;
    private float leftPercent = 0f;
    private float rightPercent = 0f;

    private boolean open = true;
    public float openPercent = 0f;

    private final int categoryWidth = 130;
    private final int categoryPadding = 20;
    private float startY = 40;
    private float startX = 0;
    private final int categoriesShow;

    private final HintsArea hints = new HintsArea(
        "RIGHT SHIFT - show keybinds\nMOUSE MIDDLE - bind module\n ⬅ ⬆ ⬇ ⮕ - move gui",
        this
    );

    public ModulesGui(Screen previous, Gui guiModule) {
        super(Text.literal("ModulesGui"));
        this.guiModule = guiModule;
        this.previous = previous;
        this.categoriesShow = 130 * guiModule.categories.size();
    }
    public void closeGui() {
        open = false;
        frameCounter = 0;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        frameCounter++;

        animHandler();
        if (previous != null) previous.render(context, mouseX, mouseY, delta);

        LinkedList<CategoryArea> categories = guiModule.categories;

        int startX = (int) (((context.getScaledWindowWidth() / 2) - (((categoryWidth + categoryPadding) * categories.size()) / 2)) + this.startX);

        context.fillGradient(
            0, 0,
            context.getScaledWindowWidth(), context.getScaledWindowHeight(),
            RGB.getColor(0, 0, 0, (int) (50 * openPercent)),
            RGB.getColor(0, 0, 0, 0)
        );

        int closeCount = 0;
        for (int i = 0; i < categories.size(); i++) {
            CategoryArea c = categories.get(i);
            if (frameCounter > (categoriesShow / GetAnimDiff.get100X() / categories.size()) * i) {
                c.visibleReverse = !open;
            }

            if (c.visiblePercent == 0) closeCount++;

            c.render(context, startX, (int) startY, categoryWidth, 0, mouseX, mouseY);

            startX += categoryWidth + categoryPadding;
        }

        if (closeCount == categories.size() && !open) {
            client.setScreen(null);
        }
        hints.render(context, 5,  context.getScaledWindowHeight() - 5, 0, 0, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (CategoryArea c : guiModule.categories) {
            if (c.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeGui();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (RenderArea area : guiModule.categories) {
            if (area.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        }

        if (scrollY < 0) {
            mouseUp = true;
            mouseUpCounter = frameCounter;
        } else {
            mouseDown = true;
            mouseDownCounter = frameCounter;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void animHandler() {
        openPercent = AnimHelper.handleAnimValue(!open, openPercent);

        float moveDiff = GetAnimDiff.get() * 10;
        float moveAnimDiff = GetAnimDiff.get() * 10;
        float mouseDiff = GetAnimDiff.get() * 10;
        float mouseAnimDiff = GetAnimDiff.get() * 10;

        if (frameCounter > mouseUpCounter + GetAnimDiff.get100X() / 3 && mouseUp) {
            mouseUp = false;
        }
        if (frameCounter > mouseDownCounter + GetAnimDiff.get100X() / 3 && mouseDown) {
            mouseDown = false;
        }
        mouseDownPercent = AnimHelper.handleAnimValue(
            !mouseDown,
            mouseDownPercent,
            mouseAnimDiff
        );
        startY += mouseDiff * mouseDownPercent;
        mouseUpPercent = AnimHelper.handleAnimValue(
            !mouseUp,
            mouseUpPercent,
            mouseAnimDiff
        );
        startY -= mouseDiff * mouseUpPercent;

        upPercent = AnimHelper.handleAnimValue(
            !(GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS),
            upPercent,
            moveAnimDiff
        );
        startY -= moveDiff * upPercent;
        downPercent = AnimHelper.handleAnimValue(
            !(GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS),
            downPercent,
            moveAnimDiff
        );
        startY += moveDiff * downPercent;
        leftPercent = AnimHelper.handleAnimValue(
            GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_RELEASE,
            leftPercent,
            moveAnimDiff
        );
        startX -= moveDiff * leftPercent;
        rightPercent = AnimHelper.handleAnimValue(
            GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_RELEASE,
            rightPercent,
            moveAnimDiff
        );
        startX += moveDiff * rightPercent;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (CategoryArea c : guiModule.categories) {
            if (c.mouseClicked(mouseX, mouseY, button)) return true;
        }
        if (hints.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (CategoryArea c : guiModule.categories) {
            if (c.charTyped(chr, modifiers)) return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (RenderArea area : guiModule.categories) {
            if (area.mouseReleased(mouseX, mouseY, button)) return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (RenderArea area : guiModule.categories) {
            if (area.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
