package pon.purr;

import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import java.lang.invoke.MethodHandles;
import pon.purr.modules.ModuleManager;

import pon.purr.modules.ui.*;
import pon.purr.modules.world.*;
import pon.purr.managers.travel.TravelChangeManager;

public class Purr implements ModInitializer {
	public static IEventBus EVENT_BUS = new EventBus();
	public static ModuleManager moduleManager = null;

	public enum Categories {
		combat,
		ui,
		world,
		example
	}

	@Override
	public void onInitialize() {
		EVENT_BUS.registerLambdaFactory(
			"pon.purr",
			(lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup())
		);

		new TravelChangeManager();

		moduleManager = new ModuleManager(
			new Gui(),
			new Keybinds(),
			new FakePlayer(),
			new Nuker(),
			new Notify(),
			new AutoResponser()
		);
	}
}
// тут был Егорушка 
