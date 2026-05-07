package com.tiestoettoet.create_train_parts.foundation.events;

import com.simibubi.create.content.contraptions.actors.trainControls.ControlsHandler;
import com.tiestoettoet.create_train_parts.content.decoration.slidingWindow.SlidingWindowRangeDisplay;
//import com.tiestoettoet.create_train_parts.content.foundation.blockEntity.behaviour.scrollValue.ScrollOptionRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent.ClientTickEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    public static void onTick(boolean isPreEvent) {
        if (!isGameActive())
            return;

//        ScrollOptionRenderer.tick();
        SlidingWindowRangeDisplay.tick();

    }

    protected static boolean isGameActive() {
        return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
    }
}
