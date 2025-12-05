package io.github.prospector.modmenu.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.util.helper.MathHelper;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.spongepowered.include.com.google.common.collect.Lists;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public abstract class EntryListWidget<E extends EntryListWidget.Entry<E>> extends Screen {
    protected static final int TOP_PADDING = 4;
    protected static final int SCROLLBAR_WIDTH = 6;
    protected final Minecraft minecraft;
    protected final int itemHeight;
    private final List<E> children = new EntryListWidget<E>.EntryList();
    protected int top;
    protected int bottom;
    protected int right;
    protected int left;
    private double scrollAmount;
    protected boolean renderSelection = true;
    protected boolean shouldRenderHeader;
    protected int headerHeight;
    private E selected;
    private E focused;

    protected EntryListWidget(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
        this.minecraft = minecraft;
        this.width = width;
        this.height = height;
        this.top = top;
        this.bottom = bottom;
        this.itemHeight = itemHeight;
        this.left = 0;
        this.right = width;
    }

    @SuppressWarnings("unused")
    public void setRenderSelection(boolean bl) {
        this.renderSelection = bl;
    }

    @SuppressWarnings("unused")
    protected void setRenderHeader(boolean bl, int i) {
        this.shouldRenderHeader = bl;
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

    @SuppressWarnings("unused")
    public void setFocused(E focused) {
        this.focused = focused;
    }

    public final List<E> children() {
        return this.children;
    }

    protected final void clearEntries() {
        this.children.clear();
    }

    @SuppressWarnings("unused")
    protected void replaceEntries(Collection<E> collection) {
        this.children.clear();
        this.children.addAll(collection);
    }

    protected E getEntry(int index) {
        return this.children().get(index);
    }

    protected int addEntry(E entry) {
        this.children.add(entry);
        return this.children.size() - 1;
    }

    protected int getItemCount() {
        return this.children().size();
    }

    protected boolean isSelectedItem(int index) {
        return Objects.equals(this.getSelected(), this.children().get(index));
    }

    @Nullable
    protected E getEntryAtPosition(double mouseX, double mouseY) {
        int halfRowWidth = this.getRowWidth() / 2;
        int centerX = this.left + this.width / 2;
        int rowLeft = centerX - halfRowWidth;
        int rowRight = centerX + halfRowWidth;

        // Convert screen Y to list-relative Y, accounting for header and scroll
        int yInList = MathHelper.floor(mouseY - this.top) - this.headerHeight + (int) this.getScrollAmount() - TOP_PADDING;
        int rowIndex = yInList / this.itemHeight;

        // Outside horizontal list area or over scrollbar
        if (mouseX >= this.getScrollbarPosition() || mouseX < rowLeft || mouseX > rowRight) {
            return null;
        }

        // Above first row or before header area
        if (yInList < 0 || rowIndex < 0 || rowIndex >= this.getItemCount()) {
            return null;
        }

        return this.children().get(rowIndex);
    }


    @SuppressWarnings("unused")
    public void updateSize(int width, int height, int top, int bottom) {
        this.width = width;
        this.height = height;
        this.top = top;
        this.bottom = bottom;
        this.left = 0;
        this.right = width;
    }

    public void setLeftEdge(int leftEdge) {
        this.left = leftEdge;
        this.right = leftEdge + this.width;
    }

    protected int getMaxPosition() {
        return this.getItemCount() * this.itemHeight + this.headerHeight;
    }

    @SuppressWarnings("unused")
    protected void onHeaderClicked(int relativeX, int relativeY) {}

    @SuppressWarnings("unused")
    protected void renderHeader(int left, int top, Tessellator tessellator) {}

    @Override
    public void renderBackground() {}

    @SuppressWarnings("unused")
    protected void renderDecorations(int mouseX, int mouseY) {}

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        // No more oldY usage – we now use deltaY in mouseDragged instead

        this.renderBackground();

        int scrollbarX = this.getScrollbarPosition();
        int scrollbarEndX = scrollbarX + SCROLLBAR_WIDTH;

        Tessellator tessellator = Tessellator.instance;

        // Background texture
        this.minecraft.textureManager.bindTexture(
                this.minecraft.textureManager.loadTexture("/gui/background.png")
        );
        GL11.glColor4f(1f, 1f, 1f, 1f);

        tessellator.startDrawingQuads();
        tessellator.setColorOpaque(32, 32, 32);
        tessellator.addVertexWithUV(this.left, this.bottom, 0.0D, this.left / 32.0F, (this.bottom + (int) this.getScrollAmount()) / 32.0F);
        tessellator.addVertexWithUV(this.right, this.bottom, 0.0D, this.right / 32.0F, (this.bottom + (int) this.getScrollAmount()) / 32.0F);
        tessellator.addVertexWithUV(this.right, this.top, 0.0D,this.right / 32.0F, (this.top + (int) this.getScrollAmount()) / 32.0F);
        tessellator.addVertexWithUV(this.left, this.top, 0.0D, this.left / 32.0F, (this.top + (int) this.getScrollAmount()) / 32.0F);
        tessellator.draw();

        int rowLeft = this.getRowLeft();
        int baseRowTop = this.top + TOP_PADDING - (int) this.getScrollAmount();

        // Optional header
        if (this.shouldRenderHeader) {
            this.renderHeader(rowLeft, baseRowTop, tessellator);
        }

        // List entries
        this.renderList(rowLeft, baseRowTop, mouseX, mouseY, delta);

        // Black top/bottom overlays
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        this.renderHoleBackground(0, this.top, 255, 255);
        this.renderHoleBackground(this.bottom, this.height, 255, 255);

        // Fade edges
        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        // Top gradient
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA(0, 0, 0, 0);
        tessellator.addVertexWithUV(this.left, (double) this.top + TOP_PADDING, 0.0D, 0.0F, 1.0F);
        tessellator.addVertexWithUV(this.right, (double) this.top + TOP_PADDING, 0.0D, 1.0F, 1.0F);
        tessellator.setColorOpaque(0, 0, 0);
        tessellator.addVertexWithUV(this.right, this.top, 0.0D, 1.0F, 0.0F);
        tessellator.addVertexWithUV(this.left, this.top, 0.0D, 0.0F, 0.0F);
        tessellator.draw();

        // Bottom gradient
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque(0, 0, 0);
        tessellator.addVertexWithUV(this.left, this.bottom, 0.0D, 0.0F, 1.0F);
        tessellator.addVertexWithUV(this.right, this.bottom, 0.0D, 1.0F, 1.0F);
        tessellator.setColorRGBA(0, 0, 0, 0);
        tessellator.addVertexWithUV(this.right, (double) this.bottom - TOP_PADDING, 0.0D, 1.0F, 0.0F);
        tessellator.addVertexWithUV(this.left, (double) this.bottom - TOP_PADDING, 0.0D, 0.0F, 0.0F);
        tessellator.draw();

        // Scrollbar
        int maxScroll = this.getMaxScroll();
        if (maxScroll > 0) {
            int visibleHeight = this.bottom - this.top;
            int contentHeight = this.getMaxPosition();

            int thumbHeight = (int) ((float) visibleHeight * visibleHeight / contentHeight);
            if (thumbHeight < 32) {
                thumbHeight = 32;
            } else if (thumbHeight > visibleHeight - 8) {
                thumbHeight = visibleHeight - 8;
            }

            int thumbY = (int) this.getScrollAmount() * (visibleHeight - thumbHeight) / maxScroll + this.top;
            if (thumbY < this.top) {
                thumbY = this.top;
            }

            // Scrollbar track
            tessellator.startDrawingQuads();
            tessellator.setColorOpaque(0, 0, 0);
            tessellator.addVertexWithUV(scrollbarX, this.bottom, 0.0D, 0.0F, 1.0F);
            tessellator.addVertexWithUV(scrollbarEndX, this.bottom, 0.0D, 1.0F, 1.0F);
            tessellator.addVertexWithUV(scrollbarEndX, this.top, 0.0D, 1.0F, 0.0F);
            tessellator.addVertexWithUV(scrollbarX, this.top, 0.0D, 0.0F, 0.0F);
            tessellator.draw();

            // Scrollbar thumb (inner)
            tessellator.startDrawingQuads();
            tessellator.setColorOpaque(128, 128, 128);
            tessellator.addVertexWithUV(scrollbarX, (double) thumbY + thumbHeight, 0.0D, 0.0F, 1.0F);
            tessellator.addVertexWithUV(scrollbarEndX, (double) thumbY + thumbHeight, 0.0D, 1.0F, 1.0F);
            tessellator.addVertexWithUV(scrollbarEndX, thumbY, 0.0D, 1.0F, 0.0F);
            tessellator.addVertexWithUV(scrollbarX, thumbY, 0.0D, 0.0F, 0.0F);
            tessellator.draw();

            // Scrollbar thumb border
            tessellator.startDrawingQuads();
            tessellator.setColorOpaque(192, 192, 192);
            tessellator.addVertexWithUV(scrollbarX, thumbY + thumbHeight - 1.0, 0.0D, 0.0F, 1.0F);
            tessellator.addVertexWithUV(scrollbarEndX - 1.0, thumbY + thumbHeight - 1.0, 0.0D, 1.0F, 1.0F);
            tessellator.addVertexWithUV(scrollbarEndX - 1.0, thumbY, 0.0D, 1.0F, 0.0F);
            tessellator.addVertexWithUV(scrollbarX, thumbY,0.0D, 0.0F, 0.0F);
            tessellator.draw();
        }

        // Extra decorations hook
        this.renderDecorations(mouseX, mouseY);

        // Restore GL state
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    @SuppressWarnings("unused")
    protected void centerScrollOn(E entry) {
        int index = this.children().indexOf(entry);
        if (index < 0) return; // entry not found

        int listHeight = this.bottom - this.top;
        int entryTop = getRowTop(index);
        int entryCenter = entryTop + (this.itemHeight / 2);

        double newScroll = entryCenter - (listHeight / 2.0);
        this.setScrollAmount(newScroll);
    }

    protected void ensureVisible(E entry) {
        int rowIndex = this.children().indexOf(entry);
        if (rowIndex < 0) {
            return;
        }

        int rowTop = this.getRowTop(rowIndex);

        // How far the row is above the visible area (negative = off the top)
        int overshootAbove = rowTop - this.top - TOP_PADDING - this.itemHeight;
        if (overshootAbove < 0) {
            this.applyScrollDelta(overshootAbove);
        }

        // How far the row is below the visible area (negative = off the bottom)
        int overshootBelow = this.bottom - rowTop - this.itemHeight - this.itemHeight;
        if (overshootBelow < 0) {
            this.applyScrollDelta(-overshootBelow);
        }
    }


    private void applyScrollDelta(double deltaY) {
        if (deltaY == 0) {
            return;
        }
        this.setScrollAmount(this.getScrollAmount() + deltaY);
    }

    public double getScrollAmount() {
        return this.scrollAmount;
    }

    public void setScrollAmount(double value) {
        this.scrollAmount = MathHelper.clamp(value, 0, this.getMaxScroll());
    }

    private int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.bottom - this.top - TOP_PADDING));
    }

    protected int getScrollbarPosition() {
        int halfList = this.getRowWidth() / 2;
        int centerX = this.left + this.width / 2;
        return centerX + halfList;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        // Ignore clicks outside the list bounds
        if (!this.isMouseOver(mouseX, mouseY)) {
            return;
        }

        E clickedEntry = this.getEntryAtPosition(mouseX, mouseY);
        if (clickedEntry != null) {
            // Only change focus/selection if it's a different entry
            if (this.focused == null || this.focused != clickedEntry) {
                this.focused = clickedEntry;
                this.selected = clickedEntry;
                super.mouseClicked(mouseX, mouseY, button);
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            // Left-click on header area
            int headerClickX = (int) (mouseX - (this.left + this.width / 2.0 - this.getRowWidth() / 2.0));
            int headerClickY = (int) (mouseY - (double) this.top) + (int) this.getScrollAmount() - TOP_PADDING;
            this.onHeaderClicked(headerClickX, headerClickY);
        }
    }


    @SuppressWarnings("unused")
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        E theFocused = this.getFocused();
        if (theFocused != null) {
            return theFocused.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double deltaX, double deltaY) {
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT && isMouseOver(mouseX, mouseY)) {
            int maxScroll = this.getMaxScroll();
            if (maxScroll <= 0) {
                return false;
            }

            int visibleHeight = this.bottom - this.top;
            int contentHeight = this.getMaxPosition();

            // Same logic as in render() for thumb height
            int thumbHeight = (int) ((float) visibleHeight * visibleHeight / contentHeight);
            if (thumbHeight < 32) {
                thumbHeight = 32;
            } else if (thumbHeight > visibleHeight - 8) {
                thumbHeight = visibleHeight - 8;
            }

            int scrollTrack = visibleHeight - thumbHeight;
            if (scrollTrack <= 0) {
                return false;
            }

            // Map drag distance in screen space to scroll distance in content space
            double scrollChange = deltaY * maxScroll / scrollTrack;

            // Sign is chosen so it matches the now-correct wheel direction
            this.setScrollAmount(this.getScrollAmount() + scrollChange);
            return true;
        }
        return false;
    }

    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        // scrollDelta is Mouse.getDWheel(), e.g. ±120 or ±1 depending on the platform.
        // We mimic ScreenOptions: scrollAmount += dWheel / -0.05F;
        double amount = scrollDelta / -0.05D;

        // Apply to our scrollAmount
        this.setScrollAmount(this.getScrollAmount() + amount);
        return true;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // First let the focused entry handle it
        E theFocused = this.getFocused();
        if (theFocused != null && theFocused.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        // Handle up/down arrow movement
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            this.moveSelection(1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_UP) {
            this.moveSelection(-1);
            return true;
        }
        return false;
    }

    protected void moveSelection(int direction) {
        if (!this.children().isEmpty()) {
            int currentIndex = this.children().indexOf(this.getSelected());
            int newIndex = MathHelper.clamp(currentIndex + direction, 0, getItemCount() - 1);

            E entry = this.children().get(newIndex);
            this.setSelected(entry);
            this.ensureVisible(entry);
        }
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseY >= this.top && mouseY <= this.bottom
                && mouseX >= this.left && mouseX <= this.right;
    }

    @SuppressWarnings("java:S1172")
    protected void renderList(int rowLeft, int baseRowTop, int mouseX, int mouseY, float delta) {
        int itemCount = this.getItemCount();
        Tessellator tessellator = Tessellator.instance;

        for (int rowIndex = 0; rowIndex < itemCount; ++rowIndex) {
            int rowTop = this.getRowTop(rowIndex);
            int rowBottom = this.getRowBottom(rowIndex);

            // Only render rows that are visible in the current viewport
            if (rowBottom >= this.top && rowTop <= this.bottom) {
                int entryTop = baseRowTop + rowIndex * this.itemHeight + this.headerHeight;
                int rowHeightInner = this.itemHeight - TOP_PADDING;
                E entry = this.getEntry(rowIndex);
                int rowWidth = this.getRowWidth();

                if (this.renderSelection && this.isSelectedItem(rowIndex)) {
                    int selectionLeft = this.left + this.width / 2 - rowWidth / 2;
                    int selectionRight = this.left + this.width / 2 + rowWidth / 2;

                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    float selectionBrightness = this.isFocused() ? 1.0F : 0.5F;
                    GL11.glColor4f(selectionBrightness, selectionBrightness, selectionBrightness, 1f);

                    // Outer selection box
                    tessellator.startDrawingQuads();
                    tessellator.addVertex(selectionLeft, entryTop + rowHeightInner + 2.0, 0.0D);
                    tessellator.addVertex(selectionRight, entryTop + rowHeightInner + 2.0, 0.0D);
                    tessellator.addVertex(selectionRight, entryTop - 2.0, 0.0D);
                    tessellator.addVertex(selectionLeft, entryTop - 2.0, 0.0D);
                    tessellator.draw();

                    // Inner border
                    GL11.glColor4f(0f, 0f, 0f, 1f);
                    tessellator.startDrawingQuads();
                    tessellator.addVertex(selectionLeft + 1.0, entryTop + rowHeightInner + 1.0, 0.0D);
                    tessellator.addVertex(selectionRight - 1.0, entryTop + rowHeightInner + 1.0, 0.0D);
                    tessellator.addVertex(selectionRight - 1.0, entryTop - 1.0, 0.0D);
                    tessellator.addVertex(selectionLeft + 1.0, entryTop - 1.0, 0.0D);
                    tessellator.draw();

                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                }

                int entryLeft = this.getRowLeft();
                boolean hovered = this.isMouseOver(mouseX, mouseY)
                        && Objects.equals(this.getEntryAtPosition(mouseX, mouseY), entry);

                entry.render(
                        rowIndex,
                        rowTop,
                        entryLeft,
                        rowWidth,
                        rowHeightInner,
                        mouseX,
                        mouseY,
                        hovered,
                        delta
                );
            }
        }
    }

    protected int getRowLeft() {
        return this.left + this.width / 2 - this.getRowWidth() / 2 + 2;
    }

    protected int getRowTop(int rowIndex) {
        return this.top + TOP_PADDING - (int)this.getScrollAmount() + rowIndex * this.itemHeight + this.headerHeight;
    }

    int getRowBottom(int rowIndex) {
        return this.getRowTop(rowIndex) + this.itemHeight;
    }

    protected boolean isFocused() {
        return false;
    }

    @SuppressWarnings("SameParameterValue")
    protected void renderHoleBackground(int topY, int bottomY, int topAlpha, int bottomAlpha) {
        Tessellator tessellator = Tessellator.instance;
        this.minecraft.textureManager.bindTexture(
                this.minecraft.textureManager.loadTexture("/gui/background.png")
        );

        GL11.glColor4f(1f, 1f, 1f, 1f);

        tessellator.startDrawingQuads();

        // bottom fade
        tessellator.setColorRGBA(64, 64, 64, bottomAlpha);
        tessellator.addVertexWithUV(this.left, bottomY, 0.0D, 0.0F, bottomY / 32.0F);
        tessellator.addVertexWithUV((double) this.left + this.width, bottomY, 0.0D, this.width / 32.0F, bottomY / 32.0F);

        // top fade
        tessellator.setColorRGBA(64, 64, 64, topAlpha);
        tessellator.addVertexWithUV((double) this.left + this.width, topY, 0.0D, this.width / 32.0F, topY / 32.0F);
        tessellator.addVertexWithUV(this.left, topY, 0.0D, 0.0F, topY / 32.0F);

        tessellator.draw();
    }


    protected E remove(int index) {
        E entry = this.children.get(index);
        return this.removeEntry(entry) ? entry : null;
    }

    protected boolean removeEntry(E entry) {
        boolean removed = this.children.remove(entry);
        if (removed && entry == this.getSelected()) {
            this.setSelected(null);
        }
        return removed;
    }

    @Environment(EnvType.CLIENT)
    class EntryList extends AbstractList<E> {
        private final List<E> entries;

        private EntryList() {
            this.entries = Lists.newArrayList();
        }
        @Override
        public E get(int index) {
            return this.entries.get(index);
        }
        @Override
        public int size() {
            return this.entries.size();
        }

        @Override
        public E set(int index, E entry) {
            return this.entries.set(index, entry);
        }

        @Override
        public void add(int index, E entry) {
            this.entries.add(index, entry);
        }

        @Override
        public E remove(int index) {
            return this.entries.remove(index);
        }
    }

    @SuppressWarnings("unused")
    @Environment(EnvType.CLIENT)
    public abstract static class Entry<E extends EntryListWidget.Entry<E>> extends Screen {
        protected Entry() {}

        public abstract void render(
                int index,
                int rowTop,
                int rowLeft,
                int rowWidth,
                int rowHeight,
                int mouseX,
                int mouseY,
                boolean hovered,
                float delta
        );

        @SuppressWarnings("unused")
        public void mouseMoved(double mouseX, double mouseY) {}

        @Override
        public void mouseClicked(int mouseX, int mouseY, int button) {}

        @SuppressWarnings({"UnusedReturnValue", "unused"})
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return false;
        }

        @SuppressWarnings({"java:S1172", "unused"})
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
            return false;
        }

        @SuppressWarnings("java:S1172")
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return false;
        }

        @SuppressWarnings({"java:S1172", "unused"})
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            return false;
        }

        @SuppressWarnings({"java:S1172", "unused"})
        public boolean charTyped(char character, int keyCode) {
            return false;
        }

        @SuppressWarnings({"java:S1172", "unused"})
        public boolean changeFocus(boolean moveForward) {
            return false;
        }
    }
}
