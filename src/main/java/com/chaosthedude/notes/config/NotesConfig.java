package com.chaosthedude.notes.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import com.chaosthedude.notes.Notes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.neoforged.fml.loading.FMLPaths;

public class NotesConfig {

	private static Path configFilePath;
	private static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public static final String DEFAULT_DATE_FORMAT = "M/d/yy h:mm a";
	public static final String DEFAULT_PINNED_NOTE_POSITION = "center_right";
	public static final double DEFAULT_PINNED_WIDTH_SCALE = 0.2;
	public static final double DEFAULT_PINNED_HEIGHT_SCALE = 1.0;

	public static final List<String> PINNED_POSITIONS = Arrays.asList("top_left", "top_right", "center_left", "center_right", "bottom_left", "bottom_right");
	public static final List<String> SORT_MODES = Arrays.asList("date_desc", "date_asc", "title_asc", "title_desc");

	public static String dateFormat = DEFAULT_DATE_FORMAT;
	public static boolean useInGameEditor = true;
	public static boolean useInGameViewer = true;
	public static String pinnedNotePosition = DEFAULT_PINNED_NOTE_POSITION;
	public static double pinnedWidthScale = DEFAULT_PINNED_WIDTH_SCALE;
	public static double pinnedHeightScale = DEFAULT_PINNED_HEIGHT_SCALE;
	public static boolean wrapNote = true;
	// Added in the Notes Reloaded fork
	public static boolean scrollableViewer = true;
	public static boolean showPinnedNote = true;
	public static boolean confirmUnsavedChanges = true;
	public static String theme = NotesTheme.DEFAULT.id;
	public static String sortMode = "date_desc";
	public static double pinnedNoteOpacity = 1.0;
	public static boolean markdown = true;

	public static void load() {
		Data data = null;
		if (getFilePath().toFile().exists()) {
			try {
				Reader reader = Files.newBufferedReader(getFilePath());
				data = gson.fromJson(reader, Data.class);
				reader.close();
			} catch (Exception e) {
				Notes.LOGGER.error("Notes config is corrupted, backing it up and recreating with defaults", e);
				backupCorruptedConfig();
				data = null;
			}
		}

		apply(data);
		save();
	}

	private static void apply(Data data) {
		if (data == null) {
			return;
		}

		// Null-safe: missing keys fall back to defaults instead of crashing
		if (data.dateFormat != null && isValidDateFormat(data.dateFormat)) {
			dateFormat = data.dateFormat;
		}
		if (data.useInGameEditor != null) {
			useInGameEditor = data.useInGameEditor;
		}
		if (data.useInGameViewer != null) {
			useInGameViewer = data.useInGameViewer;
		}
		if (data.pinnedNotePosition != null && PINNED_POSITIONS.contains(data.pinnedNotePosition.toLowerCase())) {
			pinnedNotePosition = data.pinnedNotePosition.toLowerCase();
		}
		if (data.pinnedWidthScale != null) {
			pinnedWidthScale = Math.max(0.05, Math.min(data.pinnedWidthScale, 0.9));
		}
		if (data.pinnedHeightScale != null) {
			pinnedHeightScale = Math.max(0.1, Math.min(data.pinnedHeightScale, 1.0));
		}
		if (data.wrapNote != null) {
			wrapNote = data.wrapNote;
		}
		if (data.scrollableViewer != null) {
			scrollableViewer = data.scrollableViewer;
		}
		if (data.showPinnedNote != null) {
			showPinnedNote = data.showPinnedNote;
		}
		if (data.confirmUnsavedChanges != null) {
			confirmUnsavedChanges = data.confirmUnsavedChanges;
		}
		if (data.theme != null) {
			theme = NotesTheme.byId(data.theme).id;
		}
		if (data.sortMode != null && SORT_MODES.contains(data.sortMode)) {
			sortMode = data.sortMode;
		}
		if (data.pinnedNoteOpacity != null) {
			pinnedNoteOpacity = Math.max(0.1, Math.min(data.pinnedNoteOpacity, 1.0));
		}
		if (data.markdown != null) {
			markdown = data.markdown;
		}
	}

