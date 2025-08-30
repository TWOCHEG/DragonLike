package pon.main.modules;

import pon.main.Main;

import java.util.*;


public class ModuleManager {
    public final List<Parent> modules;

    public ModuleManager(Parent... modules) {
        this.modules = List.of(modules);

        this.modules.forEach(m -> {
            Main.EVENT_BUS.subscribe(m);
        });
    }

    public Map<Main.Categories, List<Parent>> getForGui() {
        Map<Main.Categories, List<Parent>> grouped = new TreeMap<>();

        modules.forEach(m -> {
            Main.Categories category = m.getCategory();
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

    public <T extends Parent> T getModule(Class<T> c) {
        for (Parent m : modules) {
            if (Objects.equals(c, m.getClass())) return (T) m;
        }
        return null;
    }

    public List<Parent> enableModules() {
        List<Parent> l = new ArrayList<>();

        for (Parent m : modules) {
            if (m.getEnable()) l.add(m);
        }
        return l;
    }
}
