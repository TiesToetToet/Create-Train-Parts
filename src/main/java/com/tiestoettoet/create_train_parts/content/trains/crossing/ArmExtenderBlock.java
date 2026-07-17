package com.tiestoettoet.create_train_parts.content.trains.crossing;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.block.IBE;
import com.tiestoettoet.create_train_parts.AllBlockEntityTypes;
import com.tiestoettoet.create_train_parts.AllBlocks;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.tiestoettoet.create_train_parts.foundation.placement.ArmHelper;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class ArmExtenderBlock extends HorizontalDirectionalBlock implements IWrenchable, IBE<ArmExtenderBlockEntity> {
    protected static final int placementHelperId = PlacementHelpers.register(PlacementHelper.get());


    public static final BooleanProperty FLIPPED = BooleanProperty.create("flipped");
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final BooleanProperty BARRIER = BooleanProperty.create("barrier");

    public ArmExtenderBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FLIPPED, false).setValue(OPEN, false).setValue(BARRIER, false));
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);
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
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Boolean barrier = state.getValue(BARRIER);
        if (!barrier) {
            return state.getShape(level, pos);
        }
        Direction facing = state.getValue(HORIZONTAL_FACING);
        boolean flipped = state.getValue(FLIPPED);

        // Base shape from JSON: arm extends from y=6 to y=10 (height 4), z=0 to z=16
        // (full depth)
        // Normal (not flipped): x=11 to x=13 (width 2)
        // Flipped: x=3 to x=5 (width 2)

        VoxelShape baseShape;
        if (flipped) {
            // Flipped version: x=3 to x=5
            baseShape = Block.box(3, 6, 0, 5, 26, 16);
        } else {
            // Normal version: x=11 to x=13
            baseShape = Block.box(11, 6, 0, 13, 26, 16);
        }

        // Rotate the shape based on the facing direction
        // The JSON models are oriented for west facing (y=0), so we need to rotate
        // accordingly
        return switch (facing) {
            case NORTH -> flipped ? Block.box(0, 6, 3, 16, 26, 5) : Block.box(0, 6, 11, 16, 26, 13);
            case SOUTH -> flipped ? Block.box(0, 6, 11, 16, 26, 13) : Block.box(0, 6, 3, 16, 26, 5);
            case EAST -> flipped ? Block.box(11, 6, 0, 13, 26, 16) : Block.box(3, 6, 0, 5, 26, 16);
            case WEST -> flipped ? Block.box(3, 6, 0, 5, 26, 16) : Block.box(11, 6, 0, 13, 26, 16);
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

        BlockPos pos = context.getClickedPos(); // Retrieve the BlockPos
        Level level = context.getLevel();
        boolean barrier = false;
        // get neighbour block of where to place

        boolean open = false;
        BlockState rightState = level.getBlockState(pos.relative(facing.getCounterClockWise()));
        BlockState leftState = level.getBlockState(pos.relative(facing.getClockWise()));

        if (leftState.getBlock() instanceof ArmExtenderBlock && leftState.hasProperty(BARRIER) && leftState.getValue(BARRIER)) {
            barrier = true;
        }
        if (rightState.getBlock() instanceof ArmExtenderBlock && rightState.hasProperty(BARRIER) && rightState.getValue(BARRIER)) {
            barrier = true;
        }
        if ((leftState.getBlock() instanceof ArmExtenderBlock && leftState.hasProperty(FLIPPED) && leftState.getValue(FLIPPED)) ||
                (leftState.getBlock() instanceof CrossingBlock && leftState.hasProperty(CrossingBlock.FLIPPED) && leftState.getValue(CrossingBlock.FLIPPED))) {
            flipped = true;
            facing = leftState.getValue(HORIZONTAL_FACING);
        }
        if ((rightState.getBlock() instanceof ArmExtenderBlock && rightState.hasProperty(FLIPPED) && rightState.getValue(FLIPPED)) ||
                (rightState.getBlock() instanceof CrossingBlock && rightState.hasProperty(CrossingBlock.FLIPPED) && rightState.getValue(CrossingBlock.FLIPPED))) {
            flipped = true;
            facing = rightState.getValue(HORIZONTAL_FACING);
        }

//        // check which block the player is hitting
//        context.getClickLocation();
//        BlockHitResult hitResult = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, false);
//        BlockState hitState = level.getBlockState(hitResult.getBlockPos());
//        if (hitState.getBlock() instanceof CrossingBlock) {
//
//        }

        return state.setValue(HORIZONTAL_FACING, facing).setValue(FLIPPED, flipped).setValue(OPEN, true).setValue(BARRIER, barrier);
    }

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos,
						BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		level.scheduleTick(pos, this, 1);

		if (!level.isClientSide) {
			BlockEntity be = level.getBlockEntity(pos);

			if (be instanceof ArmExtenderBlockEntity arm) {
				arm.copyNeighbourColours();
			}
		}
	}

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
		ItemStack stack = player.getItemInHand(hand);
        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (placementHelper.matchesItem(stack) && !player.isShiftKeyDown()) {
            return placementHelper.getOffset(player, level, state, pos, hitResult).placeInWorld(level,
                    (BlockItem) stack.getItem(), player, hand, hitResult);
        }

		if (stack.getItem() instanceof DyeItem dyeItem) {
			DyeColor colour = dyeItem.getDyeColor();
			Vec3 hitPos = hitResult.getLocation();
			System.out.println("Hit position: " + hitPos);
			// Relative position within the block (0.0 - 1.0)
			double x = hitPos.x - pos.getX();
			double y = hitPos.y - pos.getY();
			double z = hitPos.z - pos.getZ();

			Vec3 relativePos = new Vec3(x, y, z);
			ArmExtenderBlockEntity blockEntity = (ArmExtenderBlockEntity) level.getBlockEntity(pos);
			if (isInColour1(state, relativePos)) {
				// Change colour 1
				blockEntity.setColour1(getColourIndex(colour));
			}
			if (isInColour2(state, relativePos)) {
				// Change colour 2
				blockEntity.setColour2(getColourIndex(colour));
			}
			Byte colour1 = isInColour1(state, relativePos) ? getColourIndex(colour) : null;
			Byte colour2 = isInColour2(state, relativePos) ? getColourIndex(colour) : null;
			boolean ctrl = AllKeys.ctrlDown();
			if (ctrl) {
				updateConnectedColours(level, pos, colour1, colour2);
			}

		}

        return InteractionResult.PASS;
    }

	private void updateConnectedColours(Level level, BlockPos pos, @Nullable Byte colour1, @Nullable Byte colour2) {
		//navigate to all connected arms and crossings and update their colours
		//first check to the right
		BlockPos currentPos = pos;
		Direction facing = level.getBlockState(pos).getValue(HORIZONTAL_FACING);
		boolean flipped = level.getBlockState(currentPos).getValue(FLIPPED);
		while (true) {
			BlockPos searchPos = pos.relative(
				flipped ? facing.getCounterClockWise() : facing.getClockWise()
			);
			BlockState rightState = level.getBlockState(searchPos);
			if (rightState.getBlock() instanceof ArmExtenderBlock) {
				ArmExtenderBlockEntity rightBE = (ArmExtenderBlockEntity) level.getBlockEntity(searchPos);
				if (colour1 != null)
					rightBE.setColour1(colour1);
				if (colour2 != null)
					rightBE.setColour2(colour2);
				pos = searchPos;
			} else {
				break;
			}
		}
		pos = currentPos;
		//then check to the left
		while (true) {
			BlockPos antiSearchPos = pos.relative(
				flipped ? facing.getClockWise() : facing.getCounterClockWise()
			);
			BlockState leftState = level.getBlockState(antiSearchPos);
			if (leftState.getBlock() instanceof ArmExtenderBlock) {
				ArmExtenderBlockEntity leftBE = (ArmExtenderBlockEntity) level.getBlockEntity(antiSearchPos);
				if (colour1 != null)
					leftBE.setColour1(colour1);
				if (colour2 != null)
					leftBE.setColour2(colour2);
				pos = antiSearchPos;
			} else if (leftState.getBlock() instanceof CrossingBlock) {
				CrossingBlockEntity leftBE = (CrossingBlockEntity) level.getBlockEntity(antiSearchPos);
				if (colour1 != null)
					leftBE.setColour1(colour1);
				if (colour2 != null)
					leftBE.setColour2(colour2);
				break;
			} else {
				break;
			}
		}
	}

	private boolean isInColour1(BlockState state, Vec3 hitPos) {
		Direction facing = state.getValue(HORIZONTAL_FACING);
		boolean flipped = state.getValue(FLIPPED);

		Direction effectiveFacing = flipped ? facing.getOpposite() : facing;

		AABB colour1Box1;
		AABB colour1Box2;

		switch (effectiveFacing) {
			case NORTH -> {
				colour1Box1 = box(0, 6, 11, 4, 10, 13);
				colour1Box2 = box(8, 6, 11, 12, 10, 13);
			}
			case SOUTH -> {
				colour1Box1 = box(12, 6, 3, 16, 10, 5);
				colour1Box2 = box(4, 6, 3, 8, 10, 5);
			}
			case EAST -> {
				colour1Box1 = box(3, 6, 0, 5, 10, 4);
				colour1Box2 = box(3, 6, 8, 5, 10, 12);
			}
			case WEST -> {
				colour1Box1 = box(11, 6, 12, 13, 10, 16);
				colour1Box2 = box(11, 6, 4, 13, 10, 8);
			}
			default -> throw new IllegalStateException();
		}

		return colour1Box1.contains(hitPos) || colour1Box2.contains(hitPos);
	}

	private boolean isInColour2(BlockState state, Vec3 hitPos) {
		Direction facing = state.getValue(HORIZONTAL_FACING);
		boolean flipped = state.getValue(FLIPPED);

		Direction effectiveFacing = flipped ? facing.getOpposite() : facing;

		AABB colour2Box1;
		AABB colour2Box2;

		switch (effectiveFacing) {
			case NORTH -> {
				colour2Box1 = box(4, 6, 11, 8, 10, 13);
				colour2Box2 = box(12, 6, 11, 16, 10, 13);
			}
			case SOUTH -> {
				colour2Box1 = box(8, 6, 3, 12, 10, 5);
				colour2Box2 = box(0, 6, 3, 4, 10, 5);
			}
			case EAST -> {
				colour2Box1 = box(3, 6, 4, 5, 10, 8);
				colour2Box2 = box(3, 6, 12, 5, 10, 16);
			}
			case WEST -> {
				colour2Box1 = box(11, 6, 8, 13, 10, 12);
				colour2Box2 = box(11, 6, 0, 13, 10, 4);
			}
			default -> throw new IllegalStateException();
		}

		return colour2Box1.contains(hitPos) || colour2Box2.contains(hitPos);
	}

	private static AABB box(int x1, int y1, int z1, int x2, int y2, int z2) {
		return Block.box(x1, y1, z1, x2, y2, z2)
			.bounds()
			.inflate(0.001);
	}

	private byte getColourIndex(DyeColor colour) {
		return switch (colour) {
			case WHITE -> 0;
			case LIGHT_GRAY -> 1;
			case GRAY -> 2;
			case BLACK -> 3;
			case BROWN -> 4;
			case RED -> 5;
			case ORANGE -> 6;
			case YELLOW -> 7;
			case LIME -> 8;
			case GREEN -> 9;
			case CYAN -> 10;
			case LIGHT_BLUE -> 11;
			case BLUE -> 12;
			case PURPLE -> 13;
			case MAGENTA -> 14;
			case PINK -> 15;
		};
	}

    public static boolean isArm(BlockState state) {
        return AllBlocks.ARM_EXTENDER.has(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(HORIZONTAL_FACING).add(FLIPPED).add(OPEN).add(BARRIER));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor world,
            BlockPos pos, BlockPos neighbourPos) {
        return state;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
        // RenderShape.ENTITYBLOCK_ANIMATED;
    }


	@Override
	public Class<ArmExtenderBlockEntity> getBlockEntityClass() {
		return ArmExtenderBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends ArmExtenderBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.ARM_EXTENDER.get();
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
                    List.of(
                            AllBlocks.ARM_EXTENDER::has,
                            AllBlocks.CROSSING::has
                    ),
                    state -> state.getValue(HORIZONTAL_FACING).getClockWise().getAxis(),
                    HORIZONTAL_FACING);
        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return AllBlocks.ARM_EXTENDER::isIn;
        }
    }

}
