package com.chaosthedude.notes.util;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.notes.config.NotesTheme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public class RenderUtils {

	private static final Minecraft CLIENT = Minecraft.getInstance();

	// Notes Reloaded card palette: graphite cards with crisp borders, tinted by the active theme accent
	public static final int TEXT_MUTED = 0xff8b93a2;
	public static final int TEXT_DISABLED = 0xff6d7584;
	public static final int WINDOW_BG = 0xe90d1015;
	public static final int WINDOW_BORDER = 0xff232b38;
	public static final int SHADOW = 0x66000000;
	public static final int WIDGET_BG = 0xc2151a23;
	public static final int WIDGET_BG_HOVER = 0xcc1e2532;
	public static final int WIDGET_BG_DISABLED = 0x40151a23;
	public static final int WIDGET_BORDER = 0xff2e3746;
	public static final int CARD_BG = 0xb20f131a;
	public static final int CARD_BG_HOVER = 0xc41a2230;
	public static final int CARD_BG_SELECTED = 0xe01f2a3a;

	// Theme-dependent colors, resolved live so theme changes apply instantly
	public static int accent() {
		return NotesTheme.current().accent;
	}

	public static int accentDim() {
		return NotesTheme.current().accentDim;
	}

	public static int headerColor() {
		return NotesTheme.current().header;
	}

	// Semi-transparent selection fill derived from the current accent
	public static int selectionColor() {
		return (NotesTheme.current().accent & 0x00ffffff) | 0x5c000000;
	}

	// Draws a 1px accent-colored border around a region
	public static void renderAccentBorder(GuiGraphics guiGraphics, int x, int y, int width, int height) {
		renderAccentBorder(guiGraphics, x, y, width, height, accent());
	}

	public static void renderAccentBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
		guiGraphics.fill(x, y, x + width, y + 1, color);
		guiGraphics.fill(x, y + height - 1, x + width, y + height, color);
		guiGraphics.fill(x, y + 1, x + 1, y + height - 1, color);
		guiGraphics.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
	}

	// Full-screen window card behind every Notes screen: dark panel, crisp border, drop shadow
	// and a thin accent strip along the top edge
	public static void renderWindow(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
		final int x = 8;
		final int y = 8;
		final int w = screenWidth - 16;
		final int h = screenHeight - 16;
		guiGraphics.fill(x + 2, y + 2, x + w + 2, y + h + 2, SHADOW);
		guiGraphics.fill(x, y, x + w, y + h, WINDOW_BG);
		renderAccentBorder(guiGraphics, x, y, w, h, WINDOW_BORDER);
		guiGraphics.fill(x + 1, y + 1, x + w - 1, y + 3, accentDim());
	}

	// Card panel used for the note text area and list background
	public static void renderCard(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean hovered, boolean selected) {
		guiGraphics.fill(x, y, x + width, y + height, selected ? CARD_BG_SELECTED : hovered ? CARD_BG_HOVER : CARD_BG);
		if (selected) {
			renderAccentBorder(guiGraphics, x, y, width, height);
		} else {
			renderAccentBorder(guiGraphics, x, y, width, height, WIDGET_BORDER);
		}
	}

	// Draws a screen title with an accent underline, centered over the content area (which is
	// shifted to the right because the button column sits on the left edge)
	public static void renderHeader(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font, String title, int contentCenterX, int y, int underlineWidth) {
		guiGraphics.drawCenteredString(font, title, contentCenterX, y, headerColor());
		guiGraphics.fill(contentCenterX - underlineWidth / 2, y + 11, contentCenterX + underlineWidth / 2, y + 12, accent());
	}

	public static void drawRightAlignedString(net.minecraft.client.gui.Font font, GuiGraphics guiGraphics, String text, int rightX, int y, int color) {
		guiGraphics.drawString(font, text, rightX - font.width(text), y, color, true);
	}

	public static List<String> splitStringToWidth(String str, int maxWidth) {
		List<String> splitStrings = new ArrayList<String>();
		for (FormattedText text : CLIENT.font.getSplitter().splitLines(str, maxWidth, Style.EMPTY)) {
			splitStrings.add(text.getString());
		}
		return splitStrings;
	}

	// Intermediary so we can check if any lines were removed
	public static List<String> splitStringToHeight(List<String> widthSplitStrings, int maxHeight) {
		List<String> splitStrings = new ArrayList<String>();
		int height = 0;
		for (String line : widthSplitStrings) {
			// Check if we have room for this line before adding it
			height += CLIENT.font.lineHeight;
			if (height > maxHeight) {
				break;
			}
			splitStrings.add(line);
		}
		return splitStrings;
	}

	public static List<String> splitStringToWidthAndHeight(String str, int maxWidth, int maxHeight) {
		return splitStringToHeight(splitStringToWidth(str, maxWidth), maxHeight);
	}

	public static void renderSplitString(GuiGraphics guiGraphics, List<String> splitString, int x, int y, int color) {
		for (String s : splitString) {
			guiGraphics.drawString(CLIENT.font, s, x, y, color, true);
			y += CLIENT.font.lineHeight;
		}
	}

	public static int getSplitStringWidth(List<String> splitString, int wrapWidth) {
		int width = 0;
		for (String line : splitString) {
			final int stringWidth = CLIENT.font.width(line);
			if (stringWidth > width) {
				width = stringWidth;
			}
		}

		return width;
	}

	public static int getSplitStringHeight(List<String> splitString, int wrapWidth) {
		return CLIENT.font.lineHeight * splitString.size();
	}

	// Returns the X position at which text with the given width should start rendering, assuming padding of 5 on each side
	public static int getPinnedNoteX(String position, int noteWidth) {
		final String positionLower = position.toLowerCase();
		if (positionLower.equals("top_left") || positionLower.equals("center_left")
				|| positionLower.equals("bottom_left")) {
			// Left side
			return 5;
		}

		// Right side
		return CLIENT.getWindow().getGuiScaledWidth() - noteWidth - 5;
	}

	// Returns the Y position at which text with the given height should start rendering, assuming padding of 5 on each side
	public static int getPinnedNoteY(String position, int noteHeight) {
		final String positionLower = position.toLowerCase();
		if (positionLower.equals("top_left") || positionLower.equals("top_right")) {
			// Top
			return 5;
		} else if (positionLower.equals("bottom_left") || positionLower.equals("bottom_right")) {
			// Bottom
			return CLIENT.getWindow().getGuiScaledHeight() - noteHeight - 5;
		}

		// Center
		return (CLIENT.getWindow().getGuiScaledHeight() / 2) - (noteHeight / 2);
	}

	// Chops off characters as necessary and adds ellipses (...) to the end
	public static String addEllipses(String str, int maxWidth) {
		return CLIENT.font.plainSubstrByWidth(str, Math.max(0, maxWidth - CLIENT.font.width("..."))) + "...";
	}

}
