package com.chaosthedude.notes.mixins;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.chaosthedude.notes.Notes;
import com.chaosthedude.notes.config.NotesConfig;
import com.chaosthedude.notes.note.Note;
import com.chaosthedude.notes.util.RenderUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
@Mixin(Hud.class)
public class HudMixin {

	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "TAIL"))
	private void renderPinnedNotes(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo info) {
		// Show pinned notes only when the HUD is visible and no screen (or only chat) is open
		if (minecraft.gui.hud.isHidden() || (minecraft.gui.screen() != null && !(minecraft.gui.screen() instanceof ChatScreen))) {
			return;
		}

		if (!NotesConfig.showPinnedNote || Notes.pinnedNotes.isEmpty()) {
			return;
		}

		// Refresh contents and collect the display lines of every valid pinned note
		final List<Note> pinned = new ArrayList<Note>();
		final List<List<String>> allLines = new ArrayList<List<String>>();
		final List<Integer> widths = new ArrayList<Integer>();
		final int maxWidth = Mth.floor(minecraft.getWindow().getGuiScaledWidth() * NotesConfig.pinnedWidthScale);
		final int maxHeight = Mth.floor(minecraft.getWindow().getGuiScaledHeight() * NotesConfig.pinnedHeightScale);
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
			final int renderHeight = RenderUtils.getSplitStringHeight(lines, maxHeight);
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
		final int opacity = (int) (255.0F * minecraft.options.textBackgroundOpacity().get() * (float) NotesConfig.pinnedNoteOpacity);

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
