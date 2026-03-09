package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.blocks.entity.LargeDieselGeneratorBlockEntity;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import com.simibubi.create.foundation.placement.PoleHelper;
import io.github.fabricators_of_create.porting_lib.block.ConnectableRedstoneBlock;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.POWERED;
import static com.jesz.createdieselgenerators.items.ItemRegistry.ENGINE_SILENCER;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;

public class LargeDieselGeneratorBlock extends HorizontalKineticBlock implements IBE<LargeDieselGeneratorBlockEntity>, SpecialBlockItemRequirement, ICDGKinetics, ConnectableRedstoneBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final BooleanProperty POWERED_BEFORE = BooleanProperty.create("last_powered");
    public static final BooleanProperty PIPE = BooleanProperty.create("pipe");
    public static final BooleanProperty SILENCED = BooleanProperty.create("silenced");
    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public LargeDieselGeneratorBlock(Properties properties) {
        super(properties);
        registerDefaultState(super.defaultBlockState()
                    .setValue(PIPE, true)
                    .setValue(SILENCED, false)
                    .setValue(POWERED, false));
    }
    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        withBlockEntityDo(level, pos, be -> {
            if(be.getEngineBack() == null)
                be.updateStacked();
        });
        return super.updateShape(state, direction, neighbourState, level, pos, neighbourPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        ItemStack itemInHand = player.getItemInHand(hand);

        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (!player.isShiftKeyDown() && player.mayBuild()) {
            if (placementHelper.matchesItem(itemInHand)) {
                placementHelper.getOffset(player, level, state, pos, hit)
                        .placeInWorld(level, (BlockItem) itemInHand.getItem(), player, hand, hit);
                return InteractionResult.SUCCESS;
            }
        }

        if(ENGINE_SILENCER.isIn(itemInHand)) {
            if (state.getValue(SILENCED))
                return InteractionResult.PASS;
            if (!player.isCreative())
                itemInHand.shrink(1);
            level.setBlock(pos, state.setValue(SILENCED, true), 3);
            IWrenchable.playRotateSound(level, pos);
            return InteractionResult.SUCCESS;
        }
        if(!ConfigRegistry.ENGINES_FILLED_WITH_ITEMS.get())
            return super.use(state, level, pos, player, hand, hit);
        if (itemInHand.isEmpty())
            return InteractionResult.PASS;
        if(level.getBlockEntity(pos) instanceof SmartBlockEntity){
            FluidHelper.FluidExchange exchange = null;
            FluidTankBlockEntity be = ConnectivityHandler.partAt(getBlockEntityType(), level, pos);
            if (be == null) {
                return InteractionResult.FAIL;
            }

            Direction direction = hit.getDirection();
            Storage<FluidVariant> fluidTank = be.getFluidStorage(direction);
            if (fluidTank == null) {
                return InteractionResult.PASS;
            }

            FluidStack prevFluidInTank = TransferUtil.firstCopyOrEmpty(fluidTank);

            if (FluidHelper.tryEmptyItemIntoBE(level, player, hand, itemInHand, be, direction)) {
                exchange = FluidHelper.FluidExchange.ITEM_TO_TANK;
            } else if (FluidHelper.tryFillItemFromBE(level, player, hand, itemInHand, be, direction)) {
                exchange = FluidHelper.FluidExchange.TANK_TO_ITEM;
            }

            if (exchange == null) {
                if (GenericItemEmptying.canItemBeEmptied(level, itemInHand)
                        || GenericItemFilling.canItemBeFilled(level, itemInHand))
                    return InteractionResult.SUCCESS;
                return InteractionResult.PASS;
            }

            SoundEvent soundevent = null;
            BlockState fluidState = null;
            FluidStack fluidInTank = TransferUtil.firstOrEmpty(fluidTank);

            if (exchange == FluidHelper.FluidExchange.ITEM_TO_TANK) {
                if (!level.isClientSide) {
                    FluidStack fluidInItem = GenericItemEmptying.emptyItem(level, itemInHand, true).getFirst();
                    if (!fluidInItem.isEmpty() && fluidTank instanceof CreativeFluidTankBlockEntity.CreativeSmartFluidTank)
                        ((CreativeFluidTankBlockEntity.CreativeSmartFluidTank) fluidTank).setContainedFluid(fluidInItem);
                }

                Fluid fluid = fluidInTank.getFluid();
                fluidState = fluid.defaultFluidState()
                        .createLegacyBlock();
                soundevent = FluidVariantAttributes.getEmptySound(FluidVariant.of(fluid));
            }

            if (exchange == FluidHelper.FluidExchange.TANK_TO_ITEM) {
                if (!level.isClientSide)
                    if (fluidTank instanceof CreativeFluidTankBlockEntity.CreativeSmartFluidTank)
                        ((CreativeFluidTankBlockEntity.CreativeSmartFluidTank) fluidTank).setContainedFluid(FluidStack.EMPTY);

                Fluid fluid = prevFluidInTank.getFluid();
                fluidState = fluid.defaultFluidState()
                        .createLegacyBlock();
                soundevent = FluidVariantAttributes.getFillSound(FluidVariant.of(fluid));
            }

            if (soundevent != null && !level.isClientSide) {
                float pitch = Mth.clamp(1 - (1f * fluidInTank.getAmount() / (FluidTankBlockEntity.getCapacityMultiplier() * 16)), 0, 1);
                pitch /= 1.5f;
                pitch += .5f;
                pitch += (level.random.nextFloat() - .5f) / 4f;
                level.playSound(null, pos, soundevent, SoundSource.BLOCKS, .5f, pitch);
            }

            if (!fluidInTank.isFluidEqual(prevFluidInTank)) {
                if (be instanceof FluidTankBlockEntity) {
                    FluidTankBlockEntity controllerBE = ((FluidTankBlockEntity) be).getControllerBE();
                    if (controllerBE != null) {
                        if (fluidState != null && !level.isClientSide) {
                            BlockParticleOption blockParticleData =
                                    new BlockParticleOption(ParticleTypes.BLOCK, fluidState);
                            float levelA = (float) fluidInTank.getAmount() / TransferUtil.firstCapacity(fluidTank);

                            boolean reversed = FluidVariantAttributes.isLighterThanAir(fluidInTank.getType());
                            if (reversed)
                                levelA = 1 - levelA;

                            Vec3 vec = hit.getLocation();
                            vec = new Vec3(vec.x, controllerBE.getBlockPos()
                                    .getY() + levelA * (controllerBE.getHeight() - .5f) + .25f, vec.z);
                            Vec3 motion = player.position()
                                    .subtract(vec)
                                    .scale(1 / 20f);
                            vec = vec.add(motion);
                            level.addParticle(blockParticleData, vec.x, vec.y, vec.z, motion.x, motion.y, motion.z);
                            return InteractionResult.SUCCESS;
                        }

                        controllerBE.sendDataImmediately();
                        controllerBE.setChanged();
                    }
                }
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }
    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if(context.getClickedFace() == Direction.UP){
            KineticBlockEntity.switchToBlockState(context.getLevel(), context.getClickedPos(), updateAfterWrenched(state.setValue(PIPE, !state.getValue(PIPE)), context));
            IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
            return InteractionResult.SUCCESS;
        }
        if(state.getValue(SILENCED))
            if(context.getPlayer() != null && !context.getLevel().isClientSide) {
                if (!context.getPlayer().isCreative())
                    context.getPlayer().getInventory().placeItemBackInInventory(ENGINE_SILENCER.asStack());
                context.getLevel().setBlock(context.getClickedPos(), state.setValue(SILENCED, false), 3);
                IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
                return InteractionResult.SUCCESS;
            }
        return super.onWrenched(state,context);
    }
    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return originalState;
    }
    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(PIPE, SILENCED, POWERED);
        super.createBlockStateDefinition(builder);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        if(pContext.getPlayer().isShiftKeyDown())
            return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
        else
            return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean moving) {
        withBlockEntityDo(level, pos, be -> {
            LargeDieselGeneratorBlockEntity front = be.frontEngine.get();
            if(front == null)
                front = be;
            if(front != be){
                level.setBlock(pos, state.setValue(POWERED, level.hasNeighborSignal(pos)), 2);
            }
            if(!front.getBlockState().getValue(POWERED) && level.hasNeighborSignal(pos))
                level.setBlock(front.getBlockPos(), front.getBlockState().setValue(POWERED, true), 2);
            if(front.getBlockState().getValue(POWERED) && !level.hasNeighborSignal(pos)) {
                boolean atLeastOneEnginePowered = false;
                for (int i = 0; i < front.stacked; i++) {
                    BlockState bs = level.getBlockState(pos.relative(state.getValue(FACING).getAxis(), -i));
                    if (bs.getBlock() instanceof LargeDieselGeneratorBlock && bs.getValue(FACING).getAxis() == state.getValue(FACING).getAxis() && bs.getValue(POWERED)) {
                        atLeastOneEnginePowered = true;
                        break;
                    }
                }
                if(!atLeastOneEnginePowered){
                    level.setBlock(front.getBlockPos(), front.getBlockState().setValue(POWERED, false), 2);
                }
            }
        });
        super.neighborChanged(state, level, pos, block, otherPos, moving);
    }
    @Override
    public Class<LargeDieselGeneratorBlockEntity> getBlockEntityClass() {
        return LargeDieselGeneratorBlockEntity.class;
    }
    @Override
    public BlockEntityType<? extends LargeDieselGeneratorBlockEntity> getBlockEntityType() {
        return BlockEntityRegistry.LARGE_DIESEL_ENGINE.get();
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {

        if (pState.getValue(FACING) == NORTH || pState.getValue(FACING) == SOUTH){
            return Shapes.or(Block.box(0,0,0,16,16,16), Block.box(-2,0,0,18,4,16));
        }else{
            return Shapes.or(Block.box(0,0,0,16,16,16), Block.box(0,0,-2,16,4,18));
        }



    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(FACING)
                .getAxis() == face.getAxis();
    }
    @Override
    public Direction.Axis getRotationAxis(BlockState blockState) {
        return blockState.getValue(FACING)
                .getAxis();
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity blockEntity) {
        List<ItemStack> list = new ArrayList<>();
        list.add(BlockRegistry.MODULAR_DIESEL_ENGINE.asStack());
        if(state.getValue(SILENCED))
            list.add(ItemRegistry.ENGINE_SILENCER.asStack());
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, list);
    }
    @Override
    public float getDefaultStressCapacity() {
        return 2048;
    }

    @Override
    public float getDefaultStressStressImpact() {
        return 0;
    }

    @Override
    public float getDefaultSpeed() {
        return 96;
    }

    private static class PlacementHelper extends PoleHelper<Direction>{

        public PlacementHelper() {
            super(BlockRegistry.MODULAR_DIESEL_ENGINE::has, state -> state.getValue(FACING).getAxis(), FACING);
        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return BlockRegistry.MODULAR_DIESEL_ENGINE::isIn;
        }
    }
}
