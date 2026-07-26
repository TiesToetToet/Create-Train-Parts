package com.tiestoettoet.create_train_parts.foundation.collision;

/**
 * Size of a bellow in whole blocks. The authored model is one block wide and
 * two blocks tall, so every other size is expressed as a scale of that model.
 */
public record BellowSize(int width, int height) {

    public static final int MIN = 1;
    public static final int MAX = 3;

    /** Matches the dimensions the bellow model was authored with. */
    public static final BellowSize DEFAULT = new BellowSize(1, 2);

    private static final double MODEL_HALF_WIDTH = 8 / 16.0;
    private static final double MODEL_BOTTOM = -3.5 / 16.0;
    private static final double MODEL_TOP = 28.5 / 16.0;

    /**
     * Half thickness of a collision member. The members are pushed outwards by
     * this amount so that the walkable interior of the bellow stays clear and
     * collisions always resolve away from it.
     */
    public static final double MEMBER_HALF_THICKNESS = 0.25;

    /** Vertical offset of the authored model relative to its curve anchor. */
    private static final double BASE_VERTICAL_OFFSET = 1.0;

    /** Width first, matching how the sizes are named in game. */
    public String format() {
        return width + "x" + height;
    }

    public float widthScale() {
        return width;
    }

    public float heightScale() {
        return height / 2f;
    }

    public double halfWidth() {
        return MODEL_HALF_WIDTH * width;
    }

    public double bottom() {
        return MODEL_BOTTOM * heightScale();
    }

    public double top() {
        return MODEL_TOP * heightScale();
    }

    /** Keeps the base of the frame in place no matter how tall it becomes. */
    public double verticalOffset() {
        return BASE_VERTICAL_OFFSET + MODEL_BOTTOM - bottom();
    }

    /** Vertical offset expressed in the scaled model's own space. */
    public double localVerticalOffset() {
        return verticalOffset() / heightScale();
    }
}
