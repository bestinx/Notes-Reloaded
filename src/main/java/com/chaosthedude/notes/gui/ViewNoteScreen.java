package com.chaosthedude.notes.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.chaosthedude.notes.Notes;
import com.chaosthedude.notes.config.NotesConfig;
import com.chaosthedude.notes.note.Note;
import com.chaosthedude.notes.util.RenderUtils;
import com.chaosthedude.notes.util.StringUtils;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;

/**
 * Note viewer. By default the note is displayed in a freely scrollable panel: mouse wheel,
 * a draggable scrollbar, and keyboard scrolling all work, replacing the old page-by-page
 * navigation. Text can be selected with the mouse (or double click for a word, Ctrl+A for all)
 * and copied with Ctrl+C or the copy button; the legacy page mode is still available in the config.
 */
public class ViewNoteScreen extends Screen {

	private static final int CONTENT_X = 130;
	private static final int TEXT_AREA_Y = 45;
	private static final int BUTTON_WIDTH = 110;
	private static final int LINE_SPACING = 2;

	private final Screen parentScreen;
	private NotesButton editButton;
	private NotesButton copyButton;
	private NotesButton deleteButton;
	private NotesButton pinButton;
	private NotesButton doneButton;
	private NotesButton prevButton;
	private NotesButton nextButton;

	private Note note;
	// Scroll mode state
	private List<DisplayLine> lines = new ArrayList<DisplayLine>();
	// Styled render segments per display line (markdown highlighting); concatenating the
	// segments of a line always yields the exact display line text
	private List<List<Segment>> lineSegments = new ArrayList<List<Segment>>();
	private double scrollAmount;
	private boolean draggingScrollbar;
	private boolean draggingSelection;
	// Selection state (null = no selection)
	private SelPos selAnchor;
	private SelPos selFocus;
	// Page mode state
	private List<String> pages = new ArrayList<String>();
	private int page;
	// Clipboard feedback timer (in ticks)
	private int copiedTimer;

	public ViewNoteScreen(Screen parentScreen, Note note) {
		super(Component.literal(note.getTitle()));
		this.parentScreen = parentScreen;
		this.note = note;
	}

	@Override
	public void init() {
		setupButtons();
		setupContent();
	}

