package com.chaosthedude.notes.gui;

import com.chaosthedude.notes.config.NotesConfig;
import com.chaosthedude.notes.util.RenderUtils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class SelectNoteScreen extends Screen {

	private Screen prevScreen;
	private NotesButton newButton;
	private NotesButton viewButton;
	private NotesButton editButton;
	private NotesButton copyButton;
	private NotesButton deleteButton;
	private NotesButton pinButton;
	private NotesButton settingsButton;
	private NotesButton cancelButton;
	private NotesTitleField searchField;
	private NotesList selectionList;

	public SelectNoteScreen(Screen prevScreen) {
		super(Component.translatable("notes.selectNote"));
		this.prevScreen = prevScreen;
	}

	@Override
	public void init() {
		setupButtons();
		final int searchWidth = width - 220;
		searchField = addRenderableWidget(new NotesTitleField(font, 130, 25, searchWidth, 20, Component.literal("")));
		searchField.setHint(Component.translatable("notes.search"));
		searchField.setResponder((value) -> selectionList.setFilter(value));
		addRenderableWidget(new NotesButton(width - 85, 25, 75, 20, buildSortMessage(), (onPress) -> {
			NotesConfig.cycleSortMode();
			onPress.setMessage(buildSortMessage());
			selectionList.refreshList();
			selectionList.setFilter(searchField.getValue());
		}));
		selectionList = new NotesList(this, minecraft, width + 110, height - 55, 50, 40);
		addRenderableWidget(selectionList);
		selectionList.setFilter(searchField.getValue());
	}

	private Component buildSortMessage() {
		return Component.translatable("notes.sort." + NotesConfig.sortMode);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
		RenderUtils.renderWindow(guiGraphics, width, height);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		RenderUtils.renderHeader(guiGraphics, font, I18n.get("notes.selectNote"), width / 2 + 60, 15, Math.min(150, width / 3));
		RenderUtils.drawRightAlignedString(font, guiGraphics, I18n.get("notes.notesCount", selectionList.getMatchCount()), width - 10, height - 12, RenderUtils.TEXT_MUTED);
	}

	@Override
	public void tick() {
		if (selectionList.getSelected() != null) {
			pinButton.setMessage(selectionList.getSelected().isPinned() ? Component.translatable("notes.unpin") : Component.translatable("notes.pin"));
		}
	}

	public void selectNote(NotesListEntry entry) {
		final boolean enable = entry != null;
		viewButton.active = enable;
		deleteButton.active = enable;
		editButton.active = enable;
		copyButton.active = enable;
		pinButton.active = enable;
	}

	private void setupButtons() {
		newButton = addRenderableWidget(new NotesButton(10, 40, 110, 20, Component.translatable("notes.new"), (onPress) -> {
			minecraft.setScreen(new EditNoteScreen(SelectNoteScreen.this, null));
		}));
		viewButton = addRenderableWidget(new NotesButton(10, 75, 110, 20, Component.translatable("notes.view"), (onPress) -> {
			NotesListEntry notesEntry = SelectNoteScreen.this.selectionList.getSelected();
			if (notesEntry != null) {
				notesEntry.loadNote();
			}
		}));
		editButton = addRenderableWidget(new NotesButton(10, 100, 110, 20, Component.translatable("notes.edit"), (onPress) -> {
			NotesListEntry notesEntry = SelectNoteScreen.this.selectionList.getSelected();
			if (notesEntry != null) {
				notesEntry.editNote();
			}
		}));
		copyButton = addRenderableWidget(new NotesButton(10, 125, 110, 20, Component.translatable("notes.copy"), (onPress) -> {
			NotesListEntry notesEntry = SelectNoteScreen.this.selectionList.getSelected();
			notesEntry.copyNote();
		}));
		deleteButton = addRenderableWidget(new NotesButton(10, 150, 110, 20, Component.translatable("notes.delete"), (onPress) -> {
			NotesListEntry notesEntry = SelectNoteScreen.this.selectionList.getSelected();
			if (notesEntry != null) {
				notesEntry.deleteNote();
			}
		}));
		pinButton = addRenderableWidget(new NotesButton(10, 175, 110, 20, Component.translatable("notes.pin"), (onPress) -> {
			NotesListEntry notesEntry = SelectNoteScreen.this.selectionList.getSelected();
			if (notesEntry != null) {
				notesEntry.togglePin();
				selectionList.refreshList();
			}
		}));
		settingsButton = addRenderableWidget(new NotesButton(10, height - 55, 110, 20, Component.translatable("notes.settings"), (onPress) -> {
			minecraft.setScreen(new NotesConfigScreen(SelectNoteScreen.this));
		}));
		cancelButton = addRenderableWidget(new NotesButton(10, height - 30, 110, 20, Component.translatable("gui.cancel"), (onPress) -> {
			minecraft.setScreen(prevScreen);
		}));

		viewButton.active = false;
		deleteButton.active = false;
		editButton.active = false;
		copyButton.active = false;
		pinButton.active = false;
	}

	@Override
	public void onClose() {
		minecraft.setScreen(prevScreen);
	}

}
