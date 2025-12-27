package com.tiestoettoet.create_train_parts.content.decoration.slidingWindow;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.tiestoettoet.create_train_parts.AllPartialModels;
import com.tiestoettoet.create_train_parts.CreateTrainParts;
import com.tiestoettoet.create_train_parts.content.decoration.SlidingWindowCTBehaviour;
import com.tiestoettoet.create_train_parts.content.decoration.trainSlide.TrainSlideBlock;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import java.util.ArrayList;
import java.util.List;

public class SlidingWindowRenderer  extends SafeBlockEntityRenderer<SlidingWindowBlockEntity> {
    public SlidingWindowRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(SlidingWindowBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        BlockState blockState = be.getBlockState();
        BlockState rightState = be.getLevel().getBlockState(be.getBlockPos().relative(blockState.getValue(SlidingWindowBlock.FACING).getClockWise()));
        BlockState leftState = be.getLevel().getBlockState(be.getBlockPos().relative(blockState.getValue(SlidingWindowBlock.FACING).getCounterClockWise()));
        BlockState upState = be.getLevel().getBlockState(be.getBlockPos().relative(Direction.UP));
        BlockState downState = be.getLevel().getBlockState(be.getBlockPos().relative(Direction.DOWN));
//        System.out.println("Partial Ticks: " + partialTicks);

        SlidingWindowBlockEntity.SelectionMode mode = be.getMode();
        SlidingWindowBlockEntity.SelectionMode rightMode = null;
        if (rightState.getBlock() instanceof SlidingWindowBlock)
            rightMode = rightState.getValue(SlidingWindowBlock.MODE);
        SlidingWindowBlockEntity.SelectionMode leftMode = null;
        if (leftState.getBlock() instanceof SlidingWindowBlock)
            leftMode = leftState.getValue(SlidingWindowBlock.MODE);
        SlidingWindowBlockEntity.SelectionMode upMode = null;
        if (upState.getBlock() instanceof SlidingWindowBlock)
            upMode = upState.getValue(SlidingWindowBlock.MODE);
        SlidingWindowBlockEntity.SelectionMode downMode = null;
        if (downState.getBlock() instanceof SlidingWindowBlock)
            downMode = downState.getValue(SlidingWindowBlock.MODE);
        if (!be.shouldRenderSpecial(blockState))
            return;

        Boolean right = !(rightState.getBlock() instanceof SlidingWindowBlock &&
                rightState.getValue(SlidingWindowBlock.FACING) == blockState.getValue(SlidingWindowBlock.FACING) &&
                mode == rightMode);
        Boolean left = !(leftState.getBlock() instanceof SlidingWindowBlock &&
                leftState.getValue(SlidingWindowBlock.FACING) == blockState.getValue(SlidingWindowBlock.FACING) &&
                mode == leftMode);
        Boolean up = !(upState.getBlock() instanceof SlidingWindowBlock &&
                upState.getValue(SlidingWindowBlock.FACING) == blockState.getValue(SlidingWindowBlock.FACING) &&
                mode == upMode);
        Boolean down = !(downState.getBlock() instanceof SlidingWindowBlock &&
                downState.getValue(SlidingWindowBlock.FACING) == blockState.getValue(SlidingWindowBlock.FACING) &&
                mode == downMode);

        BlockPos pos = be.getBlockPos();
        BlockAndTintGetter world = be.getLevel();


        Direction facing = blockState.getValue(TrainSlideBlock.FACING);

        float rotationAngle = switch (facing) {
            case NORTH -> 0; // No rotation needed
            case SOUTH -> 180;
            case WEST -> 90;
            case EAST -> -90;
            default -> 0;
        };

//        System.out.println("Movement mode:" + be.getMode());

//        SlidingWindowBlockEntity.SelectionMode mode = be.getMode();
        float value = be.animation.getValue(partialTicks);
        float exponentialValue = (float) value * value;

        VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

        if (blockState.getBlock() instanceof SlidingWindowBlock) {
            ResourceLocation blockTexture = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
            String blockTexturePath = blockTexture.getPath();
//            String blockName =



            CTSpriteShiftEntry spriteShift = null;
            String type = "";

            // use .equals() instead of == for string comparisons
            if (blockTexturePath.equals("glass_sliding_window")) {
                spriteShift = AllSpriteShifts.FRAMED_GLASS;
                type = "glass";
            } else if (blockTexturePath.contains("andesite_sliding_window")) {
                spriteShift = AllSpriteShifts.ANDESITE_CASING;
                type = "andesite";
            } else if (blockTexturePath.contains("brass_sliding_window")) {
                spriteShift = AllSpriteShifts.BRASS_CASING;
                type = "brass";
            } else if (blockTexturePath.contains("copper_sliding_window")) {
                spriteShift = AllSpriteShifts.COPPER_CASING;
                type = "copper";
            } else if (blockTexturePath.contains("train_sliding_window")) {
                spriteShift = AllSpriteShifts.RAILWAY_CASING;
                type = "train";
            } else {
                spriteShift = AllSpriteShifts.FRAMED_GLASS;
                type = "glass";
            }


            ConnectedTextureBehaviour behaviour = new SlidingWindowCTBehaviour(spriteShift);

            CTType dataType = behaviour.getDataType(world, pos, blockState, facing);

            float f = blockState.getValue(SlidingWindowBlock.OPEN) ? -1 : 1;

            float animFirstQuarter = Mth.clamp(exponentialValue / 0.25f, 0f, 1f);
            float animSecondQuarter = Mth.clamp((exponentialValue - 0.25f) / 0.25f, 0f, 1f);
            float animThirdQuarter = Mth.clamp((exponentialValue - 0.5f) / 0.25f, 0f, 1f);
            float animFourthQuarter = Mth.clamp((exponentialValue - 0.75f) / 0.25f, 0f, 1f);
            float movementMain = 2.9f / 16f * exponentialValue;
            float movementUp = 15f / 16f * exponentialValue; // Upward movement


            if (exponentialValue <= 0.25) {
                movementMain = (float) (0 / 16f + 2.9 / 16f * animFirstQuarter); // Main movement
                movementUp = (float) (0 / 16f + 3 / 16f * animFirstQuarter); // Upward movement
            } else if (exponentialValue > 0.25 && exponentialValue <= 0.5) {
                movementMain = (float) (2.9 / 16f + 0 / 16f * animSecondQuarter);
                movementUp = (float) (3 / 16f + 4 / 16f * animSecondQuarter);
            } else if (exponentialValue > 0.5 && exponentialValue <= 0.75) {
                movementMain = (float) (2.9 / 16f + 0 / 16f * animThirdQuarter);
                movementUp = (float) (7 / 16f + 4 / 16f * animThirdQuarter);
            } else {
                movementMain = (float) (2.9 / 16f + 0 / 16f * animFourthQuarter);
                movementUp = (float) (11 / 16f + 4 / 16f * animFourthQuarter);
            }


            Direction movementDirection = facing.getOpposite();

            Vec3 moveOffset;
            Vec3 upOffset;
            if (mode == SlidingWindowBlockEntity.SelectionMode.UP) {
                moveOffset = Vec3.atLowerCornerOf(movementDirection.getNormal()).scale(movementMain);
                upOffset = Vec3.atLowerCornerOf(Direction.UP.getNormal()).scale(movementUp);
            } else if (mode == SlidingWindowBlockEntity.SelectionMode.DOWN) {
                moveOffset = Vec3.atLowerCornerOf(movementDirection.getNormal()).scale(movementMain);
                upOffset = Vec3.atLowerCornerOf(Direction.DOWN.getNormal()).scale(movementUp);
            } else if (mode == SlidingWindowBlockEntity.SelectionMode.LEFT) {
                moveOffset = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movementMain).add(
                    Vec3.atLowerCornerOf(movementDirection.getCounterClockWise().getNormal()).scale(movementUp));
                upOffset = Vec3.ZERO;
            } else if (mode == SlidingWindowBlockEntity.SelectionMode.RIGHT) {
                moveOffset = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movementMain).add(
                    Vec3.atLowerCornerOf(movementDirection.getClockWise().getNormal()).scale(movementUp));
                upOffset = Vec3.ZERO;
            } else {
                moveOffset = Vec3.ZERO;
                upOffset = Vec3.ZERO;
            }

//            Vec3 moveOffset = Vec3.atLowerCornerOf(movementDirection.getNormal()).scale(movementMain);
//            Vec3 upOffset = moveOffset.atLowerCornerOf(Direction.UP.getNormal()).scale(movementUp);



