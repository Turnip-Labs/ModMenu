package io.github.prospector.modmenu.gui;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.util.BadgeRenderer;
import io.github.prospector.modmenu.util.HardcodedUtil;
import io.github.prospector.modmenu.util.RenderUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Font;
import net.minecraft.client.render.tessellator.Tessellator;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModListEntry extends AlwaysSelectedEntryListWidget.Entry<ModListEntry> {
    public static final String UNKNOWN_ICON = "/gui/unknown_pack.png";
    private static final Logger LOGGER = LoggerFactory.getLogger(ModMenu.MOD_ID);

    protected final Minecraft client;
    protected final ModContainer container;
    protected final ModMetadata metadata;
    protected final ModListWidget list;
    protected Integer iconLocation;

    public ModListEntry(Minecraft client, ModContainer container, ModListWidget list) {
        this.container = container;
        this.list = list;
        this.metadata = container.getMetadata();
        this.client = client;
    }

    @Override
    public void render(int index, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        int offsetX = getXOffset();
        rowLeft += offsetX;
        rowWidth -= offsetX;

        GL11.glColor4f(1f, 1f, 1f, 1f);
        bindIconTexture();
        drawIconQuad(rowLeft, rowTop);

        String displayName = getDisplayName();
        displayName = HardcodedUtil.formatFabricModuleName(displayName);

        Font font = this.client.font;
        int maxNameWidth = rowWidth - 32 - 3;
        String trimmedName = ModListScreen.getString(font, displayName, maxNameWidth);

        int nameX = rowLeft + 32 + 3;
        int nameY = rowTop + 1;
        font.drawString(trimmedName, nameX, nameY, 0xFFFFFF);

        int badgeStartX = nameX + font.getStringWidth(trimmedName) + 2;
        new BadgeRenderer(client, badgeStartX, rowTop, rowLeft + rowWidth, container, list.getParent())
                .draw(mouseX, mouseY);

        String description = metadata.getDescription();
        if (description.isEmpty() && HardcodedUtil.getHardcodedDescriptions().containsKey(metadata.getId())) {
            description = HardcodedUtil.getHardcodedDescription(metadata.getId());
        }

        int descX = nameX + 4;
        int descY = rowTop + 9 + 2;
        int descWidth = rowWidth - 32 - 7;
        RenderUtils.INSTANCE.drawWrappedString(font, description, descX, descY, descWidth, 2, 0x808080);
    }

    private String getDisplayName() {
        String name = metadata.getName();
        // BTA-specific hardcoded rename
        if ("Minecraft".equals(name)) {
            return "Better than Adventure";
        }
        return name;
    }

    private static void drawIconQuad(int x, int y) {
        GL11.glEnable(GL11.GL_BLEND);
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(x, y, 0, 0, 0);
        tess.addVertexWithUV(x, y + 32.0, 0, 0, 1);
        tess.addVertexWithUV(x + 32.0, y + 32.0, 0, 1, 1);
        tess.addVertexWithUV(x + 32.0, y, 0, 1, 0);
        tess.draw();
        GL11.glDisable(GL11.GL_BLEND);
    }

    private BufferedImage createIcon() {
        try {
            Path iconPath = resolveIconPath();
            if (iconPath == null) {
                return null;
            }

            BufferedImage cached = this.list.getCachedModIcon(iconPath);
            if (cached != null) {
                return cached;
            }

            try (InputStream inputStream = Files.newInputStream(iconPath)) {
                BufferedImage image = ImageIO.read(inputStream);
                if (image == null) {
                    return null;
                }
                if (image.getHeight() != image.getWidth()) {
                    throw new IllegalStateException("Must be square icon");
                }
                this.list.cacheModIcon(iconPath, image);
                return image;
            }
        } catch (Exception e) {
            LOGGER.error("Invalid icon for mod {}", this.metadata.getName(), e);
            return null;
        }
    }

    @SuppressWarnings("java:S1075")
    private Path resolveIconPath() {
        // Try mod-provided icon first
        Path path = container.findPath(
                metadata.getIconPath(0).orElse("assets/" + metadata.getId() + "/icon.png")
        ).orElse(null);

        if (path != null && Files.exists(path)) {
            return path;
        }

        // Fallback icons from Mod Menu
        ModContainer modMenu = FabricLoader.getInstance()
                .getModContainer(ModMenu.MOD_ID)
                .orElseThrow(IllegalAccessError::new);

        String basePath = "assets/" + ModMenu.MOD_ID + "/";
        String fallback;

        if (HardcodedUtil.getFabricMods().contains(metadata.getId())) {
            fallback = "fabric_icon.png";
        } else if ("minecraft".equals(metadata.getId())) {
            fallback = "mc_icon.png";
        } else if ("java".equals(metadata.getId())) {
            fallback = "java_icon.png";
        } else {
            fallback = "grey_fabric_icon.png";
        }

        return modMenu.findPath(basePath + fallback).orElse(null);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        list.select(this);
    }

    public ModMetadata getMetadata() {
        return metadata;
    }

    public void bindIconTexture() {
        if (this.iconLocation == null) {
            BufferedImage icon = this.createIcon();
            if (icon != null) {
                this.iconLocation = this.client.textureManager.loadBufferedTexture(icon).id();
            } else {
                this.iconLocation = this.client.textureManager.loadTexture(UNKNOWN_ICON).id();
            }
        }
        this.client.textureManager.bindTexture(this.iconLocation);
    }

    public void deleteTexture() {
        if (iconLocation != null) {
            this.client.textureManager.idToTextureMap.remove(iconLocation);
            GL11.glDeleteTextures(iconLocation);
        }
    }

    public int getXOffset() {
        return 0;
    }
}
