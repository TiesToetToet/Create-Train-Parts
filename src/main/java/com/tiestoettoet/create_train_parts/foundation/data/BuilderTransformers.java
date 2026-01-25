package com.tiestoettoet.create_train_parts.foundation.data;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
//import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;
import com.simibubi.create.foundation.data.AssetLookup;
import static com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour.interactionBehaviour;

import com.simibubi.create.foundation.data.SharedProperties;
import com.tiestoettoet.create_train_parts.content.contraptions.behaviour.SlideMovingInteraction;
import com.tiestoettoet.create_train_parts.content.contraptions.behaviour.StepMovingInteraction;
import com.tiestoettoet.create_train_parts.content.contraptions.behaviour.WindowMovingInteraction;
import com.tiestoettoet.create_train_parts.content.decoration.SlidingWindowCTBehaviour;
import com.tiestoettoet.create_train_parts.content.decoration.encasing.EncasedCTBehaviour;
import com.tiestoettoet.create_train_parts.content.decoration.slidingWindow.SlidingWindowBlock;
import com.tiestoettoet.create_train_parts.content.decoration.slidingWindow.SlidingWindowGenerator;
import com.tiestoettoet.create_train_parts.content.decoration.slidingWindow.SlidingWindowMovementBehaviour;
import com.tiestoettoet.create_train_parts.content.decoration.trainSlide.TrainSlideBlock;
import com.tiestoettoet.create_train_parts.content.decoration.trainSlide.TrainSlideGenerator;
import com.tiestoettoet.create_train_parts.content.decoration.trainSlide.TrainSlideMovementBehaviour;
import com.tiestoettoet.create_train_parts.content.decoration.trainStep.TrainStepBlock;
import com.tiestoettoet.create_train_parts.content.decoration.trainStep.TrainStepGenerator;
import com.tiestoettoet.create_train_parts.content.decoration.trainStep.TrainStepMovementBehaviour;
import com.tiestoettoet.create_train_parts.foundation.block.connected.HorizontalCTBehaviour;
import com.tiestoettoet.create_train_parts.content.trains.crossing.ArmExtenderBlock;
import com.tiestoettoet.create_train_parts.content.trains.crossing.CrossingBlock;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import static com.simibubi.create.api.behaviour.movement.MovementBehaviour.movementBehaviour;
import static com.simibubi.create.foundation.data.CreateRegistrate.casingConnectivity;
import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.*;

public class BuilderTransformers {
    public static <B extends TrainStepBlock, P>NonNullUnaryOperator<BlockBuilder<B, P>> trainStep(String type, Supplier<CTSpriteShiftEntry> ct) {
        return trainStep(type, ct, null);
    }

    public static <B extends TrainStepBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> trainStep(String type, Supplier<CTSpriteShiftEntry> ct, @Nullable Supplier<CTSpriteShiftEntry> ct2) {
        return b -> b.initialProperties(SharedProperties::stone)
                .properties(p -> p.sound(SoundType.WOOD))
                .transform(axeOrPickaxe())
                .blockstate(new TrainStepGenerator(type)::generate)
//                .addLayer(() -> RenderType::cutoutMipped)
                .transform(axeOrPickaxe())
                .onRegister(connectedTextures(() -> ct2 != null ? new HorizontalCTBehaviour(ct.get(), ct2.get()) : new EncasedCTBehaviour(ct.get())))
                .onRegister(casingConnectivity((block, cc) -> cc.makeCasing(block, ct.get())))
                .onRegister(interactionBehaviour(new StepMovingInteraction()))
                .onRegister(movementBehaviour(new TrainStepMovementBehaviour()))
                .item()
                .tag(AllTags.AllItemTags.CONTRAPTION_CONTROLLED.tag)
//        .transform(customItemModel())
                .model(AssetLookup.customBlockItemModel("train_step_" + type, "steps"))
                .build();
    }

    public static <B extends TrainSlideBlock, P>NonNullUnaryOperator<BlockBuilder<B, P>> trainSlide(String type, Supplier<CTSpriteShiftEntry> ct) {
        return trainSlide(type, ct, null);
    }

    public static <B extends TrainSlideBlock, P>NonNullUnaryOperator<BlockBuilder<B, P>> trainSlide(String type, Supplier<CTSpriteShiftEntry> ct, @Nullable Supplier<CTSpriteShiftEntry> ct2) {
        return b -> b.initialProperties(SharedProperties::stone)
                .properties(p -> p.sound(SoundType.WOOD))
                .transform(axeOrPickaxe())
                .blockstate(new TrainSlideGenerator(type)::generate)
//                .addLayer(() -> RenderType::cutoutMipped)
                .transform(axeOrPickaxe())
                .onRegister(connectedTextures(() -> ct2 != null ? new HorizontalCTBehaviour(ct.get(), ct2.get()) : new EncasedCTBehaviour(ct.get())))
                .onRegister(casingConnectivity((block, cc) -> cc.makeCasing(block, ct.get())))
                .onRegister(interactionBehaviour(new SlideMovingInteraction()))
                .onRegister(movementBehaviour(new TrainSlideMovementBehaviour()))
//                .loot((lr, block) -> lr.add(block, lr.createDoorTable(block)))
                .item()
                .tag(AllTags.AllItemTags.CONTRAPTION_CONTROLLED.tag)
                .model(AssetLookup.customBlockItemModel("train_slide_" + type, "slide"))
                .build();
    }

    public static <B extends SlidingWindowBlock, P>NonNullUnaryOperator<BlockBuilder<B, P>> slidingWindow(String type, Supplier<CTSpriteShiftEntry> ct) {
        return b -> b.initialProperties(SharedProperties::stone)
                .properties(p -> p.sound(SoundType.WOOD))
                .transform(axeOrPickaxe())
                .blockstate(new SlidingWindowGenerator(type)::generate)
                .onRegister(connectedTextures(() -> new SlidingWindowCTBehaviour(ct.get())))
                .onRegister(interactionBehaviour(new WindowMovingInteraction()))
                .onRegister(movementBehaviour(new SlidingWindowMovementBehaviour()))
//                .loot((lr, block) -> lr.add(block, lr.createDoorTable(block)))
//                .addLayer(() -> RenderType::cutoutMipped)
                .item()
                .tag(AllTags.AllItemTags.CONTRAPTION_CONTROLLED.tag)
                .model(AssetLookup.customBlockItemModel("sliding_windows", type))
                .build();
    }

    public static <B extends CrossingBlock, P>NonNullUnaryOperator<BlockBuilder<B, P>> crossing() {
        return b -> b.initialProperties(SharedProperties::softMetal)
                .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
                .transform(axeOnly())
//                .loot((lr, block) -> lr.add(block, lr.createDoorTable(block)))
//                .addLayer(() -> RenderType::cutoutMipped)
//                .addLayer(() -> RenderType::translucent)
                .item()
                .model(AssetLookup.customBlockItemModel("crossing", "item"))
                .build();
    }

    public static <B extends ArmExtenderBlock, P>NonNullUnaryOperator<BlockBuilder<B, P>> armExtender() {
        return b -> b.initialProperties(SharedProperties::softMetal)
                .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
                .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
                .transform(axeOnly())
//                .addLayer(() -> RenderType::cutoutMipped)
                .item()
                .model(AssetLookup.customBlockItemModel("crossing", "arm_item"))
                .build();
    }


}
