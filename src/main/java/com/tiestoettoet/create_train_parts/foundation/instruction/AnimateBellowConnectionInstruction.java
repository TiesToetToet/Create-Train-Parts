package com.tiestoettoet.create_train_parts.foundation.instruction;

import com.tiestoettoet.create_train_parts.foundation.ponder.element.BellowConnectionElement;

import net.createmod.ponder.api.element.ElementLink;
import net.minecraft.world.phys.Vec3;

public class AnimateBellowConnectionInstruction
        extends AnimateElementInstructionCreateTrainParts<BellowConnectionElement> {

    public static AnimateBellowConnectionInstruction move(ElementLink<BellowConnectionElement> link, Vec3 offset,
            int ticks) {
        return new AnimateBellowConnectionInstruction(link, offset, ticks);
    }

    private AnimateBellowConnectionInstruction(ElementLink<BellowConnectionElement> link, Vec3 offset, int ticks) {
        super(link, offset, ticks, BellowConnectionElement::setOffset, BellowConnectionElement::getOffset);
    }
}
