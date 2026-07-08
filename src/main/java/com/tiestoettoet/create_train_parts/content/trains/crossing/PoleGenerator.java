package com.tiestoettoet.create_train_parts.content.trains.crossing;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.generators.ModelFile;

public class PoleGenerator extends SpecialBlockStateGen {
    @Override
    protected int getXRotation(BlockState state) {
        return 0;
    }

    @Override
    protected int getYRotation(BlockState state) {
        switch (state.getValue(PoleBlock.HORIZONTAL_FACING)) {
            case NORTH -> {
                return 0;
            }
            case EAST -> {
                return 90;
            }
            case SOUTH -> {
                return 180;
            }
            case WEST -> {
                return 270;
            }
        }
        return 0;
    }

    @Override
    public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov, BlockState state) {
        boolean base = state.getValue(PoleBlock.BASE);
        boolean top = state.getValue(PoleBlock.TOP);
        if (base && top) {
            return prov.models()
                    .getExistingFile(prov.modLoc("block/crossing/crossing_pole_base_top"));
        } else if (base) {
            return prov.models()
                    .getExistingFile(prov.modLoc("block/crossing/crossing_pole_base"));
        } else if (top) {
            return prov.models()
                    .getExistingFile(prov.modLoc("block/crossing/crossing_pole_top"));
        } else {
            return prov.models()
                    .getExistingFile(prov.modLoc("block/crossing/crossing_pole"));
        }
    }
}
