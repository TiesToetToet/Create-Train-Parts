package com.tiestoettoet.create_train_parts.content.trains.bellow;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.createmod.catnip.animation.AnimationTickHolder;

public class BellowMovementBehaviour implements MovementBehaviour {
    @Override
    public void tick(MovementContext context) {
//        System.out.println("BellowMB tick: " + context.position + " in " + context.world);
        if (!context.world.isClientSide()) {
            return;
        }

        float partialTicks = AnimationTickHolder.getPartialTicks();
        BellowBlockEntity.SegmentData data = BellowBlockEntity.computeSegmentData(
                context.world,
                context.position,
                partialTicks,
                true);

//        if (data != null) {
////            System.out.println("BellowMB AABBs: "
////                    + BellowBlockEntity.getAllAABBs(data.positions(), data.yRots(), data.xRots()));
//        }
    }
}
