package com.chaosthedude.notes;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import com.chaosthedude.notes.config.NotesConfig;
import com.chaosthedude.notes.gui.SelectNoteScreen;
import com.chaosthedude.notes.note.Note;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class Notes implements ClientModInitializer {

	public static final String MODID = "notes";

	public static final Logger LOGGER = LogManager.getLogger(MODID);

	// Multiple notes can be pinned at once; they stack on the HUD
	public static final List<Note> pinnedNotes = new ArrayList<Note>();

	private static KeyMapping openNotes;

	@Override
	public void onInitializeClient() {
		NotesConfig.load();
		
		openNotes = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.openNotes", GLFW.GLFW_KEY_N, new KeyMapping.Category(Identifier.fromNamespaceAndPath(MODID, "keys"))));
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
		    while (openNotes.isDown()) {
		    	client.gui.setScreen(new SelectNoteScreen(client.gui.screen()));
		    }
		});
	}

}
