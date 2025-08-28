package pon.main.events.impl;

import pon.main.events.Event;
import net.minecraft.util.math.Vec3d;

public class EventPositionCamera extends Event {
    private double x;
    private double y;
    private double z;
    private float tickDelta;

    public EventPositionCamera(double x, double y, double z, float tickDelta) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.tickDelta = tickDelta;
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

    public float getTickDelta() {
        return tickDelta;
    }

    public void setPosition(Vec3d pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }
}
