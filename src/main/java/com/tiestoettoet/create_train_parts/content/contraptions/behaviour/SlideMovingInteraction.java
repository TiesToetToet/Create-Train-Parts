package com.tiestoettoet.create_train_parts.content.contraptions.behaviour;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.SimpleBlockMovingInteraction;
import com.tiestoettoet.create_train_parts.content.decoration.trainSlide.TrainSlideBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import static com.tiestoettoet.create_train_parts.content.decoration.trainSlide.TrainSlideBlock.CONNECTED;

public class SlideMovingInteraction extends SimpleBlockMovingInteraction {

    @Override
    protected BlockState handle(Player player, Contraption contraption, BlockPos pos, BlockState currentState) {
        boolean trainSlide = currentState.getBlock() instanceof TrainSlideBlock;
        SoundEvent sound = currentState.getValue(TrainSlideBlock.OPEN) ? trainSlide ? null : SoundEvents.WOODEN_DOOR_CLOSE
            : trainSlide ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.WOODEN_DOOR_OPEN;
        currentState = currentState.cycle(TrainSlideBlock.OPEN);
        if (player != null) {
            if (trainSlide) {
                Boolean open = currentState.getValue(TrainSlideBlock.OPEN);
                toggleSlide(currentState, pos, null, open, contraption);
            }
            float pitch = player.level().random.nextFloat() * 0.1F + 0.9F;
            if (sound != null)
                playSound(player, sound, pitch);

        }
        return currentState;
    }

    private void toggleSlide(BlockState state, BlockPos pos, String ignore, Boolean open, Contraption contraption) {
        if (ignore == null)
            ignore = "";
        Direction facing = state.getValue(TrainSlideBlock.FACING);
        TrainSlideBlock.ConnectedState connected = state.getValue(CONNECTED);
        if (connected == TrainSlideBlock.ConnectedState.BOTH) {
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
            if (leftInfo != null && leftInfo.state().getValue(CONNECTED) != TrainSlideBlock.ConnectedState.NONE && !ignore.equals("left")) {
//                handlePlayerInteraction(null, InteractionHand.MAIN_HAND, leftPos, contraption.entity);
                toggleSlide(leftState, leftPos, "right", open, contraption);
            }
            if (rightInfo != null && rightInfo.state().getValue(CONNECTED) != TrainSlideBlock.ConnectedState.NONE && !ignore.equals("right")) {
//                handlePlayerInteraction(null, InteractionHand.MAIN_HAND, rightPos, contraption.entity);
                toggleSlide(rightState, rightPos, "left", open, contraption);
            }
        } else if (connected == TrainSlideBlock.ConnectedState.LEFT) {
            BlockPos leftPos = pos.relative(facing.getClockWise());
            StructureTemplate.StructureBlockInfo leftInfo = contraption.getBlocks()
                    .get(leftPos);
            if (leftInfo != null && leftInfo.state().getValue(CONNECTED) != TrainSlideBlock.ConnectedState.NONE && !ignore.equals("left")) {
//                handlePlayerInteraction(null, InteractionHand.MAIN_HAND, leftPos, contraption.entity);
                toggleSlide(leftInfo.state(), leftPos, "right", open, contraption);
            }
        } else if (connected == TrainSlideBlock.ConnectedState.RIGHT) {
            BlockPos rightPos = pos.relative(facing.getCounterClockWise());
            StructureTemplate.StructureBlockInfo rightInfo = contraption.getBlocks()
                    .get(rightPos);
            if (rightInfo != null && rightInfo.state().getValue(CONNECTED) != TrainSlideBlock.ConnectedState.NONE && !ignore.equals("right")) {
//                handlePlayerInteraction(null, InteractionHand.MAIN_HAND, rightPos, contraption.entity);
                toggleSlide(rightInfo.state(), rightPos, "left", open, contraption);
            }
        }
        handlePlayerInteraction(null, InteractionHand.MAIN_HAND, pos, contraption.entity);
    }

    @Override
    protected boolean updateColliders() {
        return true;
    }

}
