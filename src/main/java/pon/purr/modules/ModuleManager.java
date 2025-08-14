package pon.purr.modules;

import pon.purr.Purr;

import java.util.*;


public class ModuleManager {
    public final List<Parent> modules;

    public ModuleManager(Parent... modules) {
        this.modules = List.of(modules);

        this.modules.forEach(m -> {
            Purr.EVENT_BUS.subscribe(m);
        });
    }

    public Map<Purr.Categories, List<Parent>> getModules() {
        Map<Purr.Categories, List<Parent>> grouped = new TreeMap<>();

        modules.forEach(m -> {
            Purr.Categories category = m.getCategory();
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
    public <T extends Parent> T getModuleByClass(Class<T> c) {
        for (Parent p : modules) {
            if (Objects.equals(c.getClass(), p.getClass())) return (T) p;
        }
        return null;
    }

    public Parent getModuleByKey(int keyCode) {
        return modules.stream()
            .filter(m -> Objects.equals(m.getKeybind(), keyCode))
            .findFirst()
            .orElse(null);
    }
}
