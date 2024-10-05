package io.github.prospector.modmenu.util;


import io.github.prospector.modmenu.mixin.GuiButtonAccessor;
import net.minecraft.client.gui.ButtonElement;

public final class ButtonUtil {
	public static ButtonElement createButton(int buttonId, int x, int y, int width, int height, String text) {
		ButtonElement button = new ButtonElement(buttonId, x, y, text);
		GuiButtonAccessor accessor = (GuiButtonAccessor) button;
		accessor.setWidth(width);
		accessor.setHeight(height);
		return button;
	}
}
