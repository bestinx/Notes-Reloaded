package com.chaosthedude.notes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chaosthedude.notes.config.NotesConfig;
import com.chaosthedude.notes.event.NotesEvents;
import com.chaosthedude.notes.note.Note;

import java.util.ArrayList;
import java.util.List;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Notes.MODID)
public class Notes {

	public static final String MODID = "notes";

	public static final Logger LOGGER = LogManager.getLogger(MODID);

	// Multiple notes can be pinned at once; they stack on the HUD
	public static final List<Note> pinnedNotes = new ArrayList<Note>();
	
	public Notes(ModContainer modContainer) {
		if (FMLEnvironment.getDist() == Dist.CLIENT) {
			NotesConfig.load();
			modContainer.getEventBus().addListener(NotesEvents::registerKeybinds);
			modContainer.getEventBus().addListener(NotesEvents::registerOverlay);
			NeoForge.EVENT_BUS.addListener(NotesEvents::onClientTick);
		}
	}

}
