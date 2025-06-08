package com.purr.utils;

import net.minecraft.client.MinecraftClient;

public class GetAnimDiff {
    public static int get() {
        int animDiff = Math.max(1, (10 / Math.max(1, MinecraftClient.getInstance().getCurrentFps()))) * 10;
        return animDiff;
    }
}
