package com.chaosthedude.notes.gui;

import java.awt.Desktop;
import java.io.File;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.chaosthedude.notes.Notes;
import com.chaosthedude.notes.config.NotesConfig;
import com.chaosthedude.notes.config.NotesTheme;
import com.chaosthedude.notes.util.FileUtils;
import com.chaosthedude.notes.util.RenderUtils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

/**
 * In-game settings screen for Notes. All options apply and persist immediately.
 */
public class NotesConfigScreen extends Screen {

	private static final int COL_WIDTH = 170;
	private static final int COL_GAP = 10;
	private static final int ROW_HEIGHT = 24;
	private static final int SWATCH_GAP = 4;

	private final Screen parentScreen;
	private NotesTitleField dateFormatField;
	private int dateFormatHintY;
	private String positionId;

	public NotesConfigScreen(Screen parentScreen) {
		super(Component.translatable("notes.configTitle"));
		this.parentScreen = parentScreen;
		this.positionId = NotesConfig.pinnedNotePosition;
	}

	@Override
	public void init() {
		final int leftX = width / 2 - COL_WIDTH - COL_GAP / 2;
		final int rightX = width / 2 + COL_GAP / 2;
		final int rowY = 40;

		// Left column: toggles
		addToggle(leftX, rowY, "notes.config.useInGameEditor", () -> NotesConfig.useInGameEditor, (v) -> NotesConfig.useInGameEditor = v);
		addToggle(leftX, rowY + ROW_HEIGHT, "notes.config.useInGameViewer", () -> NotesConfig.useInGameViewer, (v) -> NotesConfig.useInGameViewer = v);
		addToggle(leftX, rowY + ROW_HEIGHT * 2, "notes.config.scrollableViewer", () -> NotesConfig.scrollableViewer, (v) -> NotesConfig.scrollableViewer = v);
		addToggle(leftX, rowY + ROW_HEIGHT * 3, "notes.config.wrapNote", () -> NotesConfig.wrapNote, (v) -> NotesConfig.wrapNote = v);
		addToggle(leftX, rowY + ROW_HEIGHT * 4, "notes.config.showPinnedNote", () -> NotesConfig.showPinnedNote, (v) -> NotesConfig.showPinnedNote = v);
		addToggle(leftX, rowY + ROW_HEIGHT * 5, "notes.config.confirmUnsavedChanges", () -> NotesConfig.confirmUnsavedChanges, (v) -> NotesConfig.confirmUnsavedChanges = v);
		addToggle(leftX, rowY + ROW_HEIGHT * 6, "notes.config.markdown", () -> NotesConfig.markdown, (v) -> NotesConfig.markdown = v);
		addRenderableWidget(new NotesButton(leftX, rowY + ROW_HEIGHT * 7, COL_WIDTH, 20, Component.translatable("notes.config.openFolder"), (onPress) -> {
			openNotesFolder();
		}));

		// Right column: appearance and pinned note settings
		addRenderableWidget(new NotesLabel(rightX, rowY, I18n.get("notes.config.theme")));
		final int swatchY = rowY + 10;
		final int swatchCount = NotesTheme.all().size();
		final int swatchWidth = (COL_WIDTH - (swatchCount - 1) * SWATCH_GAP) / swatchCount;
		for (int i = 0; i < swatchCount; i++) {
			final NotesTheme swatchTheme = NotesTheme.all().get(i);
			addRenderableWidget(new NotesThemeSwatch(rightX + i * (swatchWidth + SWATCH_GAP), swatchY, swatchWidth, 14, swatchTheme, () -> NotesConfig.theme.equals(swatchTheme.id), () -> {
				NotesConfig.theme = swatchTheme.id;
				saveConfig();
			}));
		}
		final int settingsRowY = swatchY + 20;
		addRenderableWidget(new NotesButton(rightX, settingsRowY, COL_WIDTH, 20, buildPositionMessage(), (onPress) -> {
			NotesConfig.cyclePinnedPosition(true);
			positionId = NotesConfig.pinnedNotePosition;
			onPress.setMessage(buildPositionMessage());
			saveConfig();
		}));
		addRenderableWidget(new NotesSlider(rightX, settingsRowY + ROW_HEIGHT, COL_WIDTH, 20, Component.translatable("notes.config.pinnedWidth"), 5, 90, NotesConfig.pinnedWidthScale * 100, "%", (v) -> {
			NotesConfig.pinnedWidthScale = v / 100.0;
			saveConfig();
		}));
		addRenderableWidget(new NotesSlider(rightX, settingsRowY + ROW_HEIGHT * 2, COL_WIDTH, 20, Component.translatable("notes.config.pinnedHeight"), 10, 100, NotesConfig.pinnedHeightScale * 100, "%", (v) -> {
			NotesConfig.pinnedHeightScale = v / 100.0;
			saveConfig();
		}));
		addRenderableWidget(new NotesSlider(rightX, settingsRowY + ROW_HEIGHT * 3, COL_WIDTH, 20, Component.translatable("notes.config.pinnedOpacity"), 10, 100, NotesConfig.pinnedNoteOpacity * 100, "%", (v) -> {
			NotesConfig.pinnedNoteOpacity = v / 100.0;
			saveConfig();
		}));

		// Date format: label above the field, validity hint below
		final int dateLabelY = settingsRowY + ROW_HEIGHT * 4 + 4;
		final int dateFieldY = dateLabelY + 11;
		addRenderableWidget(new NotesLabel(rightX, dateLabelY, I18n.get("notes.config.dateFormat")));
		dateFormatField = addRenderableWidget(new NotesTitleField(font, rightX, dateFieldY, COL_WIDTH, 20, Component.literal("")));
		dateFormatField.setMaxLength(64);
		dateFormatField.setValue(NotesConfig.dateFormat);
		dateFormatField.setResponder((value) -> {
			if (!value.isEmpty() && NotesConfig.isValidDateFormat(value)) {
				NotesConfig.dateFormat = value;
				saveConfig();
			}
		});
		dateFormatHintY = dateFieldY + 20 + 3;

		addRenderableWidget(new NotesButton(leftX, height - 30, COL_WIDTH, 20, Component.translatable("notes.config.reset"), (onPress) -> {
			NotesConfig.resetToDefaults();
			saveConfig();
			minecraft.setScreen(new NotesConfigScreen(parentScreen));
		}));
		addRenderableWidget(new NotesButton(rightX, height - 30, COL_WIDTH, 20, Component.translatable("gui.done"), (onPress) -> {
			onClose();
		}));
	}

