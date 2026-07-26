package com.tiestoettoet.create_train_parts.content.trains.bellow;

import com.ibm.icu.impl.Pair;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.block.IHaveBigOutline;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionWorld;
import com.tiestoettoet.create_train_parts.AllBlockEntityTypes;
import com.tiestoettoet.create_train_parts.foundation.collision.BellowSize;
import com.simibubi.create.foundation.block.IBE;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.lang.reflect.Field;

import static net.minecraft.world.level.block.Block.box;

public class BellowBlock extends HorizontalDirectionalBlock implements IHaveBigOutline, IBE<BellowBlockEntity> {
    public static final BooleanProperty VISIBLE = BooleanProperty.create("visible");
    public static final IntegerProperty WIDTH = IntegerProperty.create("width", BellowSize.MIN, BellowSize.MAX);
    public static final IntegerProperty HEIGHT = IntegerProperty.create("height", BellowSize.MIN, BellowSize.MAX);

    private final BellowSize size;
    /** Outline of this bellow, indexed by horizontal facing. */
    private final VoxelShape[] shapes;

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    public BellowBlock(Properties properties, BellowSize size) {
        super(properties);
        this.size = size;
        this.shapes = buildShapes(size);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(VISIBLE, true)
                .setValue(WIDTH, size.width())
                .setValue(HEIGHT, size.height()));
    }

    public BellowSize getSize() {
        return size;
    }

    public static BellowSize getSize(BlockState state) {
        if (state.hasProperty(WIDTH) && state.hasProperty(HEIGHT))
            return new BellowSize(state.getValue(WIDTH), state.getValue(HEIGHT));
        return state.getBlock() instanceof BellowBlock bellow ? bellow.size : BellowSize.DEFAULT;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        if (facing.getAxis()
                .isVertical())
            return Shapes.block();

        VoxelShape shape = shapes[facing.get2DDataValue()];

		if (!(level instanceof ContraptionWorld cw))
			return shape;

		return shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
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

        return stateForPlacement.setValue(FACING, facing).setValue(VISIBLE, true);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VISIBLE, WIDTH, HEIGHT);
    }

    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
            BlockPos currentPos, BlockPos facingPos) {

        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    private static VoxelShape[] buildShapes(BellowSize size) {
        Direction[] clockwiseFromNorth = { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };
        VoxelShape[] shapes = new VoxelShape[4];
        double[][] boxes = northBoxes(size.width(), size.height());
        for (Direction facing : clockwiseFromNorth) {
            shapes[facing.get2DDataValue()] = toShape(boxes);
            boxes = rotateClockwise(boxes);
        }
        return shapes;
    }

    /** Frame boxes for a north facing bellow, in sixteenths of a block. */
    private static double[][] northBoxes(int width, int height) {
        double left = 8 - 8 * width;
        double right = 8 + 8 * width;
        double top = 16 * height;
        return new double[][] {
                { left, 0, 9, right, 1, 13 },
                { right - 1, 1, 9, right, top - 1, 13 },
                { left, 1, 9, left + 1, top - 1, 13 },
                { left, top - 1, 9, right, top, 13 }
        };
    }

    private static double[][] rotateClockwise(double[][] boxes) {
        double[][] rotated = new double[boxes.length][];
        for (int i = 0; i < boxes.length; i++) {
            double[] box = boxes[i];
            double x1 = 16 - box[5];
            double x2 = 16 - box[2];
            rotated[i] = new double[] { Math.min(x1, x2), box[1], box[0], Math.max(x1, x2), box[4], box[3] };
        }
        return rotated;
    }

    private static VoxelShape toShape(double[][] boxes) {
        return Stream.of(boxes)
                .map(box -> Block.box(box[0], box[1], box[2], box[3], box[4], box[5]))
                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR))
                .orElseGet(Shapes::block);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // The frame is drawn by the block entity renderer so it can be scaled to
        // the configured size without needing one block model per size.
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return IBE.super.newBlockEntity(pos, state);
    }

    @Override
    public Class<BellowBlockEntity> getBlockEntityClass() {
        return BellowBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BellowBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.BELLOW.get();
    }
}
