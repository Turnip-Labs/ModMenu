package io.github.prospector.modmenu;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.prospector.modmenu.api.ModMenuApi;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.util.HardcodedUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.options.GuiOptions;
import net.minecraft.client.gui.options.data.OptionsPages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;

public class ModMenu implements ModInitializer {
	public static final String MOD_ID = "modmenu";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();

	private static final Map<String, Runnable> LEGACY_CONFIG_SCREEN_TASKS = new HashMap<>();
	public static final List<String> LIBRARY_MODS = new ArrayList<>();
	public static final Set<String> CLIENTSIDE_MODS = new HashSet<>();
    public static final Set<String> DEPRECATED_MODS = new HashSet<>();
	public static final Set<String> PATCHWORK_FORGE_MODS = new HashSet<>();
    public static final Map<String, Map<String, Map.Entry<Integer, Integer>>> CUSTOM_BADGE_MODS = new HashMap<>();
	public static final LinkedListMultimap<ModContainer, ModContainer> PARENT_MAP = LinkedListMultimap.create();
	private static ImmutableMap<String, Function<GuiScreen, ? extends GuiScreen>> configScreenFactories = ImmutableMap.of();

	public static boolean hasConfigScreenFactory(String modid) {
		return configScreenFactories.containsKey(modid);
	}

	public static GuiScreen getConfigScreen(String modid, GuiScreen menuScreen) {
		Function<GuiScreen, ? extends GuiScreen> factory = configScreenFactories.get(modid);
		return factory != null ? factory.apply(menuScreen) : null;
	}

	public static void openConfigScreen(String modid) {
		Runnable opener = LEGACY_CONFIG_SCREEN_TASKS.get(modid);
		if (opener != null) opener.run();
	}

	public static void addLegacyConfigScreenTask(String modid, Runnable task) {
		LEGACY_CONFIG_SCREEN_TASKS.putIfAbsent(modid, task);
	}

	public static boolean hasLegacyConfigScreenTask(String modid) {
		return LEGACY_CONFIG_SCREEN_TASKS.containsKey(modid);
	}

	public static void addLibraryMod(String modid) {
		if (LIBRARY_MODS.contains(modid)) return;

		LIBRARY_MODS.add(modid);
	}

	@SuppressWarnings("RedundantCollectionOperation")
	@Override
	public void onInitialize() {
		ModMenuConfigManager.initializeConfig();
		ImmutableMap.Builder<String, Function<GuiScreen, ? extends GuiScreen>> factories = ImmutableMap.builder();
		FabricLoader.getInstance().getEntrypointContainers("modmenu", ModMenuApi.class).forEach(entrypoint -> {
			ModMenuApi api = entrypoint.getEntrypoint();
			ModContainer mod = entrypoint.getProvider();
            try {
                api.getClass().getDeclaredMethod("getConfigScreenFactory"); // Make sure the method is implemented
                factories.put(mod.getMetadata().getId(), api.getConfigScreenFactory());
            } catch (NoSuchMethodException ignored) {}
            api.attachCustomBadges((name, outlineColor, fillColor) -> {
                Map<String, Map.Entry<Integer, Integer>> map = new HashMap<>();
                map.put(name, new AbstractMap.SimpleEntry<>(outlineColor, fillColor));
                CUSTOM_BADGE_MODS.put(mod.getMetadata().getId(), map);
            });
        });
		factories.put("minecraft", (screenBase -> new GuiOptions(screenBase, OptionsPages.GENERAL)));
		configScreenFactories = factories.build();
		Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();
		HardcodedUtil.initializeHardcodings();
		for (ModContainer mod : mods) {
			ModMetadata metadata = mod.getMetadata();
			String id = metadata.getId();
			// API badges
			if (metadata.containsCustomValue("modmenu:api") && metadata.getCustomValue("modmenu:api").getAsBoolean()) {
				addLibraryMod(id);
			}

			// Client side badges
			if (metadata.getEnvironment().equals(ModEnvironment.CLIENT)) {
				CLIENTSIDE_MODS.add(id);
			}
			if (metadata.containsCustomValue("modmenu:clientsideOnly") && metadata.getCustomValue("modmenu:clientsideOnly").getAsBoolean()) {
				LOGGER.warn("Found mod with id \"{}\" using deprecated value \"modmenu:clientsideOnly\"!", metadata.getId());
				if (!(CLIENTSIDE_MODS.contains(id))) CLIENTSIDE_MODS.add(id);
			}

			// Deprecated badges
            if (metadata.containsCustomValue("modmenu:deprecated") && metadata.getCustomValue("modmenu:deprecated").getAsBoolean()) {
                DEPRECATED_MODS.add(id);
            }

			// Patchwork (unused)
			if (metadata.containsCustomValue("patchwork:source") && metadata.getCustomValue("patchwork:source").getAsObject() != null) {
				CustomValue.CvObject object = metadata.getCustomValue("patchwork:source").getAsObject();
				if ("forge".equals(object.get("loader").getAsString())) {
					PATCHWORK_FORGE_MODS.add(id);
				}
			}

			// Parent mods
			if (metadata.containsCustomValue("modmenu:parent")) {
				String parentId = metadata.getCustomValue("modmenu:parent").getAsString();
				if (parentId != null) {
					Optional<ModContainer> parent = FabricLoader.getInstance().getModContainer(parentId);
					parent.ifPresent(modContainer -> PARENT_MAP.put(modContainer, mod));
				}
			} else {
				HardcodedUtil.hardcodeModuleMetadata(mod, metadata, id);
			}
		}
	}

	public static String getFormattedModCount() {
		return NumberFormat.getInstance().format(FabricLoader.getInstance().getAllMods().size());
	}
}