	@Override
	public void tick() {
		if (copiedTimer > 0) {
			copiedTimer--;
		}

		if (!NotesConfig.scrollableViewer) {
			prevButton.active = page > 0;
			nextButton.active = page < pages.size() - 1;
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
		RenderUtils.renderWindow(guiGraphics, width, height);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
		final int contentCenterX = width / 2 + 60;

		RenderUtils.renderHeader(guiGraphics, font, title.getString(), contentCenterX, 15, Math.min(150, width / 3));
		guiGraphics.centeredText(font, note.getLastModifiedString() + "  •  " + I18n.get(note.getScope().getUnlocalizedName()), contentCenterX, 29, RenderUtils.TEXT_MUTED);

		if (NotesConfig.scrollableViewer) {
			renderScrollable(guiGraphics);
		} else {
			renderPaged(guiGraphics);
		}

		if (copiedTimer > 0) {
			guiGraphics.text(font, I18n.get("notes.copiedToClipboard"), 10, 140, RenderUtils.accent(), true);
		}
	}

	private void renderScrollable(GuiGraphicsExtractor guiGraphics) {
		final int textAreaWidth = width - CONTENT_X - 10;
		final int textAreaHeight = height - TEXT_AREA_Y - 10;

		RenderUtils.renderCard(guiGraphics, CONTENT_X, TEXT_AREA_Y, textAreaWidth, textAreaHeight, false, false);

		guiGraphics.enableScissor(CONTENT_X, TEXT_AREA_Y, CONTENT_X + textAreaWidth, TEXT_AREA_Y + textAreaHeight);
		if (lines.isEmpty()) {
			guiGraphics.text(font, I18n.get("notes.emptyNote"), CONTENT_X + 5, TEXT_AREA_Y + 5, RenderUtils.TEXT_MUTED, false);
		} else {
			renderSelection(guiGraphics);
			final int firstLine = (int) (scrollAmount / lineHeight());
			int y = TEXT_AREA_Y + 5 - (int) (scrollAmount - firstLine * lineHeight());
			for (int i = firstLine; i < lines.size() && y < TEXT_AREA_Y + textAreaHeight; i++) {
				renderStyledLine(guiGraphics, i, CONTENT_X + 5, y);
				y += lineHeight();
			}
		}
		guiGraphics.disableScissor();

		final int maxScroll = maxScrollAmount();
		if (maxScroll > 0) {
			final int scrollBarX = CONTENT_X + textAreaWidth - 8;
			guiGraphics.fill(scrollBarX, TEXT_AREA_Y, scrollBarX + 6, TEXT_AREA_Y + textAreaHeight, RenderUtils.CARD_BG);
			final int thumbY = scrollThumbY();
			guiGraphics.fill(scrollBarX, thumbY, scrollBarX + 6, thumbY + scrollThumbHeight(), RenderUtils.accentDim());
		}
	}

	private void renderSelection(GuiGraphicsExtractor guiGraphics) {
		if (!hasSelection()) {
			return;
		}

		final SelPos a = selStart();
		final SelPos b = selEnd();
		final int selColor = RenderUtils.selectionColor();
		for (int i = a.line(); i <= b.line(); i++) {
			final int x1 = CONTENT_X + 5 + (i == a.line() ? styledPrefixWidth(i, a.pos()) : 0);
			final int x2 = CONTENT_X + 5 + (i == b.line() ? styledPrefixWidth(i, b.pos()) : styledPrefixWidth(i, lines.get(i).text.length()));
			final int y = TEXT_AREA_Y + 5 + i * lineHeight() - (int) scrollAmount;
			guiGraphics.fill(x1, y, Math.max(x1, x2), y + font.lineHeight, selColor);
		}
	}

	// Renders one display line as styled segments (markdown highlighting)
	private void renderStyledLine(GuiGraphicsExtractor guiGraphics, int lineIdx, int x, int y) {
		int cx = x;
		for (Segment segment : lineSegments.get(lineIdx)) {
			if (segment.text().isEmpty()) {
				continue;
			}

			final Style style = Style.EMPTY.withColor(segment.color()).withItalic(segment.italic()).withUnderlined(segment.underline());
			final Component rendered = Component.literal(segment.text()).withStyle(style);
			guiGraphics.text(font, rendered, cx, y, 0xffffffff, true);
			cx += font.width(rendered);
		}
	}

	// Width of the first pos characters of a styled line; positions are plain-text based and
	// segments concatenate to the exact line text, so this stays consistent
	private int styledPrefixWidth(int lineIdx, int pos) {
		int width = 0;
		int remaining = pos;
		for (Segment segment : lineSegments.get(lineIdx)) {
			if (remaining <= 0) {
				break;
			}

			final int take = Math.min(remaining, segment.text().length());
			final Style style = Style.EMPTY.withColor(segment.color()).withItalic(segment.italic()).withUnderlined(segment.underline());
			width += font.width(Component.literal(segment.text().substring(0, take)).withStyle(style));
			remaining -= take;
		}

		return width;
	}

	private void renderPaged(GuiGraphicsExtractor guiGraphics) {
		final List<String> pageLines = RenderUtils.splitStringToWidth(pages.get(page), width - 200);
		RenderUtils.renderSplitString(guiGraphics, pageLines, CONTENT_X, TEXT_AREA_Y, 0xffffffff);
		guiGraphics.centeredText(font, I18n.get("notes.pageIndicator", page + 1, pages.size()), width / 2 + 60, height - 45, RenderUtils.TEXT_MUTED);
	}

	private void setupButtons() {
		editButton = addRenderableWidget(new NotesButton(10, 40, BUTTON_WIDTH, 20, Component.translatable("notes.edit"), (onPress) -> {
			minecraft.setScreen(new EditNoteScreen(parentScreen, note));
		}));
		copyButton = addRenderableWidget(new NotesButton(10, 65, BUTTON_WIDTH, 20, Component.translatable("notes.copyText"), (onPress) -> {
			minecraft.keyboardHandler.setClipboard(getCopyableText());
			copiedTimer = 60;
		}));
		deleteButton = addRenderableWidget(new NotesButton(10, 90, BUTTON_WIDTH, 20, Component.translatable("notes.delete"), (onPress) -> {
			deleteNote();
		}));
		pinButton = addRenderableWidget(new NotesButton(10, 115, BUTTON_WIDTH, 20, isPinned() ? Component.translatable("notes.unpin") : Component.translatable("notes.pin"), (onPress) -> {
			togglePin();
			if (isPinned()) {
				minecraft.setScreen(null);
			}
		}));
		doneButton = addRenderableWidget(new NotesButton(10, height - 30, BUTTON_WIDTH, 20, Component.translatable("gui.done"), (onPress) -> {
			minecraft.setScreen(parentScreen);
		}));

		if (!NotesConfig.scrollableViewer) {
			prevButton = addRenderableWidget(new NotesButton(width / 2 - 40, height - 30, 20, 20, Component.literal("<"), (onPress) -> {
				if (page > 0) {
					page--;
				}
			}));
			nextButton = addRenderableWidget(new NotesButton(width / 2 + 20, height - 30, 20, 20, Component.literal(">"), (onPress) -> {
				if (page < pages.size() - 1) {
					page++;
				}
			}));
		}
	}

	private void setupContent() {
		final String text = note.getFilteredText();
		final int wrapWidth = NotesConfig.scrollableViewer ? width - CONTENT_X - 30 : width - 200;

		if (NotesConfig.scrollableViewer) {
			buildDisplayLines(text, wrapWidth);
			selAnchor = null;
			selFocus = null;
			draggingSelection = false;
			scrollAmount = Mth.clamp(scrollAmount, 0, maxScrollAmount());
		} else {
			final List<String> wrappedLines = NotesConfig.wrapNote ? RenderUtils.splitStringToWidth(text, wrapWidth) : StringUtils.wrapToWidth(text, wrapWidth);
			pages = new ArrayList<String>();
			int lineCount = 0;
			String pageText = "";
			for (String line : wrappedLines) {
				if (lineCount > 15) {
					pages.add(pageText);
					pageText = "";
					lineCount = 0;
				}

				pageText = pageText + line + "\n";
				lineCount++;
			}

			if (!pageText.isEmpty()) {
				pages.add(pageText);
			}

			if (pages.isEmpty()) {
				pages.add("");
			}

			page = Mth.clamp(page, 0, pages.size() - 1);
		}
	}

	// Splits the note into display lines while remembering which breaks are soft
	// (pure word wrapping, no newline in the source text) so copying restores the original line structure
	private void buildDisplayLines(String text, int wrapWidth) {
		lines = new ArrayList<DisplayLine>();
		final String[] paragraphs = text.split("\n", -1);
		for (String paragraph : paragraphs) {
			final List<String> wrapped = NotesConfig.wrapNote ? RenderUtils.splitStringToWidth(paragraph, wrapWidth) : StringUtils.wrapToWidth(paragraph, wrapWidth);
			if (wrapped.isEmpty()) {
				lines.add(new DisplayLine("", false));
				continue;
			}

			for (int i = 0; i < wrapped.size(); i++) {
				lines.add(new DisplayLine(wrapped.get(i), i < wrapped.size() - 1));
			}
		}

		lineSegments = new ArrayList<List<Segment>>();
		for (DisplayLine displayLine : lines) {
			lineSegments.add(parseMarkdown(displayLine.text));
		}
	}

	// Markdown-lite highlighting: # headers, **bold**, *italic* and `code` get colors.
	// Markers themselves stay visible so displayed text and copied text always match
	private List<Segment> parseMarkdown(String line) {
		final List<Segment> segments = new ArrayList<Segment>();
		if (line.isEmpty()) {
			segments.add(new Segment("", 0xffffffff, false, false));
			return segments;
		}

		if (!NotesConfig.markdown) {
			segments.add(new Segment(line, 0xffffffff, false, false));
			return segments;
		}

		final boolean header = line.startsWith("#");
		boolean bold = false;
		boolean italic = false;
		boolean code = false;
		final StringBuilder current = new StringBuilder();
		for (int i = 0; i < line.length();) {
			final char c = line.charAt(i);
			if (c == '*' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				flushSegment(segments, current, styleColor(header, bold, code), italic && !header, header);
				bold = !bold;
				current.append("**");
				i += 2;
			} else if (c == '*') {
				flushSegment(segments, current, styleColor(header, bold, code), italic && !header, header);
				italic = !italic;
				current.append("*");
				i += 1;
			} else if (c == '`') {
				flushSegment(segments, current, styleColor(header, bold, code), italic && !header, header);
				code = !code;
				current.append("`");
				i += 1;
			} else {
				current.append(c);
				i += 1;
			}
		}

		flushSegment(segments, current, styleColor(header, bold, code), italic && !header, header);
		return segments;
	}

	private int styleColor(boolean header, boolean bold, boolean code) {
		if (header) {
			return RenderUtils.headerColor();
		}

		if (bold) {
			return RenderUtils.accent();
		}

		if (code) {
			return 0xffb0b0b0;
		}

		return 0xffffffff;
	}

	private void flushSegment(List<Segment> segments, StringBuilder current, int color, boolean italic, boolean underline) {
		if (current.length() == 0) {
			return;
		}

		segments.add(new Segment(current.toString(), color, italic, underline));
		current.setLength(0);
	}

	private int lineHeight() {
		return font.lineHeight + LINE_SPACING;
	}

	private int scrollContentHeight() {
		return lines.size() * lineHeight() + 10;
	}

	private int maxScrollAmount() {
		return Math.max(0, scrollContentHeight() - (height - TEXT_AREA_Y - 10));
	}

	private int scrollViewHeight() {
		return height - TEXT_AREA_Y - 10;
	}

	private int scrollThumbHeight() {
		return Math.max(20, (int) ((long) scrollViewHeight() * scrollViewHeight() / Math.max(1, scrollContentHeight())));
	}

	private int scrollThumbY() {
		final int track = scrollViewHeight() - scrollThumbHeight();
		if (maxScrollAmount() <= 0) {
			return TEXT_AREA_Y;
		}

		return TEXT_AREA_Y + (int) ((double) track * scrollAmount / maxScrollAmount());
	}

	private void scrollBy(double lines) {
		setScrollAmount(scrollAmount + lines * lineHeight());
	}

	private void setScrollAmount(double amount) {
		scrollAmount = Mth.clamp(amount, 0, maxScrollAmount());
	}

	private boolean overScrollbar(MouseButtonEvent event) {
		// Scrollbar sits inset at the right edge of the text panel (6px wide)
		return maxScrollAmount() > 0 && event.x() >= width - 18 && event.x() < width - 12 && event.y() >= TEXT_AREA_Y && event.y() < TEXT_AREA_Y + scrollViewHeight();
	}

	private boolean overTextArea(double mouseX, double mouseY) {
		return mouseX >= CONTENT_X && mouseX < width - 10 && mouseY >= TEXT_AREA_Y && mouseY < TEXT_AREA_Y + scrollViewHeight();
	}

	private void updateScrollbarDrag(double mouseY) {
		final int track = scrollViewHeight() - scrollThumbHeight();
		if (track <= 0) {
			return;
		}

		final double thumbCenterOffset = (mouseY - TEXT_AREA_Y - scrollThumbHeight() / 2.0) / track;
		setScrollAmount(thumbCenterOffset * maxScrollAmount());
	}

	// Selection logic

	private record SelPos(int line, int pos) {}

	// A styled run of characters; markers like ** stay visible (syntax-highlight style),
	// so segments always concatenate to the exact display line text
	private record Segment(String text, int color, boolean italic, boolean underline) {}

	private static class DisplayLine {
		public final String text;
		// True if this line break is just word wrapping (the source line continues below)
		public final boolean soft;

		public DisplayLine(String text, boolean soft) {
			this.text = text;
			this.soft = soft;
		}
	}

	private SelPos hitTest(double mouseX, double mouseY) {
		if (lines.isEmpty()) {
			return new SelPos(0, 0);
		}

		int lineIdx = (int) Math.floor((mouseY - (TEXT_AREA_Y + 5) + scrollAmount) / lineHeight());
		lineIdx = Mth.clamp(lineIdx, 0, lines.size() - 1);
		final String lineText = lines.get(lineIdx).text;
		final double relX = mouseX - (CONTENT_X + 5);
		int pos = 0;
		for (int p = 1; p <= lineText.length(); p++) {
			if (font.width(lineText.substring(0, p)) > relX) {
				break;
			}
			pos = p;
		}

		return new SelPos(lineIdx, pos);
	}

	private int compareSel(SelPos a, SelPos b) {
		if (a.line() != b.line()) {
			return Integer.compare(a.line(), b.line());
		}

		return Integer.compare(a.pos(), b.pos());
	}

	private boolean hasSelection() {
		return selAnchor != null && selFocus != null && !selAnchor.equals(selFocus);
	}

	private SelPos selStart() {
		return compareSel(selAnchor, selFocus) <= 0 ? selAnchor : selFocus;
	}

	private SelPos selEnd() {
		return compareSel(selAnchor, selFocus) >= 0 ? selAnchor : selFocus;
	}

	private void selectWord(SelPos click) {
		final String text = lines.get(click.line()).text;
		if (text.isEmpty()) {
			return;
		}

		int start = Math.min(click.pos(), text.length() - 1);
		int end = start;
		while (start > 0 && !Character.isWhitespace(text.charAt(start - 1))) {
			start--;
		}
		while (end < text.length() && !Character.isWhitespace(text.charAt(end))) {
			end++;
		}

		if (start != end) {
			selAnchor = new SelPos(click.line(), start);
			selFocus = new SelPos(click.line(), end);
		}
	}

	private void selectAll() {
		if (lines.isEmpty()) {
			return;
		}

		selAnchor = new SelPos(0, 0);
		selFocus = new SelPos(lines.size() - 1, lines.get(lines.size() - 1).text.length());
	}

	// Returns the selected text, joining soft-wrapped lines without newlines;
	// falls back to the whole note when nothing is selected
	private String getCopyableText() {
		if (!hasSelection()) {
			return note.getFilteredText();
		}

		final SelPos a = selStart();
		final SelPos b = selEnd();
		final StringBuilder sb = new StringBuilder();
		for (int i = a.line(); i <= b.line(); i++) {
			final DisplayLine dl = lines.get(i);
			if (i == a.line() && i == b.line()) {
				sb.append(dl.text, Math.min(a.pos(), dl.text.length()), Math.min(b.pos(), dl.text.length()));
			} else if (i == a.line()) {
				sb.append(dl.text.substring(Math.min(a.pos(), dl.text.length())));
			} else if (i == b.line()) {
				sb.append(dl.text, 0, Math.min(b.pos(), dl.text.length()));
			} else {
				sb.append(dl.text);
			}

			if (i < b.line() && !dl.soft) {
				sb.append('\n');
			}
		}

		return sb.toString();
	}

	// Input handling

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (NotesConfig.scrollableViewer) {
			scrollBy(-scrollY * 2);
			return true;
		}

		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (NotesConfig.scrollableViewer && overScrollbar(event)) {
			draggingScrollbar = true;
			updateScrollbarDrag(event.y());
			return true;
		}

		if (NotesConfig.scrollableViewer && overTextArea(event.x(), event.y())) {
			final SelPos hit = hitTest(event.x(), event.y());
			if (doubleClick) {
				selectWord(hit);
			} else {
				selAnchor = hit;
				selFocus = hit;
				draggingSelection = true;
			}
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (draggingScrollbar) {
			updateScrollbarDrag(event.y());
			return true;
		}

		if (draggingSelection && NotesConfig.scrollableViewer && !lines.isEmpty()) {
			selFocus = hitTest(event.x(), event.y());
			return true;
		}

		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		// Backup path for drags: called on every move while the screen is open,
		// guarded by the drag flags so plain hover does nothing
		if (draggingScrollbar) {
			updateScrollbarDrag(mouseY);
		} else if (draggingSelection && NotesConfig.scrollableViewer && !lines.isEmpty()) {
			selFocus = hitTest(mouseX, mouseY);
		}

		super.mouseMoved(mouseX, mouseY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		draggingScrollbar = false;
		draggingSelection = false;
		return super.mouseReleased(event);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (NotesConfig.scrollableViewer) {
			if ((event.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0 && event.key() == GLFW.GLFW_KEY_A) {
				selectAll();
				return true;
			}

			if ((event.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0 && event.key() == GLFW.GLFW_KEY_C) {
				minecraft.keyboardHandler.setClipboard(getCopyableText());
				copiedTimer = 60;
				return true;
			}

			switch (event.key()) {
				case GLFW.GLFW_KEY_UP:
					scrollBy(-1);
					return true;
				case GLFW.GLFW_KEY_DOWN:
					scrollBy(1);
					return true;
				case GLFW.GLFW_KEY_PAGE_UP:
					scrollBy(-(scrollViewHeight() / 2.0) / lineHeight());
					return true;
				case GLFW.GLFW_KEY_PAGE_DOWN:
					scrollBy((scrollViewHeight() / 2.0) / lineHeight());
					return true;
				case GLFW.GLFW_KEY_HOME:
					setScrollAmount(0);
					return true;
				case GLFW.GLFW_KEY_END:
					setScrollAmount(maxScrollAmount());
					return true;
			}
		} else {
			if (event.key() == GLFW.GLFW_KEY_PAGE_UP && page > 0) {
				page--;
				return true;
			} else if (event.key() == GLFW.GLFW_KEY_PAGE_DOWN && page < pages.size() - 1) {
				page++;
				return true;
			}
		}

		return super.keyPressed(event);
	}

	private boolean isPinned() {
		return note.isPinned();
	}

	private void togglePin() {
		if (isPinned()) {
			note.unpin();
			pinButton.setMessage(Component.translatable("notes.pin"));
		} else {
			note.pin();
			pinButton.setMessage(Component.translatable("notes.unpin"));
		}
	}

	private void deleteNote() {
		minecraft.setScreen(new NotesConfirmScreen((result) -> {
			if (result) {
				note.delete();
			}

			ViewNoteScreen.this.minecraft.setScreen(parentScreen);
		}, Component.translatable("notes.confirmDelete"), Component.literal(note.getTitle())));
	}

}
