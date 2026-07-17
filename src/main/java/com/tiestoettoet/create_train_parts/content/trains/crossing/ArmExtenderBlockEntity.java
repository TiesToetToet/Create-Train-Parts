package com.tiestoettoet.create_train_parts.content.trains.crossing;

import com.simibubi.create.AllSoundEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class ArmExtenderBlockEntity extends BlockEntity {

	private byte colour1 = 5;
	private byte colour2 = 0;

	public ArmExtenderBlockEntity(
		BlockEntityType<?> type,
		BlockPos pos,
		BlockState state
	) {
		super(type, pos, state);
	}

	public void copyNeighbourColours() {
		Level level = getLevel();
		BlockPos pos = getBlockPos();
		BlockState state = getBlockState();

		if (level.isClientSide)
			return;

		if (!(level.getBlockEntity(pos) instanceof ArmExtenderBlockEntity blockEntity))
			return;

		Direction facing = state.getValue(HORIZONTAL_FACING);

		BlockPos rightPos = pos.relative(facing.getClockWise());
		BlockEntity rightBE = level.getBlockEntity(rightPos);

		if (rightBE instanceof ArmExtenderBlockEntity rightArm) {
			blockEntity.setColours(rightArm.getColour1(), rightArm.getColour2());
			return;
		}

//		if (rightBE instanceof CrossingBlockEntity rightCrossing) {
//			blockEntity.setColours(rightCrossing.getColour1(), rightCrossing.getColour2());
//			return;
//		}

		BlockPos leftPos = pos.relative(facing.getCounterClockWise());
		BlockEntity leftBE = level.getBlockEntity(leftPos);

		if (leftBE instanceof ArmExtenderBlockEntity leftArm) {
			blockEntity.setColours(leftArm.getColour1(), leftArm.getColour2());
			return;
		}

//		if (leftBE instanceof CrossingBlockEntity leftCrossing) {
//			blockEntity.setColours(leftCrossing.getColour1(), leftCrossing.getColour2());
//		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);

		tag.putByte("Colour1", colour1);
		tag.putByte("Colour2", colour2);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);

		if (tag.contains("Colour1"))
			colour1 = tag.getByte("Colour1");
		if (tag.contains("Colour2"))
			colour2 = tag.getByte("Colour2");
	}

	@Override
	public CompoundTag getUpdateTag() {
		return saveWithoutMetadata();
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		load(tag);
	}

	public void playPlaceSound() {
		BlockPos pos = getBlockPos();
		AllSoundEvents.SLIME_ADDED.playOnServer(level, pos, .5f, 1);
	}

	public byte getColour1() {
		return colour1;
	}

	public byte getColour2() {
		return colour2;
	}

	public void setColour1(byte colour1) {
		this.colour1 = colour1;
		setChanged();
		playPlaceSound();

		if (level != null && !level.isClientSide)
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	public void setColour2(byte colour2) {
		this.colour2 = colour2;
		setChanged();
		playPlaceSound();

		if (level != null && !level.isClientSide)
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	public void setColours(byte colour1, byte colour2) {
		this.colour1 = colour1;
		this.colour2 = colour2;

		setChanged();
		playPlaceSound();

		if (level != null && !level.isClientSide) {
			level.sendBlockUpdated(
				worldPosition,
				getBlockState(),
				getBlockState(),
				2
			);
		}
	}
}
