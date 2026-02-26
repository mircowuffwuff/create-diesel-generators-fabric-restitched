package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.blocks.entity.OilBarrelBlockEntity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.simibubi.create.content.contraptions.MountedStorageManager;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/*
@Mixin(value = MountedFluidStorage.class, remap = false)
public abstract class MountedFluidStorageMixin {

    @Shadow protected abstract void onFluidStackChanged(FluidStack fs);

    @ModifyReturnValue(at = @At("RETURN"), method = "canUseAsStorage(Lnet/minecraft/world/level/block/entity/BlockEntity;)Z")
    private static boolean canUseAsStorage(boolean original, @Local BlockEntity be){
        if(be instanceof OilBarrelBlockEntity oil) {
            if(oil.isController()) {
                return true;
            }
        }
        return original;
    }

    @ModifyReturnValue(at = @At("RETURN"), method = "createMountedTank(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lcom/simibubi/create/foundation/fluid/SmartFluidTank;")
    private SmartFluidTank createMountedTank(SmartFluidTank original, @Local BlockEntity be){
        if(be instanceof OilBarrelBlockEntity oil)
            return (new SmartFluidTank(
                    (long) oil.getTotalTankSize() * OilBarrelBlockEntity.getCapacityMultiplier(),
                    this::onFluidStackChanged));
        return original;
    }
}

 */


