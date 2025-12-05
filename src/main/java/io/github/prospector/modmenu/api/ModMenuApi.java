package io.github.prospector.modmenu.api;

import io.github.prospector.modmenu.util.TriConsumer;
import net.minecraft.client.gui.Screen;

import java.util.function.Function;

public interface ModMenuApi {
    /**
     * Used to construct a new config screen instance when your mod's
     * configuration button is selected on the mod menu screen. The
     * screen instance parameter is the active mod menu screen.
     *
     * @return A factory function for constructing config screen instances.
     */
    default Function<Screen, ? extends Screen> getConfigScreenFactory() {
        return screen -> null;
    }

    /**
     * Add custom badges for mods to use.
     *
     * @param consumer the consumer used to add new badges.
     */
    default void attachCustomBadges(TriConsumer<String, Integer, Integer> consumer) {
    }
}
