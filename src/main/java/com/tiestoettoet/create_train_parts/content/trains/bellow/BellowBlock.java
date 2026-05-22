package com.tiestoettoet.create_train_parts.content.trains.bellow;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IHaveBigOutline;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionWorld;
import com.tiestoettoet.create_train_parts.AllBlockEntityTypes;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

import java.util.List;
import java.util.stream.Stream;
import java.lang.reflect.Field;

import static net.minecraft.world.level.block.Block.box;

public class BellowBlock extends HorizontalDirectionalBlock implements IHaveBigOutline, IBE<BellowBlockEntity> {
    public static final BooleanProperty VISIBLE = BooleanProperty.create("visible");

    protected static final VoxelShape NORTH;
    protected static final VoxelShape EAST;
    protected static final VoxelShape SOUTH;
    protected static final VoxelShape WEST;

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    public BellowBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = switch (state.getValue(FACING)) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> Shapes.block();
        };

        if (!(level instanceof ContraptionWorld world)) {
            return shape;
        }

        BellowBlockEntity blockEntity = getBlockEntity(world, pos);
        System.out.println("Block entity: " + blockEntity);

        float partialTicks = world.isClientSide() ? AnimationTickHolder.getPartialTicks() : 0f;
        System.out.println("Partial ticks: " + partialTicks);
        System.out.println("BlockPos: " + pos);
        BlockPos zero = new BlockPos(0, 0, 0);
        Vec3 selfWorldPos = getWorldCenter(world, zero, partialTicks);
        System.out.println("Self world pos: " + selfWorldPos);
        Vec3 blockOrigin = selfWorldPos.subtract(0, 0.5, 0);
        System.out.println("Block origin: " + blockOrigin);
        for (List<AABB> segment : getAssembledAabbs(world, state, selfWorldPos, partialTicks)) {
            System.out.println("Segment: " + segment);
            for (AABB box : segment) {
                AABB local = box.move(-blockOrigin.x, -blockOrigin.y, -blockOrigin.z);
                shape = Shapes.or(shape, Shapes.create(local));
            }
        }

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
        builder.add(FACING, VISIBLE);
    }

    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
            BlockPos currentPos, BlockPos facingPos) {

        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    static {
        NORTH = Stream.of(
                Block.box(0, 0, 9, 16, 1, 13),
                Block.box(15, 1, 9, 16, 31, 13),
                Block.box(0, 1, 9, 1, 31, 13),
                Block.box(0, 31, 9, 16, 32, 13)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        EAST = Stream.of(
                Block.box(3, 0, 0, 7, 1, 16),
                Block.box(3, 1, 15, 7, 31, 16),
                Block.box(3, 1, 0, 7, 31, 1),
                Block.box(3, 31, 0, 7, 32, 16)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        SOUTH = Stream.of(
                Block.box(0, 0, 3, 16, 1, 7),
                Block.box(0, 1, 3, 1, 31, 7),
                Block.box(15, 1, 3, 16, 31, 7),
                Block.box(0, 31, 3, 16, 32, 7)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

        WEST = Stream.of(
                Block.box(9, 0, 0, 13, 1, 16),
                Block.box(9, 1, 0, 13, 31, 1),
                Block.box(9, 1, 15, 13, 31, 16),
                Block.box(9, 31, 0, 13, 32, 16)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(VISIBLE) ? RenderShape.MODEL : RenderShape.ENTITYBLOCK_ANIMATED;
        // RenderShape.ENTITYBLOCK_ANIMATED;
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

    public List<List<AABB>> getAssembledAabbs(Level level, BlockState state, Vec3 selfWorldPos,
            float partialTicks) {
        if (level == null) {
            return List.of();
        }

        if (state.hasProperty(VISIBLE) && state.getValue(VISIBLE)) {
            return List.of();
        }

        BellowBlockEntity.SegmentResult result = BellowBlockEntity.computeSegmentResult(
                level,
                selfWorldPos,
                partialTicks,
                false);

        if (result == null || !result.primary()) {
            return List.of();
        }

        BellowBlockEntity.SegmentData data = result.data();
        return BellowBlockEntity.getAllAABBs(data.positions(), data.yRots(), data.xRots());
    }

    private Vec3 getWorldCenter(Level level, BlockPos localPos, float partialTicks) {
        if (!(level instanceof ContraptionWorld contraptionWorld)) {
            return Vec3.atCenterOf(localPos);
        }

        Contraption contraption = getContraption(contraptionWorld);
        if (contraption == null || contraption.entity == null) {
            return Vec3.atCenterOf(localPos);
        }

        AbstractContraptionEntity entity = contraption.entity;
        return entity.toGlobalVector(Vec3.atCenterOf(localPos), partialTicks);
    }

    private Contraption getContraption(ContraptionWorld contraptionWorld) {
        try {
            Field field = ContraptionWorld.class.getDeclaredField("contraption");
            field.setAccessible(true);
            return (Contraption) field.get(contraptionWorld);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}