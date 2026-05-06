package com.tiestoettoet.create_train_parts.content.decoration.trainStep;

import net.minecraft.util.StringRepresentable;

public enum TrainStepType implements StringRepresentable {
    ANDESITE,
    BRASS,
    COPPER,
    TRAIN;

    // private final String name;

    // private TrainStepType(String name) {
    // this.name = name;
    // }

    // public String toString() {
    // return this.name;
    // }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(); // Converts enum names to lowercase strings
    }

}
