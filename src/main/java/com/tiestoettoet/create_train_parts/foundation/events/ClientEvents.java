package com.tiestoettoet.create_train_parts.foundation.events;

import com.simibubi.create.content.contraptions.actors.trainControls.ControlsHandler;
import com.tiestoettoet.create_train_parts.content.decoration.slidingWindow.SlidingWindowRangeDisplay;
//import com.tiestoettoet.create_train_parts.content.foundation.blockEntity.behaviour.scrollValue.ScrollOptionRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onTickPre(ClientTickEvent.Pre event) {
        onTick( true);
    }

    @SubscribeEvent
    public static void onTickPost(ClientTickEvent.Post event) {
        onTick(false);
    }

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
