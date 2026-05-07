package com.tiestoettoet.create_train_parts.content.decoration.trainSlide;

import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainSlideBlockEntity extends SmartBlockEntity {
    LerpedFloat animation;
    int bridgeTicks;
    boolean deferUpdate;
    Map<String, BlockState> neighborStates = new HashMap<>();
    TrainSlideType trainSlideType;
    protected AssemblyException lastException;
    Object openObj = null;

    public TrainSlideBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        animation = LerpedFloat.linear()
                .startWithValue(isOpen(getBlockState()) ? 1 : 0);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        lastException = AssemblyException.read(tag, registries);
        super.read(tag, registries, clientPacket);
        invalidateRenderBoundingBox();

        if (tag.contains("ForceOpen"))
            openObj = tag.getBoolean("ForceOpen");
    }

    @Override
    public void tick() {
        if (deferUpdate && !level.isClientSide()) {
            deferUpdate = false;
            BlockState blockState = getBlockState();
            blockState.handleNeighborChanged(level, worldPosition, Blocks.AIR, worldPosition, false);
        }
        super.tick();
        BlockState block = getBlockState();
        TrainSlideBlock trainSlideBlock = (TrainSlideBlock) block.getBlock();
        boolean open = openObj instanceof Boolean ? (Boolean) openObj : isOpen(getBlockState());
        if (open != isOpen(getBlockState())) {
            trainSlideBlock.toggle(block, level, worldPosition, null, null, open);
        }
        boolean wasSettled = animation.settled();
        animation.chase(open ? 1 : 0, .15f, LerpedFloat.Chaser.LINEAR);
        animation.tickChaser();

        if (level.isClientSide()) {
            if (bridgeTicks < 2 && open)
                bridgeTicks++;
            else if (bridgeTicks > 0 && !open && isVisible(getBlockState()))
                bridgeTicks--;
            return;
        }

        if (!open && !wasSettled && animation.settled() && !isVisible(getBlockState()))
            showBlockModel();
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(1);
    }

    protected boolean isVisible(BlockState state) {

        return state.getOptionalValue(TrainSlideBlock.VISIBLE)
                .orElse(true);
    }

    public void setNeighborState(BlockState state) {
        if (level == null)
            return; // Ensure the level is not null

        Direction facing = state.getValue(TrainSlideBlock.FACING); // Get the block's facing direction
        BlockPos leftPos = worldPosition.relative(facing.getCounterClockWise()); // Calculate left neighbor position
        BlockPos rightPos = worldPosition.relative(facing.getClockWise()); // Calculate right neighbor position

        BlockState leftState = level.getBlockState(leftPos); // Get the left neighbor's state
        BlockState rightState = level.getBlockState(rightPos); // Get the right neighbor's state

        neighborStates.put("left", leftState); // Store the left state
        neighborStates.put("right", rightState); // Store the right state
    }

    public Map<String, BlockState> getNeighborStates() {
        return neighborStates; // Return the map of neighbor states
    }

    public void setTrainSlideType(TrainSlideType trainSlideType) {
        this.trainSlideType = trainSlideType;
    }

    public TrainSlideType getTrainSlideType() {
        return trainSlideType;
    }

    protected boolean shouldRenderSpecial(BlockState state) {
        return !isVisible(state) || bridgeTicks != 0;
    }

    protected void showBlockModel() {
        level.setBlock(worldPosition, getBlockState().setValue(TrainSlideBlock.VISIBLE, true), 3);
        level.playSound(null, worldPosition, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, .5f, 1);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public static boolean isOpen(BlockState state) {
        return state.getOptionalValue(TrainSlideBlock.OPEN)
                .orElse(false);
    }
}
