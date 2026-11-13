package com.tiestoettoet.create_train_parts.foundation.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tiestoettoet.create_train_parts.CreateTrainPartsClient;
import com.tiestoettoet.create_train_parts.content.decoration.slidingWindow.SlidingWindowRangeDisplay;
import com.tiestoettoet.create_train_parts.content.trains.entity.BellowRenderer;
//import com.tiestoettoet.create_train_parts.content.foundation.blockEntity.behaviour.scrollValue.ScrollOptionRenderer;
import com.tiestoettoet.create_train_parts.foundation.sound.SoundScapes;

import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {
	@SubscribeEvent
    public static void onTick(ClientTickEvent event) {
        // Fires for both START and END, only tick once
        if (event.phase != TickEvent.Phase.END)
            return;

        if (!isGameActive())
            return;

//        ScrollOptionRenderer.tick();
        SoundScapes.tick();
        SlidingWindowRangeDisplay.tick();

    }

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
            return;

        PoseStack ms = event.getPoseStack();
        ms.pushPose();
        SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera()
                .getPosition();

        BellowRenderer.renderAll(ms, buffer, camera);

        buffer.draw();
        RenderSystem.enableCull();
        ms.popPose();
    }

    protected static boolean isGameActive() {
        return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
    }

	@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
	public static class ModBusEvents {
		@SubscribeEvent
		public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
			event.registerReloadListener(CreateTrainPartsClient.RESOURCE_RELOAD_LISTENER);
		}
	}
}
