package com.tiestoettoet.create_train_parts.content.contraptions.behaviour;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.SimpleBlockMovingInteraction;
import com.tiestoettoet.create_train_parts.content.decoration.slidingWindow.SlidingWindowBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
public class WindowMovingInteraction extends SimpleBlockMovingInteraction {

    @Override
    protected BlockState handle(Player player, Contraption contraption, BlockPos pos, BlockState currentState) {
        boolean trainWindow = currentState.getBlock() instanceof SlidingWindowBlock;
        SoundEvent sound = currentState.getValue(SlidingWindowBlock.OPEN) ? trainWindow ? null : SoundEvents.WOODEN_DOOR_CLOSE
                : trainWindow ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.WOODEN_DOOR_OPEN;
        currentState = currentState.cycle(SlidingWindowBlock.OPEN);
        if (player != null) {
            if (trainWindow) {
                Boolean open = currentState.getValue(SlidingWindowBlock.OPEN);
                toggleWindow(currentState, pos, null, open, contraption);
            }
            float pitch = player.level().random.nextFloat() * 0.1F + 0.9F;
            if (sound != null)
                playSound(player, sound, pitch);

        }
        return currentState;
    }

    private void toggleWindow(BlockState state, BlockPos pos, String ignore, Boolean open, Contraption contraption) {
        if (ignore == null)
            ignore = "";
        Direction facing = state.getValue(SlidingWindowBlock.FACING);

        BlockPos leftPos = pos.relative(facing.getClockWise());
        BlockPos rightPos = pos.relative(facing.getCounterClockWise());
        BlockState leftState;
        BlockState rightState;
        StructureTemplate.StructureBlockInfo leftInfo = contraption.getBlocks()
                .get(leftPos);
        StructureTemplate.StructureBlockInfo rightInfo = contraption.getBlocks()
                .get(rightPos);
        try {
            leftState = leftInfo.state();;
        } catch (NullPointerException e) {
            leftState = null;
        }
        try {
            rightState = rightInfo.state();
        } catch (NullPointerException e) {
            rightState = null;
        }
        if (leftInfo != null && !ignore.equals("left") && leftInfo.state().getBlock() instanceof SlidingWindowBlock) {
//                handlePlayerInteraction(null, InteractionHand.MAIN_HAND, leftPos, contraption.entity);
            toggleWindow(leftState, leftPos, "right", open, contraption);
        }
        if (rightInfo != null && !ignore.equals("right") && rightInfo.state().getBlock() instanceof SlidingWindowBlock) {
//                handlePlayerInteraction(null, InteractionHand.MAIN_HAND, rightPos, contraption.entity);
            toggleWindow(rightState, rightPos, "left", open, contraption);
        }
        handlePlayerInteraction(null, InteractionHand.MAIN_HAND, pos, contraption.entity);
    }

    @Override
    protected boolean updateColliders() {
        return true;
    }

}
