package com.purr.modules;

import com.purr.Purr;
import com.purr.events.Listener;
import java.util.*;


public class ModuleManager {
    private final List<Parent> modules;

    public ModuleManager(Purr mainClass, List<Parent> modules) {
        this.modules = modules;

        for (Parent module : modules) {
            module.setModuleManager(this);
            module.setMainClass(mainClass);
            mainClass.bus.subscribe(module);
        }

        new Listener(mainClass.bus);
    }

    public Map<String, List<Parent>> getModules() {
        Map<String, List<Parent>> grouped = new TreeMap<>();

        for (Parent module : modules) {
            String category = module.getCategory();
            grouped
            .computeIfAbsent(category, k -> new ArrayList<>())
            .add(module);
        }

        for (List<Parent> list : grouped.values()) {
            list.sort(Comparator.comparing(Parent::getName));
        }

        return grouped;
    }

    public Parent getModuleById(String id) {
        for (Parent module : modules) {
            if (module.getId().equals(id)) {
                return module;
            }
        }
        return null;
    }

    public Parent getModuleByClass(Class c) {
        for (Parent module : modules) {
            if (module.getClass().equals(c)) {
                return module;
            }
        }
        return null;
    }
}
