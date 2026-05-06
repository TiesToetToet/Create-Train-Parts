package com.tiestoettoet.create_train_parts.foundation.mixin;

import net.createmod.catnip.render.DefaultSuperByteBuffer;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SuperByteBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(DefaultSuperByteBuffer.class)
public class DefaultSuperByteBufferMixin {
    @Shadow
    @Nullable
    protected SuperByteBuffer.SpriteShiftFunc spriteShiftFunc;

    @Overwrite
    public DefaultSuperByteBuffer shiftUVtoSheet(SpriteShiftEntry entry, float uTarget, float vTarget, int sheetSize) {
        System.out.println("Shifting UVs to sheet with target: " + entry.getTarget() + ", uTarget: " + uTarget + ", vTarget: " + vTarget + ", sheetSize: " + sheetSize);
        spriteShiftFunc = (u, v, output) -> {
            float targetU = entry.getTarget()
                    .getU((SpriteShiftEntry.getUnInterpolatedU(entry.getOriginal(), u) / sheetSize) + uTarget * 16);
            float targetV = entry.getTarget()
                    .getV((SpriteShiftEntry.getUnInterpolatedV(entry.getOriginal(), v) / sheetSize) + vTarget * 16);
            output.accept(targetU, targetV);
        };
        return (DefaultSuperByteBuffer) (Object) this;
    }
}
