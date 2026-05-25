package io.github.prospector.modmenu.util;


import io.github.prospector.modmenu.ModMenu;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HardcodedUtil {
	private static final Pattern FABRIC_PATTERN = Pattern.compile("^fabric-.*(-v\\d+)$");
	private static final Set<String> FABRIC_MODS = new HashSet<>();
	private static final HashMap<String, String> HARDCODED_DESCRIPTIONS = new HashMap<>();

	public static void initializeHardcodings() {
		HARDCODED_DESCRIPTIONS.put("minecraft", new Random().nextInt(1000) == 0 ? "The based game." : "The base game.");
	}

	public static void hardcodeModuleMetadata(ModContainer mod, ModMetadata metadata, String id) {
		Matcher matcher = FABRIC_PATTERN.matcher(id);
		if (matcher.matches() || id.equals("fabric-api-base") || id.equals("fabric-renderer-indigo")) {
			FABRIC_MODS.add(id);
			if (FabricLoader.getInstance().isModLoaded("fabric")) {
				Optional<ModContainer> parent = FabricLoader.getInstance().getModContainer("fabric");
				parent.ifPresent(modContainer -> {
					final List<ModContainer> list = ModMenu.PARENT_MAP.computeIfAbsent(modContainer, k -> new ArrayList<>());
					list.add(mod);
				});;
			}
			ModMenu.addLibraryMod(id);
			if (id.equals("fabric-keybindings-v0") || id.equals("fabric-models-v0") || id.equals("fabric-renderer-api-v1") || id.equals("fabric-renderer-indigo") || id.equals("fabric-rendering-fluids-v1") || id.equals("fabric-rendering-v0") || id.equals("fabric-textures-v0")) {
				ModMenu.CLIENTSIDE_MODS.add(id);
			}
		}
		if (id.equals("fabricloader") || id.equals("fabric") || metadata.getName().endsWith(" API")) {
			ModMenu.addLibraryMod(id);
		}
	}

	public static String formatFabricModuleName(String name) {
		Matcher matcher = FABRIC_PATTERN.matcher(name);
		if (matcher.matches() || name.equals("fabric-renderer-indigo") || name.equals("fabric-api-base")) {
			if (matcher.matches()) {
				String v = matcher.group(1);
				name = capitalize(name.replace(v, "").replace("-", " "));
				name = name + " (" + v.replace("-", "") + ")";
			} else {
				name = capitalize(name.replace("-", " "));
			}
			name = name.replace("Api", "API");
			name = name.replace("Blockentity", "BlockEntity");
		}
		return name;
	}

	private static String capitalize(String str) {
		StringBuilder sb = new StringBuilder();
		boolean capitalizeNext = true;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (capitalizeNext)
				c = Character.toTitleCase(c);
			capitalizeNext = Character.isWhitespace(c);
			sb.append(c);
		}
		return sb.toString();
	}

	public static String getHardcodedDescription(String id) {
		return HARDCODED_DESCRIPTIONS.getOrDefault(id, "");
	}

	public static Set<String> getFabricMods() {
		return FABRIC_MODS;
	}

	public static HashMap<String, String> getHardcodedDescriptions() {
		return HARDCODED_DESCRIPTIONS;
	}
}