	public static boolean isValidDateFormat(String format) {
		try {
			java.time.format.DateTimeFormatter.ofPattern(format);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static void backupCorruptedConfig() {
		try {
			Files.copy(getFilePath(), getFilePath().resolveSibling(Notes.MODID + ".json.broken"), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Notes.LOGGER.error("Failed to back up corrupted Notes config", e);
		}
	}

	public static void save() {
		try {
			Writer writer = Files.newBufferedWriter(getFilePath());
			Data data = new Data(dateFormat, useInGameEditor, useInGameViewer, pinnedNotePosition, pinnedWidthScale, pinnedHeightScale, wrapNote, scrollableViewer, showPinnedNote, confirmUnsavedChanges, theme, sortMode, pinnedNoteOpacity, markdown);
			gson.toJson(data, writer);
			writer.close();
		} catch (IOException e) {
			Notes.LOGGER.error("Failed to save Notes config", e);
		}
	}

	public static void cycleSortMode() {
		int index = SORT_MODES.indexOf(sortMode);
		if (index < 0) {
			index = 0;
		}

		sortMode = SORT_MODES.get((index + 1) % SORT_MODES.size());
	}

	public static void cyclePinnedPosition(boolean forward) {
		int index = PINNED_POSITIONS.indexOf(pinnedNotePosition);
		if (index < 0) {
			index = PINNED_POSITIONS.indexOf(DEFAULT_PINNED_NOTE_POSITION);
		}

		index = forward ? (index + 1) % PINNED_POSITIONS.size() : (index + PINNED_POSITIONS.size() - 1) % PINNED_POSITIONS.size();
		pinnedNotePosition = PINNED_POSITIONS.get(index);
	}

	public static void resetToDefaults() {
		dateFormat = DEFAULT_DATE_FORMAT;
		useInGameEditor = true;
		useInGameViewer = true;
		pinnedNotePosition = DEFAULT_PINNED_NOTE_POSITION;
		pinnedWidthScale = DEFAULT_PINNED_WIDTH_SCALE;
		pinnedHeightScale = DEFAULT_PINNED_HEIGHT_SCALE;
		wrapNote = true;
		scrollableViewer = true;
		showPinnedNote = true;
		confirmUnsavedChanges = true;
		theme = NotesTheme.DEFAULT.id;
		sortMode = "date_desc";
		pinnedNoteOpacity = 1.0;
		markdown = true;
	}

	private static Path getFilePath() {
		if (configFilePath == null) {
			configFilePath = FMLPaths.CONFIGDIR.get().resolve(Notes.MODID + ".json");
		}
		return configFilePath;
	}

	private static class Data {
		private final String dateFormatComment = "The date format used in timestamps. Uses Unicode CLDR syntax.";
		private final String dateFormat;

		private final String useInGameEditorComment = "Determines whether the in-game editor or the system's default text editor will be used to edit notes. If the system editor is not available, the in-game editor will be used.";
		private final Boolean useInGameEditor;

		private final String useInGameViewerComment = "Determines whether the in-game viewer or the system's default text viewer will be used to view notes. If the system viewer is not available, the in-game viewer will be used.";
		private final Boolean useInGameViewer;

		private final String pinnedNotePositionComment = "The HUD position of a pinned note. Values: top_left, top_right, center_left, center_right, bottom_left, bottom_right";
		private final String pinnedNotePosition;

		private final String pinnedWidthScaleComment = "The maximum width of a pinned note relative to the screen's width.";
		private final Double pinnedWidthScale;

		private final String pinnedHeightScaleComment = "The maximum percentage of the screen's display height that a pinned note can take up.";
		private final Double pinnedHeightScale;

		private final String wrapNoteComment = "Determines whether displayed notes will be word wrapped.";
		private final Boolean wrapNote;

		private final String scrollableViewerComment = "If true, the note viewer scrolls freely with the mouse wheel. If false, notes are shown in pages.";
		private final Boolean scrollableViewer;

		private final String showPinnedNoteComment = "Determines whether the pinned note is displayed on the HUD.";
		private final Boolean showPinnedNote;

		private final String confirmUnsavedChangesComment = "If true, a confirmation is shown before discarding unsaved editor changes.";
		private final Boolean confirmUnsavedChanges;

		private final String themeComment = "UI accent theme. Values: amber, sky, mint, rose, lavender, mono";
		private final String theme;

		private final String sortModeComment = "Note list sorting. Values: date_desc, date_asc, title_asc, title_desc";
		private final String sortMode;

		private final String pinnedNoteOpacityComment = "Opacity of the pinned note HUD background (0.1 - 1.0).";
		private final Double pinnedNoteOpacity;

		private final String markdownComment = "If true, simple markdown (**bold**, *italic*, `code`, # headers) is highlighted in the note viewer.";
		private final Boolean markdown;

		private Data() {
			dateFormat = DEFAULT_DATE_FORMAT;
			useInGameEditor = true;
			useInGameViewer = true;
			pinnedNotePosition = DEFAULT_PINNED_NOTE_POSITION;
			pinnedWidthScale = DEFAULT_PINNED_WIDTH_SCALE;
			pinnedHeightScale = DEFAULT_PINNED_HEIGHT_SCALE;
			wrapNote = true;
			scrollableViewer = true;
			showPinnedNote = true;
			confirmUnsavedChanges = true;
			theme = NotesTheme.DEFAULT.id;
			sortMode = "date_desc";
			pinnedNoteOpacity = 1.0;
			markdown = true;
		}

		private Data(String dateFormat, Boolean useInGameEditor, Boolean useInGameViewer, String pinnedNotePosition, Double pinnedWidthScale, Double pinnedHeightScale, Boolean wrapNote, Boolean scrollableViewer, Boolean showPinnedNote, Boolean confirmUnsavedChanges, String theme, String sortMode, Double pinnedNoteOpacity, Boolean markdown) {
			this.dateFormat = dateFormat;
			this.useInGameEditor = useInGameEditor;
			this.useInGameViewer = useInGameViewer;
			this.pinnedNotePosition = pinnedNotePosition;
			this.pinnedWidthScale = pinnedWidthScale;
			this.pinnedHeightScale = pinnedHeightScale;
			this.wrapNote = wrapNote;
			this.scrollableViewer = scrollableViewer;
			this.showPinnedNote = showPinnedNote;
			this.confirmUnsavedChanges = confirmUnsavedChanges;
			this.theme = theme;
			this.sortMode = sortMode;
			this.pinnedNoteOpacity = pinnedNoteOpacity;
			this.markdown = markdown;
		}
	}

}
