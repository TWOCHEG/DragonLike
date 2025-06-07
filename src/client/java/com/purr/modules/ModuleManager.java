package com.purr.modules;

import java.util.*;

public class ModuleManager {
    private final List<Parent> modules;

    public ModuleManager(List<Parent> modules) {
        this.modules = modules;

        for (Parent module : modules) {
            module.setModuleManager(this);
        }
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
            System.out.println("---------------------------");
            System.out.println(id);
            System.out.println(module.getId());
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
