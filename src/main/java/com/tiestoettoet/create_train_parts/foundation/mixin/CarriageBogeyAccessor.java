package com.tiestoettoet.create_train_parts.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.simibubi.create.content.trains.entity.CarriageBogey;

import net.createmod.catnip.animation.LerpedFloat;

@Mixin(CarriageBogey.class)
public interface CarriageBogeyAccessor {

    @Accessor("yaw")
    LerpedFloat createTrainParts$getYaw();
}
