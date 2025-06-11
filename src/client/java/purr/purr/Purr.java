package purr.purr;

import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import purr.purr.modules.ModuleManager;
import purr.purr.modules.ui.Gui;
import purr.purr.modules.combat.KillAura;
import purr.purr.modules.ui.ConfigMenu;
import purr.purr.modules.ui.Keybinds;
import purr.purr.modules.world.FakePlayer;
import purr.purr.modules.world.Nuker;
import purr.purr.modules.ui.Notify;

public class Purr implements ModInitializer {
	public static IEventBus eventBus = new EventBus();
	public ModuleManager moduleManager = null;

	@Override
	public void onInitialize() {
		eventBus.registerLambdaFactory(
			"com.purr",
			(lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup())
		);

		moduleManager = new ModuleManager(
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
