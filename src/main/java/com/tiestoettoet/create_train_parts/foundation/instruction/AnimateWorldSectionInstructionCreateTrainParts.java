package com.tiestoettoet.create_train_parts.foundation.instruction;

import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.AnimateElementInstruction;
import net.createmod.ponder.foundation.instruction.AnimateWorldSectionInstruction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class AnimateWorldSectionInstructionCreateTrainParts
        extends AnimateElementInstructionCreateTrainParts<WorldSectionElement> {

    private Vec3 initialRotation = null;

    public static AnimateWorldSectionInstructionCreateTrainParts rotate(ElementLink<WorldSectionElement> link,
            Vec3 rotation,
            int ticks) {
        return new AnimateWorldSectionInstructionCreateTrainParts(link, rotation, ticks,
                (wse, v) -> wse.setAnimatedRotation(v, ticks == 0), WorldSectionElement::getAnimatedRotation);
    }

    public static AnimateWorldSectionInstructionCreateTrainParts move(ElementLink<WorldSectionElement> link,
            Vec3 offset, int ticks) {
        return new AnimateWorldSectionInstructionCreateTrainParts(link, offset, ticks,
                (wse, v) -> wse.setAnimatedOffset(v, ticks == 0),
                WorldSectionElement::getAnimatedOffset);
    }

    protected AnimateWorldSectionInstructionCreateTrainParts(ElementLink<WorldSectionElement> link, Vec3 totalDelta,
            int ticks,
            BiConsumer<WorldSectionElement, Vec3> setter, Function<WorldSectionElement, Vec3> getter) {
        super(link, totalDelta, ticks, setter, getter);
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene); // Call parent's tick for basic functionality
        if (element == null)
            return;

        // Store initial rotation on first tick
        if (initialRotation == null) {
            initialRotation = getter.apply(element);
        }

        if (remainingTicks == 0) {
            Vec3 finalRotation = initialRotation.add(totalDelta);
            setter.accept(element, finalRotation);
            return;
        }

        // Calculate progress and apply easing
        float progress = (float) (totalTicks - remainingTicks) / totalTicks;
        float easedProgress = 0.5f * (1 - Mth.cos(Mth.PI * progress));

        // Calculate relative rotation from initial position
        Vec3 relativeDelta = totalDelta.scale(easedProgress);
        Vec3 newRotation = initialRotation.add(relativeDelta);

        setter.accept(element, newRotation);
    }

}
