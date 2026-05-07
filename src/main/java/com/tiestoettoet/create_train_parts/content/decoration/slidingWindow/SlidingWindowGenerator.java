package com.tiestoettoet.create_train_parts.content.decoration.slidingWindow;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.generators.ModelFile;

public class SlidingWindowGenerator extends SpecialBlockStateGen {
    private final String type;

    public SlidingWindowGenerator(String type) {
        this.type = type;
    }

    @Override
    protected int getXRotation(BlockState state) {
        return 0;
    }

    @Override
    protected int getYRotation(BlockState state) {
        return 0;
    }

    @Override
    public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
                                                BlockState state) {

        String path = "block/sliding_windows";

//        return prov.models()
//                .getExistingFile(prov.modLoc(path + "/" + "steps_" + (open ? "open" : "closed") + "_" + (connected.getSerializedName().equals("none") ? "" : connected.getSerializedName()) + "_" + facing.getSerializedName()));
        return prov.models()
                .getExistingFile(prov.modLoc(path + "/" + type));
    }


}
