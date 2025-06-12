package purr.purr.modules.combat;

import purr.purr.modules.Parent;
import purr.purr.modules.settings.Group;
import purr.purr.modules.settings.Setting;

public class KillAura extends Parent {
    public Setting<Float> range = new Setting<>("range", 6.0f, 1.0f, 6.0f);

    public Group targets = new Group("targets");
    public Setting<Boolean> playersTarget = new Setting<>("players", true).addToGroup(targets);
    public Setting<Boolean> animalsTarget = new Setting<>("animals", false).addToGroup(targets);

    public KillAura() {
        super("kill aura", "kill_aura", "combat");
    }
}

