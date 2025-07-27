package pon.purr.modules;

import pon.purr.Purr;

import java.util.*;


public class ModuleManager {
    private final List<Parent> modules;

    public ModuleManager(Parent... modules) {
        this.modules = List.of(modules);

        this.modules.forEach(m -> {
            m.setModuleManager(this);
            Purr.EVENT_BUS.subscribe(m);
        });
    }

    public Map<String, List<Parent>> getModules() {
        Map<String, List<Parent>> grouped = new TreeMap<>();

        modules.forEach(m -> {
            String category = m.getCategory();
            if (category != null) {
                grouped
                .computeIfAbsent(category, k -> new ArrayList<>())
                .add(m);
            }
        });
        for (List<Parent> list : grouped.values()) {
            list.sort(Comparator.comparing(Parent::getName));
        }

        return grouped;
    }

    public Parent getModuleById(String id) {
        return modules.stream()
            .filter(m -> Objects.equals(m.getName(), id))
            .findFirst()
            .orElse(null);
    }

    public Parent getModuleByClass(Class c) {
        return modules.stream()
            .filter(m -> Objects.equals(m.getClass(), c))
            .findFirst()
            .orElse(null);
    }

    public Parent getModuleByKey(int keyCode) {
        return modules.stream()
            .filter(m -> Objects.equals(m.getKeybind(), keyCode))
            .findFirst()
            .orElse(null);
    }
}
