package io.github.prospector.modmenu.mixin;

import io.github.prospector.modmenu.ModMenu;
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
            method = "reload",
            at = @At("TAIL")
    )
    private void modmenu$addLangEntries(String languageCode, CallbackInfo ci) {
        Properties entries = ((LanguageAccessor) currentLanguage).getEntries();
        String lang = "/lang/modmenu/" + currentLanguage.getId() + ".lang";
        try (InputStream stream = getResourceAsStream(lang)) {
            if (stream != null) {
                InputStreamReader r = new InputStreamReader(stream, StandardCharsets.UTF_8);
                entries.load(r);
            }
        } catch (IOException e) {
            ModMenu.LOGGER.error("Failed to load {} language.", currentLanguage.getId(), e);
        }
        String defaultLang = "/lang/modmenu/en_US.lang";
        try (InputStream stream = getResourceAsStream(defaultLang)) {
            if (stream != null) {
                InputStreamReader r = new InputStreamReader(stream, StandardCharsets.UTF_8);
                ((LanguageAccessor) (Object) Language.Default.INSTANCE).getEntries().load(r);
            }
        } catch (IOException e) {
            ModMenu.LOGGER.error("Failed to load default language.", e);
        }
    }
}
