//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.github.prospector.modmenu.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public abstract class AlwaysSelectedEntryListWidget<E extends EntryListWidget.Entry<E>> extends EntryListWidget<E> {
	private boolean inFocus;

	public AlwaysSelectedEntryListWidget(Minecraft minecraftClient, int i, int j, int k, int l, int m) {
		super(minecraftClient, i, j, k, l, m);
	}

	public boolean setFocus(boolean bl) {
		if (!this.inFocus && this.getItemCount() == 0) {
			return false;
		} else {
			this.inFocus = !this.inFocus;
			if (this.inFocus && this.getSelected() == null && this.getItemCount() > 0) {
				this.moveSelection(1);
			} else if (this.inFocus && this.getSelected() != null) {
				this.moveSelection(0);
			}

			return this.inFocus;
		}
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry<E extends AlwaysSelectedEntryListWidget.Entry<E>> extends EntryListWidget.Entry<E> {
		public Entry() {
		}

		public boolean changeFocus(boolean bl) {
			return false;
		}
	}
}
