package com.tiestoettoet.create_train_parts.content.trains.crossing;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.gantry.GantryShaftBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.IHaveBigOutline;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tiestoettoet.create_train_parts.AllBlockEntityTypes;
import com.tiestoettoet.create_train_parts.AllBlocks;
import com.tiestoettoet.create_train_parts.AllSoundEvents;
import com.tiestoettoet.create_train_parts.content.decoration.trainStep.TrainStepBlockEntity;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class CrossingBlock extends HorizontalKineticBlock
        implements IBE<CrossingBlockEntity>, IWrenchable, IHaveBigOutline {
    public static final int placementHelperId = PlacementHelpers.register(new GantryShaftBlock.PlacementHelper());
    public static final BooleanProperty FLIPPED = BooleanProperty.create("flipped");
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final BooleanProperty BARRIER = BooleanProperty.create("barrier");
    public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");
    public static final BooleanProperty BELL = BooleanProperty.create("bell");

//    protected static final VoxelShape NORTH_OPEN;
//    protected static final VoxelShape NORTH_OPEN_FLIPPED;
//    protected static final VoxelShape NORTH_CLOSED;
//    protected static final VoxelShape NORTH_CLOSED_BARRIER;
//    protected static final VoxelShape NORTH_CLOSED_FLIPPED;
//    protected static final VoxelShape NORTH_CLOSED_FLIPPED_BARRIER;
//    protected static final VoxelShape SOUTH_OPEN;
//    protected static final VoxelShape SOUTH_OPEN_FLIPPED;
//    protected static final VoxelShape SOUTH_CLOSED;
//    protected static final VoxelShape SOUTH_CLOSED_BARRIER;
//    protected static final VoxelShape SOUTH_CLOSED_FLIPPED;
//    protected static final VoxelShape SOUTH_CLOSED_FLIPPED_BARRIER;
//    protected static final VoxelShape WEST_OPEN;
//    protected static final VoxelShape WEST_OPEN_FLIPPED;
//    protected static final VoxelShape WEST_CLOSED;
//    protected static final VoxelShape WEST_CLOSED_BARRIER;
//    protected static final VoxelShape WEST_CLOSED_FLIPPED;
//    protected static final VoxelShape WEST_CLOSED_FLIPPED_BARRIER;
//    protected static final VoxelShape EAST_OPEN;
//    protected static final VoxelShape EAST_OPEN_FLIPPED;
//    protected static final VoxelShape EAST_CLOSED;
//    protected static final VoxelShape EAST_CLOSED_BARRIER;
//    protected static final VoxelShape EAST_CLOSED_FLIPPED;
//    protected static final VoxelShape EAST_CLOSED_FLIPPED_BARRIER;

    public CrossingBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FLIPPED, false).setValue(OPEN, false).setValue(BARRIER, false).setValue(CONNECTED, false).setValue(BELL, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
//        Boolean flipped = state.getValue(FLIPPED);
//        Boolean open = state.getValue(OPEN);
//        Direction direction = state.getValue(HORIZONTAL_FACING);
//        switch (direction) {
//            case NORTH -> {
//                if (open) {
//                    return flipped ? NORTH_OPEN_FLIPPED : NORTH_OPEN;
//                } else {
//                    return flipped ? NORTH_CLOSED_FLIPPED : NORTH_CLOSED;
//                }
//            }
//            case SOUTH -> {
//                if (open) {
//                    return flipped ? SOUTH_OPEN_FLIPPED : SOUTH_OPEN;
//                } else {
//                    return flipped ? SOUTH_CLOSED_FLIPPED : SOUTH_CLOSED;
//                }
//            }
//            case WEST -> {
//                if (open) {
//                    return flipped ? WEST_OPEN_FLIPPED : WEST_OPEN;
//                } else {
//                    return flipped ? WEST_CLOSED_FLIPPED : WEST_CLOSED;
//                }
//            }
//            case EAST -> {
//                if (open) {
//                    return flipped ? EAST_OPEN_FLIPPED : EAST_OPEN;
//                } else {
//                    return flipped ? EAST_CLOSED_FLIPPED : EAST_CLOSED;
//                }
//            }
//
//        }
//        return Shapes.block();
        VoxelShape poleShape = pole();
        VoxelShape baseShape = base(state);
        VoxelShape lightsShape = lights(state);
        VoxelShape armShape = arm(state);
        VoxelShape bellShape = bell(state);
        return Shapes.join(poleShape, Shapes.join(baseShape, Shapes.join(lightsShape, Shapes.join(armShape, bellShape, BooleanOp.OR), BooleanOp.OR), BooleanOp.OR), BooleanOp.OR);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
//        Boolean flipped = state.getValue(FLIPPED);
//        Boolean open = state.getValue(OPEN);
//        if (open) {
//            return getShape(state, level, pos, context);
//        }
//        Boolean barrier = state.getValue(BARRIER);
//        if (!barrier) {
//            return getShape(state, level, pos, context);
//        }
//        Direction direction = state.getValue(HORIZONTAL_FACING);
//        switch (direction) {
//            case NORTH -> {
//                return flipped ? NORTH_CLOSED_FLIPPED_BARRIER : NORTH_CLOSED_BARRIER;
//            }
//            case SOUTH -> {
//                return flipped ? SOUTH_CLOSED_FLIPPED_BARRIER : SOUTH_CLOSED_BARRIER;
//            }
//            case WEST -> {
//                return flipped ? WEST_CLOSED_FLIPPED_BARRIER : WEST_CLOSED_BARRIER;
//            }
//            case EAST -> {
//                return flipped ? EAST_CLOSED_FLIPPED_BARRIER : EAST_CLOSED_BARRIER;
//            }
//
//        }
//        return getShape(state, level, pos, context);
        VoxelShape poleShape = pole();
        VoxelShape baseShape = base(state);
        VoxelShape lightsShape = lights(state);
        VoxelShape armShape = arm(state);
        VoxelShape barrierShape = barrier(state);
        VoxelShape bellShape = bell(state);
//        return Shapes.join(poleShape, Shapes.join(baseShape, Shapes.join(lightsShape, Shapes.join(armShape, barrierShape, BooleanOp.OR), BooleanOp.OR), BooleanOp.OR), BooleanOp.OR);
        return Shapes.join(poleShape, Shapes.join(baseShape, Shapes.join(lightsShape, Shapes.join(armShape, Shapes.join(barrierShape, bellShape, BooleanOp.OR), BooleanOp.OR), BooleanOp.OR), BooleanOp.OR), BooleanOp.OR);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FLIPPED).add(OPEN).add(BARRIER).add(CONNECTED).add(BELL));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }

        Direction facing = context.getHorizontalDirection();

        boolean flipped = false;

        BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
        boolean connected = below.getBlock() instanceof PoleBlock;

        return state.setValue(HORIZONTAL_FACING, facing).setValue(FLIPPED, flipped).setValue(OPEN, false).setValue(BARRIER, false).setValue(CONNECTED, connected).setValue(BELL, false);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor world,
                                  BlockPos pos, BlockPos neighbourPos) {
        if (direction == Direction.DOWN) {
            boolean connected = neighbourState.getBlock() instanceof PoleBlock;
            return state.setValue(CONNECTED, connected);
        }
        return state;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();

        // Only allow rotation/flipping when the crossing is closed
        if (state.getValue(OPEN)) {
            return InteractionResult.PASS;
        }

        if (state.getValue(BELL)) {
            // If the bell is active, deactivate it and do not allow rotation/flipping
            world.setBlock(pos, state.setValue(BELL, false), 3);
            world.gameEvent(context.getPlayer(), GameEvent.BLOCK_CHANGE, pos);
            // give the bell back to the player
            if (!context.getPlayer().isCreative()) {
                ItemStack bellItem = new ItemStack(Items.BELL);
                if (!context.getPlayer().addItem(bellItem)) {
                    context.getLevel().addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), bellItem));
                }
            }
            return InteractionResult.SUCCESS;
        }

        BlockState rotated;
        if (context.getClickedFace().getAxis() == Direction.Axis.Y) {
            rotated = getRotatedBlockState(state, context.getClickedFace());
        } else {
            if (context.getClickedFace().getAxis() == state.getValue(HORIZONTAL_FACING).getAxis()) {
                rotated = state.cycle(FLIPPED);
            } else {
                rotated = state.setValue(HORIZONTAL_FACING, context.getClickedFace());
            }
        }

        if (!rotated.canSurvive(world, pos))
            return InteractionResult.PASS;

        // Update connected arm extenders to match the new crossing state
        updateConnectedArmExtenders(world, pos, state, rotated);

        KineticBlockEntity.switchToBlockState(world, pos, updateAfterWrenched(rotated, context));

        return InteractionResult.SUCCESS;
    }

    private void updateConnectedArmExtenders(Level world, BlockPos crossingPos, BlockState oldState,
                                             BlockState newState) {
        Direction oldFacing = oldState.getValue(HORIZONTAL_FACING);
        boolean oldFlipped = oldState.getValue(FLIPPED);
        Direction newFacing = newState.getValue(HORIZONTAL_FACING);
        boolean newFlipped = newState.getValue(FLIPPED);


        // Check if only the flip state changed (not the facing direction)
        boolean onlyFlipped = oldFacing == newFacing && oldFlipped != newFlipped;

        if (onlyFlipped) {
            // If only flipped, just update the FLIPPED property of existing arm extenders
            Direction direction = oldFacing.getClockWise(); // Arms are always placed in the clockwise direction relative to facing
            BlockPos armPos = crossingPos.relative(direction);
            while (world.getBlockState(armPos).getBlock() instanceof ArmExtenderBlock) {
                BlockState currentArmState = world.getBlockState(armPos);
                BlockState newArmState = currentArmState
                        .setValue(ArmExtenderBlock.FLIPPED, newFlipped);
                world.setBlock(armPos, newArmState, 3);
                armPos = armPos.relative(direction);
            }
        } else {
            // If facing direction changed, move the arms to the new direction
            // Calculate the correct new arm direction based on the new state
            // The key insight: maintain the same relative position
            // (clockwise/counter-clockwise)
            // from the crossing's perspective, regardless of which direction we're facing
            Direction newArmDirection;
            // if (newFlipped) {
            // // When flipped: arms consistently go to the left side when looking in the
            // // facing direction
            // newArmDirection = newFacing.getCounterClockWise();
            // } else {
            // // When not flipped: arms consistently go to the right side when looking in
            // the
            // // facing direction
            // newArmDirection = newFacing.getClockWise();
            // }

            newArmDirection = newFacing.getClockWise();
            // Collect all arm extenders from all directions
            java.util.List<BlockPos> allArmPositions = new java.util.ArrayList<>();

            Direction direction = oldFacing.getClockWise(); // Arms are always placed in the clockwise direction relative to facing
            BlockPos armPos = crossingPos.relative(direction);
            while (world.getBlockState(armPos).getBlock() instanceof ArmExtenderBlock) {
                allArmPositions.add(armPos);
                armPos = armPos.relative(direction);
            }

            // Remove all arm extenders from their old positions
            for (BlockPos pos : allArmPositions) {
                world.removeBlock(pos, false);
            }

            // Place them all in the new direction with updated orientation
            for (int i = 0; i < allArmPositions.size(); i++) {
                BlockPos newPos = crossingPos.relative(newArmDirection, i + 1);

                // Check if there's a block in the way and destroy it with drops
                BlockState existingState = world.getBlockState(newPos);
                if (!existingState.isAir()) {
                    world.destroyBlock(newPos, true); // true = drop items
                }

                BlockState newArmState = AllBlocks.ARM_EXTENDER.getDefaultState()
                        .setValue(HORIZONTAL_FACING, newFacing)
                        .setValue(ArmExtenderBlock.FLIPPED, newFlipped)
                        .setValue(ArmExtenderBlock.OPEN, true);

                // Always place the arm extender, overwriting whatever is there
                world.setBlock(newPos, newArmState, 3);
            }
        }
    }

    public static boolean isCrossing(BlockState state) {
        return AllBlocks.CROSSING.has(state);
    }

    @Override
    public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        boolean dropBlocks = player == null || !player.isCreative();

        // Find and destroy all connected arm extenders in all directions
        Direction armDirection = state.getValue(HORIZONTAL_FACING).getClockWise();

