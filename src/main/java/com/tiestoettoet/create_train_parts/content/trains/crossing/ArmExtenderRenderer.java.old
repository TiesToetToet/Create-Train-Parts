package com.tiestoettoet.create_train_parts.content.trains.crossing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.tiestoettoet.create_train_parts.AllPartialModels;
import com.tiestoettoet.create_train_parts.content.decoration.trainStep.TrainStepBlockEntity;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ArmExtenderRenderer extends SafeBlockEntityRenderer<ArmExtenderBlockEntity> {
    public ArmExtenderRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(ArmExtenderBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        BlockState blockState = be.getBlockState();
        if (!be.shouldRenderSpecial(blockState))
            return;

        // BlockPos pos = be.getBlockPos();
        BlockAndTintGetter world = be.getLevel();
        BlockPos pos = be.getBlockPos();

        Level level = be.getLevel();

        int adjustedLight = LevelRenderer.getLightColor(world, pos);

        Direction facing = blockState.getValue(ArmExtenderBlock.FACING);
        boolean flipped = blockState.getValue(ArmExtenderBlock.FLIPPED);

        float rotationAngle = switch (facing) {
            case NORTH -> 270; // No rotation needed
            case SOUTH -> 90;
            case WEST -> 0;
            case EAST -> 180;
            default -> 0;
        };

        float value = be.animation.getValue(partialTicks);
        // System.out.println("TrainStepRenderer: Value: " + value);
        float exponentialValue = (float) value * value;
        float relativeValue = blockState.getValue(ArmExtenderBlock.OPEN) ? exponentialValue : 1 - exponentialValue;
        float relativeAnimationValue = relativeValue;

        VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

        if (blockState.getBlock() instanceof ArmExtenderBlock) {
            float f = blockState.getValue(ArmExtenderBlock.OPEN) ? -1 : 1;
            float target = be.animation.getChaseTarget();

            // value = (2*value-1)*(target-0.5f)+0.5f;//flips pos between 0 and 1 depending
            // on target
            //// System.out.println(value);
            // float fallTime = 0.3f;
            // if(value<fallTime)
            // value = 1f-value*value/(fallTime*fallTime);
            // else {
            // value = (value-fallTime)/(1f-fallTime);
            // float bounce = (float)(Math.exp(-value*4.0)*Math.sin(value*Math.PI*3.0));
            // float smoothing = 0.1f;
            // bounce = (float)Math.sqrt(bounce*bounce+smoothing*smoothing)-smoothing;
            // value = bounce/3f;
            // }
            //
            // value = -(2*value-1)*(target-0.5f)+0.5f;

            value = 0.5f * (1 - Mth.cos(Mth.PI * value));

            // float angle = value*0.78f;
            float angle = value * 1.56f;
//            System.out.println("Angle: " + angle);

            boolean top = value < 0.2;
            boolean bottom = value > 0.8;
//            boolean flipped = blockState.getValue(ArmExtenderBlock.FACING);

            PartialModel arm = flipped ? AllPartialModels.ARM_EXTENDER_FLIPPED : AllPartialModels.ARM_EXTENDER;

            SuperByteBuffer partial_arm = CachedBuffers.partial(arm, blockState);

            int lightInFront = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().relative(Direction.UP));
            int lightBehind = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().relative(Direction.DOWN));

            Direction axis = flipped ? facing.getCounterClockWise() : facing.getClockWise();

            int armExtenderInRow = be.getArmExtenderInRow();
//            System.out.println("Arm Extender in Row: " + armExtenderInRow);

//            float movement = armExtenderInRow

            Vec3 pivot = Vec3.atLowerCornerOf(flipped ? facing.getClockWise().getNormal() : facing.getCounterClockWise().getNormal())
                    .scale(armExtenderInRow);
            partial_arm
                    .light(lightInFront)
                    .translate(pivot.x, pivot.y, pivot.z)                         // move to pivot
                    .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y) // rotate to face correct direction
                    .rotateCenteredDegrees(Mth.RAD_TO_DEG * angle, Direction.EAST)    // animate open/close
                    .translate(-pivot.x, -pivot.y, -pivot.z)                     // move back from pivot
                    .renderInto(ms, vb);

        }


    }
}
