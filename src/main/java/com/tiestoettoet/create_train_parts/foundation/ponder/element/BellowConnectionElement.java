package com.tiestoettoet.create_train_parts.foundation.ponder.element;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tiestoettoet.create_train_parts.content.trains.bellow.BellowBlock;
import com.tiestoettoet.create_train_parts.content.trains.entity.BellowRenderer;
import com.tiestoettoet.create_train_parts.foundation.collision.BellowCollisionGeometry;
import com.tiestoettoet.create_train_parts.foundation.collision.BellowSegment;

import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.element.AnimatedSceneElementBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the flexible connection between two bellow blocks inside a ponder
 * scene. Trains only exist in a real level, so the scene cannot rely on the
 * regular renderer and builds the same curve from two block positions instead.
 */
public class BellowConnectionElement extends AnimatedSceneElementBase {

    private final BlockPos first;
    private final BlockPos second;

    private Vec3 offset = Vec3.ZERO;
    private Vec3 previousOffset = Vec3.ZERO;
    private int totalTicks;
    private int remainingTicks;

    public BellowConnectionElement(BlockPos first, BlockPos second) {
        this.first = first;
        this.second = second;
    }

    /** Grows the connection from the first bellow to the second. */
    public void connect(int ticks) {
        totalTicks = Math.max(ticks, 0);
        remainingTicks = totalTicks;
    }

    public Vec3 getOffset() {
        return offset;
    }

    public void setOffset(Vec3 offset) {
        // Instructions only run on ticks, while world sections are drawn with
        // partial ticks. Keeping the previous value lets rendering interpolate
        // so the connection does not trail the carriages it belongs to.
        this.previousOffset = this.offset;
        this.offset = offset;
    }

    @Override
    public void tick(PonderScene scene) {
        if (remainingTicks > 0)
            remainingTicks--;
    }

    @Override
    public void reset(PonderScene scene) {
        remainingTicks = totalTicks;
        offset = Vec3.ZERO;
        previousOffset = Vec3.ZERO;
    }

    @Override
    protected void renderLayer(PonderLevel world, MultiBufferSource buffer, RenderType type, GuiGraphics graphics,
            float fade, float pt) {
        if (type != RenderType.solid())
            return;

        List<BellowSegment> segments = buildSegments(world);
        if (segments.isEmpty())
            return;

        int shown = Mth.ceil(progress(pt) * segments.size());
        if (shown <= 0)
            return;

        PoseStack ms = graphics.pose();
        VertexConsumer vb = buffer.getBuffer(type);
        Vec3 renderedOffset = previousOffset.lerp(offset, pt);

        ms.pushPose();
        ms.translate(renderedOffset.x, renderedOffset.y, renderedOffset.z);
        BellowRenderer.renderSegments(segments.subList(0, Math.min(shown, segments.size())), Vec3.ZERO,
                lightCoordsFromFade(fade), ms, vb);
        ms.popPose();
    }

    private List<BellowSegment> buildSegments(PonderLevel world) {
        BlockState firstState = world.getBlockState(first);
        BlockState secondState = world.getBlockState(second);
        if (!(firstState.getBlock() instanceof BellowBlock) || !(secondState.getBlock() instanceof BellowBlock))
            return List.of();

        return BellowCollisionGeometry.buildSegments(
                Vec3.atCenterOf(first), outward(firstState),
                Vec3.atCenterOf(second), outward(secondState),
                BellowBlock.getSize(firstState));
    }

    /** The frame sits on the face opposite the block's facing. */
    private static Vec3 outward(BlockState state) {
        Direction facing = state.getValue(BellowBlock.FACING);
        return Vec3.atLowerCornerOf(facing.getOpposite()
                .getNormal());
    }

    private float progress(float pt) {
        if (totalTicks == 0)
            return 1;
        return 1 - Math.max(0, remainingTicks - pt) / totalTicks;
    }
}
