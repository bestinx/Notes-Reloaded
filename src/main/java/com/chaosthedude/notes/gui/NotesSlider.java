package com.chaosthedude.notes.gui;

import java.util.function.DoubleConsumer;

import com.chaosthedude.notes.util.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Flat slider matching the Notes UI style, with a centered label showing the current value.
 */
public class NotesSlider extends AbstractWidget {

	private static final int HANDLE_WIDTH = 4;
	private static final double HANDLE_NUDGE = 0.005;

	private final String label;
	private final double minValue;
	private final double maxValue;
	private final String valueSuffix;
	private final DoubleConsumer onValueChange;
	private double value;
	private boolean dragging;

	public NotesSlider(int x, int y, int width, int height, Component label, double minValue, double maxValue, double initialValue, String valueSuffix, DoubleConsumer onValueChange) {
		super(x, y, width, height, label);
		this.label = label.getString();
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.valueSuffix = valueSuffix;
		this.onValueChange = onValueChange;
		this.value = Mth.clamp((initialValue - minValue) / (maxValue - minValue), 0.0, 1.0);
	}

	private double getActualValue() {
		return minValue + value * (maxValue - minValue);
	}

	private int getDisplayedValue() {
		return (int) Math.round(getActualValue());
	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		final int fill = isActive() && isHovered ? RenderUtils.WIDGET_BG_HOVER : isActive() ? RenderUtils.WIDGET_BG : RenderUtils.WIDGET_BG_DISABLED;
		guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), fill);
		RenderUtils.renderAccentBorder(guiGraphics, getX(), getY(), getWidth(), getHeight(), RenderUtils.WIDGET_BORDER);

		// Thin handle plus a subtle progress bar along the bottom edge so the label stays readable
		final int handleX = getX() + (int) ((getWidth() - HANDLE_WIDTH) * value);
		if (isActive()) {
			guiGraphics.fill(getX() + 1, getY() + getHeight() - 2, handleX + HANDLE_WIDTH / 2, getY() + getHeight() - 1, RenderUtils.accentDim());
		}

		final int handleColor = isActive() ? RenderUtils.accent() : RenderUtils.accentDim();
		guiGraphics.fill(handleX, getY() + 1, handleX + HANDLE_WIDTH, getY() + getHeight() - 1, handleColor);

		final String text = label + ": " + getDisplayedValue() + valueSuffix;
		guiGraphics.drawCenteredString(Minecraft.getInstance().font, text, getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, isActive() ? 0xffffffff : RenderUtils.TEXT_DISABLED);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (!active || !visible) {
			return false;
		}

		boolean clicked = super.mouseClicked(event, doubleClick);
		if (clicked) {
			dragging = true;
			updateFromMouse(event.x());
		}

		return clicked;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (dragging && active && visible) {
			updateFromMouse(event.x());
			return true;
		}

		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		dragging = false;
		return super.mouseReleased(event);
	}

	private void updateFromMouse(double mouseX) {
		final double newValue = Mth.clamp((mouseX - getX()) / getWidth(), 0.0, 1.0);
		if (Math.abs(newValue - value) >= HANDLE_NUDGE) {
			value = newValue;
			onValueChange.accept(getActualValue());
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		this.defaultButtonNarrationText(output);
	}

}
