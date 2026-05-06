package com.tiestoettoet.create_train_parts.content.decoration;

import com.simibubi.create.content.decoration.TrainTrapdoorBlock;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.tiestoettoet.create_train_parts.content.decoration.slidingWindow.SlidingWindowBlockEntity;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;

public class SlidingWindowCTBehaviour extends ConnectedTextureBehaviour.Base {
    protected CTSpriteShiftEntry mainShift;

    public SlidingWindowCTBehaviour(CTSpriteShiftEntry mainShift) {
        this.mainShift = mainShift;
    }

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        if (direction == Direction.UP || direction == Direction.DOWN) {
            return null; // No shift for vertical faces
        }
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (direction == facing || direction == facing.getOpposite()) {
            return mainShift; // Apply the shifted texture
        }
        return null; // Keep other faces unchanged
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos,
                              BlockPos otherPos, Direction face, Direction primaryOffset, Direction secondaryOffset) {
        Direction facing;
        try {
            facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        } catch (Exception e) {
            facing = null; // Handle the case where the property is missing
        }

        Direction otherFacing;
        try {
            otherFacing = other.getValue(BlockStateProperties.HORIZONTAL_FACING);
        } catch (Exception e) {
            otherFacing = null; // Handle the case where the property is missing
        }
        BlockEntity blockEntity = reader.getBlockEntity(pos);
        SlidingWindowBlockEntity.SelectionMode mode = blockEntity instanceof SlidingWindowBlockEntity slidingWindowBlockEntity
                ? slidingWindowBlockEntity.getMode()
                : SlidingWindowBlockEntity.SelectionMode.UP;
        BlockEntity otherBlockEntity = reader.getBlockEntity(otherPos);
        SlidingWindowBlockEntity.SelectionMode otherMode = otherBlockEntity instanceof SlidingWindowBlockEntity slidingWindowBlockEntity
                ? slidingWindowBlockEntity.getMode()
                : SlidingWindowBlockEntity.SelectionMode.UP;
//
//        if (mode == SlidingWindowBlockEntity.SelectionMode.DOWN || mode == SlidingWindowBlockEntity.SelectionMode.UP) {
//            int yLevel = pos.getY();
//            int otherYLevel = otherPos.getY();
//            return facing == otherFacing && yLevel == otherYLevel && state.getBlock() == other.getBlock() && mode == otherMode;
//
//        } else if (mode == SlidingWindowBlockEntity.SelectionMode.LEFT || mode == SlidingWindowBlockEntity.SelectionMode.RIGHT) {
//            int xLevel = pos.getX();
//            int otherXLevel = otherPos.getX();
////            System.out.println("Facing: " + facing + ", Other Facing: " + otherFacing);
////            System.out.println("X Level: " + xLevel + ", Other X Level: " + otherXLevel);
////            System.out.println("State Block: " + state.getBlock() + ", Other Block: " + other.getBlock());
////            System.out.println("Mode: " + mode + ", Other Mode: " + otherMode);
//            return facing == otherFacing && xLevel == otherXLevel && state.getBlock() == other.getBlock() && mode == otherMode;
//        }
        return mode == otherMode && facing == otherFacing && state.getBlock() == other.getBlock();

//        return false;
    }


}
