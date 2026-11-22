package com.chaosthedude.notes.gui;

import com.chaosthedude.notes.util.RenderUtils;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;

public class NotesTextField extends MultiLineEditBox {

	NotesTextField(Font font, int x, int y, int width, int height) {
		super(font, x, y, width, height, Component.empty(), Component.empty(), 0xffffffff, true, 0xffffffff, true, true);
	}

	@Override
	protected void extractBackground(GuiGraphicsExtractor guiGraphics) {
		// Card-style text area: accent border while focused
		guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), RenderUtils.WIDGET_BG);
		RenderUtils.renderAccentBorder(guiGraphics, getX(), getY(), getWidth(), getHeight(), isFocused() ? RenderUtils.accent() : RenderUtils.WIDGET_BORDER);
	}

	@Override
	protected void extractScrollbar(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (scrollable()) {
			guiGraphics.fill(scrollBarX(), getY(), scrollBarX() + 6, getBottom(), RenderUtils.CARD_BG);
            guiGraphics.fill(scrollBarX(), scrollBarY(), scrollBarX() + 6, scrollBarY() + scrollerHeight(), RenderUtils.accentDim());
        }
    }

	public void insertText(String text) {
		textField.insertText(text);
	}

}
