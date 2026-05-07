package com.tiestoettoet.create_train_parts.content.decoration.trainSlide;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IHaveBigOutline;
import com.tiestoettoet.create_train_parts.AllBlockEntityTypes;
import com.tiestoettoet.create_train_parts.AllBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
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
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LeverBlock;
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
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import com.simibubi.create.foundation.block.IBE;

public class TrainSlideBlock extends HorizontalDirectionalBlock implements IBE<TrainSlideBlockEntity>, IHaveBigOutline, IWrenchable {

    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final BooleanProperty VISIBLE = BooleanProperty.create("visible");

    public static final EnumProperty<ConnectedState> CONNECTED = EnumProperty.create("connected", ConnectedState.class);

    // public static final BooleanProperty VISIBLE =
    // BooleanProperty.create("visible");

    protected static final VoxelShape NORTH_OPEN_NONE;
    protected static final VoxelShape NORTH_OPEN_RIGHT;
    protected static final VoxelShape NORTH_OPEN_LEFT;
    protected static final VoxelShape NORTH_OPEN_BOTH;
    protected static final VoxelShape SOUTH_OPEN_NONE;
    protected static final VoxelShape SOUTH_OPEN_RIGHT;
    protected static final VoxelShape SOUTH_OPEN_LEFT;
    protected static final VoxelShape SOUTH_OPEN_BOTH;
    protected static final VoxelShape WEST_OPEN_NONE;
    protected static final VoxelShape WEST_OPEN_RIGHT;
    protected static final VoxelShape WEST_OPEN_LEFT;
    protected static final VoxelShape WEST_OPEN_BOTH;
    protected static final VoxelShape EAST_OPEN_NONE;
    protected static final VoxelShape EAST_OPEN_RIGHT;
    protected static final VoxelShape EAST_OPEN_LEFT;
    protected static final VoxelShape EAST_OPEN_BOTH;
    protected static final VoxelShape CLOSED;



    private final BlockSetType type = BlockSetType.OAK;

    public static final EnumProperty<TrainSlideType> TYPE = EnumProperty.create("type", TrainSlideType.class);

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    public TrainSlideBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        TrainSlideBlockEntity trainSlideBlockEntity = (TrainSlideBlockEntity) level.getBlockEntity(pos);
//        if (trainSlideBlockEntity != null) {
//            float value = trainSlideBlockEntity.animation.getValue(partialTicks);
//        }
        if (!(Boolean) state.getValue(OPEN)) {
            return CLOSED;
        } else {
            return switch (state.getValue(CONNECTED)) {
                case NONE -> switch (state.getValue(FACING)) {
                    default -> NORTH_OPEN_NONE;
                    case SOUTH -> SOUTH_OPEN_NONE;
                    case WEST -> WEST_OPEN_NONE;
                    case EAST -> EAST_OPEN_NONE;
                };
                case LEFT -> switch (state.getValue(FACING)) {
                    default -> NORTH_OPEN_LEFT;
                    case SOUTH -> SOUTH_OPEN_LEFT;
                    case WEST -> WEST_OPEN_LEFT;
                    case EAST -> EAST_OPEN_LEFT;
                };
                case RIGHT -> switch (state.getValue(FACING)) {
                    default -> NORTH_OPEN_RIGHT;
                    case SOUTH -> SOUTH_OPEN_RIGHT;
                    case WEST -> WEST_OPEN_RIGHT;
                    case EAST -> EAST_OPEN_RIGHT;
                };
                case BOTH -> switch (state.getValue(FACING)) {
                    default -> NORTH_OPEN_BOTH;
                    case SOUTH -> SOUTH_OPEN_BOTH;
                    case WEST -> WEST_OPEN_BOTH;
                    case EAST -> EAST_OPEN_BOTH;
                };
            };
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TrainSlideBlockEntity trainSlideBlockEntity) {
                trainSlideBlockEntity.setNeighborState(state);
            }
        }
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
        String function = "getStateForPlacement";


        // set the correct type

        TrainSlideType type = determineType(pContext.getItemInHand());

