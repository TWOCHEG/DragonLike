package com.skylong;

import com.skylong.modules.client.Gui;
import com.skylong.modules.combat.KillAura;
import net.fabricmc.api.ClientModInitializer;
import java.util.ArrayList;
import java.util.List;

import com.skylong.gui.ClickGui;
import com.skylong.modules.ModuleManager;

public class Skylong implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ModuleManager moduleManager = new ModuleManager(
			new ArrayList(List.of(
				new Gui(),
				new KillAura()
			))
		);
		ClickGui.init(moduleManager);
	}
}
