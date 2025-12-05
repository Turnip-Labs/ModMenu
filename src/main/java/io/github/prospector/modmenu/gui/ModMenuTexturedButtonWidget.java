package io.github.prospector.modmenu.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.render.Font;
import net.minecraft.client.render.tessellator.Tessellator;
import org.lwjgl.opengl.GL11;

public class ModMenuTexturedButtonWidget extends ButtonElement {
    private final String texture;
    private final int u;
    private final int v;
    private final int uWidth;
    private final int vHeight;

    @SuppressWarnings("unused")
    protected ModMenuTexturedButtonWidget(int id, int xPosition, int yPosition, int width, int height, int u, int v, String texture) {
        this(id, xPosition, yPosition, width, height, u, v, texture, 256, 256);
    }

    protected ModMenuTexturedButtonWidget(int id, int xPosition, int yPosition, int width, int height, int u, int v, String texture, int uWidth, int vHeight) {
        this(id, xPosition, yPosition, width, height, u, v, texture, uWidth, vHeight, "");
    }

    protected ModMenuTexturedButtonWidget(int id, int xPosition, int yPosition, int width, int height, int u, int v, String texture, int uWidth, int vHeight, String message) {
        super(id, xPosition, yPosition, width, height, message);
        this.uWidth = uWidth;
        this.vHeight = vHeight;
        this.u = u;
        this.v = v;
        this.texture = texture;
    }

    @SuppressWarnings("unused")
    protected void setPos(int x, int y) {
        this.xPosition = x;
        this.yPosition = y;
    }

    @Override
    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        render(minecraft, mouseX, mouseY);
    }

    public void render(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            Font font = mc.font;
            mc.textureManager.bindTexture(mc.textureManager.loadTexture(texture));
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            boolean hovered = isHovered(mouseX, mouseY);

            int adjustedV = this.v;
            if (!enabled) {
                adjustedV += this.height * 2;
            } else if (hovered) {
                adjustedV += this.height;
            }
            float uScale = 1f / uWidth;
            float vScale = 1f / vHeight;
            Tessellator tess = Tessellator.instance;
            tess.startDrawingQuads();
            tess.addVertexWithUV(xPosition, (double) yPosition + height, this.zLevel, u * uScale, (adjustedV + height) * vScale);
            tess.addVertexWithUV((double) xPosition + width, (double) yPosition + height, this.zLevel, ((u + width) * uScale), (adjustedV + height) * vScale);
            tess.addVertexWithUV((double) xPosition + width, yPosition, this.zLevel, (u + width) * uScale, adjustedV * vScale);
            tess.addVertexWithUV(xPosition, yPosition, this.zLevel, u * uScale, adjustedV * vScale);
            tess.draw();

            this.mouseDragged(mc, mouseX, mouseY);
            if (!this.enabled) {
                this.drawStringCentered(font, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 0xffa0a0a0);
            } else if (hovered) {
                this.drawStringCentered(font, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 0xffffa0);
            } else {
                this.drawStringCentered(font, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 0xe0e0e0);
            }
        }
    }
}
