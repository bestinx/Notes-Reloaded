package com.chaosthedude.notes.gui;

import com.chaosthedude.notes.util.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Small muted text label in the Notes UI style.
 */
public class NotesLabel extends AbstractWidget {

	public NotesLabel(int x, int y, String text) {
		super(x, y, Minecraft.getInstance().font.width(text), 9, Component.literal(text));
	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		guiGraphics.drawString(Minecraft.getInstance().font, getMessage().getString(), getX(), getY(), RenderUtils.TEXT_MUTED, false);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		// Plain label, no narration needed
	}

}
