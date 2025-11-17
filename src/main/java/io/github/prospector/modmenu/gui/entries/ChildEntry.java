package io.github.prospector.modmenu.gui.entries;

import io.github.prospector.modmenu.gui.ModListEntry;
import io.github.prospector.modmenu.gui.ModListWidget;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;

public class ChildEntry extends ModListEntry {
    private final boolean bottomChild;

    public ChildEntry(Minecraft client, ModContainer container, ModListWidget list, boolean bottomChild) {
        super(client, container, list);
        this.bottomChild = bottomChild;
    }

    @Override
    public void render(int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float delta) {
        // Render the normal mod entry row (icon, name, badges, description)
        super.render(index, rowTop, rowLeft, rowWidth, rowHeight, mouseX, mouseY, hovered, delta);

        // Draw the tree branch lines
        int branchX = rowLeft + 4;
        int color = 0xFFA0A0A0;

        // Vertical line
        drawRect(branchX, rowTop - 2, branchX + 1, rowTop + (bottomChild ? rowHeight / 2 : rowHeight + 2), color);

        // Horizontal connector
        int centerY = rowTop + rowHeight / 2;
        drawRect(branchX, centerY, branchX + 7, centerY + 1, color);
    }

    @Override
    public int getXOffset() {
        return 13;
    }
}
