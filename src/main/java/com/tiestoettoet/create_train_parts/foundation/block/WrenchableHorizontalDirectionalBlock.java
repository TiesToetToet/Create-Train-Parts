package com.tiestoettoet.create_train_parts.foundation.block;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public abstract class WrenchableHorizontalDirectionalBlock extends HorizontalDirectionalBlock implements IWrenchable {

    public WrenchableHorizontalDirectionalBlock(Properties properties) {
        super(properties);
    }

    protected abstract boolean isPathfindable(BlockState state, PathComputationType pathComputationType);

    protected abstract InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult);

    protected abstract InteractionResult use(
            ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult);

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        InteractionResult result = stack.isEmpty()
                ? useWithoutItem(state, level, pos, player, hitResult)
                : use(stack, state, level, pos, player, hand, hitResult);

        return result == null ? InteractionResult.PASS : result;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        Direction facing = originalState.getValue(FACING);

        if (facing.getAxis() == targetedFace.getAxis())
            return originalState;

        Direction newFacing = facing.getClockWise(targetedFace.getAxis());
        if (newFacing == Direction.UP || newFacing == Direction.DOWN) {
            return originalState; // Do nothing if the new facing is up or down
        }

        return originalState.setValue(FACING, newFacing);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        Direction newFacing = rot.rotate(state.getValue(FACING));
        if (newFacing == Direction.UP || newFacing == Direction.DOWN) {
            return state; // Do nothing if the new facing is up or down
        }
        return state.setValue(FACING, newFacing);
    }
}
