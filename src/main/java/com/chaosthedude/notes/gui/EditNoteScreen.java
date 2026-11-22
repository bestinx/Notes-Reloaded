package com.chaosthedude.notes.gui;

import org.lwjgl.glfw.GLFW;

import com.chaosthedude.notes.Notes;
import com.chaosthedude.notes.config.NotesConfig;
import com.chaosthedude.notes.note.Note;
import com.chaosthedude.notes.note.Scope;
import com.chaosthedude.notes.util.RenderUtils;
import com.chaosthedude.notes.util.StringUtils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class EditNoteScreen extends Screen {

	private final Screen parentScreen;
	private NotesButton saveButton;
	private NotesButton globalButton;
	private NotesButton insertBiomeButton;
	private NotesButton insertChunkButton;
	private NotesButton insertCoordsButton;
	private NotesButton cancelButton;
	private NotesTitleField noteTitleField;
	private NotesTextField noteTextField;
	private String saveDirName;
	private boolean newNote;
	private Note note;
	private Scope scope;
	private boolean pinned;
	private boolean setTextFieldFocused;
	private String originalTitle;
	private String originalText;

	public EditNoteScreen(Screen parentScreen, Note note) {
		super(Component.literal(note != null ? I18n.get("notes.editNote") : I18n.get("notes.newNote")));
		this.parentScreen = parentScreen;
		if (note != null) {
			this.note = note;
			newNote = false;
		} else {
			this.note = new Note("New Note", "", Scope.getCurrentScope());
			newNote = true;
		}

		scope = Scope.getCurrentScope();
		pinned = this.note.isPinned();
		setTextFieldFocused = false;
	}

	@Override
	public void init() {
		setupTextFields();
		setupButtons();
	}

	@Override
	public void tick() {
		insertBiomeButton.active = insertChunkButton.active = insertCoordsButton.active = noteTextField.isFocused();
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		boolean ret = super.mouseClicked(event, doubleClick);
		if (setTextFieldFocused) {
			// Change focus back to the text field after clicking the biome, coords, or chunk button
			setFocused(noteTextField);
			setTextFieldFocused = false;
		}
		return ret;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if ((event.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0 && event.key() == GLFW.GLFW_KEY_S) {
			// Ctrl+S: save and open the viewer
			saveAndOpen();
			return true;
		}

		if (event.key() == GLFW.GLFW_KEY_ENTER && noteTitleField.isFocused()) {
			// Enter in the title field jumps to the text field
			setFocused(noteTextField);
			return true;
		}

		boolean ret = super.keyPressed(event);
		updateNote();
		return ret;
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		boolean ret = super.charTyped(event);
		updateNote();
		return ret;
	}

	@Override
	public void setFocused(GuiEventListener listener) {
		super.setFocused(listener);
		if (listener != noteTextField && noteTextField != null) {
			noteTextField.setFocused(false);
		}
	}

	@Override
	public void onClose() {
		// ESC: confirm discarding unsaved changes instead of silently losing them
		if (isDirty() && NotesConfig.confirmUnsavedChanges) {
			confirmDiscard();
		} else {
			minecraft.setScreen(parentScreen);
		}
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
		RenderUtils.renderWindow(guiGraphics, width, height);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		RenderUtils.renderHeader(guiGraphics, font, title.getString(), width / 2 + 60, 15, Math.min(150, width / 3));
		guiGraphics.drawCenteredString(font, I18n.get("notes.saveAs", note.getUncollidingSaveName(note.getTitle())), width / 2 + 55, 29, RenderUtils.TEXT_MUTED);
		if (isDirty()) {
			guiGraphics.drawString(font, "•", 6, 6, RenderUtils.accent(), true);
		}

		// Editor status bar: line and character count
		final String text = noteTextField.getValue();
		final int lineCount = text.isEmpty() ? 0 : text.split("\n").length;
		RenderUtils.drawRightAlignedString(font, guiGraphics, I18n.get("notes.editorStats", lineCount, text.length()), width - 10, height - 12, RenderUtils.TEXT_MUTED);
	}

	private void setupButtons() {
		saveButton = addRenderableWidget(new NotesButton(10, 40, 110, 20, Component.translatable("notes.save"), (onPress) -> {
			saveAndOpen();
		}));
		globalButton = addRenderableWidget(new NotesButton(10, 65, 110, 20, Component.translatable("notes.global").append(Component.literal(": ").append(note.getScope() == Scope.GLOBAL ? Component.translatable("notes.on") : Component.translatable("notes.off"))), (onPress) -> {
			if (scope == Scope.GLOBAL) {
				scope = Scope.getCurrentScope();
			} else {
				scope = Scope.GLOBAL;
			}

			globalButton.setMessage(Component.literal(I18n.get("notes.global") + (scope == Scope.GLOBAL ? ": " + I18n.get("notes.on") : ": " + I18n.get("notes.off"))));
			updateNote();
		}));
		insertBiomeButton = addRenderableWidget(new NotesButton(10, 100, 110, 20, Component.translatable("notes.biome"), (onPress) -> {
			insertBiome();
			setTextFieldFocused = true;
		}));
		insertChunkButton = addRenderableWidget(new NotesButton(10, 125, 110, 20, Component.translatable("notes.chunk"), (onPress) -> {
			insertChunk();
			setTextFieldFocused = true;
		}));
		insertCoordsButton = addRenderableWidget(new NotesButton(10, 150, 110, 20, Component.translatable("notes.coordinates"), (onPress) -> {
			insertCoords();
			setTextFieldFocused = true;
		}));
		cancelButton = addRenderableWidget(new NotesButton(10, height - 30, 110, 20, Component.translatable("gui.cancel"), (onPress) -> {
			onClose();
		}));

		insertBiomeButton.active = false;
		insertChunkButton.active = false;
		insertCoordsButton.active = false;
	}

	private void setupTextFields() {
		noteTitleField = addRenderableWidget(new NotesTitleField(font, 130, 40, width - 140, 20, Component.literal("")));
		noteTitleField.setValue(note.getTitle());
		if (newNote) {
			setFocused(noteTitleField);
		}

		noteTextField = addRenderableWidget(new NotesTextField(font, 130, 70, width - 140, height - 82));
		noteTextField.setValue(note.getFilteredText());
		noteTextField.setScrollAmount(0);
		if (!newNote) {
			setFocused(noteTextField);
		}

		originalTitle = note.getTitle();
		originalText = note.getFilteredText();
	}

	private boolean isDirty() {
		if (noteTitleField == null || noteTextField == null) {
			return false;
		}

		return !noteTitleField.getValue().equals(originalTitle) || !noteTextField.getValue().equals(originalText);
	}

	private void confirmDiscard() {
		minecraft.setScreen(new NotesConfirmScreen((result) -> {
			if (result) {
				if (newNote) {
					// Nothing was written to disk for a new note, just drop it
					minecraft.setScreen(parentScreen);
				} else {
					// Restore the on-disk content
					note.update();
					minecraft.setScreen(parentScreen);
				}
			} else {
				minecraft.setScreen(this);
			}
		}, Component.translatable("notes.discardChanges"), Component.translatable("notes.discardHint")));
	}

	private void saveAndOpen() {
		updateNote();
		note.save();
		minecraft.setScreen(new ViewNoteScreen(parentScreen, note));
		if (pinned) {
			note.pin();
		}
	}

	private void updateNote() {
		note.setTitle(noteTitleField.getValue());
		note.setText(noteTextField.getValue());
		note.setScope(scope);
	}

	private void insertBiome() {
		noteTextField.insertText(StringUtils.fixBiomeName(minecraft.level, minecraft.level.getBiome(minecraft.player.blockPosition()).value()));
	}

	private void insertChunk() {
		noteTextField.insertText((int) minecraft.player.chunkPosition().x + ", " + (int) minecraft.player.chunkPosition().z);
	}

	private void insertCoords() {
		noteTextField.insertText((int) minecraft.player.getBlockX() + ", " + (int) minecraft.player.getBlockY() + ", " + (int) minecraft.player.getBlockZ());
	}

}
