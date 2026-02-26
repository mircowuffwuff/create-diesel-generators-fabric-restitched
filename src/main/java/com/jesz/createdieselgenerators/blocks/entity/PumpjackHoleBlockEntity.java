package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.TagRegistry;
import com.jesz.createdieselgenerators.fluids.FluidRegistry;
import com.jesz.createdieselgenerators.world.OilChunksSavedData;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.fabricators_of_create.porting_lib.tags.Tags;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.FACING;
import static com.simibubi.create.AllTags.optionalTag;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;

public class PumpjackHoleBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, IHaveHoveringInformation, SidedStorageBlockEntity {
    BlockState state;

    SmartFluidTankBehaviour tank;
    public int headPos = 0;
    public int bearingPos = 0;
    public boolean started = false;
    byte tt = 0;
    public int pipeLength = 0;
    boolean valid = false;

    public PumpjackHoleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.state = state;
    }
    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        tank.write(compound, false);
        compound.putInt("StoredOilAmount", storedOilAmount);
        compound.putInt("OilAmount", oilAmount);
        compound.putBoolean("Started", started);
    }
    public int oilAmount = 0;
    public int storedOilAmount = 0;

    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if(valid)
            return false;

        CreateLang.builder().add(Component.translatable("createdieselgenerators.goggle.problem_encountered")).style(ChatFormatting.GOLD).forGoggles(tooltip);
        CreateLang.builder().add(Component.translatable("createdieselgenerators.goggle.pumpjack_invalid_pipes")).style(ChatFormatting.GRAY).forGoggles(tooltip);

        return true;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        IHaveGoggleInformation.super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if(!valid || !started)
            return false;

        CreateLang.builder().add(Component.translatable("createdieselgenerators.goggle.oil_amount")).style(ChatFormatting.GRAY).forGoggles(tooltip);
        CreateLang.number(oilAmount).add(CreateLang.translate("generic.unit.buckets")).style(ChatFormatting.GOLD).forGoggles(tooltip);

        return true;
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        tank.read(compound, false);
        storedOilAmount = compound.getInt("StoredOilAmount");
        oilAmount = compound.getInt("OilAmount");
        started = compound.getBoolean("Started");
    }

    @Override
    public void handleUpdateTag(CompoundTag compound) {
        super.handleUpdateTag(compound);
        oilAmount = compound.getInt("OilAmount");
        started = compound.getBoolean("Started");
    }

    @Override
    public void tick() {
        super.tick();
        tt++;
        if (tt >= 20) {
            int pipeLength = 0;
            tt = 0;
            boolean v = false;
            for (int i = 0; i < getBlockPos().getY() - level.getMinBuildHeight(); i++) {
                pipeLength++;
                BlockState bs = level.getBlockState(getBlockPos().below(i + 1));
                if (bs.getBlock() instanceof PipeBlock || bs.getBlock() instanceof EncasedPipeBlock) {
                    if (!(bs.getValue(BlockStateProperties.UP) && bs.getValue(BlockStateProperties.DOWN)))
                        break;
                }else if(bs.getBlock() instanceof GlassFluidPipeBlock) {
                    if (!(bs.getValue(AXIS) == Direction.Axis.Y))
                        break;
                }else if(bs.is(optionalTag(BuiltInRegistries.BLOCK, new ResourceLocation("createdieselgenerators:pumpjack_pipe")))){
                    continue;
                } else if (bs.is(optionalTag(BuiltInRegistries.BLOCK, new ResourceLocation("createdieselgenerators:oil_deposit")))) {
                    v = true;
                    break;
                } else
                    break;
            }
            if(v)
                this.pipeLength = pipeLength;
            else
                this.pipeLength = 0;
            valid = v;
        }

    }

    @Override
    public AABB getRenderBoundingBox() {
        return super.getRenderBoundingBox().inflate(pipeLength);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compound = super.getUpdateTag();
        compound.putInt("OilAmount", oilAmount);
        compound.putBoolean("Started", started);
        return compound;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, 81000);
        behaviours.add(tank);
    }

    public void tickFluid(boolean isCrankLarge) {
        if(!level.isClientSide && valid) {
            ChunkPos chunkPos = new ChunkPos(getBlockPos());
            OilChunksSavedData sd = OilChunksSavedData.load((ServerLevel)level);
            int amount = sd.getChunkOilAmount(chunkPos);
            if(amount == -1)
                amount = CreateDieselGenerators.getOilAmount((ServerLevel)level, level.getBiome(new BlockPos(chunkPos.x * 16, 64,  chunkPos.z * 16)), chunkPos.x, chunkPos.z, ((ServerLevel)level).getSeed());
            oilAmount = amount;
            started = true;
            if(amount == 0)
                return;

            if(storedOilAmount == 0){
                sd.setChunkAmount(chunkPos, amount-1);
                oilAmount = amount -1;
                storedOilAmount = 81000;
            }
            int subtractedAmount = Mth.clamp((int) (8100 * Math.abs((float) headPos / (float) bearingPos)) * (isCrankLarge ? 2 : 1), 0, 81000);
            storedOilAmount = storedOilAmount < subtractedAmount ? 0 : storedOilAmount - subtractedAmount;

            TagKey<Fluid> fluidTag = TagRegistry.FluidTags.PUMPJACK_OUTPUT.tag;

            Optional<Fluid> stackList = BuiltInRegistries.FLUID.stream().filter(fluid -> FluidHelper.isTag(fluid, fluidTag)).findFirst();

            if(stackList.isEmpty())
                return;
            FluidStack oilStack = new FluidStack(stackList.get(), subtractedAmount);

            TransferUtil.insertFluid(tank.getPrimaryHandler(), oilStack);
        }
    }

    @Nullable
    @Override
    public Storage<FluidVariant> getFluidStorage(@Nullable Direction side) {
        if(side == Direction.NORTH && getBlockState().getValue(NORTH))
            return tank.getCapability();
        if(side == Direction.EAST && getBlockState().getValue(EAST))
            return tank.getCapability();
        if(side == Direction.SOUTH && getBlockState().getValue(SOUTH))
            return tank.getCapability();
        if(side == Direction.WEST && getBlockState().getValue(WEST))
            return tank.getCapability();
        return null;
    }

}
