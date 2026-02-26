package com.jesz.createdieselgenerators.other;

import com.jesz.createdieselgenerators.blocks.entity.CanisterBlockEntity;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SpoutCanisterFilling implements BlockSpoutingBehaviour {

    @Override
    public long fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
        if(!ConfigRegistry.CANISTER_SPOUT_FILLING.get())
            return 0;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof @SuppressWarnings("unused") CanisterBlockEntity _be){
            Storage<FluidVariant> handler = TransferUtil.getFluidStorage(level, pos, blockEntity, Direction.UP);
            //IFluidHandler handler = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).orElse(null);
            long amount = availableFluid.getAmount();
            try (Transaction t = TransferUtil.getTransaction()) {
                long inserted = handler.insert(availableFluid.getType(), amount, t);
                if (amount < FluidConstants.BUCKET) {
                    try (Transaction nested = t.openNested()) {
                        if (handler.insert(availableFluid.getType(), 1, nested) == 1)
                            return 0;
                    }
                }

                if (!simulate) t.commit();
                return inserted;
            }
        }
        return 0;
    }
}
