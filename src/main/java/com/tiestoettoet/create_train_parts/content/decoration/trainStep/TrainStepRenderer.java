package com.tiestoettoet.create_train_parts.content.decoration.trainStep;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.contraptions.ContraptionWorld;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import com.tiestoettoet.create_train_parts.AllPartialModels;
//import com.simibubi.create.foundation.render.
import com.tiestoettoet.create_train_parts.CreateTrainParts;
import com.tiestoettoet.create_train_parts.content.decoration.encasing.EncasedCTBehaviour;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.createmod.catnip.render.CachedBuffers;

public class TrainStepRenderer extends SafeBlockEntityRenderer<TrainStepBlockEntity> {

    public TrainStepRenderer() {
    }

    @Override
    protected void renderSafe(TrainStepBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {


        BlockState blockState = be.getBlockState();
        if (!be.shouldRenderSpecial(blockState))
            return;

        if(be.getLevel() instanceof VirtualRenderWorld) {
            return;
        }

        renderTrainStep(
                blockState,
                be.getBlockPos(),
                be.getLevel(),
                be.animation.getValue(partialTicks),
                ms,
                light,
                buffer,
                null
        );

        /*BlockPos pos = be.getBlockPos();
        BlockAndTintGetter world = be.getLevel();

        ConnectedTextureBehaviour behaviour = new EncasedCTBehaviour(AllSpriteShifts.ANDESITE_CASING);

        Direction facing = blockState.getValue(TrainStepBlock.FACING);
        CTType dataType = behaviour.getDataType(world, pos, blockState, facing);

        if (dataType == null) {
            // System.out.println("Data type is null");
            return;
        }
        // Direction face = Direction.UP;

        // int textureIndex = textureIndexV % 8;

        float rotationAngle = switch (facing) {
            case NORTH -> 0; // No rotation needed
            case SOUTH -> 180;
            case WEST -> 90;
            case EAST -> -90;
            default -> 0;
        };

        float value = be.animation.getValue(partialTicks);
        // System.out.println("TrainStepRenderer: Value: " + value);
        float exponentialValue = (float) value * value;
        float relativeValue = blockState.getValue(TrainStepBlock.OPEN) ? exponentialValue : 1 - exponentialValue;
        float relativeAnimationValue = relativeValue;

        VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

        if (blockState.getBlock() instanceof TrainStepBlock) {

            ResourceLocation blockTexture = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
            String blockTexturePath = blockTexture.getPath();
            // System.out.println("Block Texture Path: " + blockTexturePath);
            TrainStepBlock.ConnectedState connectedState = blockState.getValue(TrainStepBlock.CONNECTED);

            float f = blockState.getValue(TrainStepBlock.OPEN) ? -1 : 1;

            // partial_flap.translate(0, 10.5 / 16f, 0.5 / 16f);

            float movement = 5 / 16f * exponentialValue * f; // Clamp value to avoid unexpected results
            Direction movementDirection = blockState.getValue(TrainStepBlock.OPEN) ? facing.getOpposite() : facing;
            // System.out.println("Movement Direction: " + movementDirection);
            Vec3 moveOffset = Vec3.atLowerCornerOf(movementDirection.getNormal()).scale(movement);

            // Vec3 pivotOrigin = new Vec3(10.5 / 16f, 15.5 / 16f, 8 / 16f); // Define the
            // origin point from the model file

            float movementP = (float) (10.5 / 16f);
            Vec3 moveOffsetP = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movementP);

            float rotationPivot;

            if (blockState.getValue(TrainStepBlock.OPEN)) {
                rotationPivot = 90 * f * exponentialValue;
                // Open to closed: Translate and rotate correctly
                // partial_pivot.translate(0, 15.5 / 16f, 10.5 / 16f)
                // .rotateXDegrees(90 * value * f)
                // .light(light)
                // .renderInto(ms, vb);
                // System.out.println("Rotation: " + (90 * value * f));
            } else {
                rotationPivot = 90 * f * (1 - exponentialValue) - 90;
                // Closed to open: Reverse translation and rotation
                // partial_pivot.translate(0, 15.5 / 16f, 10.5 / 16f)
                // .rotateXDegrees(90 * (1 - value) * f - 90)
                // .light(light)
                // .renderInto(ms, vb);
                // System.out.println("Rotation: " + (90 * (1 - value) * f - 90));
            }

            float animFirstHalf = Mth.clamp(exponentialValue / 0.5f, 0f, 1f);
            float animSecondHalf = Mth.clamp((exponentialValue - 0.5f) / 0.5f, 0f, 1f);

            float rotation;
            Vec3 movementF;

            if (relativeValue <= 0.5) {
                relativeAnimationValue = (float) (relativeValue / 0.5);
                rotation = (float) 82.5 * f * relativeAnimationValue
                        - (blockState.getValue(TrainStepBlock.OPEN) ? 0 : 90);

                // float movement2 = (float) (0.5 + 1.25 / 16f * animationValue * f); // Clamp
                // value to avoid unexpected results
                // Vec3 moveOffset2 =
                // Vec3.atLowerCornerOf(movementDirection.getNormal()).scale(movement2);
                // partial_flap.translate(moveOffset2.x, movement2Up, moveOffset2.z)
                // .rotateXDegrees((float) ()
                // .light(light)
                // .renderInto(ms, vb);
                // System.out.println("Rotation: " + (82.5 * f * relativeAnimationValue -
                // (blockState.getValue(TrainStepBlock.OPEN) ? 0 : 90)));
            } else {
                // float movement3 = (float) (1.75 + 3.75 / 16f * animationValue * f); // Clamp
                // value to avoid unexpected results
                // Vec3 moveOffset3 =
                // Vec3.atLowerCornerOf(movementDirection.getNormal()).scale(movement3);
                float startRotation = (float) 82.5; // Starting rotation
                float endRotation = (float) 90; // Target rotation
                float interpolatedRotation = Mth.lerp(relativeAnimationValue, startRotation, endRotation); // Linear
                                                                                                           // interpolation
                rotation = interpolatedRotation * f - (blockState.getValue(TrainStepBlock.OPEN) ? 0 : 90);
                // partial_flap.translate(moveOffset3.x, movement3Up, moveOffset3.z)
                // .rotateXDegrees((float) (interpolatedRotation * f -
                // (blockState.getValue(TrainStepBlock.OPEN) ? 0 : 90)))
                // .light(light)
                // .renderInto(ms, vb);
                // System.out.println("Rotation: " + (interpolatedRotation * f -
                // (blockState.getValue(TrainStepBlock.OPEN) ? 0 : 90)));
            }

            if (exponentialValue <= 0.5) {
                float movement2Main = (float) (0.5 / 16f + 1.25 / 16f * animFirstHalf); // Main movement
                float movement2Up = (float) (10.5 / 16f - 3.5 / 16f * animFirstHalf); // Upward movement

                // Calculate the offset in the opposite of the movement direction and upward
                Vec3 moveOffset2 = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movement2Main);
                movementF = new Vec3(moveOffset2.x, movement2Up, moveOffset2.z);
            } else {
                float movement3Main = (float) (1.75 / 16f + 3.75 / 16f * animSecondHalf); // Secondary movement
                float movement3Up = (float) (7 / 16f - 1.5 / 16f * animSecondHalf); // Upward movement
                Vec3 moveOffset3 = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movement3Main);

                movementF = new Vec3(moveOffset3.x, movement3Up, moveOffset3.z);
            }
            // System.out.println("MovementF: " + movementF.x + ", " + movementF.y + ", " +
            // movementF.z);
            // System.out.println("Value: " + value);

            float animFirstQuarter = Mth.clamp(exponentialValue / 0.25f, 0f, 1f);
            float animSecondQuarter = Mth.clamp((exponentialValue - 0.25f) / 0.25f, 0f, 1f);
            float animThirdQuarter = Mth.clamp((exponentialValue - 0.5f) / 0.25f, 0f, 1f);
            float animFourthQuarter = Mth.clamp((exponentialValue - 0.75f) / 0.25f, 0f, 1f);

            float movementMMain;
            float movementMUp;
            Vec3 movementM;

            if (exponentialValue <= 0.25) {
                movementMMain = (float) (0 / 16f + 0.25 / 16f * animFirstQuarter); // Main movement
                movementMUp = (float) (0 / 16f - 2 / 16f * animFirstQuarter); // Upward movement
                Vec3 moveOffsetM = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movementMMain);
                movementM = new Vec3(moveOffsetM.x, movementMUp, moveOffsetM.z);
            } else if (exponentialValue > 0.25 && exponentialValue <= 0.5) {
                movementMMain = (float) (0.25 / 16f + 0.75 / 16f * animSecondQuarter);
                movementMUp = (float) (-2 / 16f - 1.5 / 16f * animSecondQuarter);
                Vec3 moveOffsetM = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movementMMain);
                movementM = new Vec3(moveOffsetM.x, movementMUp, moveOffsetM.z);
            } else if (exponentialValue > 0.5 && exponentialValue <= 0.75) {
                movementMMain = (float) (1 / 16f + 1.25 / 16f * animThirdQuarter);
                movementMUp = (float) (-3.5 / 16f - 1 / 16f * animThirdQuarter);
                Vec3 moveOffsetM = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movementMMain);
                movementM = new Vec3(moveOffsetM.x, movementMUp, moveOffsetM.z);
            } else {
                movementMMain = (float) (2.25 / 16f + 2.75 / 16f * animFourthQuarter);
                movementMUp = (float) (-4.5 / 16f - 0.5 / 16f * animFourthQuarter);
                Vec3 moveOffsetM = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movementMMain);
                movementM = new Vec3(moveOffsetM.x, movementMUp, moveOffsetM.z);
            }

            for (Direction face : Iterate.directions) {
                ConnectedTextureBehaviour.CTContext context = behaviour.buildContext(world, pos, blockState, face,
                        dataType.getContextRequirement());

                int textureIndex = dataType.getTextureIndex(context);
                if (facing == Direction.EAST && face != Direction.UP && face != Direction.DOWN)
                    face = face.getCounterClockWise();
                if (facing == Direction.SOUTH && face != Direction.UP && face != Direction.DOWN)
                    face = face.getOpposite();
                if (facing == Direction.WEST && face != Direction.UP && face != Direction.DOWN)
                    face = face.getClockWise();

                ResourceLocation resourceLocation = CreateTrainParts.asResource(
                        blockTexturePath + "/" + facing.getSerializedName() + "_" + connectedState.getSerializedName()
                                + "_" + face.getSerializedName());

                PartialModel move = AllPartialModels.TRAIN_STEP_MOVE.get(resourceLocation);
                PartialModel flap = AllPartialModels.TRAIN_STEP_FLAP.get(resourceLocation);
                PartialModel block = AllPartialModels.TRAIN_STEP.get(resourceLocation);

                PartialModel slide = AllPartialModels.TRAIN_STEP_SLIDE.get(resourceLocation);
                PartialModel pivot = AllPartialModels.TRAIN_STEP_PIVOT.get(resourceLocation);

                SuperByteBuffer partial_block = CachedBuffers.partial(block, blockState);
                CTSpriteShiftEntry spriteShift = null;
                // System.out.println("Block Texture Path: " + blockTexturePath);
                if (blockTexturePath.equals("train_step_andesite")) {
                    spriteShift = AllSpriteShifts.ANDESITE_CASING;
                    // System.out.println("Using andesite casing texture");
                } else if (blockTexturePath.equals("train_step_brass")) {
                    spriteShift = AllSpriteShifts.BRASS_CASING;
                    // System.out.println("Using brass casing texture");
                } else if (blockTexturePath.equals("train_step_copper")) {
                    spriteShift = AllSpriteShifts.COPPER_CASING;
                    // System.out.println("Using copper casing texture");
                }

                // ...existing code...
                else if (blockTexturePath.equals("train_step_train")) {
                    if (face == Direction.UP || face == Direction.DOWN) {
                        spriteShift = com.tiestoettoet.create_train_parts.AllSpriteShifts.TRAIN_STEP_TRAIN;
                    } else {
                        spriteShift = com.tiestoettoet.create_train_parts.AllSpriteShifts.TRAIN_STEP_SIDE;
                    }
                } else {
                    // System.out.println("Unknown block texture path: " + blockTexturePath);
                    return;
                }

                if (spriteShift == null) {
                    // System.out.println("Sprite shift is null, using fallback texture.");
                    return;
                }
                // System.out.println("TextureIndex: " + textureIndex + ", face: " + face +
                // ",facing" + facing
                // + ", connectedState: " + connectedState);

                SuperByteBuffer partial_slide = CachedBuffers.partial(slide, blockState);
                SuperByteBuffer partial_pivot = CachedBuffers.partial(pivot, blockState);
                SuperByteBuffer partial_move = CachedBuffers.partial(move, blockState);
                SuperByteBuffer partial_flap = CachedBuffers.partial(flap, blockState);
                float row = Math.floorDiv(textureIndex, 8);
                float column = textureIndex % 8;
                float u = (column) / 8f;
                float v = (row) / 8f;

                BlockPos blockPos = pos;
                int blockLight = getSafeLight(world, blockPos, light);

                partial_block
                        .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                        .shiftUVtoSheet(spriteShift, u, v, 8)
                        // .shiftUV(connectedShift)
                        .light(blockLight)
                        .renderInto(ms, vb);

                BlockPos flapPos = pos.offset((int) Math.round(movementF.x), (int) Math.round(movementF.y),
                        (int) Math.round(movementF.z));
                int flapLight = getSafeLight(world, flapPos, light);

                partial_flap.translate(movementF.x, movementF.y, movementF.z)
                        .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                        .shiftUVtoSheet(spriteShift, u, v, 8)
                        .rotateXDegrees(rotation)
                        .light(flapLight)
                        .renderInto(ms, vb);

                BlockPos movePos = pos.offset((int) Math.round(movementM.x), (int) Math.round(movementM.y),
                        (int) Math.round(movementM.z));
                int moveLight = getSafeLight(world, movePos, light);

                partial_move.translate(movementM.x, movementM.y, movementM.z)
                        .shiftUVtoSheet(spriteShift, u, v, 8)
                        .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                        .light(moveLight)
                        .renderInto(ms, vb);

                // if (facing == Direction.EAST || facing == Direction.WEST)
                // if (face == Direction.UP) {
                // context = behaviour.buildContext(world, pos, blockState, facing,
                // dataType.getContextRequirement());
                //
                // textureIndex = dataType.getTextureIndex(context);
                // row = Math.floorDiv(textureIndex, 8);
                // column = textureIndex % 8;
                // u = (column) / 8f;
                // v = (row) / 8f;
                // }
                TrainStepBlockEntity.SlideMode mode = be.getMode();
                if (mode == TrainStepBlockEntity.SlideMode.NO_SLIDE)
                    moveOffset = Vec3.ZERO;

                BlockPos slidePos = pos.offset((int) Math.round(moveOffset.x), (int) Math.round(moveOffset.y),
                        (int) Math.round(moveOffset.z));
                int slideLight = getSafeLight(world, slidePos, light);

                partial_slide.translate(moveOffset.x, moveOffset.y, moveOffset.z)
                        .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                        .shiftUVtoSheet(spriteShift, u, v, 8)
                        .light(slideLight)
                        .renderInto(ms, vb);
                // if (facing == Direction.EAST || facing == Direction.WEST)
                // if (face.getOpposite() == facing) {
                // context = behaviour.buildContext(world, pos, blockState, facing,
                // dataType.getContextRequirement());
                //
                // textureIndex = dataType.getTextureIndex(context);
                // row = Math.floorDiv(textureIndex, 8);
                // column = textureIndex % 8;
                // u = (column) / 8f;
                // v = (row) / 8f;
                // }
                BlockPos pivotPos = pos.offset((int) Math.round(moveOffsetP.x), (int) Math.round(15.5 / 16f),
                        (int) Math.round(moveOffsetP.z));
                int pivotLight = getSafeLight(world, pivotPos, light);

                partial_pivot.translate(moveOffsetP.x, 15.5 / 16f, moveOffsetP.z)
                        .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                        .shiftUVtoSheet(spriteShift, u, v, 8)
                        .rotateXDegrees(rotationPivot)
                        .light(pivotLight)
                        .renderInto(ms, vb);

            }
            // check if movementDirection is going north or south

        }

         */
    }

    static void renderTrainStep(
            BlockState state,
            BlockPos pos,
            Level world,
            float animValue,
            PoseStack ms,
            int light,
            MultiBufferSource buffer,
            ContraptionMatrices matrices
    ) {


        ConnectedTextureBehaviour behaviour = new EncasedCTBehaviour(AllSpriteShifts.ANDESITE_CASING);

        Direction facing = state.getValue(TrainStepBlock.FACING);
        CTType dataType = behaviour.getDataType(world, pos, state, facing);

        if (dataType == null) {
            return;
        }
        float rotationAngle = switch (facing) {
            case NORTH -> 0; // No rotation needed
            case SOUTH -> 180;
            case WEST -> 90;
            case EAST -> -90;
            default -> 0;
        };

        float exponentialValue = animValue * animValue;
        float relativeValue = state.getValue(TrainStepBlock.OPEN) ? exponentialValue : 1 - exponentialValue;
        float relativeAnimationValue = relativeValue;
        VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());
        if (state.getBlock() instanceof TrainStepBlock) {
            ResourceLocation blockTexture = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            String blockTexturePath = blockTexture.getPath();
            TrainStepBlock.ConnectedState connectedState = state.getValue(TrainStepBlock.CONNECTED);

            float f = state.getValue(TrainStepBlock.OPEN) ? -1 : 1;
            float movement = 5 / 16f * exponentialValue * f; // Clamp value to avoid unexpected results
            Direction movementDirection = state.getValue(TrainStepBlock.OPEN) ? facing.getOpposite() : facing;
            Vec3 moveOffset = Vec3.atLowerCornerOf(movementDirection.getNormal()).scale(movement);

            float movementP = (float) (10.5 / 16f);
            Vec3 moveOffsetP = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movementP);

            float rotationPivot;

            if (state.getValue(TrainStepBlock.OPEN)) {
                rotationPivot = 90 * f * exponentialValue;
            } else {
                rotationPivot = 90 * f * (1 - exponentialValue) - 90;
            }

            float animFirstHalf = Mth.clamp(exponentialValue / 0.5f, 0f, 1f);
            float animSecondHalf = Mth.clamp((exponentialValue - 0.5f) / 0.5f, 0f, 1f);

            float rotation;
            Vec3 movementF;

            if (relativeValue <= 0.5) {
                relativeAnimationValue = (float) (relativeValue / 0.5);
                rotation = (float) 82.5 * f * relativeAnimationValue
                        - (state.getValue(TrainStepBlock.OPEN) ? 0 : 90);

            } else {
                float startRotation = (float) 82.5; // Starting rotation
                float endRotation = (float) 90; // Target rotation
                float interpolatedRotation = Mth.lerp(relativeAnimationValue, startRotation, endRotation); // Linear
                rotation = interpolatedRotation * f - (state.getValue(TrainStepBlock.OPEN) ? 0 : 90);
            }

            if (exponentialValue <= 0.5) {
                float movement2Main = (float) (0.5 / 16f + 1.25 / 16f * animFirstHalf); // Main movement
                float movement2Up = (float) (10.5 / 16f - 3.5 / 16f * animFirstHalf); // Upward movement

                // Calculate the offset in the opposite of the movement direction and upward
                Vec3 moveOffset2 = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movement2Main);
                movementF = new Vec3(moveOffset2.x, movement2Up, moveOffset2.z);
            } else {
                float movement3Main = (float) (1.75 / 16f + 3.75 / 16f * animSecondHalf); // Secondary movement
                float movement3Up = (float) (7 / 16f - 1.5 / 16f * animSecondHalf); // Upward movement
                Vec3 moveOffset3 = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movement3Main);