            ResourceLocation resourceLocationMain = CreateTrainParts.asResource(
                    "sliding_windows/" + type + "_main");
//            ResourceLocation resourceLocationSide = CreateTrainParts.asResource(
//                "sliding_windows/" + type + suffix
////                ((rightState.getBlock() instanceof SlidingWindowBlock && rightState.getValue(SlidingWindowBlock.FACING) == facing && mode == rightMode) &&
////                 (leftState.getBlock() instanceof SlidingWindowBlock && leftState.getValue(SlidingWindowBlock.FACING) == facing && mode == leftMode) ? "_side_none" :
////                 (rightState.getBlock() instanceof SlidingWindowBlock && rightState.getValue(SlidingWindowBlock.FACING) == facing && mode == rightMode) ? "_side_right" :
////                 (leftState.getBlock() instanceof SlidingWindowBlock && leftState.getValue(SlidingWindowBlock.FACING) == facing && mode == leftMode) ? "_side_left" :
////                 ((upState.getBlock() instanceof SlidingWindowBlock && upState.getValue(SlidingWindowBlock.FACING) == facing && mode == upMode) &&
////                 (downState.getBlock() instanceof SlidingWindowBlock && downState.getValue(SlidingWindowBlock.FACING) == facing && mode == downMode) ? "_side_none_vertical" :
////                 (upState.getBlock() instanceof SlidingWindowBlock && upState.getValue(SlidingWindowBlock.FACING) == facing && mode == upMode) ? "_side_down" :
////                 (downState.getBlock() instanceof SlidingWindowBlock && downState.getValue(SlidingWindowBlock.FACING) == facing && mode == downMode) ? "_side_up" :
////                 "_side"))
//            );
            ResourceLocation resourceLocationUp = CreateTrainParts.asResource(
                    "sliding_windows/" + type + "_up");
            ResourceLocation resourceLocationRight = CreateTrainParts.asResource(
                    "sliding_windows/" + type + "_right");
            ResourceLocation resourceLocationDown = CreateTrainParts.asResource(
                    "sliding_windows/" + type + "_down");
            ResourceLocation resourceLocationLeft = CreateTrainParts.asResource(
                    "sliding_windows/" + type + "_left");
            ResourceLocation resourceLocationBack = CreateTrainParts.asResource(
                    "sliding_windows/" + type + "_back");