//            java.util.List<ItemStack> armExtenderItems = new java.util.ArrayList<>();

        BlockPos armPos = pos.relative(armDirection);
        while (worldIn.getBlockState(armPos).getBlock() instanceof ArmExtenderBlock) {
            worldIn.destroyBlock(armPos, dropBlocks);
            armPos = armPos.relative(armDirection);
        }

        super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // Block is being completely removed/replaced with a different block type
            // Find and collect all connected arm extenders in all directions

            java.util.List<ItemStack> armExtenderItems = new java.util.ArrayList<>();

//            for (Direction direction : Direction.Plane.HORIZONTAL) {
//                BlockPos armPos = pos.relative(direction);
//                while (world.getBlockState(armPos).getBlock() instanceof ArmExtenderBlock) {
//                    // Create the item stack for this arm extender
//                    armExtenderItems.add(new ItemStack(AllBlocks.ARM_EXTENDER.get()));
//                    // Remove the block without dropping items
//                    world.removeBlock(armPos, false);
//                    armPos = armPos.relative(direction);
//                }
//            }

            Direction armDirection = state.getValue(HORIZONTAL_FACING).getClockWise();

//            java.util.List<ItemStack> armExtenderItems = new java.util.ArrayList<>();

            BlockPos armPos = pos.relative(armDirection);
            while (world.getBlockState(armPos).getBlock() instanceof ArmExtenderBlock) {
                armExtenderItems.add(new ItemStack(AllBlocks.ARM_EXTENDER.get()));
                world.removeBlock(armPos, false);
                armPos = armPos.relative(armDirection);
            }

            // Try to find a player nearby to give the items to
            if (!armExtenderItems.isEmpty() && !world.isClientSide) {
                Player nearestPlayer = world.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 10.0, false);
                if (nearestPlayer != null) {
                    // Give items to the nearest player
                    for (ItemStack item : armExtenderItems) {
                        if (!nearestPlayer.addItem(item)) {
                            // If inventory is full, drop the item
                            Block.popResource(world, pos, item);
                        }
                    }
                } else {
                    // No player nearby, drop the items
                    for (ItemStack item : armExtenderItems) {
                        Block.popResource(world, pos, item);
                    }
                }
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    public static LootTable.Builder buildLootTable() {
        LootItemCondition.Builder survivesExplosion = ExplosionCondition.survivesExplosion();
        CrossingBlock block = AllBlocks.CROSSING.get();

        LootTable.Builder builder = LootTable.lootTable();
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1));

        // Always drop the crossing itself
        pool.add(LootItem.lootTableItem(AllBlocks.CROSSING.get())
                .when(survivesExplosion));

        // Drop a bell only if the BELL property is true
        pool.add(LootItem.lootTableItem(Items.BELL)
                .when(survivesExplosion)
                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                .hasProperty(CrossingBlock.BELL, true))));

        builder.withPool(pool);
        return builder;
    }

    public void playSound(Level world, BlockPos pos) {
        AllSoundEvents.CROSSING_BELL.playOnServer(world, pos, 2f, 1f);
    }

    private static List<BlockPos> getConnectedArmPositions(Level world, BlockPos crossingPos, BlockState state) {
        List<BlockPos> positions = new ArrayList<>();

        Direction armDirection = state.getValue(HORIZONTAL_FACING).getClockWise();
        BlockPos armPos = crossingPos.relative(armDirection);

        while (world.getBlockState(armPos).getBlock() instanceof ArmExtenderBlock) {
            positions.add(armPos);
            armPos = armPos.relative(armDirection);
        }

        return positions;
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, world, pos, oldState, isMoving);
        BlockPos currentPos = pos.below();
        for (int i = 0; i < 16; i++) {
            BlockState blockState = world.getBlockState(currentPos);
            if (AllBlocks.CROSSING.has(blockState)) {
                KineticBlockEntity.switchToBlockState(world, currentPos,
                        Block.updateFromNeighbourShapes(blockState, world, currentPos));
            }
            currentPos = currentPos.below();
        }

        currentPos = pos.above();
        for (int i = 0; i < 16; i++) {
            BlockState blockState = world.getBlockState(currentPos);
            if (AllBlocks.CROSSING.has(blockState)) {
                KineticBlockEntity.switchToBlockState(world, currentPos,
                        Block.updateFromNeighbourShapes(blockState, world, currentPos));
            }
            currentPos = currentPos.above();
        }

        // Direction facing = state.getValue(HORIZONTAL_FACING);
        // boolean flipped = state.getValue(FLIPPED);
        // BlockPos armExtenderPos;
        // if (flipped)
        // armExtenderPos = pos.relative(facing.getCounterClockWise());
        // else
        // armExtenderPos = pos.relative(facing.getClockWise());

        // BlockState armExtenderState = AllBlocks.ARM_EXTENDER.getDefaultState()
        // .setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING))
        // .setValue(ArmExtenderBlock.FLIPPED, state.getValue(FLIPPED));

        // if (world.getBlockState(armExtenderPos).canBeReplaced()) {
        // world.setBlock(armExtenderPos, armExtenderState, 3);
        // }

    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getShape(state, level, pos, CollisionContext.empty());
    }

    // @Override
    // protected InteractionResult useWithoutItem(BlockState state, Level level,
    // BlockPos pos, Player player,
    // BlockHitResult hitResult) {
    // state = state.cycle(OPEN);
    // level.setBlock(pos, state, 10);
    // level.gameEvent(player, state.getValue(OPEN) ? GameEvent.BLOCK_OPEN :
    // GameEvent.BLOCK_CLOSE, pos);
    // level.sendBlockUpdated(pos, state, state, 3);
    // return InteractionResult.sidedSuccess(level.isClientSide);
    // }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    public static class PlacementHelper implements IPlacementHelper {

        public PlacementHelper() {

        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return AllBlocks.CROSSING::isIn;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return AllBlocks.CROSSING::has;
        }

        @Override
        public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
                                         BlockHitResult ray) {

            Direction offsetDirection = ray.getLocation().subtract(Vec3.atCenterOf(pos)).y < 0 ? Direction.DOWN
                    : Direction.UP;

            BlockPos newPos = pos.relative(offsetDirection);
            BlockState newState = world.getBlockState(newPos);

            if (!newState.canBeReplaced()) {
                newPos = pos.relative(offsetDirection.getOpposite());
                newState = world.getBlockState(newPos);
            }

            if (newState.canBeReplaced()) {

                Direction facing = ray.getDirection();
                if (facing.getAxis() == Direction.Axis.Y)
                    return PlacementOffset.fail();

                Vec3 look = player.getLookAngle();
                Vec3 cross = look.cross(new Vec3(facing.step()));
                boolean flipped = cross.y < 0;

                return PlacementOffset.success(newPos,
                        x -> x.setValue(FLIPPED, flipped).setValue(HORIZONTAL_FACING, facing));
            }

            return PlacementOffset.fail();
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state,
                                              Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hitResult) {
        IPlacementHelper placementHelper =
                PlacementHelpers.get(ArmExtenderBlock.placementHelperId);

        if (placementHelper.matchesItem(stack) && !player.isShiftKeyDown())
            return placementHelper.getOffset(player, level, state, pos, hitResult)
                    .placeInWorld(level, (BlockItem) stack.getItem(), player, hand, hitResult);

        if (stack.getItem() == Items.BELL && !state.getValue(BELL)) {
            level.setBlock(pos, state.setValue(BELL, true), 3);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public Class<CrossingBlockEntity> getBlockEntityClass() {
        return CrossingBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CrossingBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.CROSSING.get();
    }

    private static VoxelShape pole() {
        return Block.box(5, 1, 5, 11, 23, 11);
    }

    private static VoxelShape base(BlockState state) {
        boolean connected = state.getValue(CONNECTED);
        if (connected) {
            return Stream.of(
                    Block.box(5, 0, 5, 6, 1, 11),
                    Block.box(6, 0, 10, 10, 1, 11),
                    Block.box(6, 0, 5, 10, 1, 6),
                    Block.box(10, 0, 5, 11, 1, 11)
            ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
        } else {
            return Stream.of(
                    Block.box(4, 0, 4, 5, 1, 12),
                    Block.box(5, 0, 11, 11, 1, 12),
                    Block.box(5, 0, 4, 11, 1, 5),
                    Block.box(11, 0, 4, 12, 1, 12)
            ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
        }
    }

    private static VoxelShape lights(BlockState state) {
        Direction facing = state.getValue(HORIZONTAL_FACING);
        if (facing == Direction.NORTH) {
            return Shapes.join(Block.box(0, 12, 7, 5, 16, 9), Block.box(0, 17, 7, 5, 21, 9), BooleanOp.OR);
        } else if (facing == Direction.SOUTH) {
            return Shapes.join(Block.box(11, 12, 7, 16, 16, 9), Block.box(11, 17, 7, 16, 21, 9), BooleanOp.OR);
        } else if (facing == Direction.WEST) {
            return Shapes.join(Block.box(7, 12, 11, 9, 16, 16), Block.box(7, 17, 11, 9, 21, 16), BooleanOp.OR);
        } else if (facing == Direction.EAST) {
            return Shapes.join(Block.box(7, 12, 0, 9, 16, 5), Block.box(7, 17, 0, 9, 21, 5), BooleanOp.OR);
        }
        return Shapes.empty();
    }

    private static VoxelShape arm(BlockState state) {
        boolean open = state.getValue(OPEN);
        boolean flipped = state.getValue(FLIPPED);
        Direction facing = state.getValue(HORIZONTAL_FACING);
        Direction effectiveFacing = flipped ? facing.getOpposite() : facing;
        VoxelShape baseArm = Shapes.empty();
        if (open) {
            switch (effectiveFacing) {
                case NORTH -> {
                    baseArm = Block.box(6, 0, 11, 10, 16, 13);
                }
                case SOUTH -> {
                    baseArm = Block.box(6, 0, 3, 10, 16, 5);
                }
                case EAST -> {
                    baseArm = Block.box(3, 0, 6, 5, 16, 10);
                }
                case WEST -> {
                    baseArm = Block.box(11, 0, 6, 13, 16, 10);
                }
            }
        } else {
            switch (effectiveFacing) {
                case NORTH -> {
                    baseArm = Block.box(0, 6, 11, 16, 10, 13);
                }
                case SOUTH -> {
                    baseArm = Block.box(0, 6, 3, 16, 10, 5);
                }
                case EAST -> {
                    baseArm = Block.box(3, 6, 0, 5, 10, 16);
                }
                case WEST -> {
                    baseArm = Block.box(11, 6, 0, 13, 10, 16);
                }
            }
        }

        VoxelShape armExtension = Shapes.empty();
        if (open) {
            if (flipped) {
                switch (facing) {
                    case NORTH -> armExtension = Block.box(2, 0, 3, 6, 4, 5);
                    case SOUTH -> armExtension = Block.box(10, 0, 11, 14, 4, 13);
                    case EAST -> armExtension = Block.box(11, 0, 2, 13, 4, 6);
                    case WEST -> armExtension = Block.box(3, 0, 10, 5, 4, 14);
                }
            } else {
                switch (facing) {
                    case NORTH -> armExtension = Block.box(2, 0, 11, 6, 4, 13);
                    case SOUTH -> armExtension = Block.box(10, 0, 3, 14, 4, 5);
                    case EAST -> armExtension = Block.box(3, 0, 2, 5, 4, 6);
                    case WEST -> armExtension = Block.box(11, 0, 10, 13, 4, 14);
                }
            }
        } else {
            if (flipped) {
                switch (facing) {
                    case NORTH -> armExtension =  Block.box(0, 10, 3, 4, 14, 5);
                    case SOUTH -> armExtension = Block.box(12, 10, 11, 16, 14, 13);
                    case EAST -> armExtension = Block.box(11, 10, 0, 13, 14, 4);
                    case WEST -> armExtension = Block.box(3, 10, 12, 5, 14, 16);
                }
            } else {
                switch (facing) {
                    case NORTH -> armExtension = Block.box(0, 10, 11, 4, 14, 13);
                    case SOUTH -> armExtension = Block.box(12, 10, 3, 16, 14, 5);
                    case EAST -> armExtension = Block.box(3, 10, 0, 5, 14, 4);
                    case WEST -> armExtension = Block.box(11, 10, 12, 13, 14, 16);
                }
            }
        }

        return Shapes.join(baseArm, armExtension, BooleanOp.OR);
    }

    private static VoxelShape barrier(BlockState state) {
        boolean barrier = state.getValue(BARRIER);
        boolean open = state.getValue(OPEN);
        if (!barrier || open) {
            return Shapes.empty();
        }
        Direction facing = state.getValue(HORIZONTAL_FACING);
        Direction effectiveFacing = state.getValue(FLIPPED) ? facing.getOpposite() : facing;
        switch (effectiveFacing) {
            case NORTH -> {
                return Shapes.join(Block.box(0, 6, 11, 16, 26, 13), Block.box(0, 10, 11, 4, 30, 13), BooleanOp.OR);
            }
            case SOUTH -> {
                return Shapes.join(Block.box(0, 6, 3, 16, 26, 5), Block.box(12, 10, 3, 16, 30, 5), BooleanOp.OR);
            }
            case EAST -> {
                return Shapes.join(Block.box(3, 6, 0, 5, 26, 16), Block.box(3, 10, 0, 5, 30, 4), BooleanOp.OR);
            }
            case WEST -> {
                return Shapes.join(Block.box(11, 6, 0, 13, 26, 16), Block.box(11, 10, 12, 13, 30, 16), BooleanOp.OR)
                ;
            }
        }
        return Shapes.empty();
    }

    private static VoxelShape bell(BlockState state) {
        boolean bell = state.getValue(BELL);
        if (!bell) {
            return Shapes.empty();
        }
        Direction facing = state.getValue(HORIZONTAL_FACING);
//        Direction effectiveFacing = state.getValue(FLIPPED) ? facing.getOpposite() : facing;
        switch (facing) {
            case NORTH -> {
                return Stream.of(
                    Block.box(11, 20, 7, 17, 22, 9),
                    Stream.of(
                        Block.box(12, 12.5, 5, 18, 13.5, 11),
                        Block.box(13, 13.5, 6, 17, 18.5, 10),
                        Block.box(14.6, 18.5, 7.5, 15.6, 20.5, 8.5)
                    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                    Block.box(17, 20, 7, 19, 22, 9)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
            }
            case SOUTH -> {
                return Stream.of(
                    Block.box(-1, 20, 7, 5, 22, 9),
                    Stream.of(
                        Block.box(-2, 12.5, 5, 4, 13.5, 11),
                        Block.box(-1, 13.5, 6, 3, 18.5, 10),
                        Block.box(0.4, 18.5, 7.5, 1.4, 20.5, 8.5)
                    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                    Block.box(-3, 20, 7, -1, 22, 9)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
            }
            case EAST -> {
                return Stream.of(
                    Block.box(7, 20, 11, 9, 22, 17),
                    Stream.of(
                        Block.box(5, 12.5, 12, 11, 13.5, 18),
                        Block.box(6, 13.5, 13, 10, 18.5, 17),
                        Block.box(7.5, 18.5, 14.6, 8.5, 20.5, 15.6)
                    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                    Block.box(7, 20, 17, 9, 22, 19)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
            }
            case WEST -> {
                return Stream.of(
                    Block.box(7, 20, -1, 9, 22, 5),
                    Stream.of(
                        Block.box(5, 12.5, -2, 11, 13.5, 4),
                        Block.box(6, 13.5, -1, 10, 18.5, 3),
                        Block.box(7.5, 18.5, 0.4, 8.5, 20.5, 1.4)
                    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                    Block.box(7, 20, -3, 9, 22, -1)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
            }
        }
        return Shapes.empty();
    }

//    static {
//        NORTH_OPEN = Stream.of(
//                Stream.of(
//                        Block.box(4, 0, 11, 12, 1, 12),
//                        Block.box(4, 0, 5, 5, 1, 11),
//                        Block.box(11, 0, 5, 12, 1, 11),
//                        Block.box(4, 0, 4, 12, 1, 5),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(4, 12, 7, 5, 16, 9),
//                        Block.box(0, 17, 7, 4, 21, 9),
//                        Block.box(0, 12, 7, 4, 16, 9)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(4, 17, 7, 5, 21, 9),
//                Shapes.join(Block.box(6, 0, 11, 10, 16, 13), Block.box(2, 0, 11, 6, 4, 13), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        NORTH_OPEN_FLIPPED = Stream.of(
//                Stream.of(
//                        Block.box(4, 0, 11, 12, 1, 12),
//                        Block.box(4, 0, 5, 5, 1, 11),
//                        Block.box(11, 0, 5, 12, 1, 11),
//                        Block.box(4, 0, 4, 12, 1, 5),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(4, 12, 7, 5, 16, 9),
//                        Block.box(0, 17, 7, 4, 21, 9),
//                        Block.box(0, 12, 7, 4, 16, 9)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(4, 17, 7, 5, 21, 9),
//                Shapes.join(Block.box(6, 0, 3, 10, 16, 5), Block.box(2, 0, 3, 6, 4, 5), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        NORTH_CLOSED = Stream.of(
//                Stream.of(
//                        Block.box(4, 0, 11, 12, 1, 12),
//                        Block.box(4, 0, 5, 5, 1, 11),
//                        Block.box(11, 0, 5, 12, 1, 11),
//                        Block.box(4, 0, 4, 12, 1, 5),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(4, 12, 7, 5, 16, 9),
//                        Block.box(0, 17, 7, 4, 21, 9),
//                        Block.box(0, 12, 7, 4, 16, 9)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(4, 17, 7, 5, 21, 9),
//                Shapes.join(Block.box(0, 6, 11, 16, 10, 13), Block.box(0, 10, 11, 4, 14, 13), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        NORTH_CLOSED_BARRIER = Stream.of(
//                Stream.of(
//                        Block.box(4, 0, 11, 12, 1, 12),
//                        Block.box(4, 0, 5, 5, 1, 11),
//                        Block.box(11, 0, 5, 12, 1, 11),
//                        Block.box(4, 0, 4, 12, 1, 5),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(4, 12, 7, 5, 16, 9),
//                        Block.box(0, 17, 7, 4, 21, 9),
//                        Block.box(0, 12, 7, 4, 16, 9)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(4, 17, 7, 5, 21, 9),
//                Shapes.join(Block.box(0, 6, 11, 16, 26, 13), Block.box(0, 10, 11, 4, 30, 13), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        NORTH_CLOSED_FLIPPED = Stream.of(
//                Stream.of(
//                        Block.box(4, 0, 11, 12, 1, 12),
//                        Block.box(4, 0, 5, 5, 1, 11),
//                        Block.box(11, 0, 5, 12, 1, 11),
//                        Block.box(4, 0, 4, 12, 1, 5),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(4, 12, 7, 5, 16, 9),
//                        Block.box(0, 17, 7, 4, 21, 9),
//                        Block.box(0, 12, 7, 4, 16, 9)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(4, 17, 7, 5, 21, 9),
//                Shapes.join(Block.box(0, 6, 3, 16, 10, 5), Block.box(0, 10, 3, 4, 14, 5), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        NORTH_CLOSED_FLIPPED_BARRIER = Stream.of(
//                Stream.of(
//                        Block.box(4, 0, 11, 12, 1, 12),
//                        Block.box(4, 0, 5, 5, 1, 11),
//                        Block.box(11, 0, 5, 12, 1, 11),
//                        Block.box(4, 0, 4, 12, 1, 5),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(4, 12, 7, 5, 16, 9),
//                        Block.box(0, 17, 7, 4, 21, 9),
//                        Block.box(0, 12, 7, 4, 16, 9)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(4, 17, 7, 5, 21, 9),
//                Shapes.join(Block.box(0, 6, 3, 16, 26, 5), Block.box(0, 10, 3, 4, 30, 5), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        EAST_OPEN = Stream.of(
//                Stream.of(
//                        Block.box(4, 0, 4, 5, 1, 12),
//                        Block.box(5, 0, 4, 11, 1, 5),
//                        Block.box(5, 0, 11, 11, 1, 12),
//                        Block.box(11, 0, 4, 12, 1, 12),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(7, 12, 4, 9, 16, 5),
//                        Block.box(7, 17, 0, 9, 21, 4),
//                        Block.box(7, 12, 0, 9, 16, 4)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(7, 17, 4, 9, 21, 5),
//                Shapes.join(Block.box(3, 0, 6, 5, 16, 10), Block.box(3, 0, 2, 5, 4, 6), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        EAST_OPEN_FLIPPED = Stream.of(
//                Stream.of(
//                        Block.box(4, 0, 4, 5, 1, 12),
//                        Block.box(5, 0, 4, 11, 1, 5),
//                        Block.box(5, 0, 11, 11, 1, 12),
//                        Block.box(11, 0, 4, 12, 1, 12),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(7, 12, 4, 9, 16, 5),
//                        Block.box(7, 17, 0, 9, 21, 4),
//                        Block.box(7, 12, 0, 9, 16, 4)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(7, 17, 4, 9, 21, 5),
//                Shapes.join(Block.box(11, 0, 6, 13, 16, 10), Block.box(11, 0, 2, 13, 4, 6), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        EAST_CLOSED = Stream.of(
//                Stream.of(
//                        Block.box(4, 0, 4, 5, 1, 12),
//                        Block.box(5, 0, 4, 11, 1, 5),
//                        Block.box(5, 0, 11, 11, 1, 12),
//                        Block.box(11, 0, 4, 12, 1, 12),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(7, 12, 4, 9, 16, 5),
//                        Block.box(7, 17, 0, 9, 21, 4),
//                        Block.box(7, 12, 0, 9, 16, 4)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(7, 17, 4, 9, 21, 5),
//                Shapes.join(Block.box(3, 6, 0, 5, 10, 16), Block.box(3, 10, 0, 5, 14, 4), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        EAST_CLOSED_BARRIER = Stream.of(
//                Stream.of(
//                        Block.box(4, 0, 4, 5, 1, 12),
//                        Block.box(5, 0, 4, 11, 1, 5),
//                        Block.box(5, 0, 11, 11, 1, 12),
//                        Block.box(11, 0, 4, 12, 1, 12),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(7, 12, 4, 9, 16, 5),
//                        Block.box(7, 17, 0, 9, 21, 4),
//                        Block.box(7, 12, 0, 9, 16, 4)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(7, 17, 4, 9, 21, 5),
//                Shapes.join(Block.box(3, 6, 0, 5, 26, 16), Block.box(3, 10, 0, 5, 30, 4), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        EAST_CLOSED_FLIPPED = Stream.of(
//                Stream.of(
//                        Block.box(4, 0, 4, 5, 1, 12),
//                        Block.box(5, 0, 4, 11, 1, 5),
//                        Block.box(5, 0, 11, 11, 1, 12),
//                        Block.box(11, 0, 4, 12, 1, 12),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(7, 12, 4, 9, 16, 5),
//                        Block.box(7, 17, 0, 9, 21, 4),
//                        Block.box(7, 12, 0, 9, 16, 4)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(7, 17, 4, 9, 21, 5),
//                Shapes.join(Block.box(11, 6, 0, 13, 10, 16), Block.box(11, 10, 0, 13, 14, 4), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        EAST_CLOSED_FLIPPED_BARRIER = Stream.of(
//                Stream.of(
//                        Block.box(4, 0, 4, 5, 1, 12),
//                        Block.box(5, 0, 4, 11, 1, 5),
//                        Block.box(5, 0, 11, 11, 1, 12),
//                        Block.box(11, 0, 4, 12, 1, 12),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(7, 12, 4, 9, 16, 5),
//                        Block.box(7, 17, 0, 9, 21, 4),
//                        Block.box(7, 12, 0, 9, 16, 4)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(7, 17, 4, 9, 21, 5),
//                Shapes.join(Block.box(11, 6, 0, 13, 26, 16), Block.box(11, 10, 0, 13, 30, 4), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        SOUTH_OPEN = Stream.of(
//                Stream.of(
//                        Block.box(4, 0, 4, 12, 1, 5),
//                        Block.box(11, 0, 5, 12, 1, 11),
//                        Block.box(4, 0, 5, 5, 1, 11),
//                        Block.box(4, 0, 11, 12, 1, 12),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(11, 12, 7, 12, 16, 9),
//                        Block.box(12, 17, 7, 16, 21, 9),
//                        Block.box(12, 12, 7, 16, 16, 9)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(11, 17, 7, 12, 21, 9),
//                Shapes.join(Block.box(6, 0, 3, 10, 16, 5), Block.box(10, 0, 3, 14, 4, 5), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        SOUTH_OPEN_FLIPPED = Stream.of(
//                Stream.of(
//                        Block.box(4, 0, 4, 12, 1, 5),
//                        Block.box(11, 0, 5, 12, 1, 11),
//                        Block.box(4, 0, 5, 5, 1, 11),
//                        Block.box(4, 0, 11, 12, 1, 12),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(11, 12, 7, 12, 16, 9),
//                        Block.box(12, 17, 7, 16, 21, 9),
//                        Block.box(12, 12, 7, 16, 16, 9)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(11, 17, 7, 12, 21, 9),
//                Shapes.join(Block.box(6, 0, 11, 10, 16, 13), Block.box(10, 0, 11, 14, 4, 13), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        SOUTH_CLOSED = Stream.of(
//                Stream.of(
//                        Block.box(4, 0, 4, 12, 1, 5),
//                        Block.box(11, 0, 5, 12, 1, 11),
//                        Block.box(4, 0, 5, 5, 1, 11),
//                        Block.box(4, 0, 11, 12, 1, 12),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(11, 12, 7, 12, 16, 9),
//                        Block.box(12, 17, 7, 16, 21, 9),
//                        Block.box(12, 12, 7, 16, 16, 9)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(11, 17, 7, 12, 21, 9),
//                Shapes.join(Block.box(0, 6, 3, 16, 10, 5), Block.box(12, 10, 3, 16, 14, 5), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        SOUTH_CLOSED_BARRIER = Stream.of(
//                Stream.of(
//                        Block.box(4, 0, 4, 12, 1, 5),
//                        Block.box(11, 0, 5, 12, 1, 11),
//                        Block.box(4, 0, 5, 5, 1, 11),
//                        Block.box(4, 0, 11, 12, 1, 12),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(11, 12, 7, 12, 16, 9),
//                        Block.box(12, 17, 7, 16, 21, 9),
//                        Block.box(12, 12, 7, 16, 16, 9)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(11, 17, 7, 12, 21, 9),
//                Shapes.join(Block.box(0, 6, 3, 16, 26, 5), Block.box(12, 10, 3, 16, 30, 5), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        SOUTH_CLOSED_FLIPPED = Stream.of(
//                Stream.of(
//                        Block.box(4, 0, 4, 12, 1, 5),
//                        Block.box(11, 0, 5, 12, 1, 11),
//                        Block.box(4, 0, 5, 5, 1, 11),
//                        Block.box(4, 0, 11, 12, 1, 12),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(11, 12, 7, 12, 16, 9),
//                        Block.box(12, 17, 7, 16, 21, 9),
//                        Block.box(12, 12, 7, 16, 16, 9)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(11, 17, 7, 12, 21, 9),
//                Shapes.join(Block.box(0, 6, 11, 16, 10, 13), Block.box(12, 10, 11, 16, 14, 13), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        SOUTH_CLOSED_FLIPPED_BARRIER = Stream.of(
//                Stream.of(
//                        Block.box(4, 0, 4, 12, 1, 5),
//                        Block.box(11, 0, 5, 12, 1, 11),
//                        Block.box(4, 0, 5, 5, 1, 11),
//                        Block.box(4, 0, 11, 12, 1, 12),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(11, 12, 7, 12, 16, 9),
//                        Block.box(12, 17, 7, 16, 21, 9),
//                        Block.box(12, 12, 7, 16, 16, 9)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(11, 17, 7, 12, 21, 9),
//                Shapes.join(Block.box(0, 6, 11, 16, 26, 13), Block.box(12, 10, 11, 16, 30, 13), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        WEST_OPEN = Stream.of(
//                Stream.of(
//                        Block.box(11, 0, 4, 12, 1, 12),
//                        Block.box(5, 0, 11, 11, 1, 12),
//                        Block.box(5, 0, 4, 11, 1, 5),
//                        Block.box(4, 0, 4, 5, 1, 12),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(7, 12, 11, 9, 16, 12),
//                        Block.box(7, 17, 12, 9, 21, 16),
//                        Block.box(7, 12, 12, 9, 16, 16)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(7, 17, 11, 9, 21, 12),
//                Shapes.join(Block.box(11, 0, 6, 13, 16, 10), Block.box(11, 0, 10, 13, 4, 14), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        WEST_OPEN_FLIPPED = Stream.of(
//                Stream.of(
//                        Block.box(11, 0, 4, 12, 1, 12),
//                        Block.box(5, 0, 11, 11, 1, 12),
//                        Block.box(5, 0, 4, 11, 1, 5),
//                        Block.box(4, 0, 4, 5, 1, 12),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(7, 12, 11, 9, 16, 12),
//                        Block.box(7, 17, 12, 9, 21, 16),
//                        Block.box(7, 12, 12, 9, 16, 16)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(7, 17, 11, 9, 21, 12),
//                Shapes.join(Block.box(3, 0, 6, 5, 16, 10), Block.box(3, 0, 10, 5, 4, 14), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        WEST_CLOSED = Stream.of(
//                Stream.of(
//                        Block.box(11, 0, 4, 12, 1, 12),
//                        Block.box(5, 0, 11, 11, 1, 12),
//                        Block.box(5, 0, 4, 11, 1, 5),
//                        Block.box(4, 0, 4, 5, 1, 12),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(7, 12, 11, 9, 16, 12),
//                        Block.box(7, 17, 12, 9, 21, 16),
//                        Block.box(7, 12, 12, 9, 16, 16)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(7, 17, 11, 9, 21, 12),
//                Shapes.join(Block.box(11, 6, 0, 13, 10, 16), Block.box(11, 10, 12, 13, 14, 16), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        WEST_CLOSED_BARRIER = Stream.of(
//                Stream.of(
//                        Block.box(11, 0, 4, 12, 1, 12),
//                        Block.box(5, 0, 11, 11, 1, 12),
//                        Block.box(5, 0, 4, 11, 1, 5),
//                        Block.box(4, 0, 4, 5, 1, 12),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(7, 12, 11, 9, 16, 12),
//                        Block.box(7, 17, 12, 9, 21, 16),
//                        Block.box(7, 12, 12, 9, 16, 16)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(7, 17, 11, 9, 21, 12),
//                Shapes.join(Block.box(11, 6, 0, 13, 26, 16), Block.box(11, 10, 12, 13, 30, 16), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        WEST_CLOSED_FLIPPED = Stream.of(
//                Stream.of(
//                        Block.box(11, 0, 4, 12, 1, 12),
//                        Block.box(5, 0, 11, 11, 1, 12),
//                        Block.box(5, 0, 4, 11, 1, 5),
//                        Block.box(4, 0, 4, 5, 1, 12),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(7, 12, 11, 9, 16, 12),
//                        Block.box(7, 17, 12, 9, 21, 16),
//                        Block.box(7, 12, 12, 9, 16, 16)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(7, 17, 11, 9, 21, 12),
//                Shapes.join(Block.box(3, 6, 0, 5, 10, 16), Block.box(3, 10, 12, 5, 14, 16), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//
//        WEST_CLOSED_FLIPPED_BARRIER = Stream.of(
//                Stream.of(
//                        Block.box(11, 0, 4, 12, 1, 12),
//                        Block.box(5, 0, 11, 11, 1, 12),
//                        Block.box(5, 0, 4, 11, 1, 5),
//                        Block.box(4, 0, 4, 5, 1, 12),
//                        Block.box(5, 1, 5, 11, 12, 11),
//                        Block.box(5, 12, 5, 11, 23, 11),
//                        Block.box(7, 12, 11, 9, 16, 12),
//                        Block.box(7, 17, 12, 9, 21, 16),
//                        Block.box(7, 12, 12, 9, 16, 16)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
//                Block.box(7, 17, 11, 9, 21, 12),
//                Shapes.join(Block.box(3, 6, 0, 5, 26, 16), Block.box(3, 10, 12, 5, 30, 16), BooleanOp.OR))
//                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
//    }

    public static boolean isArmExtender(BlockState state) {
        return AllBlocks.ARM_EXTENDER.has(state);
    }

}
