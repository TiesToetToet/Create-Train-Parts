package com.tiestoettoet.create_train_parts.content.decoration.encasing;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.decoration.encasing.CasingConnectivity;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class EncasedCTBehaviour extends ConnectedTextureBehaviour.Base {

    private CTSpriteShiftEntry shift;

    public EncasedCTBehaviour(CTSpriteShiftEntry shift) {
        this.shift = shift;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos,
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
        return shift;
    }

}
