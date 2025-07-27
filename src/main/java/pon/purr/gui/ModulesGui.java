package pon.purr.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import pon.purr.gui.components.Category;
import pon.purr.modules.ModuleManager;
import pon.purr.modules.Parent;
import pon.purr.modules.ui.Gui;
import pon.purr.utils.math.AnimHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ModulesGui extends Screen {
    private final Gui guiModule;
    private final Screen previous;
    private final LinkedList<Category> categories;

    private float visiblePercent = 0f;
    private boolean visibleReverse = false;

    private final int categoryWidth = 130;
    private final int categoryPadding = 20;
    private final int startY = 40;

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
        visibleReverse = true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        animHandler();

        if (visiblePercent == 0) client.setScreen(null);
        if (previous != null) previous.render(context, mouseX, mouseY, delta);

        int startX = (context.getScaledWindowWidth() / 2) - (((categoryWidth + categoryPadding) * categories.toArray().length) / 2);

        for (Category c : categories) {
            c.onRender(context, startX, startY, categoryWidth, 0, visiblePercent, mouseX, mouseY);

            startX += categoryWidth + categoryPadding;
        }
    }

    private void animHandler() {
        visiblePercent = AnimHelper.handleAnimValue(visibleReverse, visiblePercent * 100) / 100;
    }
}
