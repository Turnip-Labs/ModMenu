package io.github.prospector.modmenu.gui;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.gui.entries.ChildEntry;
import io.github.prospector.modmenu.gui.entries.IndependentEntry;
import io.github.prospector.modmenu.gui.entries.ParentEntry;
import io.github.prospector.modmenu.util.ModListSearch;
import io.github.prospector.modmenu.util.TestModContainer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.util.helper.MathHelper;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.*;

public class ModListWidget extends AlwaysSelectedEntryListWidget<ModListEntry> implements AutoCloseable {
    public static final boolean DEBUG = Boolean.getBoolean("modmenu.debug");

    private final Map<Path, BufferedImage> modIconsCache = new HashMap<>();
    private final ModListScreen parent;

    private List<ModContainer> modContainerList = null;
    private final Set<ModContainer> addedMods = new HashSet<>();
    private String selectedModId = null;

    public ModListWidget(Minecraft client, int width, int height, int top, int bottom, int entryHeight, String searchTerm, ModListWidget previousList, ModListScreen parent) {
        super(client, width, height, top, bottom, entryHeight);
        this.parent = parent;

        if (previousList != null) {
            this.modContainerList = previousList.modContainerList;
        }

        this.filter(searchTerm, false);

        int maxScrollRange = Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
        setScrollAmount(parent.getScrollPercent() * maxScrollRange);
    }

    @Override
    public void setScrollAmount(double amount) {
        super.setScrollAmount(amount);
        int maxScrollRange = Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
        if (maxScrollRange == 0) {
            parent.updateScrollPercent(0);
        } else {
            parent.updateScrollPercent(getScrollAmount() / maxScrollRange);
        }
    }

    public void select(ModListEntry entry) {
        this.setSelected(entry);
    }

    @Override
    public void setSelected(ModListEntry entry) {
        super.setSelected(entry);
        selectedModId = entry.getMetadata().getId();
        parent.updateSelectedEntry(getSelected());
    }

    @Override
    protected boolean isSelectedItem(int index) {
        ModListEntry selected = getSelected();
        if (selected == null) {
            return false;
        }
        return selected.getMetadata().getId().equals(getEntry(index).getMetadata().getId());
    }

    @Override
    public int addEntry(ModListEntry entry) {
        if (addedMods.contains(entry.container)) {
            return 0;
        }
        addedMods.add(entry.container);
        int idx = super.addEntry(entry);
        if (entry.getMetadata().getId().equals(selectedModId)) {
            setSelected(entry);
        }
        return idx;
    }

    @Override
    protected boolean removeEntry(ModListEntry entry) {
        addedMods.remove(entry.container);
        return super.removeEntry(entry);
    }

    @Override
    protected ModListEntry remove(int index) {
        addedMods.remove(getEntry(index).container);
        return super.remove(index);
    }

    public void reloadFilters() {
        filter(parent.getSearchInput(), true);
    }

    public void filter(String searchTerm, boolean refresh) {
        this.clearEntries();
        addedMods.clear();

        Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();

        if (DEBUG) {
            mods = new ArrayList<>(mods);
            mods.addAll(TestModContainer.getTestModContainers());
        }

        if (this.modContainerList == null || refresh) {
            this.modContainerList = new ArrayList<>();
            this.modContainerList.addAll(mods);
            this.modContainerList.sort(ModMenuConfigManager.getConfig().getSorting().getComparator());
        }

        List<ModContainer> matched = ModListSearch.search(parent, searchTerm, modContainerList);

        for (ModContainer container : matched) {
            ModMetadata metadata = container.getMetadata();
            String modId = metadata.getId();
            boolean library = ModMenu.LIBRARY_MODS.contains(modId);

            if (library && !ModMenuConfigManager.getConfig().showLibraries()) {
                continue;
            }

            if (!ModMenu.PARENT_MAP.values().contains(container)) {
                if (ModMenu.PARENT_MAP.keySet().contains(container)) {
                    List<ModContainer> children = ModMenu.PARENT_MAP.get(container);
                    children.sort(ModMenuConfigManager.getConfig().getSorting().getComparator());
                    ParentEntry parentEntry = new ParentEntry(minecraft, container, children, this);
                    this.addEntry(parentEntry);

                    if (this.parent.getShowModChildren().contains(modId)) {
                        List<ModContainer> validChildren = ModListSearch.search(this.parent, searchTerm, children);
                        for (ModContainer child : validChildren) {
                            boolean lastChild = validChildren.indexOf(child) == validChildren.size() - 1;
                            this.addEntry(new ChildEntry(minecraft, child, this, lastChild));
                        }
                    }
                } else {
                    this.addEntry(new IndependentEntry(minecraft, container, this));
                }
            }
        }
        ModListEntry theSelected = this.getSelected();
        if ((parent.getSelectedEntry() != null && !children().isEmpty())
                || (theSelected != null && theSelected.getMetadata() != parent.getSelectedEntry().getMetadata())) {
            for (ModListEntry entry : children()) {
                if (entry.getMetadata().equals(parent.getSelectedEntry().getMetadata())) {
                    setSelected(entry);
                }
            }
        } else if (getSelected() == null && !children().isEmpty() && getEntry(0) != null) {
            setSelected(getEntry(0));
        }

        int maxScrollRange = Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
        if (getScrollAmount() > maxScrollRange) {
            setScrollAmount(maxScrollRange);
        }
    }

