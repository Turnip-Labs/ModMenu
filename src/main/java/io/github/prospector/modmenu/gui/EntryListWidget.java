package io.github.prospector.modmenu.gui;

import com.google.common.collect.Lists;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Tessellator;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

@Environment(EnvType.CLIENT)
public abstract class EntryListWidget<E extends EntryListWidget.Entry<E>> extends GuiScreen {
	protected static final int DRAG_OUTSIDE = -2;
	protected final Minecraft minecraft;
	protected final int itemHeight;
	private final List<E> children = new EntryListWidget.Entries();
	protected int width;
	protected int height;
	protected int top;
	protected int bottom;
	protected int right;
	protected int left;
	protected boolean centerListVertically = true;
	protected int yDrag = -2;
	private double scrollAmount;
	protected boolean renderSelection = true;
	protected boolean renderHeader;
	protected int headerHeight;
	private boolean scrolling;
	private E selected;

	private E focused;
	private boolean dragging;

	public EntryListWidget(Minecraft minecraftClient, int i, int j, int k, int l, int m) {
		this.minecraft = minecraftClient;
		this.width = i;
		this.height = j;
		this.top = k;
		this.bottom = l;
		this.itemHeight = m;
		this.left = 0;
		this.right = i;
	}

	public void setRenderSelection(boolean bl) {
		this.renderSelection = bl;
	}

	protected void setRenderHeader(boolean bl, int i) {
		this.renderHeader = bl;
		this.headerHeight = i;
		if (!bl) {
			this.headerHeight = 0;
		}

	}

	public int getRowWidth() {
		return 220;
	}

	@Nullable
	public E getSelected() {
		return this.selected;
	}

	public void setSelected(@Nullable E entry) {
		this.selected = entry;
	}

	@Nullable
	public E getFocused() {
		return focused;
	}

	public void setFocused(E focused) {
		this.focused = focused;
	}

	public void setDragging(boolean dragging) {
		this.dragging = dragging;
	}

	public final List<E> children() {
		return this.children;
	}

	protected final void clearEntries() {
		this.children.clear();
	}

	protected void replaceEntries(Collection<E> collection) {
		this.children.clear();
		this.children.addAll(collection);
	}

	protected E getEntry(int i) {
		return this.children().get(i);
	}

	protected int addEntry(E entry) {
		this.children.add(entry);
		return this.children.size() - 1;
	}

	protected int getItemCount() {
		return this.children().size();
	}

	protected boolean isSelectedItem(int i) {
		return Objects.equals(this.getSelected(), this.children().get(i));
	}

	@Nullable
	protected final E getEntryAtPosition(double d, double e) {
		int i = this.getRowWidth() / 2;
		int j = this.left + this.width / 2;
		int k = j - i;
		int l = j + i;
		int m = MathHelper.floor_double(e - (double)this.top) - this.headerHeight + (int)this.getScrollAmount() - 4; // convertToBlockCoord
		int n = m / this.itemHeight;
		return d < (double)this.getScrollbarPosition() && d >= (double)k && d <= (double)l && n >= 0 && m >= 0 && n < this.getItemCount() ? this.children().get(n) : null;
	}

	public void updateSize(int i, int j, int k, int l) {
		this.width = i;
		this.height = j;
		this.top = k;
		this.bottom = l;
		this.left = 0;
		this.right = i;
	}

	public void setLeftPos(int i) {
		this.left = i;
		this.right = i + this.width;
	}

	protected int getMaxPosition() {
		return this.getItemCount() * this.itemHeight + this.headerHeight;
	}

	protected void clickedHeader(int i, int j) {
	}

	protected void renderHeader(int i, int j, Tessellator tessellator) {
	}


	public void renderBackground() {
	}

	protected void renderDecorations(int i, int j) {
	}

