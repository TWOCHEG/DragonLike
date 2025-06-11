package com.purr;

import com.purr.events.Listener;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import com.purr.modules.ModuleManager;
import com.purr.modules.ui.Gui;
import com.purr.modules.combat.KillAura;
import com.purr.modules.ui.ConfigMenu;
import com.purr.modules.ui.Keybinds;
import com.purr.modules.world.FakePlayer;
import com.purr.modules.world.Nuker;
import com.purr.modules.ui.Notify;

public class Purr implements ModInitializer {
	public IEventBus bus = new EventBus();

	@Override
	public void onInitialize() {
		bus.registerLambdaFactory(
			"com.purr",
			(lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup())
		);

		new ModuleManager(
			this,
			new ArrayList(List.of(
				new Gui(),
				new Keybinds(),
				new KillAura(),
				new ConfigMenu(),
				new FakePlayer(),
				new Nuker(),
				new Notify()
			))
		);
	}
}
// тут был Егорушка 