//        System.out.println("Determined TrainSlideType: " + type);

        // state = getState(state, pos, level, facing, function);

        BlockState stateForPlacement = super.getStateForPlacement(pContext);
        // get neighbour block of where to place

        boolean open = false;
        BlockState leftState = level.getBlockState(pos.relative(facing.getCounterClockWise()));
        BlockState rightState = level.getBlockState(pos.relative(facing.getClockWise()));

        if (leftState.getBlock() instanceof TrainSlideBlock && leftState.hasProperty(OPEN) && leftState.getValue(OPEN)) {
            open = true;
        }
        if (rightState.getBlock() instanceof TrainSlideBlock && rightState.hasProperty(OPEN)
                && rightState.getValue(OPEN)) {
            open = true;
        }

//        System.out.println("Left Block: " + leftState.getBlock() + ", OPEN: "
//                + (leftState.hasProperty(OPEN) ? leftState.getValue(OPEN) : "N/A"));
//        System.out.println("Right Block: " + rightState.getBlock() + ", OPEN: "
//                + (rightState.hasProperty(OPEN) ? rightState.getValue(OPEN) : "N/A"));

        Level levelForPlacement = pContext.getLevel();
        BlockEntity blockEntity = levelForPlacement.getBlockEntity(pos);
        if (blockEntity instanceof TrainSlideBlockEntity trainSlideBlockEntity) {
            trainSlideBlockEntity.setTrainSlideType(type); // Set the type in the block entity
        }


        if (stateForPlacement != null && stateForPlacement.getValue(OPEN)) {
            stateForPlacement = stateForPlacement.setValue(TYPE, type);
            state = getState(stateForPlacement, pos, level, facing, function);
//            System.out.println(state);
            return stateForPlacement.setValue(OPEN, open)
                    .setValue(VISIBLE, !open)
                    .setValue(POWERED, open)
                    .setValue(FACING, facing)
                    .setValue(CONNECTED, state.getValue(CONNECTED))
                    .setValue(TYPE, type);
        }

        return stateForPlacement;
    }

    private TrainSlideType determineType(ItemStack item) {
        // Example logic to determine type based on the item
//        System.out.println("Determining TrainSlideType for item: " + item.getItem());
//        System.out.println("Brass Item: " + AllBlocks.TRAIN_SLIDE_BRASS.asItem());
        if (item.getItem() == AllBlocks.TRAIN_SLIDE_ANDESITE.asItem()) {
            return TrainSlideType.ANDESITE;
        } else if (item.getItem() == AllBlocks.TRAIN_SLIDE_BRASS.asItem()) {
            return TrainSlideType.BRASS;
        } else if (item.getItem() == AllBlocks.TRAIN_SLIDE_COPPER.asItem()) {
            return TrainSlideType.COPPER;
        }
        else if (item.getItem() == AllBlocks.TRAIN_SLIDE_TRAIN.asItem()) {
            return TrainSlideType.TRAIN;
        }
        // else if (item.is(AllBlocks.TRAIN_STEP_TRAIN.asItem())) {
        // return TrainSlideType.TRAIN;
        // }
        return TrainSlideType.ANDESITE; // Default type
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
        toggle(state, level, pos, player, null, null);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion,
            BiConsumer<ItemStack, BlockPos> dropConsumer) {
        if (explosion.canTriggerBlocks() && this.type.canOpenByWindCharge() && !(Boolean) state.getValue(POWERED)) {
            this.toggle(state, level, pos, (Player) null, null, null);
        }

        super.onExplosionHit(state, level, pos, explosion, dropConsumer);
    }

    public void toggle(BlockState state, Level level, BlockPos pos, @Nullable Player player, String ignore,
            Boolean open) {
        toggle(state, level, pos, player, ignore, open, 10);
    }

    public void toggle(BlockState state, Level level, BlockPos pos, @Nullable Player player, String ignore,
            Boolean open, int flags) {
        state = state.cycle(OPEN);
        // level.setBlock(pos, blockstate, 2);
        if (ignore == null) {
            ignore = "";
        }
        if (open == null)
            open = state.cycle(OPEN).getValue(OPEN);
        // if (!open)
        // state = state.setValue(VISIBLE, true);
        Direction facing = state.getValue(FACING);
        BlockState otherSlideLeftState = null;
        BlockState otherSlideRightState = null;
        BlockPos otherSlideLeft = null;
        BlockPos otherSlideRight = null;
        // System.out.println("Toggle called with state: " + state + ", pos: " + pos +
        // ", player: " + player
        // + ", ignore: " + ignore + ", open: " + open + ", flags: " + flags);
        if (state.getValue(CONNECTED) == ConnectedState.BOTH) {
            otherSlideLeftState = (BlockState) getNeighbors(level, pos).get("left").get("state");
            otherSlideRightState = (BlockState) getNeighbors(level, pos).get("right").get("state");
            if (otherSlideLeftState.getBlock() instanceof TrainSlideBlock && otherSlideLeftState.getValue(FACING) == facing
                    && !ignore.equals("left")) {
                otherSlideLeftState = otherSlideLeftState.setValue(OPEN, open).setValue(POWERED, state.getValue(POWERED));
                otherSlideLeft = (BlockPos) getNeighbors(level, pos).get("left").get("pos");
                toggle(otherSlideLeftState, level, otherSlideLeft, player, "right", open, flags);
            }
            if (otherSlideRightState.getBlock() instanceof TrainSlideBlock
                    && otherSlideRightState.getValue(FACING) == facing && !ignore.equals("right")) {
                otherSlideRightState = otherSlideRightState.setValue(OPEN, open).setValue(POWERED,
                        state.getValue(POWERED));
                otherSlideRight = (BlockPos) getNeighbors(level, pos).get("right").get("pos");
                toggle(otherSlideRightState, level, otherSlideRight, player, "left", open, flags);
            }
        } else if (state.getValue(CONNECTED) == ConnectedState.LEFT) {
            otherSlideLeftState = (BlockState) getNeighbors(level, pos).get("left").get("state");
            if (otherSlideLeftState.getBlock() instanceof TrainSlideBlock && otherSlideLeftState.getValue(FACING) == facing
                    && !ignore.equals("left")) {
                otherSlideLeftState = otherSlideLeftState.setValue(OPEN, open).setValue(POWERED, state.getValue(POWERED));
                otherSlideLeft = (BlockPos) getNeighbors(level, pos).get("left").get("pos");
                toggle(otherSlideLeftState, level, otherSlideLeft, player, "right", open, flags);
            }
        } else if (state.getValue(CONNECTED) == ConnectedState.RIGHT) {
            otherSlideRightState = (BlockState) getNeighbors(level, pos).get("right").get("state");
            if (otherSlideRightState.getBlock() instanceof TrainSlideBlock
                    && otherSlideRightState.getValue(FACING) == facing && !ignore.equals("right")) {
                otherSlideRightState = otherSlideRightState.setValue(OPEN, open).setValue(POWERED,
                        state.getValue(POWERED));
                otherSlideRight = (BlockPos) getNeighbors(level, pos).get("right").get("pos");
                toggle(otherSlideRightState, level, otherSlideRight, player, "left", open, flags);
            }
        }

        if (state.getValue(OPEN))
            state = state.setValue(VISIBLE, false);
        // else
        // state = state.setValue(VISIBLE, true);
        level.setBlock(pos, state, flags);
        level.gameEvent(player, state.getValue(OPEN) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);

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

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
                                   boolean isMoving) {
        if (!(state.getBlock() instanceof TrainSlideBlock))
            return;



        //check if the neighbour was a block placement
//        if (fromPos.equals(pos) || fromPos.equals(pos.relative(state.getValue(FACING)))) {
//            return; // Ignore self or direct neighbor changes
//        }
//
        boolean isPowered = isSlidePowered(level, pos, state);
        boolean powered = state.getValue(POWERED);
        boolean open = state.getValue(OPEN);

        TrainSlideType type = state.getValue(TYPE);

//        System.out.println("Is Powered: " + isPowered + ", Powered: " + powered + ", Open: " + open + ", Type: " + type);


        BlockState leftState = level.getBlockState(pos.relative(state.getValue(FACING).getCounterClockWise()));
        BlockState rightState = level.getBlockState(pos.relative(state.getValue(FACING).getClockWise()));
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TrainSlideBlockEntity trainSlideBlockEntity) {
            Map<String, BlockState> neighborStates = trainSlideBlockEntity.getNeighborStates();
            @Nullable
            BlockState oldLeftState = neighborStates.get("left");
            @Nullable
            BlockState oldRightState = neighborStates.get("right");
            if ((oldLeftState != null && leftState.getBlock() != oldLeftState.getBlock()) || (oldRightState != null && rightState.getBlock() != oldRightState.getBlock())) {
//                System.out.println("Neighbor changed detected: Left State: " + leftState.getBlock() + ", Right State: " + rightState.getBlock() + ", Old Left State: " + oldLeftState.getBlock() + ", Old Right State: " + oldRightState.getBlock());
                state = getState(state, pos, level, state.getValue(FACING), "neighborChanged");
                state = state.setValue(POWERED, powered)
                        .setValue(OPEN, open)
//                        .setValue(VISIBLE, !open)
                        .setValue(TYPE, type);

                level.setBlock(pos, state, 2);
//                toggle(state, level, pos, null, null, !open, 2);

            } else {
                level.setBlock(pos, state, 2); // Update the block state without changing OPEN or POWERED
            }
        } else {
            level.setBlock(pos, state, 2); // Update the block state without changing OPEN or POWERED
        }

        if (isPowered != powered) {
//            state = state.cycle(OPEN);
            state = state.setValue(POWERED, isPowered);
            state = state.setValue(OPEN, !isPowered);
//            state = state.setValue(VISIBLE, !isPowered);
            toggle(state, level, pos, null, null, !isPowered, 2);
//            level.gameEvent(null, isPowered ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
        } else {
            level.setBlock(pos, state, 2); // Update the block state without changing OPEN or POWERED
        }

