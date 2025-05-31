package com.skylong;

import net.fabricmc.api.ClientModInitializer;
import java.util.ArrayList;
import java.util.List;

import com.skylong.gui.ClickGui;
import com.skylong.modules.ModuleManager;

import com.skylong.modules.ui.Gui;
import com.skylong.modules.combat.KillAura;
import com.skylong.modules.ui.ConfigMenu;
import com.skylong.modules.ui.Keybinds;
import com.skylong.modules.world.FakePlayer;
import com.skylong.modules.world.Nuker;

public class Skylong implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ModuleManager moduleManager = new ModuleManager(
			new ArrayList(List.of(
				new Gui(),
				new KillAura(),
				new ConfigMenu(),
				new FakePlayer(),
				new Nuker()
			))
		);
		ClickGui.init(moduleManager);
		Keybinds.init(moduleManager);
	}
}
// тут был Егорушка 
