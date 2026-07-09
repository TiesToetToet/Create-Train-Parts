package com.tiestoettoet.create_train_parts.foundation.placement;

import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripItem;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.tiestoettoet.create_train_parts.content.trains.crossing.ArmExtenderBlock;
import com.tiestoettoet.create_train_parts.content.trains.crossing.CrossingBlock;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeMod;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

@MethodsReturnNonnullByDefault
public abstract class ArmHelper<T extends Comparable<T>> implements IPlacementHelper {
    protected final List<Predicate<BlockState>> statePredicate;
    protected final Property<T> property;
    protected final Function<BlockState, Direction.Axis> axisFunction;

    public ArmHelper(List<Predicate<BlockState>> statePredicate, Function<BlockState, Direction.Axis> axisFunction, Property<T> property) {
        this.statePredicate = statePredicate;
        this.axisFunction = axisFunction;
        this.property = property;
    }

//    public boolean matchesAxis(BlockState state, Direction.Axis axis) {
//        if (!statePredicate.test(state))
//            return false;
//
//        return axisFunction.apply(state) == axis;
//    }

    public boolean matchesAxis(BlockState state, Direction.Axis axis) {
        if (statePredicate.stream().noneMatch(p -> p.test(state)))
            return false;

        return axisFunction.apply(state) == axis;
    }

    public int attachedArms(Level world, BlockPos pos, Direction direction) {
        BlockPos checkPos = pos.relative(direction);
        BlockState state = world.getBlockState(checkPos);
        int count = 0;
        while (matchesAxis(state, direction.getAxis())) {
            count++;
            checkPos = checkPos.relative(direction);
            state = world.getBlockState(checkPos);
        }
        return count;
    }

    public int attachedArms(Level world, BlockPos pos, Direction direction, BlockState originState) {
        int count = 0;

        BlockPos checkPos = pos.relative(direction);
        BlockState state = world.getBlockState(checkPos);

        while (true) {
            if (ArmExtenderBlock.isArm(state)) {
                // Make sure it's part of this crossing/arm chain
                if (state.getValue(HORIZONTAL_FACING) != originState.getValue(HORIZONTAL_FACING))
                    break;

                count++;
            } else {
                break;
            }

            checkPos = checkPos.relative(direction);
            state = world.getBlockState(checkPos);
        }

        return count;
    }

//    @Override
//    public Predicate<BlockState> getStatePredicate() {
//        return this.statePredicate;
//    }

    @Override
    public Predicate<BlockState> getStatePredicate() {
        return state -> statePredicate.stream().anyMatch(p -> p.test(state));
    }

    @Override
    public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
//        List<Direction> directions = IPlacementHelper.orderedByDistance(pos, ray.getLocation(), dir -> dir.getAxis() == axisFunction.apply(state));
        List<Direction> directions = IPlacementHelper.orderedByDistance(
                pos,
                ray.getLocation(),
                dir -> {
                    // Crossings only extend clockwise
                    if (CrossingBlock.isCrossing(state))
                        return dir == state.getValue(HORIZONTAL_FACING).getClockWise();

                    // Arm extenders extend along their axis like before
                    return dir.getAxis() == axisFunction.apply(state);
                });

        for (Direction dir : directions) {
            int range = AllConfigs.server().equipment.placementAssistRange.get();

            AttributeInstance reach = player.getAttribute(ForgeMod.BLOCK_REACH.get());
            if (reach != null && reach.hasModifier(ExtendoGripItem.singleRangeAttributeModifier))
                range += 4;

            //            int arms = attachedArms(world, pos, dir);
            int arms = CrossingBlock.isCrossing(state)
                    ? attachedArms(world, pos, dir, state)
                    : attachedArms(world, pos, dir);
            if (arms >= range)
                continue;

            BlockPos newPos = pos.relative(dir, arms + 1);
            BlockState newState = world.getBlockState(newPos);


            if (newState.canBeReplaced()) {
                // Copy the FLIPPED property from the original state
                Direction facing = state.getValue(HORIZONTAL_FACING);
                boolean flipped = state.getValue(CrossingBlock.isCrossing(state)
                        ? CrossingBlock.FLIPPED
                        : ArmExtenderBlock.FLIPPED);

                boolean barrier = state.getValue(CrossingBlock.isCrossing(state)
                        ? CrossingBlock.BARRIER
                        : ArmExtenderBlock.BARRIER);
                return PlacementOffset.success(newPos, bState -> bState
                        .setValue(HORIZONTAL_FACING, facing)
                        .setValue(ArmExtenderBlock.FLIPPED, flipped)
                        .setValue(ArmExtenderBlock.OPEN, true)
                        .setValue(ArmExtenderBlock.BARRIER, barrier));
            }

        }

        return PlacementOffset.fail();
    }
}