//        level.setBlock(pos, state, 2);
//
//        BlockState neighborState = level.getBlockState(fromPos);
//        boolean powered = state.getValue(POWERED);
//        boolean open = isSlideOpen(state);
////        if (open) {
////            // If the block is not powered and open, we should close it
////            isPowered = true; // Force it to be powered to close
////        }
//
//        boolean shouldBeOpen = false;
//
//
//        // if (isPowered == state.getValue(POWERED))
//        // return;
//        Direction facing = state.getValue(FACING);
//        String function = "neighborChanged";
//        BlockState newState = getState(state, pos, level, facing, function);
//
//        newState = newState.setValue(POWERED, isPowered).setValue(OPEN, isPowered);
//        if (isPowered) {
//            newState = newState.setValue(VISIBLE, false);
//        }
//
//        // System.out.println("State:" + state);
//        // System.out.println("NewState:" + newState);
//
//        level.setBlock(pos, newState, 2);
//        // level.gameEvent(null, isPowered ? GameEvent.BLOCK_OPEN :
//        // GameEvent.BLOCK_CLOSE, pos);
//        newState = level.getBlockState(pos);
//        newState = newState.cycle(OPEN);
//        toggle(newState, level, pos, null, null, !isPowered, 2);
//        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TrainSlideBlockEntity trainSlideBlockEntity) {
            trainSlideBlockEntity.setNeighborState(state);
        }

