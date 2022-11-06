package io.github.prospector.modmenu.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EnumOS2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = Minecraft.class, remap = false)
public interface MinecraftAccessor {
	@Invoker("getOs")
	static EnumOS2 getOS() {
		return EnumOS2.windows;
	}

}