                movementF = new Vec3(moveOffset3.x, movement3Up, moveOffset3.z);
            }

            float animFirstQuarter = Mth.clamp(exponentialValue / 0.25f, 0f, 1f);
            float animSecondQuarter = Mth.clamp((exponentialValue - 0.25f) / 0.25f, 0f, 1f);
            float animThirdQuarter = Mth.clamp((exponentialValue - 0.5f) / 0.25f, 0f, 1f);
            float animFourthQuarter = Mth.clamp((exponentialValue - 0.75f) / 0.25f, 0f, 1f);

            float movementMMain;
            float movementMUp;
            Vec3 movementM;

            if (exponentialValue <= 0.25) {
                movementMMain = (float) (0 / 16f + 0.25 / 16f * animFirstQuarter); // Main movement
                movementMUp = (float) (0 / 16f - 2 / 16f * animFirstQuarter); // Upward movement
                Vec3 moveOffsetM = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movementMMain);
                movementM = new Vec3(moveOffsetM.x, movementMUp, moveOffsetM.z);
            } else if (exponentialValue > 0.25 && exponentialValue <= 0.5) {
                movementMMain = (float) (0.25 / 16f + 0.75 / 16f * animSecondQuarter);
                movementMUp = (float) (-2 / 16f - 1.5 / 16f * animSecondQuarter);
                Vec3 moveOffsetM = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movementMMain);
                movementM = new Vec3(moveOffsetM.x, movementMUp, moveOffsetM.z);
            } else if (exponentialValue > 0.5 && exponentialValue <= 0.75) {
                movementMMain = (float) (1 / 16f + 1.25 / 16f * animThirdQuarter);
                movementMUp = (float) (-3.5 / 16f - 1 / 16f * animThirdQuarter);
                Vec3 moveOffsetM = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movementMMain);
                movementM = new Vec3(moveOffsetM.x, movementMUp, moveOffsetM.z);
            } else {
                movementMMain = (float) (2.25 / 16f + 2.75 / 16f * animFourthQuarter);
                movementMUp = (float) (-4.5 / 16f - 0.5 / 16f * animFourthQuarter);
                Vec3 moveOffsetM = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movementMMain);
                movementM = new Vec3(moveOffsetM.x, movementMUp, moveOffsetM.z);
            }

            for (Direction face : Iterate.directions) {
                ConnectedTextureBehaviour.CTContext context = behaviour.buildContext(world, pos, state, face,
                        dataType.getContextRequirement());

                int textureIndex = dataType.getTextureIndex(context);
                if (facing == Direction.EAST && face != Direction.UP && face != Direction.DOWN)
                    face = face.getCounterClockWise();
                if (facing == Direction.SOUTH && face != Direction.UP && face != Direction.DOWN)
                    face = face.getOpposite();
                if (facing == Direction.WEST && face != Direction.UP && face != Direction.DOWN)
                    face = face.getClockWise();

                ResourceLocation resourceLocation = CreateTrainParts.asResource(
                        blockTexturePath + "/" + facing.getSerializedName() + "_" + connectedState.getSerializedName()
                                + "_" + face.getSerializedName());

                PartialModel move = AllPartialModels.TRAIN_STEP_MOVE.get(resourceLocation);
                PartialModel flap = AllPartialModels.TRAIN_STEP_FLAP.get(resourceLocation);
                PartialModel block = AllPartialModels.TRAIN_STEP.get(resourceLocation);

                PartialModel slide = AllPartialModels.TRAIN_STEP_SLIDE.get(resourceLocation);
                PartialModel pivot = AllPartialModels.TRAIN_STEP_PIVOT.get(resourceLocation);

                SuperByteBuffer partial_block = CachedBuffers.partial(block, state);
                CTSpriteShiftEntry spriteShift = null;
                if (blockTexturePath.equals("train_step_andesite")) {
                    spriteShift = AllSpriteShifts.ANDESITE_CASING;
                } else if (blockTexturePath.equals("train_step_brass")) {
                    spriteShift = AllSpriteShifts.BRASS_CASING;
                } else if (blockTexturePath.equals("train_step_copper")) {
                    spriteShift = AllSpriteShifts.COPPER_CASING;
                } else if (blockTexturePath.equals("train_step_train")) {
                    if (face == Direction.UP || face == Direction.DOWN) {
                        spriteShift = com.tiestoettoet.create_train_parts.AllSpriteShifts.TRAIN_STEP_TRAIN;
                    } else {
                        spriteShift = com.tiestoettoet.create_train_parts.AllSpriteShifts.TRAIN_STEP_SIDE;
                    }
                } else {
                    return;
                }

                if (spriteShift == null) {
                    return;
                }

                SuperByteBuffer partial_slide = CachedBuffers.partial(slide, state);
                SuperByteBuffer partial_pivot = CachedBuffers.partial(pivot, state);
                SuperByteBuffer partial_move = CachedBuffers.partial(move, state);
                SuperByteBuffer partial_flap = CachedBuffers.partial(flap, state);
                float row = Math.floorDiv(textureIndex, 8);
                float column = textureIndex % 8;
                float u = (column) / 8f;
                float v = (row) / 8f;

                BlockPos blockPos = pos;
                int blockLight = getSafeLight(world, blockPos, light);

                if (world instanceof ContraptionWorld) {
                    partial_block
                            .transform(ms)
                            .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                            .shiftUVtoSheet(spriteShift, u, v, 8)
                            .light(blockLight)
                            .useLevelLight(world, matrices.getWorld())
                            .renderInto(matrices.getViewProjection(), vb);
                } else {
                    partial_block
                            .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                            .shiftUVtoSheet(spriteShift, u, v, 8)
                            .light(blockLight)
                            .renderInto(ms, vb);
                }

                BlockPos flapPos = pos.offset((int) Math.round(movementF.x), (int) Math.round(movementF.y),
                        (int) Math.round(movementF.z));
                int flapLight = getSafeLight(world, flapPos, light);

                if (world instanceof ContraptionWorld) {
                    partial_flap
                            .transform(ms)
                            .translate(movementF.x, movementF.y, movementF.z)
                            .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                            .shiftUVtoSheet(spriteShift, u, v, 8)
                            .rotateXDegrees(rotation)
                            .light(flapLight)
                            .useLevelLight(world, matrices.getWorld())
                            .renderInto(matrices.getViewProjection(), vb);
                } else {
                    partial_flap.translate(movementF.x, movementF.y, movementF.z)
                            .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                            .shiftUVtoSheet(spriteShift, u, v, 8)
                            .rotateXDegrees(rotation)
                            .light(flapLight)
                            .renderInto(ms, vb);
                }

                BlockPos movePos = pos.offset((int) Math.round(movementM.x), (int) Math.round(movementM.y),
                        (int) Math.round(movementM.z));
                int moveLight = getSafeLight(world, movePos, light);

                if (world instanceof ContraptionWorld) {
                    partial_move
                            .transform(ms)
                            .translate(movementM.x, movementM.y, movementM.z)
                            .shiftUVtoSheet(spriteShift, u, v, 8)
                            .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                            .light(moveLight)
                            .useLevelLight(world, matrices.getWorld())
                            .renderInto(matrices.getViewProjection(), vb);
                } else {
                    partial_move.translate(movementM.x, movementM.y, movementM.z)
                            .shiftUVtoSheet(spriteShift, u, v, 8)
                            .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                            .light(moveLight)
                            .renderInto(ms, vb);
                }

                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof TrainStepBlockEntity ts) {
                    TrainStepBlockEntity.SlideMode mode = ts.getMode();
                    if (mode == TrainStepBlockEntity.SlideMode.NO_SLIDE)
                        moveOffset = Vec3.ZERO;
                }

                BlockPos slidePos = pos.offset((int) Math.round(moveOffset.x), (int) Math.round(moveOffset.y),
                        (int) Math.round(moveOffset.z));
                int slideLight = getSafeLight(world, slidePos, light);

                if (world instanceof ContraptionWorld) {
                    partial_slide
                            .transform(ms)
                            .translate(moveOffset.x, moveOffset.y, moveOffset.z)
                            .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                            .shiftUVtoSheet(spriteShift, u, v, 8)
                            .light(slideLight)
                            .useLevelLight(world, matrices.getWorld())
                            .renderInto(matrices.getViewProjection(), vb);
                } else {
                    partial_slide.translate(moveOffset.x, moveOffset.y, moveOffset.z)
                            .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                            .shiftUVtoSheet(spriteShift, u, v, 8)
                            .light(slideLight)
                            .renderInto(ms, vb);
                }

                BlockPos pivotPos = pos.offset((int) Math.round(moveOffsetP.x), (int) Math.round(15.5 / 16f),
                        (int) Math.round(moveOffsetP.z));
                int pivotLight = getSafeLight(world, pivotPos, light);

                if (world instanceof ContraptionWorld) {
                    partial_pivot
                            .transform(ms)
                            .translate(moveOffsetP.x, 15.5 / 16f, moveOffsetP.z)
                            .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                            .shiftUVtoSheet(spriteShift, u, v, 8)
                            .rotateXDegrees(rotationPivot)
                            .light(pivotLight)
                            .useLevelLight(world, matrices.getWorld())
                            .renderInto(matrices.getViewProjection(), vb);
                } else {
                    partial_pivot.translate(moveOffsetP.x, 15.5 / 16f, moveOffsetP.z)
                            .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                            .shiftUVtoSheet(spriteShift, u, v, 8)
                            .rotateXDegrees(rotationPivot)
                            .light(pivotLight)
                            .renderInto(ms, vb);
                }

            }

        }
    }

    static int getSafeLight(BlockAndTintGetter world, BlockPos targetPos, int fallbackLight) {
        // Check if the position is valid (within world bounds)
        if (targetPos.getY() < world.getMinBuildHeight() || targetPos.getY() > world.getMaxBuildHeight()) {
            return fallbackLight;
        }

        int calculatedLight = LevelRenderer.getLightColor(world, targetPos);

        // If the calculated light is 0 (completely dark), use fallback
        if (calculatedLight == 0) {
            // Try the position above to see if that has better lighting
            BlockPos abovePos = targetPos.above();
            int aboveLight = LevelRenderer.getLightColor(world, abovePos);
            if (aboveLight > 0) {
                return aboveLight;
            }

            // If still 0, return the original fallback light
            return fallbackLight;
        }

        return calculatedLight;
    }
}
