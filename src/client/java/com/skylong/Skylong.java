package com.skylong;

import net.fabricmc.api.ClientModInitializer;

import com.skylong.gui.ClickGui;
import com.skylong.modules.ModuleManager;

public class Skylong implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ModuleManager.init();
		ClickGui.init();
		System.out.println("SkyLong");
	}
}
