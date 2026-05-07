package com.tiestoettoet.create_train_parts.content.decoration.slidingWindow;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Client-only renderer helper for SlidingWindowMovementBehaviour.
 * This class is separated to prevent client-only classes from being loaded on the server.
 */
@OnlyIn(Dist.CLIENT)
public class SlidingWindowMovementBehaviourRenderer {

    public static void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, 
                                           ContraptionMatrices matrices, MultiBufferSource buffer) {
        SlidingWindowRenderer.renderInContraption(context, renderWorld, matrices, buffer);
    }
}
