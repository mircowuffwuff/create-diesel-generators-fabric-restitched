package com.jesz.createdieselgenerators.contraption;

import com.jesz.createdieselgenerators.blocks.PumpjackBearingBBlock;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackBearingBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackHoleBlockEntity;
import net.createmod.catnip.animation.AnimationTickHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static com.jesz.createdieselgenerators.PartialModels.PUMPJACK_ROPE;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class PumpjackHeadMovementBehaviour implements MovementBehaviour {
    @Nullable
    @Override
    public ItemStack canBeDisabledVia(MovementContext context) {
        return null;
    }

    @Override
    public boolean isActive(MovementContext context) {
        if(!(context.contraption instanceof BearingContraption))
            return false;
        if(((BearingContraption) context.contraption).getFacing().getAxis() == Direction.Axis.Y || context.state.getValue(PumpjackBearingBBlock.FACING).getAxis() != ((BearingContraption) context.contraption).getFacing().getClockWise().getAxis())
            return false;
        return context.world.getBlockEntity(context.contraption.anchor.relative(((BearingContraption) context.contraption).getFacing().getOpposite())) instanceof PumpjackBearingBlockEntity;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices, MultiBufferSource buffer) {
        BlockPos hole = NbtUtils.readBlockPos(context.data.getCompound("HolePos"));
        if(!(context.world.getBlockEntity(hole) instanceof PumpjackHoleBlockEntity))
            return;
        PumpjackBearingBlockEntity bearing = null;
        if (context.world.getBlockEntity(context.contraption.anchor.relative(((BearingContraption) context.contraption).getFacing().getOpposite())) instanceof PumpjackBearingBlockEntity be)
            bearing = be;
        if(bearing == null)
            return;
        SuperByteBuffer cover = CachedBuffers.partial(PUMPJACK_ROPE, context.state);
        if(((BearingContraption) context.contraption).getFacing().getOpposite().getAxis() == Direction.Axis.X){
            double zDst = context.position.z - hole.getZ()-0.5f;
            double yDst = context.position.y - hole.getY()-0.8f;
            float distanceFromHole = (float) Math.sqrt(zDst*zDst + yDst*yDst);
            double angle = -((ControlledContraptionEntity) context.contraption.entity).getAngle(AnimationTickHolder.getPartialTicks())-(180 * Math.atan2(yDst,zDst)/Math.PI)+90;
            PoseStack ms = matrices.getModel();
            cover.transform(ms).translate(0.5, 0.5,  0.5).rotateXDegrees((float) angle)
                    .scale(1, distanceFromHole, 1)
                    .useLevelLight(context.world, matrices.getWorld())
                    .renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderType.cutoutMipped()));
            return;
        }
        double xDst = context.position.x - hole.getX()-0.5;
        double yDst = context.position.y - hole.getY()-0.8f;
        float distanceFromHole = (float) Math.sqrt(xDst*xDst + yDst*yDst);
        double angle = -((ControlledContraptionEntity) context.contraption.entity).getAngle(AnimationTickHolder.getPartialTicks())+(180 * Math.atan2(yDst,xDst)/Math.PI)-90;
        PoseStack ms = matrices.getModel();
        cover.transform(ms).translate(0.5, 0.5,  0.5).rotateZDegrees((float) angle)
                .scale(1, distanceFromHole, 1)
                .useLevelLight(context.world, matrices.getWorld())
                .renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderType.cutoutMipped()));
    }

    BlockPos holePos;
    BlockPos headPos;
    @Override
    public void tick(MovementContext context) {
        MovementBehaviour.super.tick(context);
        PumpjackBearingBlockEntity bearing = null;
        if (context.world.getBlockEntity(context.contraption.anchor.relative(((BearingContraption) context.contraption).getFacing().getOpposite())) instanceof PumpjackBearingBlockEntity be)
            bearing = be;
        if(bearing == null)
            return;
        headPos = new BlockPos(
                context.contraption.anchor.getX() + context.localPos.getX(),
                context.contraption.anchor.getY() + context.localPos.getY(),
                context.contraption.anchor.getZ() + context.localPos.getZ());
        holePos = headPos;
        for (int i = 0; i < 32; i++) {
            if (context.world.getBlockEntity(holePos) instanceof PumpjackHoleBlockEntity phbe)
                break;
            else
                holePos = holePos.below();
        }

        if(context.world.getBlockEntity(holePos) instanceof PumpjackHoleBlockEntity holeBE && bearing.crankSpeed >= 8) {

            holeBE.headPos = bearing.getBlockState().getValue(FACING).getAxis() == Direction.Axis.X ? context.localPos.getZ() : context.localPos.getX();
            holeBE.bearingPos = bearing.getBlockState().getValue(FACING).getAxis() == Direction.Axis.X ? bearing.bearingBPos.getZ() : bearing.bearingBPos.getX();
            if((bearing.crankAngle+180) % 360 < (context.data.getFloat("OldCrankAngle")+180) % 360)
                holeBE.tickFluid(bearing.isLarge);
        }
        context.data.putFloat("OldCrankAngle", bearing.crankAngle);

        context.data.put("HolePos", NbtUtils.writeBlockPos(holePos));
    }
}
