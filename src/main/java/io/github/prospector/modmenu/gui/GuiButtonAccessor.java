package io.github.prospector.modmenu.gui;

import io.github.prospector.modmenu.mixin.MixinGuiButton;
import net.minecraft.src.GuiButton;

public class GuiButtonAccessor {
	public static GuiButton createButton(int buttonId, int x, int y, int width, int height, String text) {
		GuiButton button = new GuiButton(buttonId, x, y, text);
		MixinGuiButton accessor = (MixinGuiButton) button;
		accessor.setWidth(width);
		accessor.setHeight(height);
		return button;
	}
}
