package com.chaosthedude.notes.gui;

import java.util.function.Supplier;

import com.chaosthedude.notes.config.NotesTheme;
import com.chaosthedude.notes.util.RenderUtils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

/**
 * Colored theme swatch: filled with its theme's accent color, marked with a border when selected.
 */
public class NotesThemeSwatch extends NotesButton {

	private final NotesTheme theme;
	private final Supplier<Boolean> selectedSupplier;
	private final Runnable onSelect;

	public NotesThemeSwatch(int x, int y, int width, int height, NotesTheme theme, Supplier<Boolean> selectedSupplier, Runnable onSelect) {
		super(x, y, width, height, Component.empty(), (onPress) -> {});
		this.theme = theme;
		this.selectedSupplier = selectedSupplier;
		this.onSelect = onSelect;
	}

	@Override
	public void onPress(InputWithModifiers input) {
		if (!Boolean.TRUE.equals(selectedSupplier.get())) {
			onSelect.run();
		}
	}

	@Override
	public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (!visible) {
			return;
		}

		guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), theme.accent);
		if (Boolean.TRUE.equals(selectedSupplier.get())) {
			// White border marks the active theme
			RenderUtils.renderAccentBorder(guiGraphics, getX(), getY(), getWidth(), getHeight(), 0xffffffff);
		} else if (isHovered) {
			RenderUtils.renderAccentBorder(guiGraphics, getX(), getY(), getWidth(), getHeight(), 0x80ffffff);
		}
	}

}
