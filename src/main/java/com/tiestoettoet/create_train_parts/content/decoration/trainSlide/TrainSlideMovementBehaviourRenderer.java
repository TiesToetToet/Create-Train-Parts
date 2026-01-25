package com.tiestoettoet.create_train_parts.content.decoration.trainSlide;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Client-only renderer helper for TrainSlideMovementBehaviour.
 * This class is separated to prevent client-only classes from being loaded on the server.
 */
@OnlyIn(Dist.CLIENT)
public class TrainSlideMovementBehaviourRenderer {

    public static void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, 
                                           ContraptionMatrices matrices, MultiBufferSource buffer,
                                           LerpedFloat animation) {
        float animValue = animation.getValue(AnimationTickHolder.getPartialTicks(context.world));
        PoseStack ms = matrices.getModel();
        int light = LevelRenderer.getLightColor(renderWorld, context.localPos);
        Level world = context.contraption.getContraptionWorld();
        TrainSlideRenderer.renderTrainSlide(
                context.state,
                context.localPos,
                world,
                animValue,
                ms,
                buffer,
                light,
                matrices
        );
    }
}
