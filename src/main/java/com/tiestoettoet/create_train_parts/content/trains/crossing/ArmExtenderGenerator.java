package com.tiestoettoet.create_train_parts.content.trains.crossing;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class ArmExtenderGenerator extends SpecialBlockStateGen {
    @Override
    protected int getXRotation(BlockState state) {
        return 0;
    }

    @Override
    protected int getYRotation(BlockState state) {
        return switch (state.getValue(ArmExtenderBlock.FACING)) {
            case NORTH -> 90;
            case SOUTH -> 270;
            case EAST -> 180;
            case WEST -> 0;
            default -> 90;
        };
    }

    @Override
    public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov, BlockState state) {
        boolean flipped = state.getValue(ArmExtenderBlock.FLIPPED);
        if (flipped) {
            return prov.models().getExistingFile(prov.modLoc("block/crossing/arm_extender_flipped"));
        } else {
            return prov.models().getExistingFile(prov.modLoc("block/crossing/arm_extender"));
        }
//        return null;
    }

//    {
//        "variants": {
//        "facing=north,flipped=false": { "model": "create_train_parts:block/crossing/arm_extender", "y": 90 },
//        "facing=north,flipped=true": { "model": "create_train_parts:block/crossing/arm_extender_flipped", "y": 90 },
//        "facing=south,flipped=false": { "model": "create_train_parts:block/crossing/arm_extender", "y": 270 },
//        "facing=south,flipped=true": { "model": "create_train_parts:block/crossing/arm_extender_flipped", "y": 270 },
//        "facing=east,flipped=false":  { "model": "create_train_parts:block/crossing/arm_extender", "y": 180 },
//        "facing=east,flipped=true":  { "model": "create_train_parts:block/crossing/arm_extender_flipped", "y": 180 },
//        "facing=west,flipped=false":  { "model": "create_train_parts:block/crossing/arm_extender", "y": 0 },
//        "facing=west,flipped=true":  { "model": "create_train_parts:block/crossing/arm_extender_flipped", "y": 0 }
//    }

}
