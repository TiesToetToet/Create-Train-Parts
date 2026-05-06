package com.tiestoettoet.create_train_parts.content.trains.crossing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.clock.CuckooClockBlock;
import com.simibubi.create.content.kinetics.clock.CuckooClockBlockEntity;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.foundation.block.connected.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.render.RenderTypes;
import com.tiestoettoet.create_train_parts.AllPartialModels;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CrossingRenderer extends KineticBlockEntityRenderer<CrossingBlockEntity> {
    public CrossingRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(CrossingBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
            int light, int overlay) {
        BlockState blockState = be.getBlockState();
        if (!be.shouldRenderSpecial(blockState))
            return;

        // BlockPos pos = be.getBlockPos();
        BlockAndTintGetter world = be.getLevel();
        BlockPos pos = be.getBlockPos();

        int adjustedLight = LevelRenderer.getLightColor(world, pos);

        Direction facing = blockState.getValue(CrossingBlock.HORIZONTAL_FACING);

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
        float relativeValue = blockState.getValue(CrossingBlock.OPEN) ? exponentialValue : 1 - exponentialValue;
        float relativeAnimationValue = relativeValue;

        VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

        if (blockState.getBlock() instanceof CrossingBlock) {
            float f = blockState.getValue(CrossingBlock.OPEN) ? -1 : 1;
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

            value = 0.5f * (1-Mth.cos(Mth.PI * value));

            // float angle = value*0.78f;
            float angle = value * 1.56f;

            boolean top = value < 0.2;
            boolean bottom = value > 0.8;
            boolean flipped = blockState.getValue(CrossingBlock.FLIPPED);

            PartialModel arm = flipped ? AllPartialModels.ARM_FLIPPED : AllPartialModels.ARM;

            SuperByteBuffer partial_arm = CachedBuffers.partial(arm, blockState);

            int lightInFront = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().relative(Direction.UP));
            int lightBehind = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().relative(Direction.DOWN));

            partial_arm
                    .light(lightInFront)
                    .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                    .rotateCenteredDegrees(Mth.RAD_TO_DEG * angle, Direction.EAST)
                    .renderInto(ms, vb);

            SuperByteBuffer shaftHalf =
                    CachedBuffers.partialFacing(com.simibubi.create.AllPartialModels.SHAFT_HALF, be.getBlockState(), Direction.DOWN);

            standardKineticRotationTransform2(shaftHalf, be, lightBehind).renderInto(ms, vb);

            CachedBuffers.partial(flipped ? AllPartialModels.LIGHTS_FLIPPED : AllPartialModels.LIGHTS, blockState)
                    .light(lightInFront)
                    .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                    .renderInto(ms, buffer.getBuffer(RenderType.translucent()));



            if (value != 1 && be.getSpeed() != 0) {

                float movementMain = 8 / 16f;
                float movementSecondary = 14 / 16f;
                float movementUp;

                long gameTime = be.getLevel().getGameTime();
                if ((gameTime / 10) % 2 == 0) {
                    movementUp = 19 / 16f;
                } else {
                    movementUp = 14 / 16f;
                }


                Vec3 movementMainVec = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movementMain);
                Vec3 movementUpVec = Vec3.atLowerCornerOf(Direction.UP.getNormal()).scale(movementUp);
                Vec3 movementSecondaryVec = Vec3.atLowerCornerOf(facing.getCounterClockWise().getNormal()).scale(movementSecondary);

                Vec3 movement = movementMainVec.add(movementUpVec).add(movementSecondaryVec);


                CachedBuffers.partial(com.simibubi.create.AllPartialModels.SIGNAL_WHITE_CUBE, blockState)
                        .light(0xF000F0)
                        .translate(movement.x, movement.y, movement.z)
                        .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                        .disableDiffuse()
                        .scale(1, 1, 1)
                        .renderInto(ms, buffer.getBuffer(RenderType.translucent()));



                CachedBuffers
                        .partial(
                                com.simibubi.create.AllPartialModels.SIGNAL_RED_GLOW,
                                blockState)
                        .light(0xF000F0)
                        .translate(movement.x, movement.y, movement.z)
                        .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                        .disableDiffuse()
                        .scale(1.5f,2, 2)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.additive()));

                CachedBuffers
                        .partial(AllPartialModels.CROSSING_LAMP
                                , blockState)
                        .light(0xF000F0)
                        .translate(movement.x, movement.y, movement.z)
                        .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                        .disableDiffuse()
                        .scale(1 + 1 / 16f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.additive()));


            }



//            if (value == 1) {
//                blockState = blockState.setValue(CrossingBlock.OPEN, false);
//            } else if (value == 0) {
//                blockState = blockState.setValue(CrossingBlock.OPEN, true);
//            }
//
//            Level level = be.getLevel();
//
//            level.setBlock(pos, blockState, 2);


        }
    }


    public static SuperByteBuffer standardKineticRotationTransform2(SuperByteBuffer buffer, KineticBlockEntity be,
                                                                   int light) {
        final BlockPos pos = be.getBlockPos();
        Axis axis = Direction.Axis.Y;
        return kineticRotationTransform(buffer, be, axis, getAngleForBe(be, pos, axis), light);
    }

//    @Override
//    protected SuperByteBuffer getRotatedModel(CrossingBlockEntity be, BlockState state) {
//        BlockState fakeState = AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(CreativeMotorBlock.FACING, Direction.DOWN);
//        return CachedBuffers.partialFacing(com.simibubi.create.AllPartialModels.SHAFT_HALF, fakeState);
//    }

//    @Override
//    protected SuperByteBuffer getRotatedModel(CrossingBlockEntity be, BlockState state) {
//        return CachedBuffers.partialFacing(com.simibubi.create.AllPartialModels.SHAFT_HALF, state, state
//                .getValue(CrossingBlock.HORIZONTAL_FACING)
//                .getOpposite());
//    }
}
