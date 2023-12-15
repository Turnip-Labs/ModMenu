package io.github.prospector.modmenu.mixin;


import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListScreen;
import io.github.prospector.modmenu.gui.ModMenuButtonWidget;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.core.lang.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(value = GuiMainMenu.class, remap = false)
public class MixinGuiMainMenu extends GuiScreen {
	@Inject(at = @At("RETURN"), method = "init")
	public void modmenu$drawMenuButton(CallbackInfo info) {
		I18n i18n = I18n.getInstance();
		GuiButton texturePackButton = this.controlList.get(2);
		texturePackButton.displayString = new Random().nextInt(1000) == 0 ? "Twin Peaks" : i18n.translateKey("menu.mods");
		int newWidth = ((GuiButtonAccessor) texturePackButton).getWidth() / 2 - 1;
		((GuiButtonAccessor) texturePackButton).setWidth(newWidth);
		String buttonText = i18n.translateKey("modmenu.title") + " " + i18n.translateKeyAndFormat("modmenu.loaded", ModMenu.getFormattedModCount());
		this.controlList.add(new ModMenuButtonWidget(100, this.width / 2 + 2, texturePackButton.yPosition, newWidth, 20,  buttonText));
	}

	@Inject(method = "buttonPressed", at = @At("HEAD"))
	private void modmenu$onActionPerformed(GuiButton button, CallbackInfo ci) {
		if (button.id == 100) {
			mc.displayGuiScreen(new ModListScreen(this));
		}
	}
}
