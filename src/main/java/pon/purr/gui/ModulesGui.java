package pon.purr.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import pon.purr.gui.components.Category;
import pon.purr.gui.components.RenderArea;
import pon.purr.modules.ui.Gui;
import pon.purr.utils.GetAnimDiff;
import pon.purr.utils.RGB;
import pon.purr.utils.math.AnimHelper;

import java.util.*;

public class ModulesGui extends Screen {
    private int frameCounter = 0;
    private final Gui guiModule;
    private final Screen previous;

    private int moveDiff = 5;
    private float upPercent = 0f;
    private int upCounter = 0;
    private boolean upMove = false;
    private float downPercent = 0f;
    private int downCounter = 0;
    private boolean downMove = false;
    private float leftPercent = 0f;
    private float rightPercent = 0f;

    private boolean open = true;
    private float openPercent = 0f;

    private final int categoryWidth = 130;
    private final int categoryPadding = 20;
    private float startY = 40;
    private float startX = 0;
    private final int categoriesShow = 200;

    public ModulesGui(Screen previous, Gui guiModule) {
        super(Text.literal("ModulesGui"));
        this.guiModule = guiModule;
        this.previous = previous;
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

        LinkedList<Category> categories = guiModule.categories;

        int startX = (int) (((context.getScaledWindowWidth() / 2) - (((categoryWidth + categoryPadding) * categories.size()) / 2)) + this.startX);

        context.fillGradient(
            0, 0,
            context.getScaledWindowWidth(), context.getScaledWindowHeight(),
            RGB.getColor(0, 0, 0, (int) (50 * openPercent)),
            RGB.getColor(0, 0, 0, 0)
        );

        int closeCount = 0;
        for (int i = 0; i < categories.size(); i++) {
            Category c = categories.get(i);
            if (frameCounter > ((categoriesShow / GetAnimDiff.get()) / categories.size() * i)) {
                c.visibleReverse = !open;
            }

            if (c.visiblePercent == 0) closeCount++;

            c.render(context, startX, (int) startY, categoryWidth, 0, mouseX, mouseY);

            startX += categoryWidth + categoryPadding;
        }
        if (closeCount == categories.size() && !open) {
            client.setScreen(null);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Category c : guiModule.categories) {
            if (c.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeGui();
            return true;
        }
        return false;
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0) {
            downMove = true;
            downCounter = frameCounter;
        } else {
            upMove = true;
            upCounter = frameCounter;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void animHandler() {
        openPercent = AnimHelper.handleAnimValue(!open, openPercent * 100) / 100;

        if (downMove && frameCounter > downCounter + 10) {
            downMove = false;
        }
        if (upMove && frameCounter > upCounter + 10) {
            upMove = false;
        }

        upPercent = AnimHelper.handleAnimValue(
            !(GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS || downMove),
            upPercent * 100
        ) / 100;
        startY -= moveDiff * upPercent;
        downPercent = AnimHelper.handleAnimValue(
            !(GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS || upMove),
            downPercent * 100
        ) / 100;
        startY += moveDiff * downPercent;
        leftPercent = AnimHelper.handleAnimValue(
            GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_RELEASE,
            leftPercent * 100
        ) / 100;
        startX -= moveDiff * leftPercent;
        rightPercent = AnimHelper.handleAnimValue(
            GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_RELEASE,
            rightPercent * 100
        ) / 100;
        startX += moveDiff * rightPercent;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Category c : guiModule.categories) {
            c.mouseClicked(mouseX, mouseY, button);
            if (RenderArea.checkHovered(c, mouseX, mouseY)) {
                return c.areaMouseClicked(mouseX, mouseY, button);
            }
        }
        return false;
    }
}
