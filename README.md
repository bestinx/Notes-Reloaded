# Notes Reloaded

**Notes Reloaded** — revived fork of [Notes](https://github.com/MattCzyr/Notes) by [ChaosTheDude](https://github.com/MattCzyr), a clientside Minecraft mod that adds a fully functional in-game notepad. This fork targets **Fabric 1.21.11** and brings the original back to life with a redesigned interface, new features and quality-of-life improvements.

> Made with permission to revive the mod. All original notes (`.txt` files in the `notes` folder) and the config stay fully compatible — same mod id, same file formats.

## Features

### Reader
- **Scrollable note viewer** — read notes with the mouse wheel, a draggable scrollbar, or the keyboard (`Up/Down/PageUp/PageDown/Home/End`). The legacy page-by-page mode is still available in settings.
- **Text selection** — drag to select, double-click to select a word, `Ctrl+A` / `Ctrl+C`; the copy button copies the selection when present, otherwise the whole note. Soft-wrapped lines are joined back together when copying.
- **Markdown-lite highlighting** — `# headers`, `**bold**`, `*italic*` and `` `code` `` are tinted while reading; markers stay visible, so what you see is always what you copy. Toggleable in settings.

### Editor
- **Ctrl+S** to save, line/character counter in the status bar
- `Enter` in the title field jumps to the text editor
- **Unsaved changes are protected** — closing the editor asks for confirmation and the screen shows a "dirty" marker
- Insert biome, chunk and coordinates into the text with dedicated buttons

### Notes list
- **Search** — filter notes by title and content
- **Sorting** — newest / oldest / A-Z / Z-A (persisted)
- **Multiple pinned notes** — pin as many notes as you like; they stack vertically on the HUD
- Copy, duplicate, delete and open-in-system-editor actions

### Interface
- **6 UI themes** (amber, sky, mint, rose, lavender, mono) with a visual color-swatch picker; buttons, fields, scrollbars, selection and the pinned-note HUD border all follow the theme
- **Redesigned look** — graphite window cards with crisp borders, drop shadows and themed accents (nothing shared with the original mod's flat gray panels)
- **ModMenu integration** — settings gear right on the mod's entry
- **Full in-game settings screen** — theme picker, pinned position/width/height/opacity, scrollable viewer, word wrap, markdown, in-game editor/viewer, unsaved-change confirmation, date format with live validation, open notes folder, reset to defaults
- **Robustness** — a corrupted config is backed up as `notes.json.broken` and recreated instead of crashing the game

## Controls

| Key | Action |
| --- | --- |
| `N` | Open the notes list (rebindable in the vanilla controls menu) |
| `Ctrl+S` | Save note (editor) |
| `Ctrl+A` / `Ctrl+C` | Select all / copy selection (viewer) |
| `Up/Down/PageUp/PageDown/Home/End` | Scroll the note (viewer) |

Notes are stored as plain `.txt` files in the `notes` folder of your game directory, grouped by scope: `global`, `local/<world name>` and `remote/<server ip>`.

## Building

```bash
./gradlew build
```

The jar lands in `build/libs/`. Requires JDK 21.

## Credits & License

Original mod by **[ChaosTheDude](https://github.com/MattCzyr/Notes)** — huge thanks for the codebase and the permission to revive it.

This fork is available under the same [Creative Commons Attribution-NonCommercial ShareAlike 4.0 International](https://creativecommons.org/licenses/by-nc-sa/4.0/legalcode) license as the original mod.
