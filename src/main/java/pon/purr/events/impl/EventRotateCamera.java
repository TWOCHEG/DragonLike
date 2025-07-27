package pon.purr.events.impl;

import pon.purr.events.Event;
import net.minecraft.util.math.Vec2f;

public class EventRotateCamera extends Event {
    private float yaw;
    private float pitch;

    public EventRotateCamera(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setRotation(Vec2f rotation) {
        yaw = rotation.x;
        pitch = rotation.y;
    }
}
