## Version 2.5.0 (Notes Reloaded fork)
### Fabric 1.21.11
- Complete visual redesign, no longer sharing the original mod's flat look: graphite window cards with crisp borders, drop shadows and a themed accent strip on top; card-style buttons, text fields, sliders and list rows; accent underline on hovered buttons and accent borders on focused fields

## Version 2.4.0 (Notes Reloaded fork)
### Fabric 1.21.11
- Markdown-lite highlighting in the viewer: # headers (theme header color, underlined), **bold** (accent), *italic* and `code` (gray); markers stay visible so displayed text and copied text always match; toggle in settings
- Multiple notes can be pinned at once; they stack vertically on the HUD
- Pinned note background opacity slider in settings
- Note list sorting: newest/oldest/A-Z/Z-A via a button next to the search field (persisted)

## Version 2.3.1 (Notes Reloaded fork)
### Fabric 1.21.11
- Theme picker: visual swatch panel in settings (click a color to switch instantly) instead of a cycling text button
- ModMenu integration: a settings gear now appears on the Notes entry in the mod list

## Version 2.3.0 (Notes Reloaded fork)
### Fabric 1.21.11
- Text selection in the note viewer: drag to select, double click to select a word, Ctrl+A selects all, Ctrl+C copies; the copy button copies the selection when present (soft line wraps are joined back together when copying)
- Six UI themes (amber, sky, mint, rose, lavender, mono) with a new Theme setting; the pinned note HUD border follows the theme
- Settings screen layout reworked: wider widgets, shorter labels, date format label above its field, validity hint under it, slider handle no longer covers the label
- Minor: thinner slider handle with progress bar, cleaner positioning dialog

## Version 2.2.0 (Notes Reloaded fork)
### Fabric 1.21.11
- Note viewer now scrolls freely with the mouse wheel, a draggable scrollbar, and keyboard shortcuts (Up/Down/PageUp/PageDown/Home/End) instead of page-flipping only; legacy page mode is still available in settings
- New in-game settings screen (gear from the note list): pinned note position/width/height, scrollable viewer, word wrap, in-game editor/viewer, unsaved-change confirmation, date format, open notes folder, reset to defaults
- Search bar in the note selection screen filters notes by title and content
- "Copy Text" button copies a note to the clipboard
- Ctrl+S saves a note from the editor; Enter in the title field jumps to the text editor
- Unsaved changes are now protected: canceling/ESC asks for confirmation and marks the editor with a "dirty" indicator
- Editor status bar with line and character counts
- Refreshed flat UI: accent highlights on hover/selection, header underline, hover highlight in the note list, note count display
- Corrupted config no longer crashes the game: it is backed up as notes.json.broken and recreated with defaults
- Fixed REMOTE scope reporting itself as active in singleplayer
- Fixed invalid date format in config crashing timestamps (falls back to default)
- Misc robustness fixes (resource handling, null-safe config loading with sane clamping)

## Version 3.0.1
### NeoForge 1.21.10, 1.21.8, 1.21.7, 1.21.6, 1.21.5, 1.21.4, 1.21.3, 1.21.1, 1.21
- Updated to NeoForge 1.21.10
- Updated to NeoForge 1.21.8
- Updated to NeoForge 1.21.7
- Updated to NeoForge 1.21.6
- Updated to NeoForge 1.21.5
- Updated to NeoForge 1.21.4
- Updated to NeoForge 1.21.3
- Updated to NeoForge 1.21.1
- Updated to NeoForge 1.21
- Fixed bug that caused local notes to be saved globally

## Version 2.1.1
### Fabric 1.21.10, 1.21.8, 1.21.7, 1.21.6, 1.21.5, 1.21.4, 1.21.3, 1.21.1, 1.21
- Updated to Fabric 1.21.10
- Updated to Fabric 1.21.8
- Updated to Fabric 1.21.7
- Updated to Fabric 1.21.6
- Updated to Fabric 1.21.5
- Updated to Fabric 1.21.4
- Updated to Fabric 1.21.3
- Updated to Fabric 1.21.1
- Updated to Fabric 1.21
- Fixed bug that caused local notes to be saved globally

## Version 1.3.1
### Forge 1.21.5, 1.21.4, 1.21.3, 1.21.1, 1.21
- Updated to Forge 1.21.5
- Updated to Forge 1.21.4
- Updated to Forge 1.21.3
- Updated to Forge 1.21.1
- Updated to Forge 1.21
- Fixed bug that caused local notes to be saved globally

