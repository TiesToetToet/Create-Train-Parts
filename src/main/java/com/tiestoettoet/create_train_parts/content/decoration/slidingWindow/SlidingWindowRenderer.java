package com.tiestoettoet.create_train_parts.content.decoration.slidingWindow;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.contraptions.ContraptionWorld;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import com.tiestoettoet.create_train_parts.AllPartialModels;
import com.tiestoettoet.create_train_parts.CreateTrainParts;
import com.tiestoettoet.create_train_parts.content.decoration.SlidingWindowCTBehaviour;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SlidingWindowRenderer extends SafeBlockEntityRenderer<SlidingWindowBlockEntity> {
    public SlidingWindowRenderer() {
    }

    @Override
    protected void renderSafe(
            SlidingWindowBlockEntity be,
            float partialTicks,
            PoseStack ms,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        if (!be.shouldRenderSpecial(be.getBlockState()))
            return;

        Level world = be.getLevel();
        if (world instanceof VirtualRenderWorld)
            return;


        float animValue = be.animation.getValue(partialTicks);
        BlockState blockState = be.getBlockState();
        BlockPos blockPos = be.getBlockPos();
        renderSlidingWindow(
                blockState,
                blockPos,
                world,
                world,
                animValue,
                buffer,
                ms,
                light,
                null
        );
    }


    public static void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
                                           ContraptionMatrices matrices, MultiBufferSource buffer) {

        if (!(context.temporaryData instanceof SlidingWindowMovementBehaviour.SlidingWindowAnimationData swad))
            return;

        float animVal = swad.animation.getValue(AnimationTickHolder.getPartialTicks(context.world));
        int light = LevelRenderer.getLightColor(renderWorld, context.localPos);
        PoseStack ms = matrices.getModel();
        renderSlidingWindow(
                context.state,
                context.localPos,
                context.contraption.getContraptionWorld(),
                renderWorld,
                animVal,
                buffer,
                ms,
                light,
                matrices
        );
    }

    enum Side {
        LEFT("left"),
        RIGHT("right"),
        UP("up"),
        DOWN("down");

        private final String name;

        Side(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    enum SlidingWindowTextureType {
        GLASS("glass_sliding_window", () -> AllSpriteShifts.FRAMED_GLASS, "glass"),
        ANDESITE("andesite_sliding_window", () -> AllSpriteShifts.ANDESITE_CASING, "andesite"),
        BRASS("brass_sliding_window", () -> AllSpriteShifts.BRASS_CASING, "brass"),
        COPPER("copper_sliding_window", () -> AllSpriteShifts.COPPER_CASING, "copper"),
        TRAIN("train_sliding_window", () -> AllSpriteShifts.RAILWAY_CASING, "train");

        private final String textureKey;
        private final Supplier<CTSpriteShiftEntry> spriteShiftEntrySupplier;
        private final String type;

        private CTSpriteShiftEntry spriteShift;

        SlidingWindowTextureType(String textureKey, Supplier<CTSpriteShiftEntry> spriteShiftSupplier, String type) {
            this.textureKey = textureKey;
            this.spriteShiftEntrySupplier = spriteShiftSupplier;
            this.type = type;
        }

        public CTSpriteShiftEntry getSpriteShift() {
            if (spriteShift == null) {
                spriteShift = spriteShiftEntrySupplier.get();
            }
            return spriteShift;
        }

        public String getType() {
            return this.type;
        }

        public static SlidingWindowTextureType fromBlockTexturePath(String path) {
            for (SlidingWindowTextureType slidingWindowTextureType : SlidingWindowTextureType.values()) {
                if (path.contains(slidingWindowTextureType.textureKey))
                    return slidingWindowTextureType;
            }

            return GLASS;
        }
    }
    public record SlidingMotion(Vec3 moveOffset, Vec3 upOffset) {}
    private static SlidingMotion computeSlidingMotion(
            SlidingWindowBlockEntity.SelectionMode mode,
            Direction facing,
            float exponentialValue
    ) {
        float animFirstQuarter = Mth.clamp(exponentialValue / 0.25f, 0f, 1f);
        float animSecondQuarter = Mth.clamp((exponentialValue - 0.25f) / 0.25f, 0f, 1f);
        float animThirdQuarter = Mth.clamp((exponentialValue - 0.5f) / 0.25f, 0f, 1f);
        float animFourthQuarter = Mth.clamp((exponentialValue - 0.75f) / 0.25f, 0f, 1f);

        float movementMain, movementUp;
        if (exponentialValue <= 0.25) {
            movementMain = 2.9f / 16f * animFirstQuarter;
            movementUp = 3f / 16f * animFirstQuarter;
        } else if (exponentialValue <= 0.5) {
            movementMain = 2.9f / 16f;
            movementUp = (3f + 4f * animSecondQuarter) / 16f;
        } else if (exponentialValue <= 0.75) {
            movementMain = 2.9f / 16f;
            movementUp = (7f + 4f * animThirdQuarter) / 16f;
        } else {
            movementMain = 2.9f / 16f;
            movementUp = (11f + 4f * animFourthQuarter) / 16f;
        }

        Direction movementDirection = facing.getOpposite();

        Vec3 moveOffset, upOffset;
        switch (mode) {
            case UP -> {
                moveOffset = Vec3.atLowerCornerOf(movementDirection.getNormal()).scale(movementMain);
                upOffset = Vec3.atLowerCornerOf(Direction.UP.getNormal()).scale(movementUp);
            }
            case DOWN -> {
                moveOffset = Vec3.atLowerCornerOf(movementDirection.getNormal()).scale(movementMain);
                upOffset = Vec3.atLowerCornerOf(Direction.DOWN.getNormal()).scale(movementUp);
            }
            case LEFT -> {
                moveOffset = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movementMain)
                        .add(Vec3.atLowerCornerOf(movementDirection.getCounterClockWise().getNormal()).scale(movementUp));
                upOffset = Vec3.ZERO;
            }
            case RIGHT -> {
                moveOffset = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(movementMain)
                        .add(Vec3.atLowerCornerOf(movementDirection.getClockWise().getNormal()).scale(movementUp));
                upOffset = Vec3.ZERO;
            }
            default -> {
                moveOffset = Vec3.ZERO;
                upOffset = Vec3.ZERO;
            }
        }

        return new SlidingMotion(moveOffset, upOffset);
    }

    private static Vec2 getCTUV(CTType dataType, ConnectedTextureBehaviour.CTContext context) {
        int textureIndex = dataType.getTextureIndex(context);
        float row = Math.floorDiv(textureIndex, 8);
        float column = textureIndex % 8;
        return new Vec2(column / 8f, row / 8f);
    }


    public static void renderSlidingWindow(
            BlockState state,
            BlockPos pos,
            Level world,
            Level renderWorld,
            float animValue,
            MultiBufferSource buffer,
            PoseStack ms,
            int light,
            ContraptionMatrices matrices
    ) {
        if (!(state.getBlock() instanceof SlidingWindowBlock))
            return;

        Map<Side, BlockState> blockStates = new HashMap<>();
        blockStates.put(Side.RIGHT, world.getBlockState(pos.relative(state.getValue(SlidingWindowBlock.FACING).getClockWise())));
        blockStates.put(Side.LEFT, world.getBlockState(pos.relative(state.getValue(SlidingWindowBlock.FACING).getCounterClockWise())));
        blockStates.put(Side.UP, world.getBlockState(pos.relative(Direction.UP)));
        blockStates.put(Side.DOWN, world.getBlockState(pos.relative(Direction.DOWN)));

        SlidingWindowBlockEntity.SelectionMode mode = state.getValue(SlidingWindowBlock.MODE);
        Map<Side, Boolean> sidesActive = new HashMap<>();

        for (Side side : Side.values()) {
            sidesActive.put(
                    side,
                    !(
                            blockStates.get(side).getBlock() instanceof SlidingWindowBlock
                                    && blockStates.get(side).getValue(SlidingWindowBlock.FACING) == state.getValue(SlidingWindowBlock.FACING)
                                    && mode == blockStates.get(side).getValue(SlidingWindowBlock.MODE)
                    )
            );
        }

        Direction facing = state.getValue(SlidingWindowBlock.FACING);
        float rotationAngle = switch (facing) {
            case SOUTH -> 180;
            case WEST -> 90;
            case EAST -> -90;
            default -> 0;
        };

        float exponentialValue = animValue * animValue;
        VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());
        ResourceLocation blockTexture = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        SlidingWindowTextureType textureType = SlidingWindowTextureType.fromBlockTexturePath(blockTexture.getPath());

        ConnectedTextureBehaviour ctBehaviour = new SlidingWindowCTBehaviour(textureType.getSpriteShift());
        CTType dataType = ctBehaviour.getDataType(renderWorld, pos, state, facing);


        SlidingMotion motion = computeSlidingMotion(mode, facing, exponentialValue);
        Vec3 moveOffset = motion.moveOffset();
        Vec3 upOffset = motion.upOffset();

        ResourceLocation resLocationMain = CreateTrainParts.asResource("sliding_windows/" + textureType.getType() + "_main");
        PartialModel modelMain = AllPartialModels.SLIDING_WINDOW.get(resLocationMain);
        SuperByteBuffer partialMain = CachedBuffers.partial(modelMain, state);

        ResourceLocation resLocationBack = CreateTrainParts.asResource("sliding_windows/" + textureType.getType() + "_back");
        PartialModel modelBack = AllPartialModels.SLIDING_WINDOW_BACK.get(resLocationBack);
        SuperByteBuffer particalBack = CachedBuffers.partial(modelBack, state);

        Map<Side, ResourceLocation> resLocations = new HashMap<>();
        for (Side side : Side.values()) {
            resLocations.put(side, CreateTrainParts.asResource("sliding_windows/" + textureType.getType() + "_" + side.toString()));
        }
        Map<Side, PartialModel> sideModels = new HashMap<>();
        sideModels.put(Side.UP, AllPartialModels.SLIDING_WINDOW_UP.get(resLocations.get(Side.UP)));
        sideModels.put(Side.RIGHT, AllPartialModels.SLIDING_WINDOW_RIGHT.get(resLocations.get(Side.RIGHT)));
        sideModels.put(Side.DOWN, AllPartialModels.SLIDING_WINDOW_DOWN.get(resLocations.get(Side.DOWN)));
        sideModels.put(Side.LEFT, AllPartialModels.SLIDING_WINDOW_LEFT.get(resLocations.get(Side.LEFT)));

        List<SuperByteBuffer> partialSides = new ArrayList<>();

        for (Side side : Side.values()) {
            if (sidesActive.get(side) != null && sidesActive.get(side) == true)
                partialSides.add(CachedBuffers.partial(sideModels.get(side), state));
        }


        for (SuperByteBuffer partialSide : partialSides) {
            if (world instanceof ContraptionWorld) {
                partialSide
                        .transform(ms)
                        .translate(moveOffset.x, upOffset.y, moveOffset.z)
                        .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                        .light(light)
                        .useLevelLight(world, matrices.getWorld())
                        .renderInto(matrices.getViewProjection(), vb);
            } else {
                partialSide
                        .translate(moveOffset.x, upOffset.y, moveOffset.z)
                        .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                        .light(light)
                        .renderInto(ms, vb);
            }
        }

        assert dataType != null;
        ConnectedTextureBehaviour.CTContext ctContext = ctBehaviour.buildContext(
                renderWorld,
                pos,
                state,
                facing.getOpposite(),
                dataType.getContextRequirement()
        );


        Vec2 uv = getCTUV(dataType, ctContext);
        if (world instanceof ContraptionWorld) {
            partialMain
                    .transform(ms)
                    .translate(moveOffset.x, upOffset.y, moveOffset.z)
                    .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                    .shiftUVtoSheet(textureType.getSpriteShift(), uv.x, uv.y, 8)
                    .light(light)
                    .useLevelLight(world, matrices.getWorld())
                    .renderInto(matrices.getViewProjection(), vb);
        } else {
            partialMain
                    .translate(moveOffset.x, upOffset.y, moveOffset.z)
                    .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                    .shiftUVtoSheet(textureType.getSpriteShift(), uv.x, uv.y, 8)
                    .light(light)
                    .renderInto(ms, vb);
        }


        ctContext = ctBehaviour.buildContext(renderWorld, pos, state, facing, dataType.getContextRequirement());


        uv = getCTUV(dataType,ctContext);
        if (world instanceof ContraptionWorld) {
            particalBack
                    .transform(ms)
                    .translate(moveOffset.x, upOffset.y, moveOffset.z)
                    .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                    .shiftUVtoSheet(textureType.getSpriteShift(), uv.x, uv.y, 8)
                    .light(light)
                    .useLevelLight(world, matrices.getWorld())
                    .renderInto(matrices.getViewProjection(), vb);
        } else {
            particalBack
                    .translate(moveOffset.x, upOffset.y, moveOffset.z)
                    .rotateCentered(Mth.DEG_TO_RAD * rotationAngle, Direction.Axis.Y)
                    .shiftUVtoSheet(textureType.getSpriteShift(), uv.x, uv.y, 8)
                    .light(light)
                    .renderInto(ms, vb);
        }


    }




}
