package com.jesz.createdieselgenerators.other;

import java.util.Optional;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.FluidState;

public class CombustionHelper {
    public static record PointOffset(int dx, int dy, int dz) {
    }

    public static record PointCoordinate(int x, int y, int z) {
        public PointCoordinate(PointExplosion pe) {
            this(pe.x(), pe.y(), pe.z());
        }

        /**
         * Returns a new PointCoordinate with the offset applied.
         */
        public PointCoordinate applyOffset(PointOffset po) {
            return new PointCoordinate(this.x + po.dx(), this.y + po.dy(), this.z + po.dz());
        }
    }

    public static record PointExplosion(int x, int y, int z, float explosionSize) {
        public PointExplosion(PointCoordinate pc, float explosionSize) {
            this(pc.x(), pc.y(), pc.z(), explosionSize);
        }
    }

    public static Optional<PointExplosion> detectAndClean(Level level, PointCoordinate pc) {
        return detectAndClean(level, pc.x(), pc.y(), pc.z());
    }

    /**
     * Checks if the block at the given coordinate in the given level is a combustible material. If so, it prepares it 
     * for detonation by emptying it (if it is a fluid tank) and replacing it with air.
     */
    public static Optional<PointExplosion> detectAndClean(Level level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);

        // out of bounds check
        if (!level.isInWorldBounds(pos))
            return Optional.empty();

        FluidState fluidState = level.getFluidState(pos);

        if (FuelTypeManager.getGeneratedSpeed(fluidState.getType()) != 0) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            // fixed size explosion from combustible fluid
            return Optional.of(new PointExplosion(pos.getX(), pos.getY(), pos.getZ(), 3f));
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be == null)
            return Optional.empty();
        var tank = TransferUtil.getFluidStorage(be);
        if (tank == null)
            return Optional.empty();

        FluidStack fluid = TransferUtil.getFirstFluid(tank);

        // ensure fluid tank is not empty
        if (fluid == null || fluid.isEmpty())
            return Optional.empty();

        if (FuelTypeManager.getGeneratedSpeed(fluid.getFluid()) == 0)
            return Optional.empty(); // do not explode if tank is empty? <- may be redundant?
        
        // empty current fluid tank (empties the entire multi-block fluid tank)
        long fluidAmount = fluid.getAmount();  // get current value before emptying
        TransferUtil.clearStorage(tank);
        
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        // variable size explosion from fluid tank (capped at minecraft max, 127)
        return Optional.of(new PointExplosion(pos.getX(), pos.getY(), pos.getZ(),
                Math.min(3 + ((float) fluidAmount / 40500), 127f)));
    }
}
