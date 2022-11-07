package io.github.prospector.modmenu.impl;

import io.github.prospector.modmenu.api.ModMenuApi;
import io.github.prospector.modmenu.util.TriConsumer;

public class ModMenuApiImpl implements ModMenuApi {
    @Override
    public String getModId() {
        return "modmenu";
    }

    @Override
    public void attachCustomBadges(TriConsumer<String, Integer, Integer> consumer) {
        consumer.accept("Mod Menu", 0xff7a2b7c, 0xff510d54);
    }
}
