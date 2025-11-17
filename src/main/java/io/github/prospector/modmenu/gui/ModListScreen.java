package io.github.prospector.modmenu.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.util.BadgeRenderer;
import io.github.prospector.modmenu.util.ButtonUtil;
import io.github.prospector.modmenu.util.HardcodedUtil;
import io.github.prospector.modmenu.util.RenderUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.render.Font;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.Global;
import net.minecraft.core.lang.I18n;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.spongepowered.include.com.google.common.base.Joiner;

import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.*;

public class ModListScreen extends Screen {
    private static final String FILTERS_BUTTON_LOCATION =
            "/assets/" + ModMenu.MOD_ID + "/textures/gui/filters_button.png";
    private static final String CONFIGURE_BUTTON_LOCATION =
            "/assets/" + ModMenu.MOD_ID + "/textures/gui/configure_button.png";

    private final String textTitle;
    private final Screen parent;

    private TextFieldWidget searchBox;
    private DescriptionListWidget descriptionListWidget;
    private ModListWidget modList;
    private String tooltip;
    private ModListEntry selected;
    private BadgeRenderer badgeRenderer;

    private double scrollPercent = 0;
    private boolean showModCount = false;
    private boolean init = false;
    private boolean filterOptionsShown = false;
    private int paneY;
    private int paneWidth;
    private int rightPaneX;
    private int searchBoxX;
    private final Set<String> showModChildren = new HashSet<>();
    private String lastSearchString = null;

    private static final int CONFIGURE_BUTTON_ID = 0;
    private static final int WEBSITE_BUTTON_ID = 1;
    private static final int ISSUES_BUTTON_ID = 2;
    private static final int TOGGLE_FILTER_OPTIONS_BUTTON_ID = 3;
    private static final int TOGGLE_SORT_MODE_BUTTON_ID = 4;
    private static final int TOGGLE_SHOW_LIBRARIES_BUTTON_ID = 5;
    private static final int MODS_FOLDER_BUTTON_ID = 6;
    private static final int DONE_BUTTON_ID = 7;

    public ModListScreen(Screen previousGui) {
        this.parent = previousGui;
        this.textTitle = I18n.getInstance().translateKey("modmenu.title");
    }

    @Override
    public void updateEvents() {
        super.updateEvents();

        int dWheel = Mouse.getDWheel();
        if (dWheel == 0) {
            return;
        }

        int mouseX = Mouse.getX() * this.width / this.mc.resolution.getWidthScreenCoords();
        int mouseY = this.height - Mouse.getY() * this.height / this.mc.resolution.getHeightScreenCoords() - 1;

        if (modList.isMouseOver(mouseX, mouseY)) {
            // pass raw wheel delta
            modList.mouseScrolled(mouseX, mouseY, dWheel);
        } else if (descriptionListWidget.isMouseOver(mouseX, mouseY)) {
            descriptionListWidget.mouseScrolled(mouseX, mouseY, dWheel);
        }
    }




