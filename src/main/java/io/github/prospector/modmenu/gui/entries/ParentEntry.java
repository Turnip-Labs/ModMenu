package io.github.prospector.modmenu.gui.entries;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListEntry;
import io.github.prospector.modmenu.gui.ModListWidget;
import io.github.prospector.modmenu.util.ModListSearch;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Font;
import net.minecraft.client.render.tessellator.Tessellator;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.List;

public class ParentEntry extends ModListEntry {
    private static final String PARENT_MOD_TEXTURE = "/assets/" + ModMenu.MOD_ID + "/textures/gui/parent_mod.png";

    protected List<ModContainer> children;
    protected boolean hoveringIcon = false;

    public ParentEntry(Minecraft client, ModContainer parent, List<ModContainer> children, ModListWidget list) {
        super(client, parent, list);
        this.children = children;
    }

    @Override
    public void render(int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float delta) {
        super.render(index, rowTop, rowLeft, rowWidth, rowHeight, mouseX, mouseY, hovered, delta);

        Font font = this.client.font;

        int childrenBadgeHeight = 9;
        int childrenBadgeWidth = 9;

        int childrenCount = ModListSearch.search(list.getParent(), list.getParent().getSearchInput(), getChildren()).size();

        int childrenTextWidth = font.getStringWidth(Integer.toString(childrenCount)) - 1;
        if (childrenBadgeWidth < childrenTextWidth + 4) {
            childrenBadgeWidth = childrenTextWidth + 4;
        }

        int childrenBadgeX = rowLeft + 32 - childrenBadgeWidth;
        int childrenBadgeY = rowTop + 32 - childrenBadgeHeight;

        int outlineColor = 0x8810d098;
        int fillColor = 0x88046146;

        // Outline
        drawRect(childrenBadgeX + 1, childrenBadgeY, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + 1, outlineColor);
        drawRect(childrenBadgeX, childrenBadgeY + 1, childrenBadgeX + 1, childrenBadgeY + childrenBadgeHeight - 1, outlineColor);
        drawRect(childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + 1, childrenBadgeX + childrenBadgeWidth, childrenBadgeY + childrenBadgeHeight - 1, outlineColor);
        drawRect(childrenBadgeX + 1, childrenBadgeY + childrenBadgeHeight - 1, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + childrenBadgeHeight, outlineColor);

        // Fill
        drawRect(childrenBadgeX + 1, childrenBadgeY + 1, childrenBadgeX + childrenBadgeWidth - 1, childrenBadgeY + childrenBadgeHeight - 1, fillColor);

        // Text
        font.drawString(Integer.toString(childrenCount), childrenBadgeX + childrenBadgeWidth / 2 - childrenTextWidth / 2, childrenBadgeY + 1, 0xCACACA);

        // Icon hover detection (over the 32x32 icon area)
        this.hoveringIcon = mouseX >= rowLeft - 1 && mouseX <= rowLeft - 1 + 32 && mouseY >= rowTop - 1 && mouseY <= rowTop - 1 + 32;

        // If the entry row is hovered, draw overlay and the parent-mod indicator texture
        if (hovered) {
            drawRect(rowLeft, rowTop, rowLeft + 32, rowTop + 32, 0xA0909090);

            this.client.textureManager.bindTexture(this.client.textureManager.loadTexture(PARENT_MOD_TEXTURE));

            boolean childrenVisible = list.getParent().getShowModChildren().contains(getMetadata().getId());
            int xOffset = childrenVisible ? 32 : 0;
            int yOffset = hoveringIcon ? 32 : 0;

            GL11.glColor4f(1f, 1f, 1f, 1f);
            Tessellator tess = Tessellator.instance;
            tess.startDrawingQuads();
            tess.addVertexWithUV(rowLeft, rowTop, 0, xOffset / 256f, yOffset / 256f);
            tess.addVertexWithUV(rowLeft, rowTop + 32.0, 0, xOffset / 256f, (yOffset + 32) / 256f);
            tess.addVertexWithUV(rowLeft + 32.0, rowTop + 32.0, 0, (xOffset + 32) / 256f, (yOffset + 32) / 256f);
            tess.addVertexWithUV(rowLeft + 32.0, rowTop, 0, (xOffset + 32) / 256f, yOffset / 256f);
            tess.draw();
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (hoveringIcon) {
            String id = getMetadata().getId();
            if (list.getParent().getShowModChildren().contains(id)) {
                list.getParent().getShowModChildren().remove(id);
            } else {
                list.getParent().getShowModChildren().add(id);
            }
            list.filter(list.getParent().getSearchInput(), false);
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            String id = getMetadata().getId();
            if (list.getParent().getShowModChildren().contains(id)) {
                list.getParent().getShowModChildren().remove(id);
            } else {
                list.getParent().getShowModChildren().add(id);
            }
            list.filter(list.getParent().getSearchInput(), false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @SuppressWarnings("unused")
    public void setChildren(List<ModContainer> children) {
        this.children = children;
    }

    @SuppressWarnings("unused")
    public void addChildren(List<ModContainer> children) {
        this.children.addAll(children);
    }

    @SuppressWarnings("unused")
    public void addChildren(ModContainer... children) {
        this.children.addAll(Arrays.asList(children));
    }

    public List<ModContainer> getChildren() {
        return children;
    }
}
