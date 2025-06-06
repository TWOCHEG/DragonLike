package com.purr.modules.ui;

import com.purr.modules.Parent;
import com.purr.modules.settings.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class Notify extends Parent {
    private Setting<Boolean> important = new Setting<>("important", "important", true);
    private Setting<Boolean> system = new Setting<>("important", "important", true);
    private Setting<Boolean> module = new Setting<>("important", "important", true);

    private Map<String, Object> impHistory = new HashMap<>();
    private Map<String, Object> impReverseHist = new HashMap<>();

    public Notify() {
        super("notify", "notify", "ui");

        WorldRenderEvents.START.register(context -> {

        });
    }

    public void addImportan(String text) {
        if (!enable || !important.getValue() || impHistory.containsKey(text)) return;

        impHistory.put(text, 0);
        impReverseHist.put(text, false);
    }
}
