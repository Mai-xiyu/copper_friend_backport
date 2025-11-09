package org.xiyu.yee.copper_friend_backport.coppergolem;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.xiyu.yee.copper_friend_backport.WeatheringCopper;
import org.xiyu.yee.copper_friend_backport.registry.ModSoundEvents;

public class CopperGolemOxidationLevels {
	private static final CopperGolemOxidationLevel UNAFFECTED = new CopperGolemOxidationLevel(
		ModSoundEvents.COPPER_GOLEM_SPIN.get(),
		ModSoundEvents.COPPER_GOLEM_HURT.get(),
		ModSoundEvents.COPPER_GOLEM_DEATH.get(),
		ModSoundEvents.COPPER_GOLEM_STEP.get(),
		ResourceLocation.withDefaultNamespace("textures/entity/copper_golem/copper_golem.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/copper_golem/copper_golem_eyes.png")
	);
	private static final CopperGolemOxidationLevel EXPOSED = new CopperGolemOxidationLevel(
		ModSoundEvents.COPPER_GOLEM_SPIN.get(),
		ModSoundEvents.COPPER_GOLEM_HURT.get(),
		ModSoundEvents.COPPER_GOLEM_DEATH.get(),
		ModSoundEvents.COPPER_GOLEM_STEP.get(),
		ResourceLocation.withDefaultNamespace("textures/entity/copper_golem/exposed_copper_golem.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/copper_golem/exposed_copper_golem_eyes.png")
	);
	private static final CopperGolemOxidationLevel WEATHERED = new CopperGolemOxidationLevel(
		ModSoundEvents.COPPER_GOLEM_WEATHERED_SPIN.get(),
		ModSoundEvents.COPPER_GOLEM_WEATHERED_HURT.get(),
		ModSoundEvents.COPPER_GOLEM_WEATHERED_DEATH.get(),
		ModSoundEvents.COPPER_GOLEM_WEATHERED_STEP.get(),
		ResourceLocation.withDefaultNamespace("textures/entity/copper_golem/weathered_copper_golem.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/copper_golem/weathered_copper_golem_eyes.png")
	);
	private static final CopperGolemOxidationLevel OXIDIZED = new CopperGolemOxidationLevel(
		ModSoundEvents.COPPER_GOLEM_OXIDIZED_SPIN.get(),
		ModSoundEvents.COPPER_GOLEM_OXIDIZED_HURT.get(),
		ModSoundEvents.COPPER_GOLEM_OXIDIZED_DEATH.get(),
		ModSoundEvents.COPPER_GOLEM_OXIDIZED_STEP.get(),
		ResourceLocation.withDefaultNamespace("textures/entity/copper_golem/oxidized_copper_golem.png"),
		ResourceLocation.withDefaultNamespace("textures/entity/copper_golem/oxidized_copper_golem_eyes.png")
	);
	private static final Map<WeatheringCopper.WeatherState, CopperGolemOxidationLevel> WEATHERED_STATES = Map.of(
		WeatheringCopper.WeatherState.UNAFFECTED,
		UNAFFECTED,
		WeatheringCopper.WeatherState.EXPOSED,
		EXPOSED,
		WeatheringCopper.WeatherState.WEATHERED,
		WEATHERED,
		WeatheringCopper.WeatherState.OXIDIZED,
		OXIDIZED
	);

	public static CopperGolemOxidationLevel getOxidationLevel(WeatheringCopper.WeatherState weatherState) {
		return (CopperGolemOxidationLevel)WEATHERED_STATES.get(weatherState);
	}
}