    @SuppressWarnings("unused")
    public void mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (modList.isMouseOver(mouseX, mouseY)) {
            this.modList.mouseScrolled(mouseX, mouseY, scrollDelta);
        }
        if (descriptionListWidget.isMouseOver(mouseX, mouseY)) {
            this.descriptionListWidget.mouseScrolled(mouseX, mouseY, scrollDelta);
        }
    }

    @Override
    public void tick() {
        this.searchBox.updateCursorCounter();
    }

    @Override
    public void init() {
        I18n i18n = I18n.getInstance();
        Keyboard.enableRepeatEvents(true);
        Font font = this.font;

        paneY = 48;
        paneWidth = this.width / 2 - 8;
        rightPaneX = width - paneWidth;

        int searchBoxWidth = paneWidth - 32 - 22;
        searchBoxX = paneWidth / 2 - searchBoxWidth / 2 - 22 / 2;

        String oldText = this.searchBox == null ? "" : this.searchBox.getText();
        this.searchBox = new TextFieldWidget(this.font, searchBoxX, 22, searchBoxWidth, 20,
                i18n.translateKey("modmenu.search"));
        this.searchBox.setText(oldText);

        this.modList = new ModListWidget(this.mc, paneWidth, this.height,
                paneY + 19, this.height - 36, 36,
                this.searchBox.getText(), this.modList, this);
        this.modList.setLeftEdge(0);

        this.descriptionListWidget = new DescriptionListWidget(this.mc, paneWidth, this.height,
                paneY + 60, this.height - 36, 9 + 1, this);
        this.descriptionListWidget.setLeftEdge(rightPaneX);

        ButtonElement configureButton = new ModMenuTexturedButtonWidget(
                CONFIGURE_BUTTON_ID, width - 24, paneY, 20, 20,
                0, 0, CONFIGURE_BUTTON_LOCATION, 32, 64
        ) {
            @Override
            public void render(Minecraft mc, int mouseX, int mouseY) {
                if (selected != null) {
                    String modid = selected.getMetadata().getId();
                    enabled = ModMenu.hasConfigScreenFactory(modid)
                            || ModMenu.hasLegacyConfigScreenTask(modid);
                } else {
                    enabled = false;
                }
                visible = enabled;
                GL11.glColor4f(1f, 1f, 1f, 1f);
                super.render(mc, mouseX, mouseY);
            }
        };

        int urlButtonWidths = paneWidth / 2 - 2;
        int cappedButtonWidth = Math.min(urlButtonWidths, 200);

        ButtonElement websiteButton = new ButtonElement(
                WEBSITE_BUTTON_ID,
                rightPaneX + (urlButtonWidths / 2) - (cappedButtonWidth / 2),
                paneY + 36,
                cappedButtonWidth, 20,
                i18n.translateKey("modmenu.website")
        ) {
            @Override
            public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                visible = selected != null;
                enabled = visible
                        && selected.getMetadata().getContact().get("homepage").isPresent();
                super.drawButton(mc, mouseX, mouseY);
            }
        };

        ButtonElement issuesButton = new ButtonElement(
                ISSUES_BUTTON_ID,
                rightPaneX + urlButtonWidths + 4 + (urlButtonWidths / 2) - (cappedButtonWidth / 2),
                paneY + 36,
                cappedButtonWidth, 20,
                i18n.translateKey("modmenu.issues")
        ) {
            @Override
            public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                visible = selected != null;
                enabled = visible
                        && selected.getMetadata().getContact().get("issues").isPresent();
                super.drawButton(mc, mouseX, mouseY);
            }
        };

        this.buttons.add(new ModMenuTexturedButtonWidget(
                TOGGLE_FILTER_OPTIONS_BUTTON_ID,
                paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 2,
                22, 20, 20,
                0, 0, FILTERS_BUTTON_LOCATION, 32, 64
        ) {
            @Override
            public void render(Minecraft mc, int mouseX, int mouseY) {
                super.render(mc, mouseX, mouseY);
                if (isHovered(mouseX, mouseY)) {
                    setTooltip(i18n.translateKey("modmenu.toggleFilterOptions"));
                }
            }
        });

        String showLibrariesText = i18n.translateKeyAndFormat(
                "modmenu.showLibraries",
                i18n.translateKey("modmenu.showLibraries." + ModMenuConfigManager.getConfig().showLibraries())
        );
        String sortingText = i18n.translateKeyAndFormat(
                "modmenu.sorting",
                ModMenuConfigManager.getConfig().getSorting().getName()
        );
        int showLibrariesWidth = font.getStringWidth(showLibrariesText) + 20;
        int sortingWidth = font.getStringWidth(sortingText) + 20;

        int filtersWidth = showLibrariesWidth + sortingWidth + 2;
        int filtersX;

        String modCountString = i18n.translateKeyAndFormat(
                "modmenu.showingMods",
                NumberFormat.getInstance().format(modList.getDisplayedCount())
                        + "/"
                        + NumberFormat.getInstance().format(FabricLoader.getInstance().getAllMods().size())
        );

        if ((filtersWidth + font.getStringWidth(modCountString) + 20)
                >= searchBoxX + searchBoxWidth + 22) {
            filtersX = paneWidth / 2 - filtersWidth / 2;
            showModCount = false;
        } else {
            filtersX = searchBoxX + searchBoxWidth + 22 - filtersWidth + 1;
            showModCount = true;
        }

        this.buttons.add(new ButtonElement(
                TOGGLE_SORT_MODE_BUTTON_ID,
                filtersX, 45,
                sortingWidth, 20,
                sortingText
        ) {
            @Override
            public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                visible = enabled = filterOptionsShown;
                this.displayString = i18n.translateKeyAndFormat(
                        "modmenu.sorting",
                        ModMenuConfigManager.getConfig().getSorting().getName()
                );
                super.drawButton(mc, mouseX, mouseY);
            }
        });

        this.buttons.add(new ButtonElement(
                TOGGLE_SHOW_LIBRARIES_BUTTON_ID,
                filtersX + sortingWidth + 2, 45,
                showLibrariesWidth, 20,
                showLibrariesText
        ) {
            @Override
            public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                visible = enabled = filterOptionsShown;
                this.displayString = i18n.translateKeyAndFormat(
                        "modmenu.showLibraries",
                        i18n.translateKey("modmenu.showLibraries."
                                + ModMenuConfigManager.getConfig().showLibraries())
                );
                super.drawButton(mc, mouseX, mouseY);
            }
        });

        this.buttons.add(configureButton);
        this.buttons.add(websiteButton);
        this.buttons.add(issuesButton);
        this.buttons.add(ButtonUtil.createButton(
                MODS_FOLDER_BUTTON_ID,
                this.width / 2 - 154,
                this.height - 28,
                150, 20,
                "Open Mods Folder"
        ));
        this.buttons.add(ButtonUtil.createButton(
                DONE_BUTTON_ID,
                this.width / 2 + 4,
                this.height - 28,
                150, 20,
                "Done"
        ));

        this.searchBox.setFocused(true);
        init = true;
    }

    @SuppressWarnings("java:S131")
    @Override
    protected void buttonClicked(ButtonElement button) {
        switch (button.id) {
            case CONFIGURE_BUTTON_ID: {
                String modid = Objects.requireNonNull(selected).getMetadata().getId();
                Screen screen = ModMenu.getConfigScreen(modid, this);
                if (screen != null) {
                    mc.displayScreen(screen);
                } else {
                    ModMenu.openConfigScreen(modid);
                }
                break;
            }
            case WEBSITE_BUTTON_ID: {
                ModMetadata metadata = Objects.requireNonNull(selected).getMetadata();
                metadata.getContact().get("homepage").ifPresent(Sys::openURL);
                break;
            }
            case ISSUES_BUTTON_ID: {
                ModMetadata metadata = Objects.requireNonNull(selected).getMetadata();
                metadata.getContact().get("issues").ifPresent(Sys::openURL);
                break;
            }
            case TOGGLE_FILTER_OPTIONS_BUTTON_ID: {
                filterOptionsShown = !filterOptionsShown;
                break;
            }
            case TOGGLE_SORT_MODE_BUTTON_ID: {
                ModMenuConfigManager.getConfig().toggleSortMode();
                modList.reloadFilters();
                break;
            }
            case TOGGLE_SHOW_LIBRARIES_BUTTON_ID: {
                ModMenuConfigManager.getConfig().toggleShowLibraries();
                modList.reloadFilters();
                break;
            }
            case MODS_FOLDER_BUTTON_ID: {
                Path modsFolder = FabricLoader.getInstance().getGameDir().resolve("mods");
                try {
                    String os = System.getProperty("os.name").toLowerCase();

                    if (os.contains("win")) {
                        // Windows Explorer
                        new ProcessBuilder("explorer.exe", modsFolder.toString()).start();
                    } else if (os.contains("mac")) {
                        // macOS Finder
                        new ProcessBuilder("open", modsFolder.toString()).start();
                    } else {
                        // Linux / BSD: xdg-open (freedesktop standard)
                        new ProcessBuilder("xdg-open", modsFolder.toString()).start();
                    }
                } catch (IOException e) {
                    ModMenu.LOGGER.error("Failed to open mods folder", e);
                }
                break;
            }
            case DONE_BUTTON_ID: {
                mc.displayScreen(parent);
                break;
            }
        }
    }

    @SuppressWarnings("unused")
    public ModListWidget getModList() {
        return modList;
    }

    @Override
    public void keyPressed(char typedChar, int keyCode, int mouseX, int mouseY) {
        this.searchBox.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == 1) { // ESC (Minecraft's internal keycode)
            this.mc.displayScreen(this.parent);
        }
        modList.keyPressed(keyCode, 0, 0);
        descriptionListWidget.keyPressed(keyCode, 0, 0);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        modList.mouseClicked(mouseX, mouseY, mouseButton);
        descriptionListWidget.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        if (mouseButton != -1) {
            modList.mouseReleased(mouseX, mouseY, mouseButton);
            descriptionListWidget.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        I18n i18n = I18n.getInstance();

        int mouseDX = Mouse.getDX() * this.width / this.mc.resolution.getWidthScreenCoords();
        int mouseDY = -Mouse.getDY() * this.height / this.mc.resolution.getHeightScreenCoords();

        for (int button = 0; button < Mouse.getButtonCount(); button++) {
            if (Mouse.isButtonDown(button)) {
                modList.mouseDragged(mouseX, mouseY, button, mouseDX, mouseDY);
                descriptionListWidget.mouseDragged(mouseX, mouseY, button, mouseDX, mouseDY);
            }
        }

        Font font = this.font;

        if (!searchBox.getText().equals(lastSearchString)) {
            lastSearchString = searchBox.getText();
            modList.filter(lastSearchString, false);
        }

        overlayBackground(paneWidth, 0, rightPaneX, height, 64, 64, 64, 255, 255);
        this.tooltip = null;

        ModListEntry selectedEntry = selected;
        if (selectedEntry != null) {
            this.descriptionListWidget.render(mouseX, mouseY, delta);
        }

        this.modList.render(mouseX, mouseY, delta);
        this.searchBox.drawTextBox();

        GL11.glDisable(GL11.GL_BLEND);
        this.drawStringCentered(font, this.textTitle, this.modList.getWidth() / 2, 8, 0xffffff);
        super.render(mouseX, mouseY, delta);

        if (showModCount || !filterOptionsShown) {
            String showModCountString = i18n.translateKeyAndFormat(
                    "modmenu.showingMods",
                    NumberFormat.getInstance().format(modList.getDisplayedCount())
                            + "/"
                            + NumberFormat.getInstance().format(FabricLoader.getInstance().getAllMods().size())
            );
            font.drawString(showModCountString, searchBoxX, 52, 0xFFFFFF);
        }

        if (selectedEntry != null) {
            ModMetadata metadata = selectedEntry.getMetadata();
            int x = rightPaneX;

            GL11.glColor4f(1f, 1f, 1f, 1f);
            this.selected.bindIconTexture();
            drawIconQuad(x, paneY); // replaces ModListEntry.internalRender

            int lineSpacing = 9 + 1;
            int imageOffset = 36;

            String name = metadata.getName();
            if (name.equals("Minecraft")) { // BAD CODE
                name = "Better than Adventure";
            }
            name = HardcodedUtil.formatFabricModuleName(name);

            String trimmedName = getString(font, name, this.width - (x + imageOffset));
            font.drawString(trimmedName, x + imageOffset, paneY + 1, 0xFFFFFF);

            if (mouseX > x + imageOffset
                    && mouseY > paneY + 1
                    && mouseY < paneY + 1 + 9
                    && mouseX < x + imageOffset + font.getStringWidth(trimmedName)) {
                setTooltip(i18n.translateKeyAndFormat("modmenu.modIdToolTip", metadata.getId()));
            }

            if (init || badgeRenderer == null || badgeRenderer.getMetadata() != metadata) {
                badgeRenderer = new BadgeRenderer(
                        mc,
                        x + imageOffset + font.getStringWidth(trimmedName) + 2,
                        paneY,
                        width - 28,
                        selectedEntry.container,
                        this
                );
                init = false;
            }
            badgeRenderer.draw(mouseX, mouseY);

            String versionString;
            if (metadata.getName().equals("Minecraft")) {  // BAD CODE
                versionString = Global.VERSION;
            } else {
                versionString = metadata.getVersion().getFriendlyString();
            }
            font.drawString("v" + versionString, x + imageOffset, paneY + 2 + lineSpacing, 0x808080);

            List<String> names = new ArrayList<>();
            metadata.getAuthors().stream()
                    .filter(Objects::nonNull)
                    .map(Person::getName)
                    .filter(Objects::nonNull)
                    .forEach(names::add);

            if (!names.isEmpty()) {
                String authors = names.size() > 1
                        ? Joiner.on(", ").join(names)
                        : names.get(0);

                RenderUtils.INSTANCE.drawWrappedString(
                        font,
                        i18n.translateKeyAndFormat("modmenu.authorPrefix", authors),
                        x + imageOffset,
                        paneY + 2 + lineSpacing * 2,
                        paneWidth - imageOffset - 4,
                        1,
                        0x808080
                );
            }

            if (this.tooltip != null) {
                this.renderTooltip(Lists.newArrayList(Splitter.on("\n").split(this.tooltip)), mouseX, mouseY);
            }
        }
    }

    private void drawIconQuad(int x, int y) {
        GL11.glEnable(GL11.GL_BLEND);
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(x, y, 0, 0, 0);
        tess.addVertexWithUV(x, y + 32.0, 0, 0, 1);
        tess.addVertexWithUV(x + 32.0, y + 32.0,  0, 1, 1);
        tess.addVertexWithUV(x + 32.0, y, 0, 1, 0);
        tess.draw();
        GL11.glDisable(GL11.GL_BLEND);
    }

    static String getString(Font font, String fullName, int maxNameWidth) {
        // If it already fits, just keep what the caller gave us
        if (font.getStringWidth(fullName) <= maxNameWidth) {
            return fullName;
        }

        int maxWidth = maxNameWidth - font.getStringWidth("...");
        StringBuilder sb = new StringBuilder();

        // Build up characters until we exceed the allowed width or run out of chars
        while (sb.length() < fullName.length()
                && font.getStringWidth(sb.toString()) < maxWidth) {
            sb.append(fullName.charAt(sb.length()));
        }

        if (sb.length() == 0) {
            return "...";
        }

        // Drop the last char to make room for "..."
        sb.setLength(sb.length() - 1);
        sb.append("...");

        return sb.toString();
    }


    public void overlayBackground(int x1, int y1, int x2, int y2, int red, int green, int blue, int startAlpha, int endAlpha) {
        Tessellator tessellator = Tessellator.instance;
        mc.textureManager.bindTexture(mc.textureManager.loadTexture("/gui/background.png"));
        GL11.glColor4f(1f, 1f, 1f, 1f);
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA(red, green, blue, endAlpha);
        tessellator.addVertexWithUV(x1, y2, 0.0D, x1 / 32.0F, y2 / 32.0F);
        tessellator.addVertexWithUV(x2, y2, 0.0D, x2 / 32.0F, y2 / 32.0F);
        tessellator.setColorRGBA(red, green, blue, startAlpha);
        tessellator.addVertexWithUV(x2, y1, 0.0D, x2 / 32.0F, y1 / 32.0F);
        tessellator.addVertexWithUV(x1, y1, 0.0D, x1 / 32.0F, y1 / 32.0F);
        tessellator.draw();
    }

    @Override
    public void removed() {
        super.removed();
        this.modList.close();
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public ModListEntry getSelectedEntry() {
        return selected;
    }

    public void updateSelectedEntry(ModListEntry entry) {
        if (entry != null) {
            this.selected = entry;
        }
    }

    public double getScrollPercent() {
        return scrollPercent;
    }

    public void updateScrollPercent(double scrollPercent) {
        this.scrollPercent = scrollPercent;
    }

    public String getSearchInput() {
        return searchBox.getText();
    }

    @SuppressWarnings("unused")
    public boolean showingFilterOptions() {
        return filterOptionsShown;
    }

    public void renderTooltip(List<String> list, int i, int j) {
        if (!list.isEmpty()) {
            Font font = this.font;

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int maxWidth = 0;

            for (String string : list) {
                int w = font.getStringWidth(string);
                if (w > maxWidth) {
                    maxWidth = w;
                }
            }

            int x = i + 12;
            int y = j - 12;
            int height = 8;
            if (list.size() > 1) {
                height += 2 + (list.size() - 1) * 10;
            }

            if (x + maxWidth > this.width) {
                x -= 28 + maxWidth;
            }

            if (y + height + 6 > this.height) {
                y = this.height - height - 6;
            }

            int transparentGrey = 0xC0000000;
            int margin = 3;
            this.fillGradient(x - margin, y - margin, x + maxWidth + margin,
                    y + height + margin, transparentGrey, transparentGrey);

            GL11.glPushMatrix();
            GL11.glTranslatef(0, 0, 300);

            for (int idx = 0; idx < list.size(); ++idx) {
                String line = list.get(idx);
                if (line != null) {
                    font.drawString(line, x, y, 0xffffff);
                }

                if (idx == 0) {
                    y += 2;
                }

                y += 10;
            }

            GL11.glPopMatrix();
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        }
    }

    protected void fillGradient(int i, int j, int k, int l, int m, int n) {
        float a1 = (m >> 24 & 255) / 255.0F;
        float r1 = (m >> 16 & 255) / 255.0F;
        float g1 = (m >> 8 & 255) / 255.0F;
        float b1 = (m & 255) / 255.0F;
        float a2 = (n >> 24 & 255) / 255.0F;
        float r2 = (n >> 16 & 255) / 255.0F;
        float g2 = (n >> 8 & 255) / 255.0F;
        float b2 = (n & 255) / 255.0F;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL14.glBlendFuncSeparate(
                GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE, GL11.GL_ZERO
        );
        GL11.glShadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(r1, g1, b1, a1);
        tessellator.addVertex(k, j, 300);
        tessellator.addVertex(i, j, 300);
        tessellator.setColorRGBA_F(r2, g2, b2, a2);
        tessellator.addVertex(i, l, 300);
        tessellator.addVertex(k, l, 300);
        tessellator.draw();

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public Set<String> getShowModChildren() {
        return showModChildren;
    }
}
