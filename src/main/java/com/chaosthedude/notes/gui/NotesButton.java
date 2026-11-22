package com.chaosthedude.notes.gui;

import com.chaosthedude.notes.util.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class NotesButton extends Button {

	public NotesButton(int x, int y, int width, int height, Component label, OnPress onPress) {
		super(x, y, width, height, label, onPress, DEFAULT_NARRATION);
	}

	@Override
	public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			Minecraft mc = Minecraft.getInstance();

			// Card-style button: graphite fill, crisp border, accent highlight on hover
			final int fill = !active ? RenderUtils.WIDGET_BG_DISABLED : isHovered ? RenderUtils.WIDGET_BG_HOVER : RenderUtils.WIDGET_BG;
			guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), fill);
			if (active && isHovered) {
				RenderUtils.renderAccentBorder(guiGraphics, getX(), getY(), getWidth(), getHeight());
				// Accent underline strip along the bottom edge
				guiGraphics.fill(getX() + 1, getY() + getHeight() - 3, getX() + getWidth() - 1, getY() + getHeight() - 1, RenderUtils.accentDim());
			} else {
				RenderUtils.renderAccentBorder(guiGraphics, getX(), getY(), getWidth(), getHeight(), RenderUtils.WIDGET_BORDER);
			}

			guiGraphics.centeredText(mc.font, getMessage(), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, active ? 0xffffffff : RenderUtils.TEXT_DISABLED);
		}
	}

}
