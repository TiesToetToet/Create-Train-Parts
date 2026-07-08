package com.tiestoettoet.create_train_parts.content.trains.crossing;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import com.tiestoettoet.create_train_parts.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.stream.Stream;

public class PoleBlock extends HorizontalKineticBlock implements IBE<KineticBlockEntity> {

    public static final BooleanProperty BASE = BooleanProperty.create("base");
    public static final BooleanProperty TOP = BooleanProperty.create("top");

    public PoleBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BASE, true).setValue(TOP, true));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        return state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        boolean base = state.getValue(BASE);
        if (base) {
            return Stream.of(
                    Block.box(5, 1, 5, 11, 15, 11),
                    Block.box(10, 15, 5, 11, 16, 11),
                    Block.box(6, 15, 10, 10, 16, 11),
                    Block.box(6, 15, 5, 10, 16, 6),
                    Block.box(5, 15, 5, 6, 16, 11),
                    Block.box(4, 0, 4, 5, 1, 12),
                    Block.box(5, 0, 11, 11, 1, 12),
                    Block.box(11, 0, 4, 12, 1, 12),
                    Block.box(5, 0, 4, 11, 1, 5)
            ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
        } else {
            return Stream.of(
                    Block.box(10, 15, 5, 11, 16, 11),
                    Block.box(6, 15, 10, 10, 16, 11),
                    Block.box(6, 15, 5, 10, 16, 6),
                    Block.box(5, 15, 5, 6, 16, 11),
                    Block.box(6, 0, 5, 10, 1, 6),
                    Block.box(6, 0, 10, 10, 1, 11),
                    Block.box(10, 0, 5, 11, 1, 11),
                    Block.box(5, 0, 5, 6, 1, 11),
                    Block.box(5, 1, 5, 11, 15, 11)
            ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
        }
    }


    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // get block under current position
        BlockState blockStateBelow = context.getLevel().getBlockState(context.getClickedPos().below());
        BlockState blockStateAbove = context.getLevel().getBlockState(context.getClickedPos().above());
        boolean isBase = !(blockStateBelow.getBlock() instanceof PoleBlock);
        boolean isTop = !(blockStateAbove.getBlock() instanceof PoleBlock || blockStateAbove.getBlock() instanceof CrossingBlock);
        return defaultBlockState().setValue(BASE, isBase).setValue(TOP, isTop).setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
//        if (blockState.getBlock() instanceof PoleBlock) {
//            return defaultBlockState().setValue(BASE, false).setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
//        } else {
//            return defaultBlockState().setValue(BASE, true).setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
//        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BASE, TOP, HORIZONTAL_FACING);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {

        if (direction == Direction.DOWN) {
            return state.setValue(BASE, !neighborState.is(this));
        }

        if (direction == Direction.UP) {
            return state.setValue(TOP, !(neighborState.is(this) || neighborState.getBlock() instanceof CrossingBlock));
        }

        return state;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.UP || face == Direction.DOWN;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public Class<KineticBlockEntity> getBlockEntityClass() {
        return KineticBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.POLE.get();
    }
}
