package io.github.prospector.modmenu.util;


import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListScreen;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class ModListSearch {

	public static boolean validSearchQuery(String query) {
		return query != null && !query.isEmpty();
	}

	public static List<ModContainer> search(ModListScreen screen, String query, List<ModContainer> candidates) {
		if (!validSearchQuery(query)) {
			return candidates;
		}
		return candidates.stream()
			.filter(modContainer -> passesFilters(screen, modContainer, query.toLowerCase(Locale.ROOT)))
			.collect(Collectors.toList());
	}

	private static boolean passesFilters(ModListScreen screen, ModContainer container, String query) {
		ModMetadata metadata = container.getMetadata();
		String modId = metadata.getId();


		//Some basic search, could do with something more advanced but this will do for now
		if (HardcodedUtil.formatFabricModuleName(metadata.getName()).toLowerCase(Locale.ROOT).contains(query) //Search mod name
			|| modId.toLowerCase(Locale.ROOT).contains(query) // Search mod name
			|| authorMatches(container, query) //Search via author
			|| (ModMenu.LIBRARY_MODS.contains(modId) && "api library".contains(query)) //Search for lib mods
			|| ("clientside".contains(query) && ModMenu.CLIENTSIDE_MODS.contains(modId)) //Search for clientside mods
            || ("deprecated".contains(query) && ModMenu.DEPRECATED_MODS.contains(modId)) //Search for clientside mods
			|| ("configurations configs configures configurable".contains(query) && ModMenu.hasConfigScreenFactory(modId)) //Search for mods that can be configured
		) {
			return true;
		}

		//Allow parent to pass filter if a child passes
		if (ModMenu.PARENT_MAP.keySet().contains(container)) {
			for (ModContainer child : ModMenu.PARENT_MAP.get(container)) {
				if (passesFilters(screen, child, query)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean authorMatches(ModContainer modContainer, String query) {
		return modContainer.getMetadata().getAuthors().stream()
			.filter(Objects::nonNull)
			.map(Person::getName)
			.filter(Objects::nonNull)
			.map(s -> s.toLowerCase(Locale.ROOT))
			.anyMatch(s -> s.contains(query.toLowerCase(Locale.ROOT)));
	}

}
