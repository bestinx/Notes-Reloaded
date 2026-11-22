package com.chaosthedude.notes.config;

import java.util.Arrays;
import java.util.List;

import com.chaosthedude.notes.Notes;

/**
 * UI color themes. Only accent colors change; backgrounds stay dark and neutral for readability.
 */
public enum NotesTheme {

	AMBER("amber", 0xffeec25e, 0x80eec25e, 0xffeee5cf),
	SKY("sky", 0xff6fb7ff, 0x806fb7ff, 0xffd6e9ff),
	MINT("mint", 0xff7fdfa8, 0x807fdfa8, 0xffd9f2e3),
	ROSE("rose", 0xffff8fa3, 0x80ff8fa3, 0xffffdfe5),
	LAVENDER("lavender", 0xffb69dff, 0x80b69dff, 0xffe6dcff),
	MONO("mono", 0xffd8d8d8, 0x80d8d8d8, 0xffffffff);

	public static final NotesTheme DEFAULT = AMBER;

	public final String id;
	public final int accent;
	public final int accentDim;
	public final int header;

	NotesTheme(String id, int accent, int accentDim, int header) {
		this.id = id;
		this.accent = accent;
		this.accentDim = accentDim;
		this.header = header;
	}

	public String getNameKey() {
		return "notes.theme." + id;
	}

	public static List<NotesTheme> all() {
		return Arrays.asList(values());
	}

	public static NotesTheme byId(String id) {
		for (NotesTheme theme : values()) {
			if (theme.id.equalsIgnoreCase(id)) {
				return theme;
			}
		}

		Notes.LOGGER.warn("Unknown notes theme id: " + id + ", falling back to " + DEFAULT.id);
		return DEFAULT;
	}

	public static NotesTheme current() {
		return byId(NotesConfig.theme);
	}

	public static void cycle(boolean forward) {
		NotesTheme current = current();
		NotesTheme[] values = values();
		int index = current.ordinal();
		index = forward ? (index + 1) % values.length : (index + values.length - 1) % values.length;
		NotesConfig.theme = values[index].id;
	}

}
