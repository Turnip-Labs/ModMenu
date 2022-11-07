package io.github.prospector.modmenu.mixin;

import net.minecraft.src.GuiButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GuiButton.class, remap = false)
public interface GuiButtonAccessor {
	@Accessor
	int getWidth();

	@Accessor
	void setWidth(int width);

	@Accessor
	void setHeight(int height);
}
