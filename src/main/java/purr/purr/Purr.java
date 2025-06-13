package purr.purr;

import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import purr.purr.modules.ModuleManager;
import purr.purr.modules.example.Example;
import purr.purr.modules.ui.*;
import purr.purr.modules.combat.*;
import purr.purr.modules.world.*;

public class Purr implements ModInitializer {
	public static IEventBus eventBus = new EventBus();
	public ModuleManager moduleManager = null;

	@Override
	public void onInitialize() {
		eventBus.registerLambdaFactory(
			"purr.purr",
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
				new Notify(),
				new Example()
			))
		);
	}
}
// тут был Егорушка 
