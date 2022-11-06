package io.github.prospector.modmenu.gui;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.util.BadgeRenderer;
import io.github.prospector.modmenu.util.HardcodedUtil;
import io.github.prospector.modmenu.util.RenderUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.Tessellator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class ModListEntry extends AlwaysSelectedEntryListWidget.Entry<ModListEntry> {
	public static final String UNKNOWN_ICON = "/gui/unknown_pack.png";
	private static final Logger LOGGER = LogManager.getLogger();

	protected final Minecraft client;
	protected final ModContainer container;
	protected final ModMetadata metadata;
	protected final ModListWidget list;
	protected Integer iconLocation;

	public ModListEntry(Minecraft mc, ModContainer container, ModListWidget list) {
		this.container = container;
		this.list = list;
		this.metadata = container.getMetadata();
		this.client = mc;
	}

	@Override
	public void render(int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
		x += getXOffset();
		rowWidth -= getXOffset();
		GL11.glColor4f(1f, 1f, 1f, 1f);
		this.bindIconTexture();
        internalRender(y, x);
        String name = HardcodedUtil.formatFabricModuleName(metadata.getName());
		String trimmedName = name;
		int maxNameWidth = rowWidth - 32 - 3;
		FontRenderer font = this.client.fontRenderer;
        trimmedName = ModListScreen.getString(font, name, trimmedName, maxNameWidth);
        font.drawString(trimmedName, x + 32 + 3, y + 1, 0xFFFFFF);
		new BadgeRenderer(client, x + 32 + 3 + font.getStringWidth(name) + 2, y, x + rowWidth, container, list.getParent()).draw(mouseX, mouseY);
		String description = metadata.getDescription();
		if (description.isEmpty() && HardcodedUtil.getHardcodedDescriptions().containsKey(metadata.getId())) {
			description = HardcodedUtil.getHardcodedDescription(metadata.getId());
		}
		RenderUtils.INSTANCE.drawWrappedString(font, description, (x + 32 + 3 + 4), (y + 9 + 2), rowWidth - 32 - 7, 2, 0x808080);
	}

    static void internalRender(int y, int x) {
        GL11.glEnable(GL11.GL_BLEND);
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(x, y, 0, 0, 0);
        tess.addVertexWithUV(x, y + 32, 0, 0, 1);
        tess.addVertexWithUV(x + 32, y + 32, 0, 1, 1);
        tess.addVertexWithUV(x + 32, y, 0, 1, 0);
        tess.draw();
        GL11.glDisable(GL11.GL_BLEND);
    }

    private BufferedImage createIcon() {
		try {
			Path path = container.getPath(metadata.getIconPath(0).orElse("assets/" + metadata.getId() + "/icon.png"));
			BufferedImage cached = this.list.getCachedModIcon(path);
			if (cached != null) {
				return cached;
			}
			if (!Files.exists(path)) {
				ModContainer modMenu = FabricLoader.getInstance().getModContainer(ModMenu.MOD_ID).orElseThrow(IllegalAccessError::new);
				if (HardcodedUtil.getFabricMods().contains(metadata.getId())) {
					path = modMenu.getPath("assets/" + ModMenu.MOD_ID + "/fabric_icon.png");
				} else if (metadata.getId().equals("minecraft")) {
					path = modMenu.getPath("assets/" + ModMenu.MOD_ID + "/mc_icon.png");
				} else {
					path = modMenu.getPath("assets/" + ModMenu.MOD_ID + "/grey_fabric_icon.png");
				}
			}
			cached = this.list.getCachedModIcon(path);
			if (cached != null) {
				return cached;
			}
			try (InputStream inputStream = Files.newInputStream(path)) {
				BufferedImage image = ImageIO.read(Objects.requireNonNull(inputStream));
				if (image.getHeight() != image.getWidth())
					throw new IllegalStateException("Must be square icon");
				this.list.cacheModIcon(path, image);
				return image;
			}

		} catch (Throwable t) {
			LOGGER.error("Invalid icon for mod {}", this.container.getMetadata().getName(), t);
			return null;
		}
	}

	@Override
	public void mouseClicked(int v, int v1, int i) {
		list.select(this);
	}

	public ModMetadata getMetadata() {
		return metadata;
	}

	public void bindIconTexture() {
		if (this.iconLocation == null) {
			BufferedImage icon = this.createIcon();
			if (icon != null) {
				this.iconLocation = this.client.renderEngine.allocateAndSetupTexture(icon);
			} else {
				this.iconLocation = this.client.renderEngine.getTexture(UNKNOWN_ICON);
			}
		}
		this.client.renderEngine.bindTexture(this.iconLocation);
	}

	public void deleteTexture() {
		if (iconLocation != null) {
			this.client.renderEngine.deleteTexture(iconLocation); // func_1078_a
		}
	}

	public int getXOffset() {
		return 0;
	}
}
