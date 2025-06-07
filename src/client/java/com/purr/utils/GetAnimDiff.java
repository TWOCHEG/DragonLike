package com.purr.utils;

import net.minecraft.client.MinecraftClient;

public class GetAnimDiff {
    public static int get() {
        int animDiff = Math.max(1, (100 / Math.max(1, MinecraftClient.getInstance().getCurrentFps())));
        if (animDiff == 1 && MinecraftClient.getInstance().getCurrentFps() < 100) animDiff*=10;
        return animDiff;
    }
}
