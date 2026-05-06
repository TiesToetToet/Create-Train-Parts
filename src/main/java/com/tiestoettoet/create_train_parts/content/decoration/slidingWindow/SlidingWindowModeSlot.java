package com.tiestoettoet.create_train_parts.content.decoration.slidingWindow;

import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SlidingWindowModeSlot extends CenteredSideValueBoxTransform {
    public SlidingWindowModeSlot() {
        super((state, d) -> d == state.getValue(SlidingWindowBlock.FACING) || d == state.getValue(SlidingWindowBlock.FACING).getOpposite());
    }

    @Override
    protected Vec3 getSouthLocation() { return VecHelper.voxelSpace(8, 8, 2.5); }

    @Override
    public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
        Vec3 location = VecHelper.voxelSpace(8, 8, state.getValue(SlidingWindowBlock.FACING) == direction ? 2.5 : 15.5);
        location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
        location = VecHelper.rotateCentered(location, AngleHelper.verticalAngle(getSide()), Direction.Axis.X);
        return location;
    }

}
