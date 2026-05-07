package com.tiestoettoet.create_train_parts.foundation.mixin;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tiestoettoet.create_train_parts.foundation.block.connected.HorizontalCTBehaviour;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Supplier;

import static com.simibubi.create.foundation.data.CreateRegistrate.casingConnectivity;
import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

@Mixin(BuilderTransformers.class)
public class BuilderTransformersMixin {

    // Redirect the instantiation of HorizontalCTBehaviour in layeredCasing
    @Overwrite
    public static <B extends CasingBlock> NonNullUnaryOperator<BlockBuilder<B, CreateRegistrate>> layeredCasing(
            Supplier<CTSpriteShiftEntry> ct, Supplier<CTSpriteShiftEntry> ct2) {
        return b -> b.initialProperties(SharedProperties::stone)
                .transform(axeOrPickaxe())
                .blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
                        .cubeColumn(c.getName(), ct.get()
                                        .getOriginalResourceLocation(),
                                ct2.get()
                                        .getOriginalResourceLocation())))
                .onRegister(connectedTextures(() -> new HorizontalCTBehaviour(ct.get(), ct2.get())))
                .onRegister(casingConnectivity((block, cc) -> cc.makeCasing(block, ct.get())))
                .tag(AllTags.AllBlockTags.CASING.tag)
                .item()
                .tag(AllTags.AllItemTags.CASING.tag)
                .build();
    }

}