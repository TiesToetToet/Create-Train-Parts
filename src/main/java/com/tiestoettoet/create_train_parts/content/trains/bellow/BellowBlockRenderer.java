package com.tiestoettoet.create_train_parts.content.trains.bellow;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tiestoettoet.create_train_parts.AllPartialModels;
import com.tiestoettoet.create_train_parts.foundation.collision.BellowSize;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Draws the bellow frame by tiling the authored parts, so every width and
 * height combination is built from the same one block pieces.
 */
public class BellowBlockRenderer implements BlockEntityRenderer<BellowBlockEntity> {

    /** Height of a single post piece, in blocks. */
    private static final double POST_HEIGHT = 15 / 16.0;
    /** Height of the bottom and top bars, in blocks. */
    private static final double BAR_HEIGHT = 1 / 16.0;

    @Override
    public void render(BellowBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
            int overlay) {
        BlockState state = be.getBlockState();
        if (!state.hasProperty(BellowBlock.VISIBLE) || !state.getValue(BellowBlock.VISIBLE))
            return;

        BellowSize size = BellowBlock.getSize(state);
        Direction facing = state.getValue(BellowBlock.FACING);
        VertexConsumer vb = buffer.getBuffer(RenderType.solid());

        int width = size.width();
        int height = size.height();
        // The parts are authored for the left hand block of the frame, so the
        // whole frame is shifted to stay centred on its block.
        double frameLeft = 0.5 - 0.5 * width;
        double frameRight = 0.5 * width - 0.5;
        double topBar = height - BAR_HEIGHT;

        for (int column = 0; column < width; column++) {
            double x = frameLeft + column;
            part(AllPartialModels.BELLOW_END_BOTTOM, state, facing, x, 0, 1, light, ms, vb);
            part(AllPartialModels.BELLOW_END_TOP, state, facing, x, topBar, 1, light, ms, vb);
        }

        // Posts fill the gap between both bars exactly, which needs a slight
        // stretch whenever that gap is not a whole number of post pieces.
        double postSpan = height - 2 * BAR_HEIGHT;
        double postStep = postSpan / height;
        float postScale = (float) (postStep / POST_HEIGHT);

        for (int row = 0; row < height; row++) {
            double y = BAR_HEIGHT + row * postStep;
            part(AllPartialModels.BELLOW_END_LEFT, state, facing, frameRight, y, postScale, light, ms, vb);
            part(AllPartialModels.BELLOW_END_RIGHT, state, facing, frameLeft, y, postScale, light, ms, vb);
        }
    }

    private static void part(PartialModel model, BlockState state, Direction facing, double x, double y,
            float verticalScale, int light, PoseStack ms, VertexConsumer vb) {
        CachedBuffers.partial(model, state)
                .translate(0.5, 0, 0.5)
                .rotateYDegrees(-(facing.toYRot() + 180))
                .translate(-0.5, 0, -0.5)
                .translate(x, y, 0)
                .scale(1, verticalScale, 1)
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