	private Component buildPositionMessage() {
		return Component.literal(I18n.get("notes.config.pinnedPosition") + ": " + I18n.get("notes.position." + positionId));
	}

	private void addToggle(int x, int y, String labelKey, Supplier<Boolean> getter, Consumer<Boolean> setter) {
		addRenderableWidget(new NotesToggle(x, y, COL_WIDTH, 20, labelKey, getter, (v) -> {
			setter.accept(v);
			saveConfig();
		}));
	}

	private void openNotesFolder() {
		final File folder = FileUtils.getRootSaveDirectory();
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().open(folder);
			} catch (Exception e) {
				Notes.LOGGER.error("Failed to open notes folder", e);
			}
		}
	}

	private void saveConfig() {
		NotesConfig.save();
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
		RenderUtils.renderWindow(guiGraphics, width, height);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		RenderUtils.renderHeader(guiGraphics, font, title.getString(), width / 2, 15, Math.min(170, width / 3));

		// Validity hint directly under the date format field
		if (dateFormatField != null && !dateFormatField.getValue().isEmpty()) {
			final boolean valid = NotesConfig.isValidDateFormat(dateFormatField.getValue());
			guiGraphics.drawString(font, I18n.get(valid ? "notes.config.dateFormatValid" : "notes.config.dateFormatInvalid"), width / 2 + COL_GAP / 2 + 2, dateFormatHintY, valid ? RenderUtils.accent() : 0xffff5555, false);
		}
	}

	@Override
	public void onClose() {
		saveConfig();
		minecraft.setScreen(parentScreen);
	}

}
