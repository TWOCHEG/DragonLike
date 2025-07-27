package pon.purr.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import pon.purr.gui.components.Category;
import pon.purr.gui.components.RenderArea;
import pon.purr.modules.ModuleManager;
import pon.purr.modules.Parent;
import pon.purr.modules.ui.Gui;
import pon.purr.utils.GetAnimDiff;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ModulesGui extends Screen {
    private int categoriesAnim = 0;
    private final Gui guiModule;
    private final Screen previous;
    private final LinkedList<Category> categories;

    private boolean open = true;

    private final int categoryWidth = 130;
    private final int categoryPadding = 20;
    private final int startY = 40;
    private final int categoriesShow = 200;

    public ModulesGui(Screen previous, ModuleManager moduleManager, Gui guiModule) {
        super(Text.literal("ModulesGui"));
        this.guiModule = guiModule;
        this.previous = previous;

        Map<String, List<Parent>> modules = moduleManager.getModules();

        this.categories = new LinkedList<>();

        for (Map.Entry<String, List<Parent>> entry : modules.entrySet()) {
            LinkedList<Parent> moduleList = new LinkedList<>(entry.getValue());
            categories.add(new Category(moduleList, entry.getKey()));
        }
    }
    public void closeGui() {
        open = false;
        categoriesAnim = 0;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (categoriesAnim < categoriesShow) {
            categoriesAnim++;
        }
        animHandler();

        if (previous != null) previous.render(context, mouseX, mouseY, delta);

        int startX = (context.getScaledWindowWidth() / 2) - (((categoryWidth + categoryPadding) * categories.size()) / 2);

        int closeCount = 0;
        for (int i = 0; i < categories.size(); i++) {
            Category c = categories.get(i);
            if (categoriesAnim > ((categoriesShow / GetAnimDiff.get()) / categories.size() * i)) {
                c.visibleReverse = !open;
            }

            if (c.visiblePercent == 0) closeCount++;

            c.render(context, startX, startY, categoryWidth, 0, mouseX, mouseY);

            startX += categoryWidth + categoryPadding;
        }
        if (closeCount == categories.size() && !open) {
            client.setScreen(null);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeGui();
            return true;
        }
        return false;
    }

    private void animHandler() {

    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Category c : categories) {
            if (RenderArea.checkHovered(c, mouseX, mouseY)) {
                return c.mouseClicked(mouseX, mouseY, button);
            }
        }
        return false;
    }
}
