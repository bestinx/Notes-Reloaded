package com.chaosthedude.notes.gui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

/**
 * Flat toggle button showing "Label: On/Off" in the Notes UI style.
 */
public class NotesToggle extends NotesButton {

	public NotesToggle(int x, int y, int width, int height, String labelKey, Supplier<Boolean> getter, Consumer<Boolean> setter) {
		super(x, y, width, height, buildMessage(labelKey, getter.get()), (onPress) -> {
			final boolean newValue = !getter.get();
			setter.accept(newValue);
			onPress.setMessage(buildMessage(labelKey, newValue));
		});
	}

	private static Component buildMessage(String labelKey, boolean value) {
		return Component.literal(I18n.get(labelKey) + ": " + I18n.get(value ? "notes.on" : "notes.off"));
	}

}
