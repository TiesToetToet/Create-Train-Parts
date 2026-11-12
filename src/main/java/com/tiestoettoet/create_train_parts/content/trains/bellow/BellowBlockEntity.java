package com.tiestoettoet.create_train_parts.content.trains.bellow;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.tiestoettoet.create_train_parts.foundation.collision.BellowSize;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BellowBlockEntity extends SmartBlockEntity {

    public BellowBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public BellowSize getSize() {
        return BellowBlock.getSize(getBlockState());
    }

    @Override
    protected AABB createRenderBoundingBox() {
        BellowSize size = getSize();
        return new AABB(worldPosition).inflate(size.width(), 0, size.width())
                .expandTowards(0, size.height(), 0);
    }
}
