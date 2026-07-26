package com.tiestoettoet.create_train_parts.content.trains.bellow;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tiestoettoet.create_train_parts.AllPartialModels;
import com.tiestoettoet.create_train_parts.foundation.collision.BellowSize;

import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Draws the bellow frame scaled to its configured size, so a single model can
 * serve every width and height combination.
 */
public class BellowBlockRenderer implements BlockEntityRenderer<BellowBlockEntity> {

    @Override
    public void render(BellowBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
            int overlay) {
        BlockState state = be.getBlockState();
        if (!state.hasProperty(BellowBlock.VISIBLE) || !state.getValue(BellowBlock.VISIBLE))
            return;

        BellowSize size = BellowBlock.getSize(state);
        Direction facing = state.getValue(BellowBlock.FACING);
        VertexConsumer vb = buffer.getBuffer(RenderType.solid());

        // The model is authored for a north facing, one by two frame, so it is
        // rotated and scaled around the block centre to stay symmetrical.
        CachedBuffers.partial(AllPartialModels.BELLOW_END, state)
                .translate(0.5, 0, 0.5)
                .rotateYDegrees(-(facing.toYRot() + 180))
                .scale(size.widthScale(), size.heightScale(), 1)
                .translate(-0.5, 0, -0.5)
                .light(light)
                .renderInto(ms, vb);
    }

    @Override
    public boolean shouldRenderOffScreen(BellowBlockEntity be) {
        // The frame reaches above its own chunk section, which would otherwise
        // cull the whole bellow as soon as that section leaves the frustum.
        return true;
    }
}