    @Override
    protected void renderList(int rowLeft, int baseRowTop, int mouseX, int mouseY, float delta) {
        int itemCount = this.getItemCount();
        Tessellator tessellator = Tessellator.instance;

        for (int index = 0; index < itemCount; ++index) {
            int rowTop = this.getRowTop(index);
            int rowBottom = this.getRowBottom(index);

            if (rowBottom >= this.top && rowTop <= this.bottom) {
                ModListEntry entry = this.getEntry(index);
                int entryTop = rowTop + 2;
                int rowHeightInner = this.itemHeight - 4;
                int rowWidth = this.getRowWidth();

                if (this.renderSelection && this.isSelectedItem(index)) {
                    int selectionLeft = this.getRowLeft() - 2 + entry.getXOffset();
                    int selectionRight = rowLeft + rowWidth + 2;

                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    float brightness = this.isFocused() ? 1.0F : 0.5F;
                    GL11.glColor4f(brightness, brightness, brightness, 1f);

                    tessellator.startDrawingQuads();
                    tessellator.addVertex(selectionLeft, entryTop + rowHeightInner + 2.0, 0.0D);
                    tessellator.addVertex(selectionRight, entryTop + rowHeightInner + 2.0, 0.0D);
                    tessellator.addVertex(selectionRight, entryTop - 2.0, 0.0D);
                    tessellator.addVertex(selectionLeft, entryTop - 2.0, 0.0D);
                    tessellator.draw();

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
                        index,
                        entryTop,
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
    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!this.isMouseOver(mouseX, mouseY)) {
            return;
        }

        ModListEntry clickedEntry = this.getEntryAtPosition(mouseX, mouseY);
        if (clickedEntry != null) {
            if (this.getFocused() != clickedEntry) {
                this.setFocused(clickedEntry);
            }
            // use setter so ModListWidget.setSelected runs
            this.setSelected(clickedEntry);

            // let the entry react (ModListEntry will call list.select(this) etc)
            clickedEntry.mouseClicked(mouseX, mouseY, button);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            int headerClickX = (int)(mouseX - (this.left + this.width / 2.0 - this.getRowWidth() / 2.0));
            int headerClickY = (int)(mouseY - (double)this.top) + (int)this.getScrollAmount() - TOP_PADDING;
            this.onHeaderClicked(headerClickX, headerClickY);
        }
    }

    @Override
    protected ModListEntry getEntryAtPosition(double mouseX, double mouseY) {
        int yInList = MathHelper.floor(mouseY - this.top) - this.headerHeight + (int) this.getScrollAmount() - 4;
        int rowIndex = yInList / this.itemHeight;

        int rowLeft = this.getRowLeft();
        int rowRight = rowLeft + this.getRowWidth();

        if (mouseX >= this.getScrollbarPosition() || mouseX < rowLeft || mouseX > rowRight) {
            return null;
        }

        if (yInList < 0 || rowIndex < 0 || rowIndex >= this.getItemCount()) {
            return null;
        }

        return this.children().get(rowIndex);
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width - SCROLLBAR_WIDTH;
    }

    @Override
    public int getRowWidth() {
        int scrollRange = Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
        return this.width - (scrollRange > 0 ? 18 : 12);
    }

    @Override
    protected int getRowLeft() {
        return this.left + 6;
    }

    public int getWidth() {
        return this.width;
    }

    @SuppressWarnings("unused")
    public int getTop() {
        return this.top;
    }

    public ModListScreen getParent() {
        return parent;
    }

    @Override
    protected int getMaxPosition() {
        return super.getMaxPosition() + 4;
    }

    public int getDisplayedCount() {
        return children().size();
    }

    @Override
    public void close() {
        this.children().forEach(ModListEntry::deleteTexture);
    }

    BufferedImage getCachedModIcon(Path path) {
        return this.modIconsCache.get(path);
    }

    void cacheModIcon(Path path, BufferedImage texture) {
        this.modIconsCache.put(path, texture);
    }

    @SuppressWarnings("unused")
    public Set<ModContainer> getCurrentModSet() {
        return addedMods;
    }
}
