package com.tiestoettoet.create_train_parts.content.decoration.trainStep;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.ContraptionWorld;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Client-only renderer helper for TrainStepMovementBehaviour.
 * This class is separated to prevent client-only classes from being loaded on the server.
 */
@OnlyIn(Dist.CLIENT)
public class TrainStepMovementBehaviourRenderer {

    public static void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, 
                                           ContraptionMatrices matrices, MultiBufferSource buffer,
                                           LerpedFloat animation) {
        ContraptionWorld world = context.contraption.getContraptionWorld();
        int light = LevelRenderer.getLightColor(renderWorld, context.localPos);
        float animValue = animation.getValue(AnimationTickHolder.getPartialTicks(context.world));
        PoseStack ms = matrices.getModel();
        TrainStepRenderer.renderTrainStep(
                context.state,
                context.localPos,
                world,
                animValue,
                ms,
                light,
                buffer,
                matrices
        );
    }
}
