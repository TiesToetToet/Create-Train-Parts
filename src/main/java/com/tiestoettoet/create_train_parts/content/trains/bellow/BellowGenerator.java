package com.tiestoettoet.create_train_parts.content.trains.bellow;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.generators.ModelFile;

/**
 * Emits the facing variants for every bellow size. The frame itself is drawn by
 * {@link BellowBlockRenderer}, so all sizes share one model for particles.
 */
public class BellowGenerator extends SpecialBlockStateGen {

    @Override
    protected Property<?>[] getIgnoredProperties() {
        return new Property<?>[] { BellowBlock.VISIBLE, BellowBlock.WIDTH, BellowBlock.HEIGHT };
    }

    @Override
    protected int getXRotation(BlockState state) {
        return 0;
    }

    @Override
    protected int getYRotation(BlockState state) {
        return switch (state.getValue(BellowBlock.FACING)) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }

    @Override
    public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
            BlockState state) {
        return prov.models()
                .getExistingFile(prov.modLoc("block/bellow/bellow_end_bottom"));
    }
}
