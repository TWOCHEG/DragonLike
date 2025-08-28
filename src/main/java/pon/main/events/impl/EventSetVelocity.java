package pon.main.events.impl;

import net.minecraft.util.math.Vec3d;
import pon.main.events.Event;

public class EventSetVelocity extends Event {
    private Vec3d velocity;

    public EventSetVelocity(Vec3d velocity) {
        this.velocity = velocity;
    }

    public Vec3d getVelocity() {
        return velocity;
    }

    public void setVelocity(Vec3d value) {
        velocity = value;
    }
}
