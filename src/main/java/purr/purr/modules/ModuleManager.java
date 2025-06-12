package purr.purr.modules;

import purr.purr.Purr;

import java.util.*;


public class ModuleManager {
    private final List<Parent> modules;

    public ModuleManager(List<Parent> modules) {
        this.modules = modules;

        modules.forEach(m -> {
            m.setModuleManager(this);
            Purr.eventBus.subscribe(m);
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
