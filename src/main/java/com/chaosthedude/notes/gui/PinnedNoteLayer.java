package com.chaosthedude.notes.gui;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.notes.Notes;
import com.chaosthedude.notes.config.NotesConfig;
import com.chaosthedude.notes.note.Note;
import com.chaosthedude.notes.util.RenderUtils;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.gui.GuiLayer;

public class PinnedNoteLayer implements GuiLayer {

	private static final Minecraft CLIENT = Minecraft.getInstance();

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
		if (CLIENT.gui.hud.isHidden() || (CLIENT.gui.screen() != null && !(CLIENT.gui.screen() instanceof ChatScreen))) {
			return;
		}

		if (!NotesConfig.showPinnedNote || Notes.pinnedNotes.isEmpty()) {
			return;
		}

		// Refresh contents and collect the display lines of every valid pinned note
		final List<Note> pinned = new ArrayList<Note>();
		final List<List<String>> allLines = new ArrayList<List<String>>();
		final List<Integer> widths = new ArrayList<Integer>();
		final int maxWidth = Mth.floor(CLIENT.getWindow().getGuiScaledWidth() * NotesConfig.pinnedWidthScale);
		final int maxHeight = Mth.floor(CLIENT.getWindow().getGuiScaledHeight() * NotesConfig.pinnedHeightScale);
		int totalHeight = 0;
		int maxNoteWidth = 0;

		for (Note note : Notes.pinnedNotes) {
			if (!note.isValidScope()) {
				continue;
			}

			note.update();
			final String text = note.getFilteredText();
			final List<String> widthSplitLines = RenderUtils.splitStringToWidth(text, maxWidth);
			final List<String> lines = RenderUtils.splitStringToHeight(widthSplitLines, maxHeight);

			// If any lines were removed, add ellipses
			if (widthSplitLines.size() > lines.size() && !lines.isEmpty()) {
				String lastLine = lines.get(lines.size() - 1);
				lines.set(lines.size() - 1, RenderUtils.addEllipses(lastLine, maxWidth));
			}

			final int renderWidth = RenderUtils.getSplitStringWidth(lines, maxWidth);
			final int renderHeight = RenderUtils.getSplitStringHeight(lines, maxWidth);
			if (renderHeight <= 0) {
				continue;
			}

			pinned.add(note);
			allLines.add(lines);
			widths.add(renderWidth);
			maxNoteWidth = Math.max(maxNoteWidth, renderWidth);
			totalHeight += renderHeight;
		}

		if (pinned.isEmpty()) {
			return;
		}

		// 10px visual gap between stacked notes (5px padding on each side)
		totalHeight += 10 * (pinned.size() - 1);

		final int renderX = RenderUtils.getPinnedNoteX(NotesConfig.pinnedNotePosition, maxNoteWidth);
		final int renderY = RenderUtils.getPinnedNoteY(NotesConfig.pinnedNotePosition, totalHeight);
		final int opacity = (int) (255.0F * CLIENT.options.textBackgroundOpacity().get() * (float) NotesConfig.pinnedNoteOpacity);

		int y = renderY;
		for (int i = 0; i < pinned.size(); i++) {
			final int renderWidth = widths.get(i);
			final int renderHeight = RenderUtils.getSplitStringHeight(allLines.get(i), maxWidth);

			// Render opaque background with padding of 5 on each side
			guiGraphics.fill(renderX - 5, y - 5, renderX + renderWidth + 5, y + renderHeight + 5, opacity << 24);

			// Accent border tinted with the current theme
			RenderUtils.renderAccentBorder(guiGraphics, renderX - 5, y - 5, renderWidth + 10, renderHeight + 10, RenderUtils.accentDim());

			// Render note
			RenderUtils.renderSplitString(guiGraphics, allLines.get(i), renderX, y, 0xffffffff);

			y += renderHeight + 10;
		}
	}

}
