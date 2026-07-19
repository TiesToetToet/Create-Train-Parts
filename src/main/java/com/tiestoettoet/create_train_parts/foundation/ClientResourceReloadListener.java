package com.tiestoettoet.create_train_parts.foundation;

import com.tiestoettoet.create_train_parts.foundation.sound.SoundScapes;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class ClientResourceReloadListener implements ResourceManagerReloadListener {
	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		SoundScapes.invalidateAll();
	}
}
