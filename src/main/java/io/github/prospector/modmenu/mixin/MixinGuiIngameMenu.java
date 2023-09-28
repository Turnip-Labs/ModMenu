package io.github.prospector.modmenu.mixin;


import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListScreen;
import io.github.prospector.modmenu.gui.ModMenuButtonWidget;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.core.lang.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiIngameMenu.class, remap = false)
public class MixinGuiIngameMenu extends GuiScreen {
	@SuppressWarnings("unchecked")
	@Inject(at = @At("RETURN"), method = "initGui")
	public void modmenu$drawMenuButton(CallbackInfo info) {
		I18n i18n = I18n.getInstance();
		String buttonText = i18n.translateKey("modmenu.title") + " " + i18n.translateKeyAndFormat("modmenu.loaded", ModMenu.getFormattedModCount());
		this.controlList.add(new ModMenuButtonWidget(100, this.width / 2 - 100, this.height / 4 + 72 - 16, 200, 20,  buttonText));
	}

	@Inject(method = "buttonPressed", at = @At("HEAD"))
	private void modmenu$onActionPerformed(GuiButton button, CallbackInfo ci) {
		if (button.id == 100) {
			mc.displayGuiScreen(new ModListScreen(this));
		}
	}
}
