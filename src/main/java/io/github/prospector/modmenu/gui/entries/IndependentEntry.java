package io.github.prospector.modmenu.gui.entries;


import io.github.prospector.modmenu.gui.ModListEntry;
import io.github.prospector.modmenu.gui.ModListWidget;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;

public class IndependentEntry extends ModListEntry {

	public IndependentEntry(Minecraft mc, ModContainer container, ModListWidget list) {
		super(mc, container, list);
	}
}
