package com.tiestoettoet.create_train_parts.content.trains.crossing;

import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

import static com.tiestoettoet.create_train_parts.content.trains.crossing.ArmExtenderBlock.OPEN;

public class ArmExtenderBlockEntity extends SmartBlockEntity {
    LerpedFloat animation;

    int bridgeTicks;
    int armExtenderInRow;
    boolean deferUpdate;
    protected AssemblyException lastException;

    public ArmExtenderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        animation = LerpedFloat.linear()
                .startWithValue(isOpen(getBlockState()) ? 1 : 0);
    }

    @Override
    public void tick() {
        super.tick();

        // Update the animation state
        animation.tickChaser();

        // Optionally, update the block state if needed
        boolean openAnimation = (animation.getValue() != 0);
        BlockState block = getBlockState();
        if (block.getValue(OPEN) != openAnimation) {
            block = block.setValue(OPEN, openAnimation);
            level.setBlock(worldPosition, block, 10);
        }

        calculateArmExtenderInRow();

    }

    public void chaseAnimation(float target, float speed) {
        animation.chase(target, speed, LerpedFloat.Chaser.LINEAR);
    }

    protected boolean shouldRenderSpecial(BlockState state) {
        return true;
    }

    private void calculateArmExtenderInRow() {
        int count = 0;
        Level level = getLevel();
        BlockState state = getBlockState();
        BlockPos pos = getBlockPos();
        Direction facing = state.getValue(ArmExtenderBlock.FACING);
        BlockPos currentPos = pos;
        BlockState currentState = level.getBlockState(currentPos);
        boolean flipped = state.getValue(ArmExtenderBlock.FLIPPED);

        while (currentState.getBlock() instanceof ArmExtenderBlock) {
            count++;
            currentPos = flipped ? currentPos.relative(facing.getClockWise())
                    : currentPos.relative(facing.getCounterClockWise());
//            System.out.println("Current Position: " + currentPos + ", Current State: " + currentState);
            currentState = level.getBlockState(currentPos);
        }

        armExtenderInRow = count;
    }

    public int getArmExtenderInRow() {
        return armExtenderInRow;
    }

//    @Override
//    public void tick() {
//        super.tick();
//        BlockState block = getBlockState();
//        CrossingBlock crossingBlock = (CrossingBlock) block.getBlock();
//        boolean open = isOpen(getBlockState());
////        if (open != isOpen(getBlockState())) {
////            block = block.setValue(OPEN, !open);
////            level.setBlock(worldPosition, block, 10);
////            level.gameEvent(null, block.getValue(OPEN) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, worldPosition);
////            level.sendBlockUpdated(worldPosition, block, block, 3);
////        }
//        boolean wasSettled = animation.settled();
//        BlockEntity below = level.getBlockEntity(worldPosition.below());
//        float speed = 0;
//        if (below instanceof KineticBlockEntity kbe) {
//            speed = getSpeed();
//        }
//
////        System.out.println("Speed: " + speed);
//        boolean shouldOpen = speed < 0;
//        if (speed < 0) {
//            speed = -speed;
//        }
//        speed = speed / 50f * 0.05f;
//        animation.chase(shouldOpen ? 0 : 1, speed, LerpedFloat.Chaser.LINEAR);
//        animation.tickChaser();
//
//        if (level.isClientSide()) {
//            if (bridgeTicks < 2 && open)
//                bridgeTicks++;
//            else if (bridgeTicks > 0 && !open)
//                bridgeTicks--;
//        }
//
//        if (animation.settled()) {
//            boolean openAnimation = (animation.getValue() != 0);
////            System.out.println("Open Animation: " + openAnimation);
//            if (openAnimation == open)
//                return;
//            block = block.setValue(OPEN, openAnimation);
//            level.setBlock(worldPosition, block, 10);
//        }
//
//        // System.out.println("Animation value: " + animation.getValue());
//        // System.out.println("Bridge ticks: " + bridgeTicks);
//
//    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public static boolean isOpen(BlockState state) {
        return state.getOptionalValue(OPEN)
                .orElse(false);
    }


}