            PartialModel main = AllPartialModels.SLIDING_WINDOW.get(resourceLocationMain);
            PartialModel upModel = AllPartialModels.SLIDING_WINDOW_UP.get(resourceLocationUp);
            PartialModel rightModel = AllPartialModels.SLIDING_WINDOW_RIGHT.get(resourceLocationRight);
            PartialModel downModel = AllPartialModels.SLIDING_WINDOW_DOWN.get(resourceLocationDown);
            PartialModel leftModel = AllPartialModels.SLIDING_WINDOW_LEFT.get(resourceLocationLeft);
            PartialModel back = AllPartialModels.SLIDING_WINDOW_BACK.get(resourceLocationBack);
//            System.out.println("Main model: " + resourceLocationMain);

            // prevent crash if main is still null.
            if (main == null) return;
            SuperByteBuffer partial_main = CachedBuffers.partial(main, blockState);
            SuperByteBuffer partial_up = CachedBuffers.partial(upModel, blockState);
            SuperByteBuffer partial_right = CachedBuffers.partial(rightModel, blockState);
            SuperByteBuffer partial_down = CachedBuffers.partial(downModel, blockState);
            SuperByteBuffer partial_left = CachedBuffers.partial(leftModel, blockState);
            SuperByteBuffer partial_back = CachedBuffers.partial(back, blockState);

            List<SuperByteBuffer> partialSides = new ArrayList<>();

            if (up) {
                partialSides.add(partial_up);
            }
            if (right) {
                partialSides.add(partial_right);
            }
            if (down) {
                partialSides.add(partial_down);
            }
            if (left) {
                partialSides.add(partial_left);
            }

            for (SuperByteBuffer partial_side : partialSides) {
                partial_side.translate(moveOffset.x, upOffset.y, moveOffset.z)
                        .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                        .light(light)
                        .renderInto(ms, vb);
            }

            ConnectedTextureBehaviour.CTContext context = behaviour.buildContext(world, pos, blockState, facing.getOpposite(), dataType.getContextRequirement());

            int textureIndex = dataType.getTextureIndex(context);

            float row = Math.floorDiv(textureIndex, 8);
            float column = textureIndex % 8;
            float u = column / 8f;
            float v = row / 8f;
            partial_main.translate(moveOffset.x, upOffset.y, moveOffset.z)
                    .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                    .shiftUVtoSheet(spriteShift, u, v, 8)
                    .light(light)
                    .renderInto(ms, vb);

            context = behaviour.buildContext(world, pos, blockState, facing, dataType.getContextRequirement());

            textureIndex = dataType.getTextureIndex(context);

            row = Math.floorDiv(textureIndex, 8);
            column = textureIndex % 8;
            u = column / 8f;
            v = row / 8f;
            partial_back.translate(moveOffset.x, upOffset.y, moveOffset.z)
                    .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                    .shiftUVtoSheet(spriteShift, u, v, 8)
                    .light(light)
                    .renderInto(ms, vb);






        }


    }
}
