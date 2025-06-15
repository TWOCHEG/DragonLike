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
        return modules.stream()
            .filter(m -> m.getName() != null && m.getName().equals(id))
            .findFirst()
            .orElse(null);
    }

    public Parent getModuleByClass(Class c) {
        return modules.stream()
            .filter(m -> m.getClass().equals(c))
            .findFirst()
            .orElse(null);
    }
}
