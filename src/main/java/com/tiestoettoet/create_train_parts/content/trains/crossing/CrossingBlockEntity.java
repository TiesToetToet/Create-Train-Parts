package com.tiestoettoet.create_train_parts.content.trains.crossing;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.*;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.tiestoettoet.create_train_parts.foundation.gui.AllIcons;
import com.tiestoettoet.create_train_parts.foundation.sound.SoundScapes;
import com.tiestoettoet.create_train_parts.foundation.utility.CreateTrainPartsLang;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;
import static com.tiestoettoet.create_train_parts.content.trains.crossing.CrossingBlock.BELL;
import static com.tiestoettoet.create_train_parts.content.trains.crossing.CrossingBlock.OPEN;

public class CrossingBlockEntity extends KineticBlockEntity implements IControlContraption {
    LerpedFloat animation;
    int bridgeTicks;
    public boolean running;
    protected float angle;
    protected double sequencedAngleLimit;
    protected boolean assembleNextTick;
    protected ScrollOptionBehaviour<CrossingBarrierMode> barrierMode;
    public int bellTicks = 0;
    public BellState bellState = BellState.OFF;
    public float bellFade = 0;

	private byte colour1 = 5;
	private byte colour2 = 0;


	public ControlledContraptionEntity movedContraption;
    // boolean deferUpdate;
    // Map<String, BlockState> neighborStates = new HashMap<>();

    protected AssemblyException lastException;
    Object openObj = null;

    public CrossingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        animation = LerpedFloat.linear()
                .startWithValue(isOpen(getBlockState()) ? 1 : 0);
        sequencedAngleLimit = -1;
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        lastException = AssemblyException.read(tag, registries);
        super.read(tag, registries, clientPacket);
        invalidateRenderBoundingBox();

        if (tag.contains("ForceOpen"))
            openObj = tag.getBoolean("ForceOpen");

		bellTicks = tag.getInt("BellTicks");
		bellFade = tag.getFloat("BellFade");

		int state = tag.getInt("BellState");
		if (state >= 0 && state < BellState.values().length)
			bellState = BellState.values()[state];
		else
			bellState = BellState.OFF;

		if (tag.contains("Colour1"))
			colour1 = tag.getByte("Colour1");

