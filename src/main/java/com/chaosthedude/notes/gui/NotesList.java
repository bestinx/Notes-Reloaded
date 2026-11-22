package com.chaosthedude.notes.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.chaosthedude.notes.config.NotesConfig;
import com.chaosthedude.notes.note.Note;
import com.chaosthedude.notes.util.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;

public class NotesList extends ObjectSelectionList<NotesListEntry> {

	private final SelectNoteScreen parentScreen;
	private List<Note> allNotes = new ArrayList<Note>();
	private String filter = "";
	private int matchCount;
	private double lastMouseX;
	private double lastMouseY;

	public NotesList(SelectNoteScreen notesScreen, Minecraft mc, int x, int y, int width, int height, int itemHeight) {
		super(mc, width, height, y, itemHeight);
		setX(x);
		this.parentScreen = notesScreen;
		refreshList();
	}

	@Override
	protected int scrollBarX() {
		return getRowLeft() + getRowWidth() - 6;
	}

	@Override
	public int getRowWidth() {
		return getWidth();
	}

	@Override
	public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		enableScissor(guiGraphics);
		extractListBackground(guiGraphics);
		extractListItems(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.disableScissor();
		extractScrollbar(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void extractListBackground(GuiGraphicsExtractor guiGraphics) {
		for (int i = 0; i < getItemCount(); ++i) {
			if (getRowBottom(i) >= getY() && getRowTop(i) <= getBottom()) {
				NotesListEntry entry = children().get(i);
				boolean highlighted = entry == getSelected() || entry.isMouseOver(lastMouseX, lastMouseY);
				// Card-style rows with 2px gaps between them, selected row gets an accent border
				RenderUtils.renderCard(guiGraphics, getRowLeft(), getRowTop(i) + 1, getRowWidth() - 6, defaultEntryHeight - 2, highlighted, entry == getSelected());
			}
		}
	}

	@Override
	protected void extractSelection(GuiGraphicsExtractor guiGraphics, NotesListEntry entry, int backgroundColor) {
		// Selection is rendered in extractListBackground()
	}

	@Override
	protected void extractScrollbar(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (scrollable()) {
			guiGraphics.fill(scrollBarX(), getY(), scrollBarX() + 6, getBottom(), RenderUtils.CARD_BG);
			guiGraphics.fill(scrollBarX(), scrollBarY(), scrollBarX() + 6, scrollBarY() + scrollerHeight(), RenderUtils.accentDim());
		}
	}

	public void refreshList() {
		allNotes = Note.getCurrentNotes();
		applyFilter();
	}

	public void setFilter(String filter) {
		this.filter = filter == null ? "" : filter;
		applyFilter();
	}

	private void applyFilter() {
		clearEntries();
		matchCount = 0;
		final String lowered = filter.toLowerCase(Locale.ROOT).trim();
		final List<Note> filtered = new ArrayList<Note>();
		for (Note note : allNotes) {
			if (matches(note, lowered)) {
				filtered.add(note);
				matchCount++;
			}
		}

		sortNotes(filtered);
		for (Note note : filtered) {
			final NotesListEntry entry = new NotesListEntry(this, note);
			if (note.isPinned()) {
				addEntryToTop(entry);
			} else {
				addEntry(entry);
			}
		}
		setScrollAmount(0);
	}

	// Pinned notes are added to the top of the list separately, so plain sorting here is fine
	private void sortNotes(List<Note> notes) {
		switch (NotesConfig.sortMode) {
			case "date_asc":
				notes.sort(Comparator.comparingLong(Note::getLastModified));
				break;
			case "title_asc":
				notes.sort(Comparator.comparing(note -> note.getTitle().toLowerCase(Locale.ROOT)));
				break;
			case "title_desc":
				notes.sort(Comparator.comparing((Note note) -> note.getTitle().toLowerCase(Locale.ROOT)).reversed());
				break;
			default:
				// date_desc: getCurrentNotes() already sorts by last modified, newest first
				break;
		}
	}

	private boolean matches(Note note, String lowered) {
		if (lowered.isEmpty()) {
			return true;
		}

		return note.getTitle().toLowerCase(Locale.ROOT).contains(lowered) || note.getRawText().toLowerCase(Locale.ROOT).contains(lowered);
	}

	public int getMatchCount() {
		return matchCount;
	}

	public void selectNote(NotesListEntry entry) {
		setSelected(entry);
		parentScreen.selectNote(entry);
	}

	public SelectNoteScreen getParentScreen() {
		return parentScreen;
	}

}
