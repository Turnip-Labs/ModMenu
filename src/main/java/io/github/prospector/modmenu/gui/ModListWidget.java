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
import net.minecraft.client.render.Tessellator;
import net.minecraft.core.util.helper.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.*;

public class ModListWidget extends AlwaysSelectedEntryListWidget<ModListEntry> implements AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final boolean DEBUG = Boolean.getBoolean("modmenu.debug");

	private final Map<Path, BufferedImage> modIconsCache = new HashMap<>();
	private final ModListScreen parent;
	private List<ModContainer> modContainerList = null;
	private Set<ModContainer> addedMods = new HashSet<>();
	private String selectedModId = null;
	private boolean scrolling;
	private boolean isFocused;

	public ModListWidget(Minecraft client, int width, int height, int y1, int y2, int entryHeight, String searchTerm, ModListWidget list, ModListScreen parent) {
		super(client, width, height, y1, y2, entryHeight);
		this.parent = parent;
		if (list != null) {
			this.modContainerList = list.modContainerList;
		}
		this.filter(searchTerm, false);
		setScrollAmount(parent.getScrollPercent() * Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
	}

	@Override
	public void setScrollAmount(double amount) {
		super.setScrollAmount(amount);
		int denominator = Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
		if (denominator <= 0) {
			parent.updateScrollPercent(0);
		} else {
			parent.updateScrollPercent(getScrollAmount() / Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
		}
	}

	@Override
	protected boolean isFocused() {
		return isFocused;
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
		return selected != null && selected.getMetadata().getId().equals(getEntry(index).getMetadata().getId());
	}

	@Override
	public int addEntry(ModListEntry entry) {
		if (addedMods.contains(entry.container)) {
			return 0;
		}
		addedMods.add(entry.container);
		int i = super.addEntry(entry);
		if (entry.getMetadata().getId().equals(selectedModId)) {
			setSelected(entry);
		}
		return i;
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
		filter(parent.getSearchInput(), true, false);
	}


	public void filter(String searchTerm, boolean refresh) {
		filter(searchTerm, refresh, true);
	}

	private void filter(String searchTerm, boolean refresh, boolean search) {
		this.clearEntries();
		addedMods.clear();
		Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();

		if (DEBUG) {
			mods = new ArrayList<>(mods);
			mods.addAll(TestModContainer.getTestModContainers());
		}

		if (this.modContainerList == null || refresh) {
			this.modContainerList = new ArrayList<>();
			modContainerList.addAll(mods);
			this.modContainerList.sort(ModMenuConfigManager.getConfig().getSorting().getComparator());
		}

		boolean validSearch = ModListSearch.validSearchQuery(searchTerm);
		List<ModContainer> matched = ModListSearch.search(parent, searchTerm, modContainerList);

		for (ModContainer container : matched) {
			ModMetadata metadata = container.getMetadata();
			String modId = metadata.getId();
			boolean library = ModMenu.LIBRARY_MODS.contains(modId);

			//Hide parent lib mods when the config is set to hide
			if (library && !ModMenuConfigManager.getConfig().showLibraries()) {
				continue;
			}

			if (!ModMenu.PARENT_MAP.values().contains(container)) {
				if (ModMenu.PARENT_MAP.keySet().contains(container)) {
					//Add parent mods when not searching
					List<ModContainer> children = ModMenu.PARENT_MAP.get(container);
					children.sort(ModMenuConfigManager.getConfig().getSorting().getComparator());
					ParentEntry parent = new ParentEntry(minecraft, container, children, this);
					this.addEntry(parent);
					//Add children if they are meant to be shown
					if (this.parent.showModChildren.contains(modId)) {
						List<ModContainer> validChildren = ModListSearch.search(this.parent, searchTerm, children);
						for (ModContainer child : validChildren) {
							this.addEntry(new ChildEntry(minecraft, child, parent, this, validChildren.indexOf(child) == validChildren.size() - 1));
						}
					}
				} else {
					//A mod with no children
					this.addEntry(new IndependentEntry(minecraft, container, this));
				}
			}
		}

		if (parent.getSelectedEntry() != null && !children().isEmpty() || this.getSelected() != null && getSelected().getMetadata() != parent.getSelectedEntry().getMetadata()) {
			for (ModListEntry entry : children()) {
				if (entry.getMetadata().equals(parent.getSelectedEntry().getMetadata())) {
					setSelected(entry);
				}
			}
		} else {
			if (getSelected() == null && !children().isEmpty() && getEntry(0) != null) {
				setSelected(getEntry(0));
			}
		}

		if (getScrollAmount() > Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4))) {
			setScrollAmount(Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
		}
	}
	@Override
	protected void renderList(int x, int y, int mouseX, int mouseY, float delta) {
		int itemCount = this.getItemCount();
		Tessellator tessellator_1 = Tessellator.instance;

		for (int index = 0; index < itemCount; ++index) {
			int entryTop = this.getRowTop(index) + 2;
			int entryBottom = this.getRowTop(index) + this.itemHeight;
			if (entryBottom >= this.top && entryTop <= this.bottom) {
				int entryHeight = this.itemHeight - 4;
				ModListEntry entry = this.getEntry(index);
				int rowWidth = this.getRowWidth();
				int entryLeft;
				if (this.renderSelection && this.isSelectedItem(index)) {
					entryLeft = getRowLeft() - 2 + entry.getXOffset();
					int selectionRight = x + rowWidth + 2;
					GL11.glDisable(GL11.GL_TEXTURE_2D);
					float float_2 = this.isFocused() ? 1.0F : 0.5F;
					GL11.glColor4f(float_2, float_2, float_2, 1f);
					tessellator_1.startDrawingQuads();
					tessellator_1.addVertex((double) entryLeft, (double) (entryTop + entryHeight + 2), 0.0D);
					tessellator_1.addVertex((double) selectionRight, (double) (entryTop + entryHeight + 2), 0.0D);
					tessellator_1.addVertex((double) selectionRight, (double) (entryTop - 2), 0.0D);
					tessellator_1.addVertex((double) entryLeft, (double) (entryTop - 2), 0.0D);
					tessellator_1.draw();
					GL11.glColor4f(0f, 0f, 0f, 1f);
					tessellator_1.startDrawingQuads();
					tessellator_1.addVertex((double) (entryLeft + 1), (double) (entryTop + entryHeight + 1), 0.0D);
					tessellator_1.addVertex((double) (selectionRight - 1), (double) (entryTop + entryHeight + 1), 0.0D);
					tessellator_1.addVertex((double) (selectionRight - 1), (double) (entryTop - 1), 0.0D);
					tessellator_1.addVertex((double) (entryLeft + 1), (double) (entryTop - 1), 0.0D);
					tessellator_1.draw();
					GL11.glEnable(GL11.GL_TEXTURE_2D);
				}

				entryLeft = this.getRowLeft();
				entry.render(index, entryTop, entryLeft, rowWidth, entryHeight, mouseX, mouseY, this.isMouseOver((double) mouseX, (double) mouseY) && Objects.equals(this.getEntryAtPos((double) mouseX, (double) mouseY), entry), delta);
			}
		}

	}

	@Override
	protected void updateScrollingState(double double_1, double double_2, int int_1) {
		super.updateScrollingState(double_1, double_2, int_1);
		this.scrolling = int_1 == 0 && double_1 >= (double) this.getScrollbarPosition() && double_1 < (double) (this.getScrollbarPosition() + 6);
	}

	@Override
	public void mouseClicked(int double_1, int double_2, int int_1) {
		this.updateScrollingState(double_1, double_2, int_1);
		if (this.isMouseOver(double_1, double_2))  {
			ModListEntry entry = this.getEntryAtPos(double_1, double_2);
			if (entry != null) {
                if (entry.list.getFocused() != null) {
                    if (!entry.list.getFocused().equals(entry)) {
                        this.setFocused(entry);
                        this.setSelected(entry);
                        this.setDragging(true);
                        super.mouseClicked(double_1, double_2, int_1);
                    }
                } else {
                    this.setFocused(entry);
                    this.setSelected(entry);
                    this.setDragging(true);
                    super.mouseClicked(double_1, double_2, int_1);
                }
			} else if (int_1 == 0) {
				this.clickedHeader((int) (double_1 - (double) (this.left + this.width / 2 - this.getRowWidth() / 2)), (int) (double_2 - (double) this.top) + (int) this.getScrollAmount() - 4);
			}
		}
	}

	public final ModListEntry getEntryAtPos(double x, double y) {
		int int_5 = MathHelper.floor_double(y - (double) this.top) - this.headerHeight + (int) this.getScrollAmount() - 4; // convertToBlockCoord
		int index = int_5 / this.itemHeight;
		return x < (double) this.getScrollbarPosition() && x >= (double) getRowLeft() && x <= (double) (getRowLeft() + getRowWidth()) && index >= 0 && int_5 >= 0 && index < this.getItemCount() ? this.children().get(index) : null;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.width - 6;
	}

	@Override
	public int getRowWidth() {
		return this.width - (Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)) > 0 ? 18 : 12);
	}

	@Override
	protected int getRowLeft() {
		return left + 6;
	}

	public int getWidth() {
		return width;
	}

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

	void cacheModIcon(Path path, BufferedImage tex) {
		this.modIconsCache.put(path, tex);
	}

	public Set<ModContainer> getCurrentModSet() {
		return addedMods;
	}
}
