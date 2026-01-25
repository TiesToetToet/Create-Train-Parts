package com.tiestoettoet.create_train_parts.foundation.block.connected;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.decoration.encasing.CasingConnectivity;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class HorizontalCTBehaviour extends com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour {

    protected CTSpriteShiftEntry topShift;
    protected CTSpriteShiftEntry layerShift;

    public HorizontalCTBehaviour(CTSpriteShiftEntry layerShift) {
        this(layerShift, null);
    }

    public HorizontalCTBehaviour(CTSpriteShiftEntry layerShift, CTSpriteShiftEntry topShift) {
        super(layerShift, topShift);
        this.layerShift = layerShift;
        this.topShift = topShift;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos,
            BlockPos otherPos,
            Direction face) {
        if (isBeingBlocked(state, reader, pos, otherPos, face))
            return false;
        CasingConnectivity cc = CreateClient.CASING_CONNECTIVITY;
        CasingConnectivity.Entry entry = cc.get(state);
        CasingConnectivity.Entry otherEntry = cc.get(other);
        if (entry == null || otherEntry == null)
            return false;
        if (!entry.isSideValid(state, face) || !otherEntry.isSideValid(other, face))
            return false;
        return entry.getCasing() == otherEntry.getCasing();
    }

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        return direction.getAxis()
                .isHorizontal() ? layerShift : topShift;
    }

}