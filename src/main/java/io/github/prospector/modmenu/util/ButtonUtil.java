package io.github.prospector.modmenu.util;


import io.github.prospector.modmenu.mixin.GuiButtonAccessor;
import net.minecraft.client.gui.GuiButton;

public class ButtonUtil {
	public static GuiButton createButton(int buttonId, int x, int y, int width, int height, String text) {
		GuiButton button = new GuiButton(buttonId, x, y, text);
		GuiButtonAccessor accessor = (GuiButtonAccessor) button;
		accessor.setWidth(width);
		accessor.setHeight(height);
		return button;
	}
}
