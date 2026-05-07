package com.tiestoettoet.create_train_parts.content.decoration.slidingWindow;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.IHaveBigOutline;
import com.tiestoettoet.create_train_parts.AllBlockEntityTypes;
import com.tiestoettoet.create_train_parts.AllBlocks;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public class SlidingWindowBlock extends HorizontalDirectionalBlock
        implements IBE<SlidingWindowBlockEntity>, IHaveBigOutline, IWrenchable {
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final BooleanProperty VISIBLE = BooleanProperty.create("visible");

    public static final EnumProperty<SlidingWindowBlockEntity.SelectionMode> MODE = EnumProperty.create("mode",
            SlidingWindowBlockEntity.SelectionMode.class);

    protected static final VoxelShape NORTH_OPEN_UP;
    protected static final VoxelShape NORTH_OPEN_DOWN;
    protected static final VoxelShape NORTH_OPEN_LEFT;
    protected static final VoxelShape NORTH_OPEN_RIGHT;
    protected static final VoxelShape NORTH_CLOSED;
    protected static final VoxelShape SOUTH_OPEN_UP;
    protected static final VoxelShape SOUTH_OPEN_DOWN;
    protected static final VoxelShape SOUTH_OPEN_LEFT;
    protected static final VoxelShape SOUTH_OPEN_RIGHT;

    protected static final VoxelShape SOUTH_CLOSED;
    protected static final VoxelShape EAST_OPEN_UP;
    protected static final VoxelShape EAST_OPEN_DOWN;
    protected static final VoxelShape EAST_OPEN_LEFT;
    protected static final VoxelShape EAST_OPEN_RIGHT;
    protected static final VoxelShape EAST_CLOSED;
    protected static final VoxelShape WEST_OPEN_UP;
    protected static final VoxelShape WEST_OPEN_DOWN;
    protected static final VoxelShape WEST_OPEN_LEFT;
    protected static final VoxelShape WEST_OPEN_RIGHT;
    protected static final VoxelShape WEST_CLOSED;

    private final BlockSetType type = BlockSetType.OAK;

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    public SlidingWindowBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Use MODE from blockstate directly - block entity may not exist (e.g., in
        // contraptions)
        SlidingWindowBlockEntity.SelectionMode mode = state.getValue(MODE);
        switch (state.getValue(FACING)) {
            case NORTH -> {
                if (state.getValue(OPEN)) {
                    switch (mode) {
                        case UP -> {
                            return NORTH_OPEN_UP;
                        }
                        case DOWN -> {
                            return NORTH_OPEN_DOWN;
                        }
                        case LEFT -> {
                            return NORTH_OPEN_LEFT;
                        }
                        case RIGHT -> {
                            return NORTH_OPEN_RIGHT;
                        }
                    }
                } else {
                    return NORTH_CLOSED;

                }
            }
            case EAST -> {
                if (state.getValue(OPEN)) {
                    switch (mode) {
                        case UP -> {
                            return EAST_OPEN_UP;
                        }
                        case DOWN -> {
                            return EAST_OPEN_DOWN;
                        }
                        case LEFT -> {
                            return EAST_OPEN_LEFT;
                        }
                        case RIGHT -> {
                            return EAST_OPEN_RIGHT;
                        }
                    }
                } else {
                    return EAST_CLOSED;
                }

            }
            case SOUTH -> {
                if (state.getValue(OPEN)) {
                    switch (mode) {
                        case UP -> {
                            return SOUTH_OPEN_UP;
                        }
                        case DOWN -> {
                            return SOUTH_OPEN_DOWN;
                        }
                        case LEFT -> {
                            return SOUTH_OPEN_LEFT;
                        }
                        case RIGHT -> {
                            return SOUTH_OPEN_RIGHT;
                        }
                    }
                } else {
                    return SOUTH_CLOSED;
                }

            }
            case WEST -> {
                if (state.getValue(OPEN)) {
                    switch (mode) {
                        case UP -> {
                            return WEST_OPEN_UP;
                        }
                        case DOWN -> {
                            return WEST_OPEN_DOWN;
                        }
                        case LEFT -> {
                            return WEST_OPEN_LEFT;
                        }
                        case RIGHT -> {
                            return WEST_OPEN_RIGHT;
                        }
                    }
                } else {
                    return WEST_CLOSED;
                }

            }
        }
        ;
        return NORTH_CLOSED;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        // if (!level.isClientSide) {
        // BlockEntity blockEntity = level.getBlockEntity(pos);
        // if (blockEntity instanceof SlidingWindowBlockEntity slidingWindowBlockEntity)
        // {
        // slidingWindowBlockEntity.setNeighborState(state);
        // }
        // }
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getShape(state, level, pos, CollisionContext.empty());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos(); // Retrieve the BlockPos
        BlockState state = pContext.getLevel().getBlockState(pos);
        Direction facing = pContext.getHorizontalDirection().getOpposite();
        Level level = pContext.getLevel();

        BlockState stateForPlacement = super.getStateForPlacement(pContext);

        boolean open = false;
        BlockState leftState = level.getBlockState(pos.relative(facing.getCounterClockWise()));
        BlockState rightState = level.getBlockState(pos.relative(facing.getClockWise()));

        if (leftState.getBlock() instanceof SlidingWindowBlock && leftState.hasProperty(OPEN)
                && leftState.getValue(OPEN)) {
            open = true;
        }
        if (rightState.getBlock() instanceof SlidingWindowBlock && rightState.hasProperty(OPEN)
                && rightState.getValue(OPEN)) {
            open = true;
        }

        if (stateForPlacement != null && stateForPlacement.getValue(OPEN)) {
            // System.out.println(state);
            return stateForPlacement.setValue(OPEN, open)
                    .setValue(VISIBLE, !open)
                    .setValue(POWERED, open)
                    .setValue(FACING, facing)
                    .setValue(MODE, SlidingWindowBlockEntity.SelectionMode.UP);

        }

        return stateForPlacement;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return switch (pathComputationType) {
            case LAND, AIR -> (Boolean) state.getValue(OPEN);
            default -> false;
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        toggle(state, level, pos, player, null);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion,
            BiConsumer<ItemStack, BlockPos> dropConsumer) {
        if (explosion.canTriggerBlocks() && this.type.canOpenByWindCharge() && !(Boolean) state.getValue(POWERED)) {
            this.toggle(state, level, pos, (Player) null, null);
        }

        super.onExplosionHit(state, level, pos, explosion, dropConsumer);
    }

    public void toggle(BlockState state, Level level, BlockPos pos, @Nullable Player player,
            Boolean open) {
        toggle(state, level, pos, player, open, 10);
    }

    public void toggle(BlockState state, Level level, BlockPos pos, @Nullable Player player,
            Boolean open, int flags) {
        // state = state.cycle(OPEN);
        // // level.setBlock(pos, blockstate, 2);
        // if (open == null)
        // open = state.cycle(OPEN).getValue(OPEN);
        // // if (!open)
        // // state = state.setValue(VISIBLE, true);
        // Direction facing = state.getValue(FACING);
        // BlockEntity blockEntity = level.getBlockEntity(pos);

        if (open == null) {
            open = !state.getValue(OPEN);
        }

        // Update the current block's state
        state = state.setValue(OPEN, open).setValue(POWERED, open);
        if (open) {
            state = state.setValue(VISIBLE, false);
        }
        level.setBlock(pos, state, flags);
        level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);

        // SlidingWindowBlockEntity.SelectionMode mode = blockEntity instanceof
        // SlidingWindowBlockEntity
        // ? ((SlidingWindowBlockEntity) blockEntity).getMode() :
        // SlidingWindowBlockEntity.SelectionMode.UP;
        // if (mode == SlidingWindowBlockEntity.SelectionMode.UP || mode ==
        // SlidingWindowBlockEntity.SelectionMode.DOWN) {
        // BlockState otherWindowLeftState = null;
        // BlockState otherWindowRightState = null;
        // BlockPos otherWindowLeft = null;
        // BlockPos otherWindowRight = null;
        //
        // // System.out.println("Toggle called with state: " + state + ", pos: " + pos
        // +
        // // ", player: " + player
        // // + ", ignore: " + ignore + ", open: " + open + ", flags: " + flags);
        // otherWindowLeftState = (BlockState) getNeighbors(level,
        // pos).get("left").get("state");
        // otherWindowRightState = (BlockState) getNeighbors(level,
        // pos).get("right").get("state");
        //
        // if (otherWindowLeftState.getBlock() instanceof SlidingWindowBlock
        // && otherWindowLeftState.getValue(FACING) == facing
        // && !ignore.equals("left") && mode == otherWindowLeftState.getValue(MODE)) {
        // otherWindowLeftState = otherWindowLeftState.setValue(OPEN,
        // open).setValue(POWERED, state.getValue(POWERED));
        // otherWindowLeft = (BlockPos) getNeighbors(level, pos).get("left").get("pos");
        // toggle(otherWindowLeftState, level, otherWindowLeft, player, "right", open,
        // flags);
        // }
        // if (otherWindowRightState.getBlock() instanceof SlidingWindowBlock
        // && otherWindowRightState.getValue(FACING) == facing &&
        // !ignore.equals("right") && mode == otherWindowRightState.getValue(MODE)) {
        // otherWindowRightState = otherWindowRightState.setValue(OPEN,
        // open).setValue(POWERED,
        // state.getValue(POWERED));
        // otherWindowRight = (BlockPos) getNeighbors(level,
        // pos).get("right").get("pos");
        // toggle(otherWindowRightState, level, otherWindowRight, player, "left", open,
        // flags);
        // }
        // } else if (mode == SlidingWindowBlockEntity.SelectionMode.LEFT
        // || mode == SlidingWindowBlockEntity.SelectionMode.RIGHT) {
        // BlockState otherWindowUpState = null;
        // BlockState otherWindowDownState = null;
        // BlockPos otherWindowUp = null;
        // BlockPos otherWindowDown = null;
        // // System.out.println("Toggle called with state: " + state + ", pos: " + pos
        // +
        // // ", player: " + player
        // // + ", ignore: " + ignore + ", open: " + open + ", flags: " + flags);
        // otherWindowUpState = (BlockState) getNeighbors(level,
        // pos).get("up").get("state");
        // otherWindowDownState = (BlockState) getNeighbors(level,
        // pos).get("down").get("state");
        // if (otherWindowUpState.getBlock() instanceof SlidingWindowBlock
        // && otherWindowUpState.getValue(FACING) == facing
        // && !ignore.equals("up") && mode == otherWindowUpState.getValue(MODE)) {
        // otherWindowUpState = otherWindowUpState.setValue(OPEN,
        // open).setValue(POWERED, state.getValue(POWERED));
        // otherWindowUp = (BlockPos) getNeighbors(level, pos).get("up").get("pos");
        // toggle(otherWindowUpState, level, otherWindowUp, player, "down", open,
        // flags);
        // }
        // if (otherWindowDownState.getBlock() instanceof SlidingWindowBlock
        // && otherWindowDownState.getValue(FACING) == facing && !ignore.equals("down")
        // && mode == otherWindowDownState.getValue(MODE)) {
        // otherWindowDownState = otherWindowDownState.setValue(OPEN,
        // open).setValue(POWERED,
        // state.getValue(POWERED));
        // otherWindowDown = (BlockPos) getNeighbors(level, pos).get("down").get("pos");
        // toggle(otherWindowDownState, level, otherWindowDown, player, "up", open,
        // flags);
        // }
        // }

        List<Map<String, Object>> windows = getConnectedWindows(state, level, pos, player);
        for (Map<String, Object> window : windows) {
            BlockState windowState = (BlockState) window.get("state");
            BlockPos windowPos = (BlockPos) window.get("pos");

            // Update the connected window's state
            windowState = windowState.setValue(OPEN, open).setValue(POWERED, open);
            if (open) {
                windowState = windowState.setValue(VISIBLE, false);
            }
            level.setBlock(windowPos, windowState, flags);
            level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, windowPos);
        }

        // if (state.getValue(OPEN))
        // state = state.setValue(VISIBLE, false);
        // // else
        // // state = state.setValue(VISIBLE, true);
        // level.setBlock(pos, state, flags);
        // level.gameEvent(player, state.getValue(OPEN) ? GameEvent.BLOCK_OPEN :
        // GameEvent.BLOCK_CLOSE, pos);

    }

    public void setOpen(@Nullable Entity entity, Level level, BlockState state, BlockPos pos, boolean open) {
        if (!state.is(this))
            return;
        if (state.getValue(OPEN) == open)
            return;
        BlockState changedState = state.setValue(OPEN, open);
        if (open)
            changedState = changedState.setValue(VISIBLE, false);
        level.setBlock(pos, changedState, 10);

        level.gameEvent(entity, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);

    }

    private List<Map<String, Object>> getConnectedWindows(BlockState state, Level level, BlockPos pos,
            @Nullable Player player) {
        Queue<BlockPos> frontier = new LinkedList<>();
        List<Map<String, Object>> connectedWindows = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();

        frontier.add(pos);

        Direction.Axis axis = state.getValue(SlidingWindowBlock.FACING).getAxis();
        SlidingWindowBlockEntity.SelectionMode mode = state.getValue(SlidingWindowBlock.MODE);

        while (!frontier.isEmpty()) {
            BlockPos currentPos = frontier.poll();
            if (visited.contains(currentPos))
                continue;
            visited.add(currentPos);

            BlockState currentState = level.getBlockState(currentPos);
            if (!(currentState.getBlock() instanceof SlidingWindowBlock))
                continue;

            if (!SlidingWindowBlock.sameKind(state, currentState))
                continue;

            if (currentState.getValue(SlidingWindowBlock.FACING).getAxis() != axis)
                continue;

            if (currentState.getValue(SlidingWindowBlock.MODE) != mode)
                continue;

            Map<String, Object> windowData = new HashMap<>();
            windowData.put("state", currentState);
            windowData.put("pos", currentPos);
            windowData.put("player", player);
            connectedWindows.add(windowData);

            for (Direction direction : Iterate.directions) {
                BlockPos neighborPos = currentPos.relative(direction);
                if (visited.contains(neighborPos))
                    continue;
                if (!level.isLoaded(neighborPos))
                    continue;

                BlockState neighborState = level.getBlockState(neighborPos);
                if (!AllBlocks.GLASS_SLIDING_WINDOW.has(neighborState) &&
                        !AllBlocks.ANDESITE_SLIDING_WINDOW.has(neighborState) &&
                        !AllBlocks.BRASS_SLIDING_WINDOW.has(neighborState) &&
                        !AllBlocks.COPPER_SLIDING_WINDOW.has(neighborState) &&
                        !AllBlocks.TRAIN_SLIDING_WINDOW.has(neighborState))
                    continue;

                frontier.add(neighborPos);
            }
        }
        // System.out.println(connectedWindows);
        return connectedWindows;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean isMoving) {
        if (!(state.getBlock() instanceof SlidingWindowBlock))
            return;

        SlidingWindowBlockEntity blockEntity = (SlidingWindowBlockEntity) level.getBlockEntity(pos);
        boolean isPowered = level.hasNeighborSignal(pos);
        boolean manuallyClosed = blockEntity != null && blockEntity.isManuallyClosed();

        // Determine shouldOpen
        boolean shouldOpen;
        if (isPowered) {
            shouldOpen = !manuallyClosed; // Open if powered and not manually closed
        } else {
            shouldOpen = false; // Close if not powered
        }

        toggle(state, level, pos, null, shouldOpen, 2);

        // TrainSlideType type = state.getValue(TYPE);

        // BlockState leftState =
        // level.getBlockState(pos.relative(state.getValue(FACING).getCounterClockWise()));
        // BlockState rightState =
        // level.getBlockState(pos.relative(state.getValue(FACING).getClockWise()));
        // BlockEntity blockEntity = level.getBlockEntity(pos);
        // SlidingWindowBlockEntity.SelectionMode mode = blockEntity instanceof
        // SlidingWindowBlockEntity
        // ? ((SlidingWindowBlockEntity) blockEntity).getMode()
        // : SlidingWindowBlockEntity.SelectionMode.UP;
        // if (mode == SlidingWindowBlockEntity.SelectionMode.UP || mode ==
        // SlidingWindowBlockEntity.SelectionMode.DOWN) {
        // if (blockEntity instanceof SlidingWindowBlockEntity slidingWindowBlockEntity)
        // {
        // Map<String, Map<String, Object>> neighborStates = getNeighbors(level, pos);
        // @Nullable
        // BlockState oldLeftState = (BlockState)
        // neighborStates.get("left").get("state");
        // @Nullable
        // BlockState oldRightState = (BlockState)
        // neighborStates.get("right").get("state");
        // if ((oldLeftState != null && leftState.getBlock() != oldLeftState.getBlock())
        // || (oldRightState != null && rightState.getBlock() !=
        // oldRightState.getBlock())) {
        // // System.out.println("Neighbor changed detected: Left State: " +
        // // leftState.getBlock() + ", Right State: " + rightState.getBlock() + ", Old
        // // Left State: " + oldLeftState.getBlock() + ", Old Right State: " +
        // // oldRightState.getBlock());
        // state = getState(state, pos, level, state.getValue(FACING),
        // "neighborChanged");
        // state = state.setValue(POWERED, powered)
        // .setValue(OPEN, open);
        //
        // level.setBlock(pos, state, 2);
        //
        // } else {
        // level.setBlock(pos, state, 2); // Update the block state without changing
        // OPEN or POWERED
        // }
        // } else {
        // level.setBlock(pos, state, 2); // Update the block state without changing
        // OPEN or POWERED
        // }
        // } else if (mode == SlidingWindowBlockEntity.SelectionMode.LEFT || mode ==
        // SlidingWindowBlockEntity.SelectionMode.RIGHT) {
        // if (blockEntity instanceof SlidingWindowBlockEntity slidingWindowBlockEntity)
        // {
        // Map<String, Map<String, Object>> neighborStates = getNeighbors(level, pos);
        // @Nullable
        // BlockState oldUpState = (BlockState) neighborStates.get("up").get("state");
        // @Nullable
        // BlockState oldDownState = (BlockState)
        // neighborStates.get("down").get("state");
        // if ((oldUpState != null && leftState.getBlock() != oldUpState.getBlock())
        // || (oldDownState != null && rightState.getBlock() !=
        // oldDownState.getBlock())) {
        // // System.out.println("Neighbor changed detected: Left State: " +
        // // leftState.getBlock() + ", Right State: " + rightState.getBlock() + ", Old
        // // Left State: " + oldLeftState.getBlock() + ", Old Right State: " +
        // // oldRightState.getBlock());
        // state = getState(state, pos, level, state.getValue(FACING),
        // "neighborChanged");
        // state = state.setValue(POWERED, powered)
        // .setValue(OPEN, open);
        //
        // level.setBlock(pos, state, 2);
        //
        // } else {
        // level.setBlock(pos, state, 2); // Update the block state without changing
        // OPEN or POWERED
        // }
        // }
        // }
        //
        // if (isPowered != powered) {
        // state = state.setValue(POWERED, isPowered);
        // state = state.setValue(OPEN, !isPowered);
        // toggle(state, level, pos, null, null, !isPowered, 2);
        // } else {
        // level.setBlock(pos, state, 2); // Update the block state without changing
        // OPEN or POWERED
        // }

        List<Map<String, Object>> windows = getConnectedWindows(state, level, pos, null);
        for (Map<String, Object> window : windows) {
            BlockState windowState = (BlockState) window.get("state");
            BlockPos windowPos = (BlockPos) window.get("pos");
            if (windowState.getValue(FACING) == state.getValue(FACING)
                    && SlidingWindowBlock.sameKind(state, windowState)) {
                boolean windowPowered = isWindowPowered(level, windowPos, windowState);
                if (windowPowered != windowState.getValue(POWERED)) {
                    windowState = windowState.setValue(POWERED, windowPowered);
                    windowState = windowState.setValue(OPEN, !windowPowered);
                    level.setBlock(windowPos, windowState, 2);
                    level.gameEvent(null, !windowPowered ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, windowPos);
                } else {
                    level.setBlock(windowPos, windowState, 2); // Update the block state without changing OPEN or
                                                               // POWERED
                }
            }
        }

        // state = state.setValue(POWERED, isPowered)
        // .setValue(OPEN, !isPowered);
        // level.setBlock(pos, state, 2);

    }

    public static Map<String, Map<String, Object>> getNeighbors(Level level, BlockPos pos) {
        Map<String, Map<String, Object>> neighbors = new HashMap<>();
        Map<String, Object> leftData = new HashMap<>();
        Map<String, Object> rightData = new HashMap<>();
        Map<String, Object> upData = new HashMap<>();
        Map<String, Object> downData = new HashMap<>();
        Direction facing = level.getBlockState(pos).getValue(FACING);
        BlockState leftState;
        BlockState rightState;
        BlockPos left;
        BlockPos right;
        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            // look in x axis
            int x = pos.getX();
            left = new BlockPos(facing == Direction.NORTH ? x + 1 : x - 1, pos.getY(), pos.getZ());
            right = new BlockPos(facing == Direction.NORTH ? x - 1 : x + 1, pos.getY(), pos.getZ());
            leftState = level.getBlockState(left);
            rightState = level.getBlockState(right);
        } else {
            // look in z axis
            int z = pos.getZ();
            left = new BlockPos(pos.getX(), pos.getY(), facing == Direction.EAST ? z + 1 : z - 1);
            right = new BlockPos(pos.getX(), pos.getY(), facing == Direction.EAST ? z - 1 : z + 1);
            leftState = level.getBlockState(left);
            rightState = level.getBlockState(right);
        }
        BlockState upState = level.getBlockState(pos.above());
        BlockState downState = level.getBlockState(pos.below());
        BlockPos up = pos.above();
        BlockPos down = pos.below();
        leftData.put("state", leftState);
        leftData.put("pos", left);
        rightData.put("state", rightState);
        rightData.put("pos", right);
        upData.put("state", upState);
        upData.put("pos", up);
        downData.put("state", downState);
        downData.put("pos", down);
        neighbors.put("left", leftData);
        neighbors.put("right", rightData);
        neighbors.put("up", upData);
        neighbors.put("down", downData);

        return neighbors;
        // json structure:
        // {
        // "left": {
        // "state": <BlockState>,
        // "pos": <BlockPos>
        // },
        // "right": {
        // "state": <BlockState>,
        // "pos": <BlockPos>
        // }

    }

    public static BlockState getState(BlockState state, BlockPos pos, Level level, Direction facing, String function) {
        // System.out.println("getState called from " + function + " with pos: " + pos +
        // " and facing: " + facing);
        // System.out.println("Type: " + state.getValue(TYPE));
        BlockState finalState;
        if (state.getBlock() instanceof SlidingWindowBlock) {
            finalState = state.setValue(FACING, facing)
                    .setValue(OPEN, false)
                    .setValue(VISIBLE, true);
        } else {
            finalState = state;

        }
        if (finalState.getValue(OPEN))
            finalState = finalState.setValue(VISIBLE, false);
        // else
        // finalState = finalState.setValue(VISIBLE, true);
        return finalState;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState other, Direction side) {
        // if (isConnected(state, other, side))
        // System.out.println("Skipping rendering for " + state + " and " + other + " on
        // side " + side);
        return isConnected(state, other, side);
    }

    public static boolean isConnected(BlockState state, BlockState other, Direction direction) {
        if (state.getBlock() instanceof SlidingWindowBlock && other.getBlock() instanceof SlidingWindowBlock) {
            SlidingWindowBlockEntity.SelectionMode mode = state.getValue(MODE);
            SlidingWindowBlockEntity.SelectionMode otherMode = other.getValue(MODE);
            Direction facing = (Direction) state.getValue(FACING);
            Direction otherFacing = (Direction) other.getValue(FACING);
            return facing == otherFacing && mode == otherMode;
        } else {
            return false;
        }

    }

    public static boolean sameKind(BlockState state1, BlockState state2) {
        return state1.getBlock() == state2.getBlock();
    }

    public static boolean isWindowPowered(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        BlockPos leftPos = pos.relative(facing.getCounterClockWise());
        BlockPos rightPos = pos.relative(facing.getClockWise());

        // Check if the current block or its neighbors are powered
        return level.hasNeighborSignal(pos) ||
                level.hasNeighborSignal(leftPos) ||
                level.hasNeighborSignal(rightPos);
    }

    public static boolean isSlideOpen(BlockState state) {
        return state.getValue(OPEN);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, POWERED, VISIBLE, MODE);
    }

    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
            BlockPos currentPos, BlockPos facingPos) {

        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(VISIBLE) ? RenderShape.MODEL : RenderShape.ENTITYBLOCK_ANIMATED;
        // RenderShape.ENTITYBLOCK_ANIMATED;
    }

    static {
        NORTH_OPEN_UP = Block.box(0, 15, 15.9, 16, 31, 18.9);
        NORTH_OPEN_DOWN = Block.box(0, -15, 15.9, 16, 1, 18.9);
        NORTH_OPEN_LEFT = Block.box(15, 0, 15.9, 31, 16, 18.9);
        NORTH_OPEN_RIGHT = Block.box(-15, 0, 15.9, 1, 16, 18.9);
        NORTH_CLOSED = Block.box(0, 0, 13, 16, 16, 16);

        SOUTH_OPEN_UP = Block.box(0, 15, -2.9, 16, 31, 0.1);
        SOUTH_OPEN_DOWN = Block.box(0, -15, -2.9, 16, 1, 0.1);
        SOUTH_OPEN_LEFT = Block.box(-15, 0, -2.9, 1, 16, 0.1);
        SOUTH_OPEN_RIGHT = Block.box(15, 0, -2.9, 31, 16, 0.1);
        SOUTH_CLOSED = Block.box(0, 0, 0, 16, 16, 3);

        EAST_OPEN_UP = Block.box(-2.9, 15, 0, 0.1, 31, 16);
        EAST_OPEN_DOWN = Block.box(-2.9, -15, 0, 0.1, 1, 16);
        EAST_OPEN_LEFT = Block.box(-2.9, 0, 15, 0.1, 16, 31);
        EAST_OPEN_RIGHT = Block.box(-2.9, 0, -15, 0.1, 16, 1);
        EAST_CLOSED = Block.box(0, 0, 0, 3, 16, 16);

        WEST_OPEN_UP = Block.box(15.9, 15, 0, 18.9, 31, 16);
        WEST_OPEN_DOWN = Block.box(15.9, -15, 0, 18.9, 1, 16);
        WEST_OPEN_LEFT = Block.box(15.9, 0, -15, 18.9, 16, 1);
        WEST_OPEN_RIGHT = Block.box(15.9, 0, 15, 18.9, 16, 31);
        WEST_CLOSED = Block.box(13, 0, 0, 16, 16, 16);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return IBE.super.newBlockEntity(pos, state);
    }

    @Override
    public Class<SlidingWindowBlockEntity> getBlockEntityClass() {
        return SlidingWindowBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SlidingWindowBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.SLIDING_WINDOW.get();
    }
}
