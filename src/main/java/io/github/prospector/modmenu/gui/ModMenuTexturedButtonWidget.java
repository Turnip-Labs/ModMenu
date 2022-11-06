package io.github.prospector.modmenu.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.Tessellator;
import org.lwjgl.opengl.GL11;

public class ModMenuTexturedButtonWidget extends GuiButton {
	private final String texture;
	private final int u;
	private final int v;
	private final int uWidth;
	private final int vHeight;

	protected ModMenuTexturedButtonWidget(int buttonId, int x, int y, int width, int height, int u, int v, String texture) {
		this(buttonId, x, y, width, height, u, v, texture, 256, 256);
	}

	protected ModMenuTexturedButtonWidget(int buttonId, int x, int y, int width, int height, int u, int v, String texture, int uWidth, int vHeight) {
		this(buttonId, x, y, width, height, u, v, texture, uWidth, vHeight, "");
	}

	protected ModMenuTexturedButtonWidget(int buttonId, int x, int y, int width, int height, int u, int v, String texture, int uWidth, int vHeight, String message) {
		super(buttonId, x, y, width, height, message);
		this.uWidth = uWidth;
		this.vHeight = vHeight;
		this.u = u;
		this.v = v;
		this.texture = texture;
	}

	protected void setPos(int x, int y) {
		this.xPosition = x;
		this.yPosition = y;
	}

	public boolean isHovered(int mouseX, int mouseY) {
		return mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
	}

    @Override
    public void drawButton(Minecraft minecraft, int i, int j) {
        render(minecraft, i, j);
    }

	public void render(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible) {
			FontRenderer font = mc.fontRenderer;
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture(texture));
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
			tess.addVertexWithUV(xPosition, yPosition + height, this.zLevel, (float) u * uScale, (float)(adjustedV + height) * vScale);
			tess.addVertexWithUV(xPosition + width, yPosition + height, this.zLevel, ((float)(u + width) * uScale), (float)(adjustedV + height) * vScale);
			tess.addVertexWithUV(xPosition + width, yPosition, this.zLevel, (float)(u + width) * uScale, (float)adjustedV * vScale);
			tess.addVertexWithUV(xPosition, yPosition, this.zLevel, (float) u * uScale, (float) adjustedV * vScale);
			tess.draw();

			this.mouseDragged(mc, mouseX, mouseY);
			if (!this.enabled) {
				this.drawCenteredString(font, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 0xffa0a0a0);
			} else if (hovered) {
				this.drawCenteredString(font, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 0xffffa0);
			} else {
				this.drawCenteredString(font, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 0xe0e0e0);
			}
		}
	}
}
