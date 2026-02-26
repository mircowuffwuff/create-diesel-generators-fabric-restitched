package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.blocks.LargeDieselGeneratorBlock;
import com.jesz.createdieselgenerators.compat.computercraft.CCProxy;
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
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.List;

import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.POWERED;
import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.SILENCED;
import static com.jesz.createdieselgenerators.blocks.LargeDieselGeneratorBlock.*;

public class LargeDieselGeneratorBlockEntity extends GeneratingKineticBlockEntity implements SidedStorageBlockEntity {
    BlockState state;
    public boolean validFuel;
    public int stacked;
    boolean end = true;
    public WeakReference<LargeDieselGeneratorBlockEntity> forw;
    public WeakReference<LargeDieselGeneratorBlockEntity> back;
    public WeakReference<LargeDieselGeneratorBlockEntity> frontEngine = new WeakReference<>(null);
    public SmartFluidTankBehaviour tank;
    public ScrollOptionBehaviour<WindmillBearingBlockEntity.RotationDirection> movementDirection;
    public AbstractComputerBehaviour computerBehaviour;
    int partialSecond;
    int t = 0;
    int totalSize = 0;

    public LargeDieselGeneratorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        forw = new WeakReference<>(null);
        back = new WeakReference<>(null);

        this.state = state;
    }

    @Nullable
    @Override
    public Storage<FluidVariant> getFluidStorage(@Nullable Direction face) {
        if (state.getValue(PIPE)) {
            LargeDieselGeneratorBlockEntity frontEngine = this.frontEngine.get();
            if (face == Direction.UP)
                if(frontEngine != null)
                    return frontEngine.tank.getCapability();
                else
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
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), this, new LargeDieselGeneratorValueBox());
        movementDirection.withCallback($ -> onDirectionChanged(true));

        behaviours.add(movementDirection);
        tank = SmartFluidTankBehaviour.single(this, 81000);
        behaviours.add(tank);
        super.addBehaviours(behaviours);
    }

    public void onDirectionChanged(boolean first) {
        LargeDieselGeneratorBlockEntity frontEngine = this.frontEngine.get();
        if(frontEngine == null)
            return;
        if(first && getEngineFor() != null){
            frontEngine.movementDirection.setValue(movementDirection.getValue());
            frontEngine.onDirectionChanged(false);
            return;
        }
        movementDirection.setValue(frontEngine.movementDirection.getValue());
        if(getEngineBack() != null)
            getEngineBack().onDirectionChanged(false);
    }

    @Override
    public void initialize() {
        super.initialize();
        updateStacked();
        if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed())
            updateGeneratedRotation();
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (getGeneratedSpeed() == 0 || !end)
            return 0;
        if(state.getValue(POWERED))
            return 0;
        return FuelTypeManager.getGeneratedStress(this, tank.getPrimaryHandler().getFluid().getFluid()) / Math.abs(getGeneratedSpeed()) * stacked;
    }

    @Override
    public float getGeneratedSpeed() {
        if(!end)
            return 0;
        if(state.getValue(POWERED))
            return 0;
        return convertToDirection((movementDirection.getValue() == 1 ? -1 : 1)* FuelTypeManager.getGeneratedSpeed(this, tank.getPrimaryHandler().getFluid().getFluid()), getBlockState().getValue(LargeDieselGeneratorBlock.FACING));
    }

    public void updateStacked(){
        LargeDieselGeneratorBlockEntity engineForward = getEngineFor();
        LargeDieselGeneratorBlockEntity engineBack = getEngineBack();

        if(engineBack == null) {
            totalSize = 1;
            stacked = 1;

        }else{
            stacked = engineBack.stacked + 1;
        }
        if(engineForward == null) {
            totalSize = stacked;
            setEveryEnginesFront();
        } else
            engineForward.updateStacked();
    }

    public void setEveryEnginesFront(){
        LargeDieselGeneratorBlockEntity engineForward = getEngineFor();
        LargeDieselGeneratorBlockEntity engineBack = getEngineBack();

        if(engineForward == null){
            frontEngine = new WeakReference<>(this);
        }else{
            frontEngine = engineForward.frontEngine;
            totalSize = engineForward.totalSize;
        }
        if(engineBack != null)
            engineBack.setEveryEnginesFront();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        LargeDieselGeneratorBlockEntity frontEngine = this.frontEngine.get();
        if (!StressImpact.isEnabled() || frontEngine == null)
            return added;
        float stressBase = frontEngine.calculateAddedStressCapacity();
        if (Mth.equal(stressBase, 0))
            return added;
        if(frontEngine != this){
            CreateLang.translate("gui.goggles.generator_stats")
                    .forGoggles(tooltip);
            CreateLang.translate("tooltip.capacityProvided")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);

            float stressTotal = Math.abs(frontEngine.getGeneratedSpeed()* stressBase);

            CreateLang.number(stressTotal)
                    .translate("generic.unit.stress")
                    .style(ChatFormatting.AQUA)
                    .space()
                    .add(CreateLang.translate("gui.goggles.at_current_speed")
                            .style(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);

        }
        return containedFluidTooltip(tooltip, isPlayerSneaking, frontEngine.tank.getCapability());
    }

    @Override
    public void tick() {
        super.tick();

        LargeDieselGeneratorBlockEntity engineForward = getEngineFor();
        state = getBlockState();

        end = engineForward == null;

        reActivateSource = true;

        LargeDieselGeneratorBlockEntity frontEngine = this.frontEngine.get();

        if(!tank.isEmpty() && engineForward != null && frontEngine != null){
            TransferUtil.insertFluid(tank.getPrimaryHandler(), tank.getPrimaryHandler().getFluid());
            TransferUtil.extractFluid(tank.getPrimaryHandler(), tank.getPrimaryHandler().getFluid());
        }
        if(state.getValue(POWERED))
            validFuel = false;
        else
            validFuel = FuelTypeManager.getGeneratedSpeed(this, tank.getPrimaryHandler().getFluid().getFluid()) != 0;

        if(frontEngine != null && t > FuelTypeManager.getSoundSpeed(frontEngine.tank.getPrimaryHandler().getFluid().getFluid()) && frontEngine.validFuel && !state.getValue(SILENCED) && (((stacked % 6) == 0) || end)){
            level.playLocalSound(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), SoundRegistry.DIESEL_ENGINE_SOUND.getMainEvent(), SoundSource.BLOCKS, 0.5f,1f, false);
            t = 0;
        }else
            t++;

        partialSecond++;
        if(partialSecond >= 20){
            partialSecond = 0;
            if(validFuel)
                if(tank.getPrimaryHandler().getFluid().getAmount() >= FuelTypeManager.getBurnRate(this, tank.getPrimaryHandler().getFluid().getFluid()) * stacked)
                    tank.getPrimaryHandler().setFluid(FluidHelper.copyStackWithAmount(tank.getPrimaryHandler().getFluid(),
                            tank.getPrimaryHandler().getFluid().getAmount() - FuelTypeManager.getBurnRate(this, tank.getPrimaryHandler().getFluid().getFluid()) * stacked));
                else
                    tank.getPrimaryHandler().setFluid(FluidStack.EMPTY);
        }
    }

    public LargeDieselGeneratorBlockEntity getEngineFor() {
        LargeDieselGeneratorBlockEntity engine = forw.get();
        if (engine == null || engine.isRemoved() || engine.state.getValue(LargeDieselGeneratorBlock.FACING) == state.getValue(LargeDieselGeneratorBlock.FACING)) {
            if (engine != null)
                forw = new WeakReference<>(null);
            Direction facing = this.state.getValue(LargeDieselGeneratorBlock.FACING);
            BlockEntity be = level.getBlockEntity(worldPosition.relative(facing.getAxis() == Direction.Axis.Z ? Direction.SOUTH : Direction.EAST));
            if (be instanceof LargeDieselGeneratorBlockEntity engineBE)
                forw = new WeakReference<>(engine = engineBE);
        }
        if(engine != null){
            if (engine.state.getValue(LargeDieselGeneratorBlock.FACING).getAxis() != state.getValue(LargeDieselGeneratorBlock.FACING).getAxis()) {
                forw = new WeakReference<>(null);
                return null;
            }
        }
        return engine;
    }

    public LargeDieselGeneratorBlockEntity getEngineBack() {
        LargeDieselGeneratorBlockEntity engine = back.get();
        if (engine == null || engine.isRemoved()) {
            if (engine != null)
                back = new WeakReference<>(null);
            Direction facing = this.state.getValue(LargeDieselGeneratorBlock.FACING);
            BlockEntity be = level.getBlockEntity(worldPosition.relative(facing.getAxis() == Direction.Axis.Z ? Direction.NORTH : Direction.WEST));
            if (be instanceof LargeDieselGeneratorBlockEntity engineBE)
                back = new WeakReference<>(engine = engineBE);

        }
        if(engine != null){
            if (engine.state.getValue(LargeDieselGeneratorBlock.FACING).getAxis() != state.getValue(LargeDieselGeneratorBlock.FACING).getAxis()) {
                back = new WeakReference<>(null);
                return null;
            }
        }
        return engine;
    }

}
