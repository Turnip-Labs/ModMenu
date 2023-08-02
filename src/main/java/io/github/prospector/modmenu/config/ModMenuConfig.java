package io.github.prospector.modmenu.config;


import io.github.prospector.modmenu.util.HardcodedUtil;
import net.fabricmc.loader.api.ModContainer;

import java.util.Comparator;

public class ModMenuConfig {
	private boolean showLibraries = false;
	private Sorting sorting = Sorting.ASCENDING;

	public void toggleShowLibraries() {
		this.showLibraries = !this.showLibraries;
		ModMenuConfigManager.save();
	}

	public void toggleSortMode() {
		this.sorting = Sorting.values()[(sorting.ordinal() + 1) % Sorting.values().length];
		ModMenuConfigManager.save();
	}

	public boolean showLibraries() {
		return showLibraries;
	}

	public Sorting getSorting() {
		return sorting;
	}

	public enum Sorting {
		ASCENDING(Comparator.comparing(modContainer -> HardcodedUtil.formatFabricModuleName(modContainer.getMetadata().getName())), "A-Z"),
		DECENDING(ASCENDING.getComparator().reversed(), "Z-A");

		final Comparator<ModContainer> comparator;
		final String name;

		Sorting(Comparator<ModContainer> comparator, String name) {
			this.comparator = comparator;
			this.name = name;
		}

		public Comparator<ModContainer> getComparator() {
			return comparator;
		}

		public String getName() {
			return name;
		}
	}
}
