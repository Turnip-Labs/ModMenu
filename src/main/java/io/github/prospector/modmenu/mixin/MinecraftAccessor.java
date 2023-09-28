package io.github.prospector.modmenu.mixin;


import net.minecraft.client.Minecraft;
import net.minecraft.core.enums.EnumOS;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = Minecraft.class, remap = false)
public interface MinecraftAccessor {
	@Invoker("getOs")
	static EnumOS getOS() {
		throw new AssertionError("This should never be thrown");
	}
}