		if (tag.contains("Colour2"))
			colour2 = tag.getByte("Colour2");
    }

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);

		tag.putInt("BellTicks", bellTicks);
		tag.putInt("BellState", bellState.ordinal());
		tag.putFloat("BellFade", bellFade);
		tag.putByte("Colour1", colour1);
		tag.putByte("Colour2", colour2);
	}

    public void assemble() {
//        System.out.println("CrossingBlockEntity.assemble() called");
        if (!(level.getBlockState(worldPosition)
                .getBlock() instanceof CrossingBlock))
            return;

        Direction direction = getBlockState().getValue(HORIZONTAL_FACING);
        CrossingContraption contraption = new CrossingContraption(direction);
//        System.out.println("Created CrossingContraption with direction: " + direction);
        try {
            boolean assembleResult = contraption.assemble(level, worldPosition);
//            System.out.println("Contraption assemble result: " + assembleResult);
            if (!assembleResult)
                return;
            lastException = null;
        } catch (AssemblyException e) {
//            System.out.println("Assembly exception: " + e.getMessage());
            lastException = e;
            sendData();
            return;
        }

        contraption.removeBlocksFromWorld(level, BlockPos.ZERO);
        movedContraption = ControlledContraptionEntity.create(level, this, contraption);
        BlockPos anchor = worldPosition;
        movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
        movedContraption.setRotationAxis(direction.getAxis());
        level.addFreshEntity(movedContraption);

        // System.out.println(movedContraption);

        AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(level, worldPosition);

        if (contraption.containsBlockBreakers())
            award(AllAdvancements.CONTRAPTION_ACTORS);

        running = true;
        angle = 90;
        sendData();
    }

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		return saveWithoutMetadata(provider);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
		loadWithComponents(tag, provider);
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

		if (level != null && !level.isClientSide)
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	public void setColour2(byte colour2) {
		this.colour2 = colour2;
		setChanged();

		if (level != null && !level.isClientSide)
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

    public void disassemble() {
//        System.out.println(
//                "Disassemble called - running: " + running + ", movedContraption: " + (movedContraption != null));
        if (!running && movedContraption == null)
            return;
        angle = 0;
        sequencedAngleLimit = -1;
        if (movedContraption != null) {
//            System.out.println("Disassembling contraption");
            movedContraption.disassemble();
            AllSoundEvents.CONTRAPTION_DISASSEMBLE.playOnServer(level, worldPosition);
        }

        movedContraption = null;
        running = false;
        assembleNextTick = false;
        sendData();
    }

    @Override
    public void tick() {
        super.tick();
        BlockState block = getBlockState();
        boolean open = isOpen(getBlockState());

        // Get speed from the kinetic network (this block receives power)
        float speed = Math.abs(getSpeed());
        boolean shouldOpen = getSpeed() < 0;

        if (speed < 0) {
            speed = -speed;
        }
        speed = speed / 50f * 0.05f * 0.25f;



        float targetValue = shouldOpen ? 0 : 1;
        animation.chase(targetValue, speed, LerpedFloat.Chaser.LINEAR);
        animation.tickChaser();

        boolean moving = !animation.settled();
        boolean opening = moving && animation.getChaseTarget() == 1;
        boolean closing = moving && animation.getChaseTarget() == 0;
        boolean opened = !moving && animation.getValue() > 0.99f;
        boolean closed = !moving && animation.getValue() < 0.01f;
        switch (bellState) {
            case OFF:
                if (closing && speed != 0) {
                    bellState = BellState.RINGING;
                    bellTicks = 0;
                }
                break;

            case RINGING:
				if (speed == 0) {
					bellState = BellState.FADING;
					bellFade = 1f;
					break;
				}

                bellTicks++;
                if (opened) {
                    bellState = BellState.FADING;
                    bellFade = 1f;
                }
                break;

            case FADING:
                bellTicks++;
                bellFade = Math.max(0, bellFade - 0.04f);

                if (bellFade == 0)
                    bellState = BellState.OFF;
                break;
        }
        if (!block.getValue(BELL)) {
            bellState = BellState.OFF;
        }

        if (level.isClientSide()) {
            if (bridgeTicks < 2 && open)
                bridgeTicks++;
            else if (bridgeTicks > 0 && !open)
                bridgeTicks--;
        }

        if (animation.settled() && open == (animation.getValue() != 0)) {
            return;
        }

		if (level.isClientSide)
			CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> this.tickAudio());

        block = block.setValue(OPEN, animation.getValue() != 0);
        level.setBlock(worldPosition, block, 10);

        if (!level.isClientSide) {
            assembleNextTick = false;
            if (running) {
                // Disassemble when speed is 0 OR when animation reaches the "closed" state
                // For shouldOpen=true (negative speed), closed is when animation < 0.1
                // For shouldOpen=false (positive speed), closed is when animation > 0.9 (since
                // target is 1)
                // boolean isInClosedState = shouldOpen ?
                // (animation.settled() && animation.getValue() < 0.1f) :
                // (animation.settled() && animation.getValue() > 0.9f);

                if (animation.getValue() == 0) {
//                    System.out.println("Disassembling: speed=" + speed + ", shouldOpen=" + shouldOpen + ", animValue="
//                            + animation.getValue() + ", settled=" + animation.settled());
                    if (movedContraption != null)
                        movedContraption.getContraption()
                                .stop(level);
                    disassemble();
                    return;
                }
            } else {
                if (speed == 0) {
                    return;
                }
                // Only assemble when we have speed AND animation is not in closed position
                boolean isInClosedState = shouldOpen ? (animation.settled() && animation.getValue() < 0.1f)
                        : (animation.settled() && animation.getValue() > 0.9f);

                if (!isInClosedState) {
//                    System.out.println("Assembling: speed=" + speed + ", shouldOpen=" + shouldOpen + ", animValue="
//                            + animation.getValue() + ", settled=" + animation.settled());
                    assemble();
                }
            }
        }

        if (movedContraption == null)
            return;

        // Apply the same smooth animation curve as in the renderer
        float rawValue = animation.getValue();
        float smoothValue = 0.5f * (1 - Mth.cos(Mth.PI * rawValue));

        // Convert radians to degrees: 1.56 radians ≈ 89.4 degrees (roughly 90 degrees)
        float angleInRadians = (float) (1.56 * smoothValue);
        float angleInDegrees = (float) Math.toDegrees(angleInRadians);

        // Apply direction-specific angle signs
        Direction facing = getBlockState().getValue(HORIZONTAL_FACING);
        float finalAngle;
        switch (facing) {
            case EAST:
            case SOUTH:
                // East and South need negative angles
                finalAngle = -angleInDegrees;
                break;
            case NORTH:
            case WEST:
                // North and West need positive angles
                finalAngle = angleInDegrees;
                break;
            default:
                finalAngle = -angleInDegrees; // fallback
                break;
        }

        movedContraption.setAngle(finalAngle);
    }

	@OnlyIn(Dist.CLIENT)
	public void tickAudio() {
		boolean ringing = bellState == BellState.RINGING;
        if (ringing)
            SoundScapes.play(SoundScapes.AmbienceGroup.CROSSING, worldPosition, 1.0f);
	}

    public void onBellAdded() {
        if (bellState == BellState.OFF && !isOpen(getBlockState())) {
            bellState = BellState.RINGING;
            bellTicks = 0;
        }
    }

	public void addBell() {
		BlockState blockState = getBlockState();
		if (blockState.getBlock() instanceof CrossingBlock) {
			level.setBlock(worldPosition, blockState.setValue(BELL, true), 3);
		}
	}

	public void onBellRemoved() {
		bellState = BellState.OFF;
	}

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(1);
    }

    protected boolean shouldRenderSpecial(BlockState state) {
        return true;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        BlockState state = getBlockState();
		barrierMode = new ScrollOptionBehaviour<>(CrossingBarrierMode.class,
			CreateTrainPartsLang.translateDirect("crossing.mode"), this, getBarrierModeSlot());
		barrierMode.withCallback($ -> onBarrierModeChanged());
		behaviours.add(barrierMode);
		barrierMode.requiresWrench();
	}

    private void onBarrierModeChanged() {
        if (level == null)
            return;

        Direction facing = getBlockState().getValue(HORIZONTAL_FACING);
        boolean barrierModeActive = barrierMode.get() == CrossingBarrierMode.BARRIER;
        Direction direction = facing.getClockWise(); // Arms are always placed in the clockwise direction relative to facing

        // While assembled the arms no longer exist in the world, so the change
        // has to be applied to the copies the contraption is carrying.
        if (movedContraption != null)
            updateArmsInContraption(direction, barrierModeActive);
        else
            updateArmsInWorld(direction, barrierModeActive);

        //set the barrier mode to the blockstate aswell
        BlockState blockState = level.getBlockState(worldPosition);
        if (blockState.getBlock() instanceof CrossingBlock) {
            level.setBlock(worldPosition, blockState.setValue(CrossingBlock.BARRIER, barrierModeActive), 3);
        }
    }

    private void updateArmsInWorld(Direction direction, boolean barrierModeActive) {
        BlockPos armPos = worldPosition.relative(direction);
        while (level.getBlockState(armPos).getBlock() instanceof ArmExtenderBlock) {
            BlockState currentArmState = level.getBlockState(armPos);
            BlockState newArmState = currentArmState
                    .setValue(ArmExtenderBlock.BARRIER, barrierModeActive);
            level.setBlock(armPos, newArmState, 3);
            armPos = armPos.relative(direction);
        }
    }

    private void updateArmsInContraption(Direction direction, boolean barrierModeActive) {
        if (level.isClientSide())
            return;

        Contraption contraption = movedContraption.getContraption();
        if (contraption == null)
            return;

        // The crossing anchors the contraption, so its arms sit at plain
        // offsets from the origin of the stored block map.
        for (BlockPos localPos = BlockPos.ZERO.relative(direction);; localPos = localPos.relative(direction)) {
            StructureTemplate.StructureBlockInfo info = contraption.getBlocks()
                    .get(localPos);
            if (info == null || !(info.state().getBlock() instanceof ArmExtenderBlock))
                return;
            if (!info.state().hasProperty(ArmExtenderBlock.BARRIER))
                return;

            BlockState newState = info.state()
                    .setValue(ArmExtenderBlock.BARRIER, barrierModeActive);
            movedContraption.setBlock(localPos,
                    new StructureTemplate.StructureBlockInfo(info.pos(), newState, info.nbt()));
        }
    }

    public static boolean isOpen(BlockState state) {
        return state.getOptionalValue(OPEN)
                .orElse(false);
    }

    protected ValueBoxTransform getBarrierModeSlot() {
        return new CrossingValueBoxTransform();
    }

    private class CrossingValueBoxTransform extends CenteredSideValueBoxTransform {

        public CrossingValueBoxTransform() {
            super((state, d) ->
				(d == state.getValue(HORIZONTAL_FACING)
					|| d == state.getValue(HORIZONTAL_FACING).getOpposite()
					|| d == state.getValue(HORIZONTAL_FACING).getClockWise()));
        }

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 18, 10.5);
        }

    }

    public enum CrossingBarrierMode implements INamedIconOptions {
        NO_BARRIER(AllIcons.I_CROSSING_NO_BARRIER),
        BARRIER(AllIcons.I_CROSSING_BARRIER),
        ;

        private String translationKey;
        private AllIcons icon;

        CrossingBarrierMode(AllIcons icon) {
            this.icon = icon;
            translationKey = "crossing.mode." + Lang.asId(name());
        }

        @Override
        public AllIcons getIcon() {
            return icon;
        }

        @Override
        public String getTranslationKey() {
            return translationKey;
        }
    }

    @Override
    public boolean isAttachedTo(AbstractContraptionEntity contraption) {
        return movedContraption == contraption;
    }

    @Override
    public void attach(ControlledContraptionEntity contraption) {
        this.movedContraption = contraption;
        if (!level.isClientSide) {
            this.running = true;
            sendData();
        }
    }

    @Override
    public void onStall() {
        if (!level.isClientSide) {
            sendData();
        }
    }

    @Override
    public boolean isValid() {
        return !isRemoved();
    }

    @Override
    public BlockPos getBlockPosition() {
        return worldPosition;
    }

    public enum BellState {
        OFF,
        RINGING,
        FADING
    }


}
