package io.github.prospector.modmenu.gui;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.Shaders;
import net.minecraft.client.render.tessellator.TessellatorGeneral;

public class ModMenuTexturedButtonWidget extends ButtonElement {
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
			FontRenderer font = mc.font;
			boolean hovered = isHovered(mouseX, mouseY);

			int adjustedV = this.v;
			if (!enabled) {
				adjustedV += this.height * 2;
			} else if (hovered) {
				adjustedV += this.height;
			}
			float uScale = 1f / uWidth;
			float vScale = 1f / vHeight;

			GLRenderer.pushFrame();
			GLRenderer.setShader(Shaders.INTERFACE);
			GLRenderer.setColor4f(1, 1, 1, 1);

			mc.textureManager.bindTexture(mc.textureManager.loadTexture(texture));

			TessellatorGeneral t = GLRenderer.getTessellator();
			t.startDrawingQuads();
			t.addVertexWithUV(xPosition, yPosition + height, this.zLevel, (float) u * uScale, (float)(adjustedV + height) * vScale);
			t.addVertexWithUV(xPosition + width, yPosition + height, this.zLevel, ((float)(u + width) * uScale), (float)(adjustedV + height) * vScale);
			t.addVertexWithUV(xPosition + width, yPosition, this.zLevel, (float)(u + width) * uScale, (float)adjustedV * vScale);
			t.addVertexWithUV(xPosition, yPosition, this.zLevel, (float) u * uScale, (float) adjustedV * vScale);
			t.draw();

			GLRenderer.popFrame();

			this.mouseDragged(mc, mouseX, mouseY);
			if (!this.enabled) {
				this.drawStringCenteredNoShadow(font, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 0xffa0a0a0);
			} else if (hovered) {
				this.drawStringCenteredNoShadow(font, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 0xffffa0);
			} else {
				this.drawStringCenteredNoShadow(font, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 0xe0e0e0);
			}
		}
	}
}
