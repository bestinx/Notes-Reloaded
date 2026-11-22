package com.chaosthedude.notes;

import com.chaosthedude.notes.gui.NotesConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Shows the settings gear on the Notes entry in ModMenu, opening the in-game config screen.
 */
public class ModMenuIntegration implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return NotesConfigScreen::new;
	}

}
