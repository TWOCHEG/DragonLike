package com.skylong.gui;

import com.skylong.config.ConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import com.skylong.modules.ui.ConfigMenu;
import com.skylong.utils.GetColor;

import java.nio.file.Path;
import java.util.List;

public class ConfigsGui extends Screen {
    private final Screen previous;
    public ConfigMenu configMenu;
    private final ConfigManager config = new ConfigManager();


    public ConfigsGui(Screen previous, ConfigMenu configMenu) {
        super(Text.literal("Configs Gui"));
        this.previous = previous;
        this.configMenu = configMenu;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // потом сделаю щас похуй
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public void close() {
        configMenu.setEnable(false);
        configMenu.show = false;
        client.setScreen(null);
    }

    private void onDelete(Path path) {
        System.out.println("delete");
    }

    private void onSetActive(Path path) {
        System.out.println("set active");
    }
}
