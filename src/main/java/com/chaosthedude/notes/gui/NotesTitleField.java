package com.chaosthedude.notes.gui;

import com.chaosthedude.notes.util.RenderUtils;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class NotesTitleField extends EditBox {

	public NotesTitleField(Font font, int x, int y, int width, int height, Component label) {
		super(font, x, y, width, height, label);
	}

	@Override
	public int getInnerWidth() {
		return getWidth() - 8;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (isVisible()) {
			// Card-style field: accent border while focused
			guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), RenderUtils.WIDGET_BG);
			RenderUtils.renderAccentBorder(guiGraphics, getX(), getY(), getWidth(), getHeight(), isFocused() ? RenderUtils.accent() : RenderUtils.WIDGET_BORDER);
		}

		// Disable bordered to skip rendering the default background
		bordered = false;
		super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
		bordered = true;
	}

}
