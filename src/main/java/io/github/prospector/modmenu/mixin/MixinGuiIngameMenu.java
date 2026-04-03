package io.github.prospector.modmenu.mixin;


import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListScreen;
import io.github.prospector.modmenu.gui.ModMenuButtonWidget;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ScreenPause;
import net.minecraft.core.lang.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ScreenPause.class, remap = false)
public class MixinGuiIngameMenu extends Screen {
	@SuppressWarnings("unchecked")
	@Inject(at = @At("RETURN"), method = "init")
	public void modmenu$drawMenuButton(CallbackInfo info) {
		I18n i18n = I18n.getInstance();
		String buttonText = i18n.translateKey("modmenu.title") + " " + i18n.translateKeyAndFormat("modmenu.loaded", ModMenu.getFormattedModCount());
		this.buttons.add(new ModMenuButtonWidget(100, this.width / 2 - 100, this.height / 4 + 72 - 16, 200, 20,  buttonText));
	}

	@Inject(method = "buttonClicked", at = @At("HEAD"))
	private void modmenu$onActionPerformed(ButtonElement button, CallbackInfo ci) {
		if (button.id == 100) {
			mc.displayScreen(new ModListScreen(this));
		}
	}
}
