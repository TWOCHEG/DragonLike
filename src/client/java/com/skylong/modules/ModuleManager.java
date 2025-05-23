package com.skylong.modules;

import com.skylong.modules.client.Gui;
import com.skylong.modules.combat.KillAura;
import com.skylong.modules.example.Example;

import java.util.*;

public class ModuleManager {
    private static final Map<String, List<Parent>> modules = new HashMap<>();

    public static void init() {
        modules.put("client", new ArrayList(List.of(new Gui(), new Example())));
        modules.put("combat", new ArrayList(List.of(new KillAura())));
    }

    public static Map<String, List<Parent>> getModules() {
        return modules;
    }
}