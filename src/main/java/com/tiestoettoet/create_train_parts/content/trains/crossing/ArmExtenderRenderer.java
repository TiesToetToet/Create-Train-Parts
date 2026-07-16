package com.tiestoettoet.create_train_parts.content.trains.crossing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlock;
import com.tiestoettoet.create_train_parts.AllPartialModels;
import com.tiestoettoet.create_train_parts.AllSpriteShifts;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;


public class ArmExtenderRenderer implements BlockEntityRenderer<ArmExtenderBlockEntity> {

	public ArmExtenderRenderer(BlockEntityRendererProvider.Context context) {
	}


	@Override
	public void render(
		ArmExtenderBlockEntity be,
		float partialTicks,
		PoseStack ms,
		MultiBufferSource buffer,
		int light,
		int overlay
	) {

		BlockState state = be.getBlockState();

		boolean flipped = state.getValue(ArmExtenderBlock.FLIPPED);
		Direction facing = state.getValue(ArmExtenderBlock.FACING);

		PartialModel colour1Part;
		PartialModel colour2Part;

		if (flipped) {
			colour1Part = AllPartialModels.ARM_EXTENDER_FLIPPED_1;
			colour2Part = AllPartialModels.ARM_EXTENDER_FLIPPED_2;
		} else {
			colour1Part = AllPartialModels.ARM_EXTENDER_1;
			colour2Part = AllPartialModels.ARM_EXTENDER_2;
		}

		VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

		float rotation = switch (facing) {
			case WEST -> 0;
			case NORTH -> 270;
			case EAST -> 180;
			case SOUTH -> 90;
			default -> 0;
		};

//		System.out.println("Rendering ArmExtenderBlockEntity with colour1: " + be.getColour1() + ", colour2: " + be.getColour2() + ", rotation: " + rotation);

		CachedBuffers.partial(colour1Part, state)
			.shiftUVtoSheet(
				AllSpriteShifts.ARM_COLOURS,
				getColourU(be.getColour1()),
				getColourV(be.getColour1()),
				1
			)
			.rotateCentered(Mth.DEG_TO_RAD * rotation, Direction.Axis.Y)
			.light(light)
			.renderInto(ms, vb);

		CachedBuffers.partial(colour2Part, state)
			.shiftUVtoSheet(
				AllSpriteShifts.ARM_COLOURS,
				getColourU(be.getColour2()),
				getColourV(be.getColour2()),
				1
			)
			.rotateCentered(Mth.DEG_TO_RAD * rotation, Direction.Axis.Y)
			.light(light)
			.renderInto(ms, vb);
	}

	private float getColourU(byte colour) {
		float column = colour % 4;
		float u = (column) / 4f;
		return u;
	}


	private float getColourV(byte colour) {
		float row = Math.floorDiv(colour, 4);
		float v = (row) / 4f;
		return v;
	}

}
