package com.tiestoettoet.create_train_parts.content.decoration.trainStep;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.neoforged.neoforge.client.model.generators.ModelFile;

public class TrainStepGenerator extends SpecialBlockStateGen {
    private final String type;

    public TrainStepGenerator(String type) {
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
        Direction facing = state.getValue(TrainStepBlock.FACING);
        boolean open = state.getValue(TrainStepBlock.OPEN);
        TrainStepBlock.ConnectedState connected = state.getValue(TrainStepBlock.CONNECTED);

        String path = "block/train_step_" + type;

//        return prov.models()
//                .getExistingFile(prov.modLoc(path + "/" + "steps_" + (open ? "open" : "closed") + "_" + (connected.getSerializedName().equals("none") ? "" : connected.getSerializedName()) + "_" + facing.getSerializedName()));
        return prov.models()
                .getExistingFile(prov.modLoc(path + "/" + "steps"));
    }


}
