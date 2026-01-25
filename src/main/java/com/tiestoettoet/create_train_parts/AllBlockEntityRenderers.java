package com.tiestoettoet.create_train_parts;

import com.tiestoettoet.create_train_parts.content.decoration.slidingWindow.SlidingWindowBlockEntity;
import com.tiestoettoet.create_train_parts.content.decoration.slidingWindow.SlidingWindowRenderer;
import com.tiestoettoet.create_train_parts.content.decoration.trainSlide.TrainSlideBlockEntity;
import com.tiestoettoet.create_train_parts.content.decoration.trainSlide.TrainSlideRenderer;
import com.tiestoettoet.create_train_parts.content.decoration.trainStep.TrainStepBlockEntity;
import com.tiestoettoet.create_train_parts.content.decoration.trainStep.TrainStepRenderer;
import com.tiestoettoet.create_train_parts.content.trains.crossing.CrossingBlockEntity;
import com.tiestoettoet.create_train_parts.content.trains.crossing.CrossingRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Client-only class that provides block entity renderer factories.
 * This class isolates all renderer class references to prevent them from being loaded on the server.
 */
@OnlyIn(Dist.CLIENT)
public class AllBlockEntityRenderers {

    public static BlockEntityRenderer<TrainStepBlockEntity> trainStepRenderer(BlockEntityRendererProvider.Context context) {
        return new TrainStepRenderer();
    }

    public static BlockEntityRenderer<TrainSlideBlockEntity> trainSlideRenderer(BlockEntityRendererProvider.Context context) {
        return new TrainSlideRenderer();
    }

    public static BlockEntityRenderer<SlidingWindowBlockEntity> slidingWindowRenderer(BlockEntityRendererProvider.Context context) {
        return new SlidingWindowRenderer();
    }

    public static BlockEntityRenderer<CrossingBlockEntity> crossingRenderer(BlockEntityRendererProvider.Context context) {
        return new CrossingRenderer(context);
    }
}
