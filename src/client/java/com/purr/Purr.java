package com.purr;

import net.fabricmc.api.ModInitializer;
import java.util.ArrayList;
import java.util.List;

import com.purr.modules.ModuleManager;

import com.purr.modules.ui.Gui;
import com.purr.modules.combat.KillAura;
import com.purr.modules.ui.ConfigMenu;
import com.purr.modules.ui.Keybinds;
import com.purr.modules.world.FakePlayer;
import com.purr.modules.world.Nuker;

public class Purr implements ModInitializer {

	@Override
	public void onInitialize() {
		ModuleManager moduleManager = new ModuleManager(
			new ArrayList(List.of(
				new Gui(),
				new Keybinds(),
				new KillAura(),
				new ConfigMenu(),
				new FakePlayer(),
				new Nuker()
			))
		);
	}
}
// тут был Егорушка 
