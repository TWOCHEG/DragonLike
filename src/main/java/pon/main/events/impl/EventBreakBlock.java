package pon.main.events.impl;

import net.minecraft.util.math.BlockPos;
import pon.main.events.Event;

public class EventBreakBlock extends Event {
    private BlockPos bp;

    public EventBreakBlock(BlockPos bp) {
        this.bp = bp;
    }

    public BlockPos getPos() {
        return bp;
    }
}