	public void render(int i, int j, float f) {
		oldY = j;
		this.renderBackground();
		int k = this.getScrollbarPosition();
		int l = k + 6;
		Tessellator tessellator = Tessellator.instance;
		this.minecraft.renderEngine.bindTexture(this.minecraft.renderEngine.getTexture("/gui/background.png"));
		GL11.glColor4f(1f, 1f, 1f, 1f);
		float g = 32.0F;
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque(32, 32, 32);
		tessellator.addVertexWithUV(this.left, this.bottom, 0.0D, (float)this.left / 32.0F, (float)(this.bottom + (int)this.getScrollAmount()) / 32.0F);
		tessellator.addVertexWithUV(this.right, this.bottom, 0.0D, (float)this.right / 32.0F, (float)(this.bottom + (int)this.getScrollAmount()) / 32.0F);
		tessellator.addVertexWithUV(this.right, this.top, 0.0D, (float)this.right / 32.0F, (float)(this.top + (int)this.getScrollAmount()) / 32.0F);
		tessellator.addVertexWithUV(this.left, this.top, 0.0D, (float)this.left / 32.0F, (float)(this.top + (int)this.getScrollAmount()) / 32.0F);
		tessellator.draw();
		int m = this.getRowLeft();
		int n = this.top + 4 - (int)this.getScrollAmount();
		if (this.renderHeader) {
			this.renderHeader(m, n, tessellator);
		}

		this.renderList(m, n, i, j, f);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		this.renderHoleBackground(0, this.top, 255, 255);
		this.renderHoleBackground(this.bottom, this.height, 255, 255);
		GL11.glEnable(GL11.GL_BLEND);
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		boolean o = true;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA(0, 0, 0, 0);
		tessellator.addVertexWithUV(this.left, this.top + 4, 0.0D, 0.0F, 1.0F);
		tessellator.addVertexWithUV(this.right, this.top + 4, 0.0D, 1.0F, 1.0F);
		tessellator.setColorOpaque(0, 0, 0);
		tessellator.addVertexWithUV(this.right, this.top, 0.0D, 1.0F, 0.0F);
		tessellator.addVertexWithUV(this.left, this.top, 0.0D, 0.0F, 0.0F);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque(0, 0, 0);
		tessellator.addVertexWithUV(this.left, this.bottom, 0.0D, 0.0F, 1.0F);
		tessellator.addVertexWithUV(this.right, this.bottom, 0.0D, 1.0F, 1.0F);
		tessellator.setColorRGBA(0, 0, 0, 0);
		tessellator.addVertexWithUV(this.right, this.bottom - 4, 0.0D, 1.0F, 0.0F);
		tessellator.addVertexWithUV(this.left, this.bottom - 4, 0.0D, 0.0F, 0.0F);
		tessellator.draw();
		int p = this.getMaxScroll();
		if (p > 0) {
			int q = (int)((float)((this.bottom - this.top) * (this.bottom - this.top)) / (float)this.getMaxPosition());
			if (q < 32)
				q = 32;
			else if (q > this.bottom - this.top - 8)
				q = this.bottom - this.top - 8;
			int r = (int)this.getScrollAmount() * (this.bottom - this.top - q) / p + this.top;
			if (r < this.top) {
				r = this.top;
			}

			tessellator.startDrawingQuads();
			tessellator.setColorOpaque(0, 0, 0);
			tessellator.addVertexWithUV(k, this.bottom, 0.0D, 0.0F, 1.0F);
			tessellator.addVertexWithUV(l, this.bottom, 0.0D, 1.0F, 1.0F);
			tessellator.addVertexWithUV(l, this.top, 0.0D, 1.0F, 0.0F);
			tessellator.addVertexWithUV(k, this.top, 0.0D, 0.0F, 0.0F);
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setColorOpaque(128, 128, 128);
			tessellator.addVertexWithUV(k, r + q, 0.0D, 0.0F, 1.0F);
			tessellator.addVertexWithUV(l, r + q, 0.0D, 1.0F, 1.0F);
			tessellator.addVertexWithUV(l, r, 0.0D, 1.0F, 0.0F);
			tessellator.addVertexWithUV(k, r, 0.0D, 0.0F, 0.0F);
			tessellator.draw();
			tessellator.startDrawingQuads();
			tessellator.setColorOpaque(192, 192, 192);
			tessellator.addVertexWithUV(k, r + q - 1, 0.0D, 0.0F, 1.0F);
			tessellator.addVertexWithUV(l - 1, r + q - 1, 0.0D, 1.0F, 1.0F);
			tessellator.addVertexWithUV(l - 1, r, 0.0D, 1.0F, 0.0F);
			tessellator.addVertexWithUV(k, r, 0.0D, 0.0F, 0.0F);
			tessellator.draw();
		}

		this.renderDecorations(i, j);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	protected void centerScrollOn(E entry) {
		this.setScrollAmount(this.children().indexOf(entry) * this.itemHeight + this.itemHeight / 2 - (this.bottom - this.top) / 2);
	}

	protected void ensureVisible(E entry) {
		int i = this.getRowTop(this.children().indexOf(entry));
		int j = i - this.top - 4 - this.itemHeight;
		if (j < 0) {
			this.scroll(j);
		}

		int k = this.bottom - i - this.itemHeight - this.itemHeight;
		if (k < 0) {
			this.scroll(-k);
		}

	}

	private void scroll(int i) {
		this.setScrollAmount(this.getScrollAmount() + (double)i);
		this.yDrag = -2;
	}

	public double getScrollAmount() {
		return this.scrollAmount;
	}

	public void setScrollAmount(double d) {
		if (d < 0)
			d = 0;
		if (d > getMaxScroll())
			d = getMaxScroll();
		this.scrollAmount = d;
	}

	private int getMaxScroll() {
		return Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
	}

	public int getScrollBottom() {
		return (int)this.getScrollAmount() - this.height - this.headerHeight;
	}

	protected void updateScrollingState(double d, double e, int i) {
		this.scrolling = i == 0 && d >= (double)this.getScrollbarPosition() && d < (double)(this.getScrollbarPosition() + 6);
	}

	protected int getScrollbarPosition() {
		return this.width / 2 + 124;
	}

    @Override
	public void mouseClicked(int d, int e, int i) {
		this.updateScrollingState(d, e, i);
		if (this.isMouseOver(d, e))  {
			E entry = this.getEntryAtPosition(d, e);
			if (entry != null) {
                if (entry.list.getFocused() != null) {
                    if (!entry.list.getFocused().equals(entry)) {
                        this.focused = entry;
                        this.selected = entry;
                        this.dragging = true;
                        super.mouseClicked(d, e, i);
                    }
                } else {
                    this.focused = entry;
                    this.selected = entry;
                    this.dragging = true;
                    super.mouseClicked(d, e, i);
                }
			} else if (i == 0) {
				this.clickedHeader((int)(d - (double)(this.left + this.width / 2 - this.getRowWidth() / 2)), (int)(e - (double)this.top) + (int)this.getScrollAmount() - 4);
			}
		}
	}

	public boolean mouseReleased(double d, double e, int i) {
		if (this.getFocused() != null) {
			this.getFocused().mouseReleased(d, e, i);
		}
		return false;
	}

	double oldY = -1;
	public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double mouseDX, double mouseDY) {
		if (mouseButton == 0 && isMouseOver(mouseX, mouseY)) {
			setScrollAmount(getScrollAmount() - mouseY + oldY);
			return true;
		} else {
			return false;
		}
	}

