package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock;
import com.jesz.createdieselgenerators.compat.computercraft.CCProxy;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.jesz.createdieselgenerators.sounds.SoundRegistry;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

import java.util.List;

import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.*;

public class DieselGeneratorBlockEntity extends GeneratingKineticBlockEntity implements SidedStorageBlockEntity {

    public SmartFluidTankBehaviour tank;
    int partialSecond;
    BlockState state;
    public boolean validFuel;
    public AbstractComputerBehaviour computerBehaviour;
    public ScrollOptionBehaviour<WindmillBearingBlockEntity.RotationDirection> movementDirection;

    public DieselGeneratorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        this.state = state;
    }

    @Nullable
    @Override
    public Storage<FluidVariant> getFluidStorage(@Nullable Direction face) {
        if(state.getValue(FACING) == Direction.DOWN) {
            if (face == Direction.WEST)
                return tank.getCapability();
            if (face == Direction.EAST)
                return tank.getCapability();
        }else if(state.getValue(FACING) == Direction.UP){
            if (face == Direction.NORTH)
                return tank.getCapability();
            if (face == Direction.SOUTH)
                return tank.getCapability();
        }else{
            if (face == Direction.DOWN)
                return tank.getCapability();
        }


        if (face != Direction.DOWN) {
            return tank.getCapability();
        }
        return null;
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putInt("PartialSecond", partialSecond);
        tank.write(compound, false);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        partialSecond = compound.getInt("PartialSecond");
        tank.read(compound, false);
    }


    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(computerBehaviour = CCProxy.behaviour(this));

        movementDirection = new ScrollOptionBehaviour<>(WindmillBearingBlockEntity.RotationDirection.class,
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), this, new DieselGeneratorValueBox());
        movementDirection.withCallback($ -> onDirectionChanged());

        behaviours.add(movementDirection);
        tank = SmartFluidTankBehaviour.single(this, 81000);
        behaviours.add(tank);
        super.addBehaviours(behaviours);
    }
    public void onDirectionChanged(){}
    @Override
    public void initialize() {
        super.initialize();
        if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed())
            updateGeneratedRotation();
    }
    @Override
    public float calculateAddedStressCapacity() {
        if(getGeneratedSpeed() == 0 || state.getValue(POWERED))
            return 0;
        return FuelTypeManager.getGeneratedStress(this, tank.getPrimaryHandler().getFluid().getFluid()) / Math.abs(getGeneratedSpeed());
    }

    @Override
    public float getGeneratedSpeed() {
        if(state.getValue(POWERED))
            return 0;
        return convertToDirection((movementDirection.getValue() == 1 ? -1 : 1)* FuelTypeManager.getGeneratedSpeed(this, tank.getPrimaryHandler().getFluid().getFluid()), getBlockState().getValue(DieselGeneratorBlock.FACING))*(state.getValue(TURBOCHARGED) ? ConfigRegistry.TURBOCHARGED_ENGINE_MULTIPLIER.get().floatValue() : 1);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (!StressImpact.isEnabled())
            return added;

        float stressBase = calculateAddedStressCapacity();
        if (Mth.equal(stressBase, 0))
            return added;
        return containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability());
    }
    int t = 0;
    @Override
    public void tick() {
        super.tick();
        state = getBlockState();
        reActivateSource = true;
        if (level.isClientSide && !state.getValue(SILENCED))
            if (state.getValue(TURBOCHARGED) ? t > FuelTypeManager.getSoundSpeed(tank.getPrimaryHandler().getFluid().getFluid()) / 2 : t > FuelTypeManager.getSoundSpeed(tank.getPrimaryHandler().getFluid().getFluid())) {
                if (validFuel) {
                    t = 0;
                    level.playLocalSound(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), SoundRegistry.DIESEL_ENGINE_SOUND.getMainEvent(), SoundSource.BLOCKS, state.getValue(TURBOCHARGED) ? 0.5f : 0.3f, state.getValue(TURBOCHARGED) ? 1.1f : 1f, false);
                }
            } else {
                t++;
            }
        if(state.getValue(POWERED)) {
            validFuel = false;
        } else {
            validFuel = FuelTypeManager.getGeneratedSpeed(this, tank.getPrimaryHandler().getFluid().getFluid()) != 0;
        }

        partialSecond++;
        if (partialSecond >= 20) {
            partialSecond = 0;
            if (validFuel) {
                if (tank.getPrimaryHandler().getFluid().getAmount() >= FuelTypeManager.getBurnRate(this, tank.getPrimaryHandler().getFluid().getFluid()) * (!state.getValue(TURBOCHARGED) ? 1 : ConfigRegistry.TURBOCHARGED_ENGINE_BURN_RATE_MULTIPLIER.get().floatValue()))
                    tank.getPrimaryHandler().setFluid(FluidHelper.copyStackWithAmount(tank.getPrimaryHandler().getFluid(),
                            (int) (tank.getPrimaryHandler().getFluid().getAmount() - FuelTypeManager.getBurnRate(this, tank.getPrimaryHandler().getFluid().getFluid()) * (!state.getValue(TURBOCHARGED) ? 1 : ConfigRegistry.TURBOCHARGED_ENGINE_BURN_RATE_MULTIPLIER.get().floatValue()))));
                else
                    tank.getPrimaryHandler().setFluid(FluidStack.EMPTY);
            }
        }
    }
}
