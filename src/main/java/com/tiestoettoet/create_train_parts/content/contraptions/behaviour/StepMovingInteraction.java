package com.tiestoettoet.create_train_parts.content.contraptions.behaviour;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.SimpleBlockMovingInteraction;
import com.tiestoettoet.create_train_parts.content.decoration.trainStep.TrainStepBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import static com.tiestoettoet.create_train_parts.content.decoration.trainStep.TrainStepBlock.CONNECTED;

public class StepMovingInteraction extends SimpleBlockMovingInteraction {

    @Override
    protected BlockState handle(Player player, Contraption contraption, BlockPos pos, BlockState currentState) {
        boolean trainStep = currentState.getBlock() instanceof TrainStepBlock;
        SoundEvent sound = currentState.getValue(TrainStepBlock.OPEN) ? trainStep ? null : SoundEvents.WOODEN_DOOR_CLOSE
            : trainStep ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.WOODEN_DOOR_OPEN;
        currentState = currentState.cycle(TrainStepBlock.OPEN);
        if (player != null) {
            if (trainStep) {
                Boolean open = currentState.getValue(TrainStepBlock.OPEN);
                toggleStep(currentState, pos, null, open, contraption);
            }
            float pitch = player.level().random.nextFloat() * 0.1F + 0.9F;
            if (sound != null)
                playSound(player, sound, pitch);

        }
        return currentState;
    }

    private void toggleStep(BlockState state, BlockPos pos, String ignore, Boolean open, Contraption contraption) {
        if (ignore == null)
            ignore = "";
        Direction facing = state.getValue(TrainStepBlock.FACING);
        TrainStepBlock.ConnectedState connected = state.getValue(CONNECTED);
        if (connected == TrainStepBlock.ConnectedState.BOTH) {
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
            if (leftInfo != null && leftInfo.state().getValue(CONNECTED) != TrainStepBlock.ConnectedState.NONE && !ignore.equals("left")) {
//                handlePlayerInteraction(null, InteractionHand.MAIN_HAND, leftPos, contraption.entity);
                toggleStep(leftState, leftPos, "right", open, contraption);
            }
            if (rightInfo != null && rightInfo.state().getValue(CONNECTED) != TrainStepBlock.ConnectedState.NONE && !ignore.equals("right")) {
//                handlePlayerInteraction(null, InteractionHand.MAIN_HAND, rightPos, contraption.entity);
                toggleStep(rightState, rightPos, "left", open, contraption);
            }
        } else if (connected == TrainStepBlock.ConnectedState.LEFT) {
            BlockPos leftPos = pos.relative(facing.getClockWise());
            StructureTemplate.StructureBlockInfo leftInfo = contraption.getBlocks()
                    .get(leftPos);
            if (leftInfo != null && leftInfo.state().getValue(CONNECTED) != TrainStepBlock.ConnectedState.NONE && !ignore.equals("left")) {
//                handlePlayerInteraction(null, InteractionHand.MAIN_HAND, leftPos, contraption.entity);
                toggleStep(leftInfo.state(), leftPos, "right", open, contraption);
            }
        } else if (connected == TrainStepBlock.ConnectedState.RIGHT) {
            BlockPos rightPos = pos.relative(facing.getCounterClockWise());
            StructureTemplate.StructureBlockInfo rightInfo = contraption.getBlocks()
                    .get(rightPos);
            if (rightInfo != null && rightInfo.state().getValue(CONNECTED) != TrainStepBlock.ConnectedState.NONE && !ignore.equals("right")) {
//                handlePlayerInteraction(null, InteractionHand.MAIN_HAND, rightPos, contraption.entity);
                toggleStep(rightInfo.state(), rightPos, "left", open, contraption);
            }
        }
        handlePlayerInteraction(null, InteractionHand.MAIN_HAND, pos, contraption.entity);
    }

    @Override
    protected boolean updateColliders() {
        return true;
    }

}
