package com.tiestoettoet.create_train_parts.foundation.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.tiestoettoet.create_train_parts.content.decoration.slidingWindow.SlidingWindowBlock;
import com.tiestoettoet.create_train_parts.content.decoration.trainSlide.TrainSlideBlock;
import com.tiestoettoet.create_train_parts.content.decoration.trainStep.TrainStepBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Contraption.class)
public abstract class ContraptionMixin {

    /**
     * Modifies the captured block state to hide TrainStep, TrainSlide, and SlidingWindow blocks
     * similar to how Create handles SlidingDoorBlock.VISIBLE
     */
    @Inject(method = "capture", at = @At("RETURN"), cancellable = true)
    private void modifyCapturedState(Level world, BlockPos pos, CallbackInfoReturnable<Pair<StructureTemplate.StructureBlockInfo, BlockEntity>> cir) {
        Pair<StructureTemplate.StructureBlockInfo, BlockEntity> result = cir.getReturnValue();
        if (result == null) return;
        
        StructureTemplate.StructureBlockInfo info = result.getLeft();
        BlockState state = info.state();
        boolean modified = false;
        
        if (state.hasProperty(TrainStepBlock.VISIBLE)) {
            state = state.setValue(TrainStepBlock.VISIBLE, false);
            modified = true;
        }
        if (state.hasProperty(TrainSlideBlock.VISIBLE)) {
            state = state.setValue(TrainSlideBlock.VISIBLE, false);
            modified = true;
        }
        if (state.hasProperty(SlidingWindowBlock.VISIBLE)) {
            state = state.setValue(SlidingWindowBlock.VISIBLE, false);
            modified = true;
        }
        
        if (modified) {
            cir.setReturnValue(Pair.of(
                new StructureTemplate.StructureBlockInfo(info.pos(), state, info.nbt()),
                result.getRight()
            ));
        }
    }

    /**
     * Modifies the block state when placing blocks back into the world during disassembly.
     * Sets VISIBLE based on OPEN state and resets POWERED, similar to SlidingDoorBlock handling.
     */
    @ModifyExpressionValue(
        method = "addBlocksToWorld",
        at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/StructureTransform;apply(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;")
    )
    private BlockState modifyStateOnDisassembly(BlockState state) {
        if (state.hasProperty(TrainStepBlock.VISIBLE)) {
            state = state.setValue(TrainStepBlock.VISIBLE, !state.getValue(TrainStepBlock.OPEN))
                    .setValue(TrainStepBlock.POWERED, false);
        }
        if (state.hasProperty(TrainSlideBlock.VISIBLE)) {
            state = state.setValue(TrainSlideBlock.VISIBLE, !state.getValue(TrainSlideBlock.OPEN))
                    .setValue(TrainSlideBlock.POWERED, false);
        }
        if (state.hasProperty(SlidingWindowBlock.VISIBLE)) {
            state = state.setValue(SlidingWindowBlock.VISIBLE, !state.getValue(SlidingWindowBlock.OPEN))
                    .setValue(SlidingWindowBlock.POWERED, false);
        }
        return state;
    }

    /**
     * Prevents TrainStepBlock, TrainSlideBlock, and SlidingWindowBlock from being updated after movement
     * (same behavior as SlidingDoorBlock in Create)
     */
    @Inject(method = "shouldUpdateAfterMovement", at = @At("HEAD"), cancellable = true)
    private void skipUpdateForTrainParts(StructureTemplate.StructureBlockInfo info, CallbackInfoReturnable<Boolean> cir) {
        if (info.state().getBlock() instanceof TrainStepBlock) {
            cir.setReturnValue(false);
        }
        if (info.state().getBlock() instanceof TrainSlideBlock) {
            cir.setReturnValue(false);
        }
        if (info.state().getBlock() instanceof SlidingWindowBlock) {
            cir.setReturnValue(false);
        }
    }
}
