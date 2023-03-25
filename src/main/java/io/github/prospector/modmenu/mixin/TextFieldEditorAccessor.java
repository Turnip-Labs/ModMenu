package io.github.prospector.modmenu.mixin;

import net.minecraft.core.gui.text.TextFieldEditor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = TextFieldEditor.class, remap = false)
public interface TextFieldEditorAccessor {
    @Invoker("clipboardToString")
    String getClipboardContentString();
}
