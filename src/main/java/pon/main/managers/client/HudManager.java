package pon.main.managers.client;

import pon.main.modules.hud.components.HudArea;

import java.util.*;

public class HudManager {
    private final List<HudArea> hudList;

    public HudManager (HudArea... hudList) {
        this.hudList = List.of(hudList);

        this.hudList.forEach(HudArea::getSettings);
    }

    public <T extends HudArea> T getHud(Class<T> c) {
        for (HudArea m : hudList) {
            if (Objects.equals(c, m.getClass())) return (T) m;
        }
        return null;
    }

    public List<HudArea> hudList() {
        return hudList;
    }

    public List<HudArea> enableHuds() {
        List<HudArea> l = new ArrayList<>();

        for (HudArea m : hudList) {
            if (m.getEnable()) l.add(m);
        }
        return l;
    }
}
