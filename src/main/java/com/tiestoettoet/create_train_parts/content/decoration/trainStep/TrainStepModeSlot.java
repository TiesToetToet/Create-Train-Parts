package com.tiestoettoet.create_train_parts.content.decoration.trainStep;

import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import net.minecraft.core.Direction;


public class TrainStepModeSlot extends CenteredSideValueBoxTransform {
    public TrainStepModeSlot() {
        super((state, d) -> d == Direction.NORTH || d == Direction.SOUTH || d == Direction.EAST || d == Direction.WEST);
    }

}