package io.github.prospector.modmenu.mixin;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListScreen;
import io.github.prospector.modmenu.gui.ModMenuButtonWidget;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiMainMenu;
import net.minecraft.src.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(value = GuiMainMenu.class, remap = false)
public class MixinGuiMainMenu extends GuiScreen {
	@Inject(at = @At("RETURN"), method = "initGui")
	public void drawMenuButton(CallbackInfo info) {
		GuiButton texturePackButton = this.controlList.get(2);
		texturePackButton.displayString = new Random().nextInt(1000) == 0 ? "Twin Peaks" : "Texture Packs";
		int newWidth = ((GuiButtonAccessor) texturePackButton).getWidth() / 2 - 1;
		((GuiButtonAccessor) texturePackButton).setWidth(newWidth);
		this.controlList.add(new ModMenuButtonWidget(100, this.width / 2 + 2, texturePackButton.yPosition, newWidth, 20,  "Mods (" + ModMenu.getFormattedModCount() + " loaded)"));
	}

	@Inject(method = "actionPerformed", at = @At("HEAD"))
	private void onActionPerformed(GuiButton button, CallbackInfo ci) {
		if (button.id == 100) {
			mc.displayGuiScreen(new ModListScreen(this));
		}
	}

}