## Version 3.0.0
### NeoForge 1.20.6, 1.20.5
- Updated to NeoForge 1.20.6
- Updated to NeoForge 1.20.5

## Version 1.3.0
### Forge 1.20.6, 1.20.4, 1.20.3, 1.20.2, 1.20.1, 1.20, 1.19.4
- Updated to Forge 1.20.6
- Updated to Forge 1.20.4
- Updated to Forge 1.20.3
- Updated to Forge 1.20.2
- Updated to Forge 1.20.1
- Updated to Forge 1.20
- Fixed pinned notes not respecting configured maximum width and height
- Fixed crash that occurred rarely when clicking the insert biome button
- Fixed text spilling off right side of note body background
- Adjusted button positions in note selection and edit GUI's
- Note body will no longer lose focus after clicking the insert biome, coordinates, or chunk buttons

## Version 2.1.0
### Fabric 1.20.6, 1.20.4, 1.20.3, 1.20.2, 1.20.1, 1.20, 1.19.4
- Updated to Fabric 1.20.6
- Updated to Fabric 1.20.4
- Updated to Fabric 1.20.3
- Updated to Fabric 1.20.2
- Updated to Fabric 1.20.1
- Updated to Fabric 1.20
- Fixed pinned notes not respecting configured maximum width and height
- Fixed crash that occurred rarely when clicking the insert biome button
- Fixed text spilling off right side of note body background
- Fixed button color not changing when hovered
- Adjusted button positions in note selection and edit GUI's
- Note body will no longer lose focus after clicking the insert biome, coordinates, or chunk buttons

## Version 1.2.6
### Forge 1.19.4
- Updated to Forge 1.19.4
- Fixed pinned note background opacity not matching chat opacity

## Version 2.0.1
### Fabric 1.19.4
- Updated to Fabric 1.19.4
- Fixed pinned note background opacity not matching chat opacity

## Version 2.0.0
### Fabric 1.19.3, 1.19.2
- Updated to Fabric 1.19.3
- Updated to Fabric 1.19.2

## Version 1.2.5
### Forge 1.19.3, 1.19.2, 1.19.1, 1.19
- Updated to Forge 1.19.3
- Updated to Forge 1.19.2
- Updated to Forge 1.19.1
- Made compatible with Forge 41.0.64 or higher

## Version 1.2.4
### Forge 1.19.1, 1.19, 1.18.2, 1.18.1, 1.17.1
- Updated to Forge 1.19.1
- Updated to Forge 1.19
- Updated to Forge 1.18.2
- Updated to Forge 1.18.1
- Updated to Forge 1.17.1
- Fixed incorrect scroll direction when trying to scroll down
- Added Indonesian translations

## Version 1.2.3
### Forge 1.16.5, 1.16.4
- Updated to Forge 1.16.5
- Fixed issue that caused servers to be marked as incompatible when mod is not installed on server
- Allowed mod to be installed on servers without crashing

## Version 1.2.2
### Forge 1.16.4
- Fixed a bug that prevented phrases from being translated when using English localization

## Version 1.2.1
### Forge 1.16.4
- Updated to Forge 1.16.4
- Added Swedish translations

## Version 1.2.0
### Forge 1.15.2, 1.14.4, 1.13.2
- Added a "pinned" indicator next to the pinned note in the note selection GUI
- Automatically size note text field based on GUI height
- Fixed a HeadlessException when trying to open a note in the system text editor or viewer on an unsupported system
- Updated to Forge 1.15.2
- Updated to Forge 1.14.4
- Updated to Forge 1.13.2

## Version 1.1.1
### Forge 1.12, 1.11.2, 1.10.2, 1.7.10
- Updated to Forge 1.12
- Fixed FileNotFoundException after editing a pinned note
- Fixed StringIndexOutOfBoundsException when attempting to copy an empty selection

## Version 1.1.0
### Forge 1.11.2, 1.10.2, 1.7.10
- Added the ability to tab from title field to text field
- Added a config field to set the HUD position of a pinned note
- Fixed IndexOutOfBoundsException when ending a selection on an empty line

## Version 1.0.1
### Forge 1.11.2, 1.10.2, 1.7.10
- Fixed NPE that occurred when opening the Notes GUI on some multiplayer servers
- Fixed pinned note not being unpinned upon deletion
- Fixed notes being loaded if the left side of its list entry is clicked

## Version 1.0.0
### Forge 1.11.2, 1.11, 1.10.2, 1.7.10
- Initial release