//        if (isPowered != state.getValue(POWERED)) {
//            // BlockState newState = state.setValue(POWERED, isPowered).setValue(OPEN,
//            // isPowered);
//
//            if (isPowered) {
//                newState = newState.setValue(VISIBLE, false);
//            }
//
//            level.setBlock(pos, newState, 2);
//
//            // Trigger toggle logic only if the OPEN state changes
//            if (isPowered != state.getValue(OPEN)) {
//                toggle(newState, level, pos, null, null, isPowered, 2);
//            }
//        }

        // if (defaultBlockState().is(block))
        // return;
        // if (isPowered == state.getValue(POWERED)) {
        // return;
        // }
        //
        // TrainSlideBlockEntity be = getBlockEntity(level, pos);
        // if (be != null && be.deferUpdate) {
        // return;
        // }
        // level.setBlock(pos, newState, 2);

        // if (isPowered)
        // changedState = changedState.setValue(VISIBLE, false);
        //
        // if (isPowered != state.getValue(OPEN)) {
        // level.gameEvent(null, isPowered ? GameEvent.BLOCK_OPEN :
        // GameEvent.BLOCK_CLOSE, pos);
        // Direction facing = changedState.getValue(FACING);
        // BlockPos otherPos = pos.relative(facing);
        // BlockState otherSlide = level.getBlockState(otherPos);
        // }
        //
        // level.setBlock(pos, changedState, 2);

    }

    public static Map<String, Map<String, Object>> getNeighbors(Level level, BlockPos pos) {
        Map<String, Map<String, Object>> neighbors = new HashMap<>();
        Map<String, Object> leftData = new HashMap<>();
        Map<String, Object> rightData = new HashMap<>();
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
        leftData.put("state", leftState);
        leftData.put("pos", left);
        rightData.put("state", rightState);
        rightData.put("pos", right);
        neighbors.put("left", leftData);
        neighbors.put("right", rightData);
        return neighbors;

    }

    public static BlockState getState(BlockState state, BlockPos pos, Level level, Direction facing, String function) {
        // System.out.println("getState called from " + function + " with pos: " + pos +
        // " and facing: " + facing);
//        System.out.println("Type: " + state.getValue(TYPE));
        BlockState finalState;
        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            // look in x axis
            int x = pos.getX();
            BlockPos nextLeftPos = new BlockPos(facing == Direction.NORTH ? x + 1 : x - 1, pos.getY(), pos.getZ());
            BlockPos nextRightPos = new BlockPos(facing == Direction.NORTH ? x - 1 : x + 1, pos.getY(), pos.getZ());
            BlockState nextLeftState = level.getBlockState(nextLeftPos);
            BlockState nextRightState = level.getBlockState(nextRightPos);
            boolean blockLeft = false;
            boolean blockRight = false;
            // check if the neighbour block is the same trainslideblock type
            if (nextLeftState.getBlock() instanceof TrainSlideBlock && nextLeftState.getValue(FACING) == facing
                    && state.getValue(TYPE) == nextLeftState.getValue(TYPE))
                blockLeft = true;
            if (nextRightState.getBlock() instanceof TrainSlideBlock && nextRightState.getValue(FACING) == facing
                    && state.getValue(TYPE) == nextRightState.getValue(TYPE))
                blockRight = true;
            if (blockLeft && blockRight) {
                finalState = state.setValue(CONNECTED, ConnectedState.BOTH);
            } else if (blockLeft) {
                finalState = state.setValue(CONNECTED, ConnectedState.LEFT);
            } else if (blockRight) {
                finalState = state.setValue(CONNECTED, ConnectedState.RIGHT);
            } else {
                finalState = state.setValue(CONNECTED, ConnectedState.NONE);
            }
        } else {
            // look in z axis
            int z = pos.getZ();
            BlockPos nextLeftPos = new BlockPos(pos.getX(), pos.getY(), facing == Direction.EAST ? z + 1 : z - 1);
            BlockPos nextRightPos = new BlockPos(pos.getX(), pos.getY(), facing == Direction.EAST ? z - 1 : z + 1);
            BlockState nextLeftState = level.getBlockState(nextLeftPos);
            BlockState nextRightState = level.getBlockState(nextRightPos);
            boolean blockLeft = false;
            boolean blockRight = false;
            if (nextLeftState.getBlock() instanceof TrainSlideBlock && nextLeftState.getValue(FACING) == facing
                    && state.getValue(TYPE) == nextLeftState.getValue(TYPE))
                blockLeft = true;
            if (nextRightState.getBlock() instanceof TrainSlideBlock && nextRightState.getValue(FACING) == facing
                    && state.getValue(TYPE) == nextRightState.getValue(TYPE))
                blockRight = true;
            if (blockLeft && blockRight) {
                finalState = state.setValue(CONNECTED, ConnectedState.BOTH);
            } else if (blockLeft) {
                finalState = state.setValue(CONNECTED, ConnectedState.LEFT);
            } else if (blockRight) {
                finalState = state.setValue(CONNECTED, ConnectedState.RIGHT);
            } else {
                finalState = state.setValue(CONNECTED, ConnectedState.NONE);
            }

        }
        if (finalState.getValue(OPEN))
            finalState = finalState.setValue(VISIBLE, false);
        // else
        // finalState = finalState.setValue(VISIBLE, true);
        return finalState;
    }

    public static boolean isSlidePowered(Level level, BlockPos pos, BlockState state) {
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
        builder.add(FACING, OPEN, POWERED, CONNECTED, VISIBLE, TYPE);
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

    protected BlockSetType getType() {
        return this.type;
    }

    static {
        CLOSED = Block.box(0, 0, 0, 16, 16, 16);

        NORTH_OPEN_NONE = Stream.of(
                Stream.of(
                        Block.box(0, 0, 0, 1, 16, 16),
                        Block.box(15, 0, 0, 16, 16, 16),
                        Block.box(1, 0, 15, 15, 16, 16),
                        Block.box(1, 15, 0, 15, 16, 15),
                        Block.box(1, 0, 14, 15, 1, 15)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(1, 10, -4, 15, 15, 1),
                Block.box(1, 5, -9, 15, 10, 1),
                Block.box(1, 0, -13, 15, 5, 1)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        NORTH_OPEN_RIGHT = Stream.of(
                Stream.of(
//                        Block.box(0, 0, 0, 1, 16, 16),
                        Block.box(15, 0, 0, 16, 16, 16),
                        Block.box(0, 0, 15, 15, 16, 16),
                        Block.box(0, 15, 0, 15, 16, 15),
                        Block.box(0, 0, 14, 15, 1, 15)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(0, 10, -4, 15, 15, 1),
                Block.box(0, 5, -9, 15, 10, 1),
                Block.box(0, 0, -13, 15, 5, 1)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        NORTH_OPEN_LEFT = Stream.of(
                Stream.of(
                        Block.box(0, 0, 0, 1, 16, 16),
//                        Block.box(15, 0, 0, 16, 16, 16),
                        Block.box(1, 0, 15, 16, 16, 16),
                        Block.box(1, 15, 0, 16, 16, 15),
                        Block.box(1, 0, 14, 16, 1, 15)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(1, 10, -4, 16, 15, 1),
                Block.box(1, 5, -9, 16, 10, 1),
                Block.box(1, 0, -13, 16, 5, 1)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        NORTH_OPEN_BOTH = Stream.of(
                Stream.of(
//                        Block.box(0, 0, 0, 1, 16, 16),
//                        Block.box(15, 0, 0, 16, 16, 16),
                        Block.box(0, 0, 15, 16, 16, 16),
                        Block.box(0, 15, 0, 16, 16, 15),
                        Block.box(0, 0, 14, 16, 1, 15)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(0, 10, -4, 16, 15, 1),
                Block.box(0, 5, -9, 16, 10, 1),
                Block.box(0, 0, -13, 16, 5, 1)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        EAST_OPEN_NONE = Stream.of(
                Stream.of(
                        Block.box(0, 0, 0, 16, 16, 1),
                        Block.box(0, 0, 15, 16, 16, 16),
                        Block.box(0, 0, 1, 1, 16, 16),
                        Block.box(1, 15, 1, 16, 16, 16),
                        Block.box(1, 0, 1, 2, 1, 16)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(15, 10, 1, 20, 15, 15),
                Block.box(15, 5, 1, 25, 10, 15),
                Block.box(15, 0, 1, 29, 5, 15)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        EAST_OPEN_RIGHT = Stream.of(
                Stream.of(
//                        Block.box(0, 0, 0, 16, 16, 1),
                        Block.box(0, 0, 15, 16, 16, 16),
                        Block.box(0, 0, 0, 1, 16, 15),
                        Block.box(1, 15, 0, 16, 16, 15),
                        Block.box(1, 0, 0, 2, 1, 15)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(15, 10, 0, 20, 15, 15),
                Block.box(15, 5, 0, 25, 10, 15),
                Block.box(15, 0, 0, 29, 5, 15)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        EAST_OPEN_LEFT = Stream.of(
                Stream.of(
                        Block.box(0, 0, 0, 16, 16, 1),
//                        Block.box(0, 0, 15, 16, 16, 16),
                        Block.box(0, 0, 1, 1, 16, 16),
                        Block.box(1, 15, 1, 16, 16, 16),
                        Block.box(1, 0, 1, 2, 1, 16)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(15, 10, 1, 20, 15, 16),
                Block.box(15, 5, 1, 25, 10, 16),
                Block.box(15, 0, 1, 29, 5, 16)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        EAST_OPEN_BOTH = Stream.of(
                Stream.of(
//                        Block.box(0, 0, 0, 16, 16, 1),
//                        Block.box(0, 0, 15, 16, 16, 16),
                        Block.box(0, 0, 0, 1, 16, 16),
                        Block.box(1, 15, 0, 16, 16, 16),
                        Block.box(1, 0, 0, 2, 1, 16)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(15, 10, 0, 20, 15, 16),
                Block.box(15, 5, 0, 25, 10, 16),
                Block.box(15, 0, 0, 29, 5, 16)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        SOUTH_OPEN_NONE = Stream.of(
                Stream.of(
                        Block.box(15, 0, 0, 16, 16, 16),
                        Block.box(0, 0, 0, 1, 16, 16),
                        Block.box(1, 0, 0, 15, 16, 1),
                        Block.box(1, 15, 1, 15, 16, 16),
                        Block.box(1, 0, 1, 15, 1, 2)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(1, 10, 15, 15, 15, 20),
                Block.box(1, 5, 15, 15, 10, 25),
                Block.box(1, 0, 15, 15, 5, 29)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        SOUTH_OPEN_RIGHT = Stream.of(
                Stream.of(
//                        Block.box(15, 0, 0, 16, 16, 16),
                        Block.box(0, 0, 0, 1, 16, 16),
                        Block.box(1, 0, 0, 16, 16, 1),
                        Block.box(1, 15, 1, 16, 16, 16),
                        Block.box(1, 0, 1, 16, 1, 2)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(1, 10, 15, 16, 15, 20),
                Block.box(1, 5, 15, 16, 10, 25),
                Block.box(1, 0, 15, 16, 5, 29)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        SOUTH_OPEN_LEFT = Stream.of(
                Stream.of(
                        Block.box(15, 0, 0, 16, 16, 16),
//                        Block.box(0, 0, 0, 1, 16, 16),
                        Block.box(0, 0, 0, 15, 16, 1),
                        Block.box(0,15, 1, 15, 16, 16),
                        Block.box(0, 0, 1, 15, 1, 2)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(0, 10, 15, 15, 15, 20),
                Block.box(0, 5, 15, 15, 10, 25),
                Block.box(0, 0, 15, 15, 5, 29)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        SOUTH_OPEN_BOTH = Stream.of(
                Stream.of(
//                        Block.box(15, 0, 0, 16, 16, 16),
//                        Block.box(0, 0, 0, 1, 16, 16),
                        Block.box(0, 0, 0, 16, 16, 1),
                        Block.box(0, 15, 1, 16, 16, 16),
                        Block.box(0, 0, 1, 16, 1, 2)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(0, 10, 15, 16, 15, 20),
                Block.box(0, 5, 15, 16, 10, 25),
                Block.box(0, 0, 15, 16, 5, 29)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        WEST_OPEN_NONE = Stream.of(
                Stream.of(
                        Block.box(0, 0, 15, 16, 16, 16),
                        Block.box(0, 0, 0, 16, 16, 1),
                        Block.box(15, 0, 1, 16, 16, 15),
                        Block.box(0, 15, 1, 15, 16, 15),
                        Block.box(14, 0, 1, 15, 1, 15)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(-4, 10, 1, 1, 15, 15),
                Block.box(-9, 5, 1, 1, 10, 15),
                Block.box(-13, 0, 1, 1, 5, 15)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        WEST_OPEN_RIGHT = Stream.of(
                Stream.of(
//                        Block.box(0, 0, 15, 16, 16, 16),
                        Block.box(0, 0, 0, 16, 16, 1),
                        Block.box(15, 0, 1, 16, 16, 16),
                        Block.box(0, 15, 1, 15, 16, 16),
                        Block.box(14, 0, 1, 15, 1, 16)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(-4, 10, 1, 1, 15, 16),
                Block.box(-9, 5, 1, 1, 10, 16),
                Block.box(-13, 0, 1, 1, 5, 16)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        WEST_OPEN_LEFT = Stream.of(
                Stream.of(
                        Block.box(0, 0, 15, 16, 16, 16),
//                        Block.box(0, 0, 0, 16, 16, 1),
                        Block.box(15, 0, 0, 16, 16, 15),
                        Block.box(0, 15, 0, 15, 16, 15),
                        Block.box(14, 0, 0, 15, 1, 15)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(-4, 10, 0, 1, 15, 15),
                Block.box(-9, 5, 0, 1, 10, 15),
                Block.box(-13, 0, 0, 1, 5, 15)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        WEST_OPEN_BOTH = Stream.of(
                Stream.of(
//                        Block.box(0, 0, 15, 16, 16, 16),
//                        Block.box(0, 0, 0, 16, 16, 1),
                        Block.box(15, 0, 0, 16, 16, 16),
                        Block.box(0, 15, 0, 15, 16, 16),
                        Block.box(14, 0, 0, 15, 1, 16)
                ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
                Block.box(-4, 10, 0, 1, 15, 16),
                Block.box(-9, 5, 0, 1, 10, 16),
                Block.box(-13, 0, 0, 1, 5, 16)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return IBE.super.newBlockEntity(pos, state);
    }

    @Override
    public Class<TrainSlideBlockEntity> getBlockEntityClass() {
        return TrainSlideBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TrainSlideBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.TRAIN_SLIDE.get();
    }

    public enum ConnectedState implements StringRepresentable {
        NONE, RIGHT, LEFT, BOTH;

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }

    // public
}