	public boolean mouseScrolled(double d, double e, double f) {
		this.setScrollAmount(this.getScrollAmount() - f * (double)this.itemHeight / 2.0D);
		return true;
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.getFocused() != null && this.getFocused().keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		} else if (keyCode == 264) {
			this.moveSelection(1);
			return true;
		} else if (keyCode == 265) {
			this.moveSelection(-1);
			return true;
		} else {
			return false;
		}
	}

	protected void moveSelection(int i) {
		if (!this.children().isEmpty()) {
			int j = this.children().indexOf(this.getSelected());
			int k = j + i;
			if (k < 0)
				k = 0;
			else if (k > getItemCount() - 1)
				k = getItemCount() - 1;
			E entry = this.children().get(k);
			this.setSelected(entry);
			this.ensureVisible(entry);
		}

	}

	public boolean isMouseOver(double d, double e) {
		return e >= (double)this.top && e <= (double)this.bottom && d >= (double)this.left && d <= (double)this.right;
	}

	protected void renderList(int i, int j, int k, int l, float f) {
		int m = this.getItemCount();
		Tessellator tessellator = Tessellator.instance;

		for(int n = 0; n < m; ++n) {
			int o = this.getRowTop(n);
			int p = this.getRowBottom(n);
			if (p >= this.top && o <= this.bottom) {
				int q = j + n * this.itemHeight + this.headerHeight;
				int r = this.itemHeight - 4;
				E entry = this.getEntry(n);
				int s = this.getRowWidth();
				int v;
				if (this.renderSelection && this.isSelectedItem(n)) {
					v = this.left + this.width / 2 - s / 2;
					int u = this.left + this.width / 2 + s / 2;
					GL11.glDisable(GL11.GL_TEXTURE_2D);
					float g = this.isFocused() ? 1.0F : 0.5F;
					GL11.glColor4f(g, g, g, 1f);
					tessellator.startDrawingQuads();
					tessellator.addVertex(v, q + r + 2, 0.0D);
					tessellator.addVertex(u, q + r + 2, 0.0D);
					tessellator.addVertex(u, q - 2, 0.0D);
					tessellator.addVertex(v, q - 2, 0.0D);
					tessellator.draw();
					GL11.glColor4f(0f, 0f, 0f, 1f);
					tessellator.startDrawingQuads();
					tessellator.addVertex(v + 1, q + r + 1, 0.0D);
					tessellator.addVertex(u - 1, q + r + 1, 0.0D);
					tessellator.addVertex(u - 1, q - 1, 0.0D);
					tessellator.addVertex(v + 1, q - 1, 0.0D);
					tessellator.draw();
					GL11.glEnable(GL11.GL_TEXTURE_2D);
				}

				v = this.getRowLeft();
				entry.render(n, o, v, s, r, k, l, this.isMouseOver(k, l) && Objects.equals(this.getEntryAtPosition(k, l), entry), f);
			}
		}

	}

	protected int getRowLeft() {
		return this.left + this.width / 2 - this.getRowWidth() / 2 + 2;
	}

	protected int getRowTop(int i) {
		return this.top + 4 - (int)this.getScrollAmount() + i * this.itemHeight + this.headerHeight;
	}

	private int getRowBottom(int i) {
		return this.getRowTop(i) + this.itemHeight;
	}

	protected boolean isFocused() {
		return false;
	}

	protected void renderHoleBackground(int i, int j, int k, int l) {
		Tessellator tessellator = Tessellator.instance;
		this.minecraft.renderEngine.bindTexture(this.minecraft.renderEngine.getTexture("/gui/background.png"));
		GL11.glColor4f(1f, 1f, 1f, 1f);
		float f = 32.0F;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA(64, 64, 64, l);
		tessellator.addVertexWithUV(this.left, j, 0.0D, 0.0F, (float)j / 32.0F);
		tessellator.addVertexWithUV(this.left + this.width, j, 0.0D, (float)this.width / 32.0F, (float)j / 32.0F);
		tessellator.setColorRGBA(64, 64, 64, k);
		tessellator.addVertexWithUV(this.left + this.width, i, 0.0D, (float)this.width / 32.0F, (float)i / 32.0F);
		tessellator.addVertexWithUV(this.left, i, 0.0D, 0.0F, (float)i / 32.0F);
		tessellator.draw();
	}

	protected E remove(int i) {
		E entry = this.children.get(i);
		return this.removeEntry(this.children.get(i)) ? entry : null;
	}

	protected boolean removeEntry(E entry) {
		boolean bl = this.children.remove(entry);
		if (bl && entry == this.getSelected()) {
			this.setSelected(null);
		}

		return bl;
	}

	@Environment(EnvType.CLIENT)
	class Entries extends AbstractList<E> {
		private final List<E> entries;

		private Entries() {
			this.entries = Lists.newArrayList();
		}

		public E get(int i) {
			return this.entries.get(i);
		}

		public int size() {
			return this.entries.size();
		}

		public E set(int i, E entry) {
			E entry2 = this.entries.set(i, entry);
			entry.list = EntryListWidget.this;
			return entry2;
		}

		public void add(int i, E entry) {
			this.entries.add(i, entry);
			entry.list = EntryListWidget.this;
		}

		public E remove(int i) {
			return this.entries.remove(i);
		}
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry<E extends EntryListWidget.Entry<E>> extends GuiScreen {
		@Deprecated
		EntryListWidget<E> list;

		public Entry() {
		}

		public abstract void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f);

		public boolean isMouseOver(double d, double e) {
			return Objects.equals(this.list.getEntryAtPosition(d, e), this);
		}

		public void mouseMoved(double d, double e) {
		}

		public void mouseClicked(int d, int e, int f) {
		}

		public boolean mouseReleased(double d, double e, int i) {
			return false;
		}

		public boolean mouseScrolled(double d, double e, double f) {
			return false;
		}

		public boolean keyPressed(int i, int j, int k) {
			return false;
		}

		public boolean keyReleased(int i, int j, int k) {
			return false;
		}

		public boolean charTyped(char c, int i) {
			return false;
		}

		public boolean changeFocus(boolean bl) {
			return false;
		}

	}
}
