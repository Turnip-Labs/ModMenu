package io.github.prospector.modmenu.mixin;

import net.minecraft.core.lang.I18n;
import net.minecraft.core.lang.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Mixin(value = I18n.class, remap = false)
public class MixinI18n {
    @Shadow private Language currentLanguage;

    @Shadow
    public static InputStream getResourceAsStream(String path) {
        throw new AssertionError();
    }

    @Inject(
            method = "reload(Ljava/lang/String;Z)V",
            at = @At("TAIL")
    )
    private void modmenu$addLangEntries(String languageCode, boolean save, CallbackInfo ci) {
        Properties entries = ((LanguageAccessor) currentLanguage).getEntries();
        String lang = "/lang/modmenu/" + currentLanguage.getId() + ".json";
        try (InputStream stream = getResourceAsStream(lang)) {
            if (stream != null) {
                InputStreamReader r = new InputStreamReader(stream, StandardCharsets.UTF_8);
                entries.load(r);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
