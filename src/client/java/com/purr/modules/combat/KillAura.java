package com.purr.modules.combat;

import com.purr.modules.Parent;
import com.purr.modules.settings.*;

import java.util.*;

public class KillAura extends Parent {
    public Setting<Float> range = new Setting<>("range", 6.0f, 1.0f, 6.0f);

    public Group targets = new Group("targets");
    public Setting<Boolean> playersTarget = new Setting<>("players", true).addToGroup(targets);
    public Setting<Boolean> animalsTarget = new Setting<>("animals", false).addToGroup(targets);

    public KillAura() {
        super("kill aura", "kill_aura", "combat");
    }
}

