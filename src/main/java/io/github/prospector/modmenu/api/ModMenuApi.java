package io.github.prospector.modmenu.api;


import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.util.TriConsumer;
import net.minecraft.client.gui.GuiScreen;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ModMenuApi {
	/**
	 * Replaced with {@link ModMenuApi#getConfigScreen(GuiScreen)}, with
	 * the ModMenuApi implemented onto a class that is added as an
	 * entry point to your fabric mod metadata.
	 *
	 * @deprecated Will be removed in 1.15 snapshots.
	 */
	@Deprecated
	static void addConfigOverride(String modid, Runnable action) {
		ModMenu.addLegacyConfigScreenTask(modid, action);
	}

	/**
	 * Used to determine the owner of this API implementation.
	 * Will be deprecated and removed once Fabric has support
	 * for providing ownership information about entry points.
	 */
	String getModId();

	/**
	 * Replaced with {@link ModMenuApi#getConfigScreenFactory()}, which
	 * now allows ModMenu to open the screen for you, rather than depending
	 * on you to open it, and gets rid of the messy Optional->Supplier wrapping.
	 *
	 * @deprecated For internal use only.
	 */
	@Deprecated
    @ApiStatus.Internal
	default Optional<Supplier<GuiScreen>> getConfigScreen(GuiScreen screen) {
		return Optional.empty();
	}

	/**
	 * Used to construct a new config screen instance when your mod's
	 * configuration button is selected on the mod menu screen. The
	 * screen instance parameter is the active mod menu screen.
	 *
	 * @return A factory function for constructing config screen instances.
	 */
	default Function<GuiScreen, ? extends GuiScreen> getConfigScreenFactory() {
		return screen -> getConfigScreen(screen).map(Supplier::get).orElse(null);
	}


    /**
     * Add custom badges for mods to use.
     *
     * @param consumer the consumer used to add new badges.
     */
    default void attachCustomBadges(TriConsumer<String, Integer, Integer> consumer) {
    }
}
