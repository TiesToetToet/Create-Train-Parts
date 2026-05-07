package com.tiestoettoet.create_train_parts.content.trains.crossing;

import com.mojang.serialization.MapCodec;
import com.tiestoettoet.create_train_parts.AllBlocks;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.tiestoettoet.create_train_parts.foundation.placement.ArmHelper;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Predicate;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class ArmExtenderBlock extends HorizontalDirectionalBlock implements IWrenchable {
    private static final int placementHelperId = PlacementHelpers.register(PlacementHelper.get());

    public static final BooleanProperty FLIPPED = BooleanProperty.create("flipped");
    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    public ArmExtenderBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FLIPPED, false).setValue(OPEN, false));
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
        Direction facing = state.getValue(HORIZONTAL_FACING);
        boolean flipped = state.getValue(FLIPPED);

        // Base shape from JSON: arm extends from y=6 to y=10 (height 4), z=0 to z=16
        // (full depth)
        // Normal (not flipped): x=11 to x=13 (width 2)
        // Flipped: x=3 to x=5 (width 2)

        VoxelShape baseShape;
        if (flipped) {
            // Flipped version: x=3 to x=5
            baseShape = Block.box(3, 6, 0, 5, 10, 16);
        } else {
            // Normal version: x=11 to x=13
            baseShape = Block.box(11, 6, 0, 13, 10, 16);
        }

        // Rotate the shape based on the facing direction
        // The JSON models are oriented for west facing (y=0), so we need to rotate
        // accordingly
        return switch (facing) {
            case NORTH -> flipped ? Block.box(0, 6, 3, 16, 10, 5) : Block.box(0, 6, 11, 16, 10, 13);
            case SOUTH -> flipped ? Block.box(0, 6, 11, 16, 10, 13) : Block.box(0, 6, 3, 16, 10, 5);
            case EAST -> flipped ? Block.box(11, 6, 0, 13, 10, 16) : Block.box(3, 6, 0, 5, 10, 16);
            case WEST -> flipped ? Block.box(3, 6, 0, 5, 10, 16) : Block.box(11, 6, 0, 13, 10, 16);
            default -> baseShape;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }

        Direction facing = context.getHorizontalDirection(); // Get the horizontal direction the player is facing
        boolean flipped = false; // Default value for flipped

        return state.setValue(HORIZONTAL_FACING, facing).setValue(FLIPPED, flipped).setValue(OPEN, true);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (placementHelper.matchesItem(stack) && !player.isShiftKeyDown())
            return placementHelper.getOffset(player, level, state, pos, hitResult).placeInWorld(level,
                    (BlockItem) stack.getItem(), player, hand, hitResult);

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public static boolean isArm(BlockState state) {
        return AllBlocks.ARM_EXTENDER.has(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(HORIZONTAL_FACING).add(FLIPPED).add(OPEN));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor world,
            BlockPos pos, BlockPos neighbourPos) {
        return state;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
        // RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    // @Override
    // public Class<ArmExtenderBlockEntity> getBlockEntityClass() {
    // return ArmExtenderBlockEntity.class;
    // }
    //
    // @Override
    // public BlockEntityType<? extends ArmExtenderBlockEntity> getBlockEntityType()
    // {
    // return AllBlockEntityTypes.ARM_EXTENDER.get();
    // }

    @MethodsReturnNonnullByDefault
    public static class PlacementHelper extends ArmHelper<Direction> {

        private static final PlacementHelper instance = new PlacementHelper();

        public static PlacementHelper get() {
            return instance;
        }

        private PlacementHelper() {
            super(
                    AllBlocks.ARM_EXTENDER::has,
                    state -> state.getValue(HORIZONTAL_FACING).getClockWise().getAxis(),
                    HORIZONTAL_FACING);
        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return AllBlocks.ARM_EXTENDER::isIn;
        }
    }

}
