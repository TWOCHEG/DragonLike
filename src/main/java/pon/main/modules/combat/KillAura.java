package pon.main.modules.combat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import pon.main.Main;
import pon.main.modules.Parent;
import pon.main.modules.settings.Header;

public class KillAura extends Parent {
    private Header header = new Header("килка пока в разработке сори");

    public static PlayerEntity target;
    public Box resolvedBox;

    public enum RayTrace {
        OFF, OnlyTarget, AllEntities
    }

    public enum Resolver {
        Off, Advantage, Predictive, BackTrack
    }

    public KillAura() {
        super("kill aura", Main.Categories.combat);
    }

    public static class Position {
        private double x, y, z;
        private int ticks;

        public Position(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public boolean shouldRemove() {
            return ticks++ > 1;  // Managers.MODULE_MANAGER.getModule(KillAura.class).backTicks.getValue();
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;

        }
    }
}
