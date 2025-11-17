package io.github.prospector.modmenu.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.prospector.modmenu.ModMenu;
import net.minecraft.client.render.texturepack.TexturePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.InputStream;

@Mixin(value = TexturePack.class, remap = false)
public abstract class MixinTexturePacks {
    @WrapOperation(method = "getResourceAsStream", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;", remap = false))
    private InputStream modmenu$onGetResource(Class<?> instance, String name, Operation<InputStream> original) {
        InputStream inputStream = original.call(ModMenu.class, name);
        if (inputStream != null) return inputStream;
        return original.call(instance, name);
    }
}
