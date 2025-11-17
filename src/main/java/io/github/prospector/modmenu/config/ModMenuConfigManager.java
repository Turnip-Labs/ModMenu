package io.github.prospector.modmenu.config;

import io.github.prospector.modmenu.ModMenu;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModMenuConfigManager {
    private static Path file;
    private static ModMenuConfig config;

    private static void prepareBiomeConfigFile() {
        if (file != null) return;
        file = FabricLoader.getInstance().getConfigDir().resolve(ModMenu.MOD_ID + ".json");
    }

    @SuppressWarnings("UnusedReturnValue")
    public static ModMenuConfig initializeConfig() {
        if (config != null) return config;
        config = new ModMenuConfig();
        load();
        return config;
    }

    private static void load() {
        prepareBiomeConfigFile();
        try {
            if (Files.notExists(file)) save();
            if (Files.exists(file)) {
                BufferedReader br = Files.newBufferedReader(file);
                config = ModMenu.GSON.fromJson(br, ModMenuConfig.class);
            }
        } catch (IOException e) {
            ModMenu.LOGGER.error("Couldn't load Mod Menu configuration file; reverting to defaults", e);
        }
    }

    public static void save() {
        prepareBiomeConfigFile();
        String jsonString = ModMenu.GSON.toJson(config);
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(jsonString);
        } catch (IOException e) {
            ModMenu.LOGGER.error("Couldn't save Mod Menu configuration file", e);
        }
    }

    public static ModMenuConfig getConfig() {
        return config;
    }
}
