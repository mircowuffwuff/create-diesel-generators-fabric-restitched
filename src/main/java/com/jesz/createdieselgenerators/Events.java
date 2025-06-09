package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock;
import com.jesz.createdieselgenerators.blocks.ICDGKinetics;
import com.jesz.createdieselgenerators.commands.CDGCommands;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.mojang.brigadier.CommandDispatcher;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.infrastructure.command.ConfigCommand;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CKinetics;
import io.github.fabricators_of_create.porting_lib.event.common.ExplosionEvents;
import io.github.fabricators_of_create.porting_lib.mixin.accessors.common.accessor.BucketItemAccessor;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.tropheusj.milk.Milk;
import io.github.tropheusj.milk.MilkFluid;
import io.github.tropheusj.milk.MilkFluidBlock;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Events {

    public static void onCommandRegister(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection commandSelection) {
        new CDGCommands(dispatcher);
    }

    /* TODO
    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event){
        event.addListener(FuelTypeManager.ReloadListener.INSTANCE);
    }
     */

    public static void onExplosion(Level level, Explosion explosion, List<Entity> entities, double v) {
        System.out.println("onExplosion called!");

        if (ConfigRegistry.COMBUSTIBLES_BLOW_UP.get() && !level.isClientSide) {
            final int radius = 2;
            final int radiusSq = 4;

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        // restrict effect to sphere with radius 2
                        if (x*x + y*y + z*z >= radiusSq)
                            continue; 

                        BlockPos pos = new BlockPos((int)(x + explosion.x), (int)(y + explosion.y),
                                (int)(z + explosion.z));

                        // out of bounds check
                        if (!level.isInWorldBounds(pos))
                            continue; 

                        FluidState fluidState = level.getFluidState(pos);
                        
                        // fixed size explosion from combustible fluid
                        if (FuelTypeManager.getGeneratedSpeed(fluidState.getType()) != 0) {
                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                            try {
                                System.out.println("    constant (3) explosion triggered!");
                                level.explode(null, pos.getX(), pos.getY(), pos.getZ(), 3, true,
                                        Level.ExplosionInteraction.BLOCK);
                                System.out.println("    ...exited!");
                            } catch (StackOverflowError ignored) {}
                            continue;
                        }

                        BlockEntity be = level.getBlockEntity(pos);
                        if (be == null)
                            continue;
                        var tank = TransferUtil.getFluidStorage(be);
                        if (tank == null)
                            continue;

                        FluidStack fluid = TransferUtil.getFirstFluid(tank);

                        // ensure fluid tank is not empty
                        if (fluid == null || fluid.isEmpty())
                            continue;

                        // variable size explosion from fluid tank
                        if (FuelTypeManager.getGeneratedSpeed(fluid.getFluid()) == 0)
                            continue;  // do not explode if tank is empty? <- may be redundant?
                        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        try {
                            System.out.println("    variable " + (Math.min(3 + ((float) fluid.getAmount() / 40500), 127f)) + " explosion triggered");
                            level.explode(null, pos.getX(), pos.getY(), pos.getZ(),
                                    Math.min(3 + ((float) fluid.getAmount() / 40500), 127f), true, Level.ExplosionInteraction.BLOCK);
                            System.out.println("    ...exited!");
                        } catch (StackOverflowError ignored) {}
                    }
                }
            }
        }
    }

    /*TODO
    public static void addTrade(VillagerTradesEvent event) {
        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
        if(!(event.getType() == VillagerProfession.TOOLSMITH))
            return;
        trades.get(2).add((t, r) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 5),
                new ItemStack(ItemRegistry.LIGHTER.get()),
                10,8,0.02f));
    }

     */

    public static void addToItemTooltip(List<Component> tooltip, Item item, Player player) {
        if (!AllConfigs.client().tooltips.get())
            return;
        if (player == null)
            return;
        if((item instanceof BucketItem || item instanceof MilkBucketItem) && ConfigRegistry.FUEL_TOOLTIPS.get()){

            Fluid fluid = Milk.STILL_MILK.getSource();
            if(item instanceof BucketItem bi)
                fluid = fluid = ((BucketItemAccessor) item).port_lib$getContent();



            if(FuelTypeManager.getGeneratedSpeed(fluid) != 0){
                if(Screen.hasAltDown()) {
                    tooltip.add(1, Components.translatable("createdieselgenerators.tooltip.holdForFuelStats", Components.translatable("createdieselgenerators.tooltip.keyAlt").withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(2, Components.immutableEmpty());
                    byte enginesEnabled = (byte) ((DieselGeneratorBlock.EngineTypes.NORMAL.enabled() ? 1 : 0) + (DieselGeneratorBlock.EngineTypes.MODULAR.enabled() ? 1 : 0) + (DieselGeneratorBlock.EngineTypes.HUGE.enabled() ? 1 : 0));
                    int currentEngineIndex = (AnimationTickHolder.getTicks() % (120)) / 20;
                    List<DieselGeneratorBlock.EngineTypes> enabledEngines = Arrays.stream(DieselGeneratorBlock.EngineTypes.values()).filter(DieselGeneratorBlock.EngineTypes::enabled).toList();
                    DieselGeneratorBlock.EngineTypes currentEngine = enabledEngines.get(currentEngineIndex % enginesEnabled);
                    float currentSpeed = FuelTypeManager.getGeneratedSpeed(currentEngine, fluid);
                    float currentCapacity = FuelTypeManager.getGeneratedStress(currentEngine, fluid);
                    float currentBurn = FuelTypeManager.getBurnRate(currentEngine, fluid);
                    if(enginesEnabled != 1)
                        tooltip.add(3, Components.translatable("block.createdieselgenerators."+
                                (currentEngine == DieselGeneratorBlock.EngineTypes.MODULAR ? "large_" : currentEngine == DieselGeneratorBlock.EngineTypes.HUGE ? "huge_" : "")+"diesel_engine").withStyle(ChatFormatting.GRAY));
                    tooltip.add(enginesEnabled != 1 ? 4 : 3, Components.translatable("createdieselgenerators.tooltip.fuelSpeed", Lang.number(currentSpeed).component().withStyle(TooltipHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(enginesEnabled != 1 ? 5 : 4, Components.translatable("createdieselgenerators.tooltip.fuelStress", Lang.number(currentCapacity).component().withStyle(TooltipHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(enginesEnabled != 1 ? 6 : 5, Components.translatable("createdieselgenerators.tooltip.fuelBurnRate", Lang.number(currentBurn).component().withStyle(TooltipHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(enginesEnabled != 1 ? 7 : 6, Components.immutableEmpty());
                }else {
                    tooltip.add(1, Components.translatable("createdieselgenerators.tooltip.holdForFuelStats", Components.translatable("createdieselgenerators.tooltip.keyAlt").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
        if(!BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("createdieselgenerators"))
            return;
        String path = "createdieselgenerators." + BuiltInRegistries.ITEM.getKey(item).getPath();
        List<Component> tooltipList = new ArrayList<>();
        if(!Component.translatable(path + ".tooltip.summary").getString().equals(path + ".tooltip.summary")) {
            if (Screen.hasShiftDown()) {
                tooltipList.add(Lang.translateDirect("tooltip.holdForDescription", Component.translatable("create.tooltip.keyShift").withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.DARK_GRAY));
                tooltipList.add(Components.immutableEmpty());
                tooltipList.addAll(TooltipHelper.cutStringTextComponent(Component.translatable(path + ".tooltip.summary").getString(), TooltipHelper.Palette.STANDARD_CREATE));

                if(!Component.translatable(path + ".tooltip.condition1").getString().equals(path + ".tooltip.condition1")) {
                    tooltipList.add(Components.immutableEmpty());
                    tooltipList.add(Component.translatable(path + ".tooltip.condition1").withStyle(ChatFormatting.GRAY));
                    tooltipList.addAll(TooltipHelper.cutStringTextComponent(Component.translatable(path + ".tooltip.behaviour1").getString(), TooltipHelper.Palette.STANDARD_CREATE.primary(), TooltipHelper.Palette.STANDARD_CREATE.highlight(), 1));
                    if(!Component.translatable(path + ".tooltip.condition2").getString().equals(path + ".tooltip.condition2")) {
                        tooltipList.add(Component.translatable(path + ".tooltip.condition2").withStyle(ChatFormatting.GRAY));
                        tooltipList.addAll(TooltipHelper.cutStringTextComponent(Component.translatable(path + ".tooltip.behaviour2").getString(), TooltipHelper.Palette.STANDARD_CREATE.primary(), TooltipHelper.Palette.STANDARD_CREATE.highlight(), 1));
                    }
                }
            } else {
                tooltipList.add(Lang.translateDirect("tooltip.holdForDescription", Component.translatable("create.tooltip.keyShift").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        tooltip.addAll(1,tooltipList);
        CKinetics config = AllConfigs.server().kinetics;

        if(item instanceof BlockItem bi)
            if(bi.getBlock() instanceof ICDGKinetics k){
                boolean hasGoggles = GogglesItem.isWearingGoggles(player);



                if(k.getDefaultStressCapacity() != 0){
                    float stressCapacity = k.getDefaultStressCapacity();
                    float speed = k.getDefaultSpeed();

                    tooltip.add(Components.immutableEmpty());

                    tooltip.add(Component.translatable("create.tooltip.capacityProvided").withStyle(ChatFormatting.GRAY));
                    MutableComponent component;
                    if (k.getDefaultStressCapacity() >= config.highCapacity.get())
                        component = Components.literal(TooltipHelper.makeProgressBar(3, 3)).append(hasGoggles ? Component.empty() : Component.translatable("create.tooltip.capacityProvided.high")).withStyle(IRotate.StressImpact.LOW.getAbsoluteColor());
                    else if (k.getDefaultStressCapacity() >= config.mediumCapacity.get())
                        component = Components.literal(TooltipHelper.makeProgressBar(3, 2)).append(hasGoggles ? Component.empty() : Component.translatable("create.tooltip.capacityProvided.medium")).withStyle(IRotate.StressImpact.MEDIUM.getAbsoluteColor());
                    else
                        component = Components.literal(TooltipHelper.makeProgressBar(3, 1)).append(hasGoggles ? Component.empty() : Components.translatable("create.tooltip.capacityProvided.low")).withStyle(IRotate.StressImpact.HIGH.getAbsoluteColor());

                    if (hasGoggles) {
                        tooltip.add(component.append(Lang.number(stressCapacity / speed)
                                .text("x ")
                                .add(Lang.translate("generic.unit.rpm"))
                                .component()));

                        if (speed != 0) {
                            tooltip.add(Component.literal(" -> ")
                                    .append(Lang.translate("tooltip.up_to", Lang.number(k.getDefaultStressCapacity())).add(Lang.translate("generic.unit.stress")).component()).withStyle(ChatFormatting.DARK_GRAY));
                        }
                    }else
                        tooltip.add(component);
                }else if(k.getDefaultStressStressImpact() != 0){
                    tooltip.add(Components.immutableEmpty());

                    tooltip.add(Component.translatable("create.tooltip.stressImpact").withStyle(ChatFormatting.GRAY));
                    if(k.getDefaultStressStressImpact() >= config.highStressImpact.get())
                        tooltip.add(Components.literal(TooltipHelper.makeProgressBar(3, 3)).append(hasGoggles ? Lang.number(k.getDefaultStressStressImpact()).add(Lang.text("x ").add(Lang.translate("generic.unit.rpm"))).component() : Component.translatable("create.tooltip.stressImpact.high")).withStyle(IRotate.StressImpact.HIGH.getAbsoluteColor()));
                    else if(k.getDefaultStressStressImpact() >= config.mediumStressImpact.get())
                        tooltip.add(Components.literal(TooltipHelper.makeProgressBar(3, 2)).append(hasGoggles ? Lang.number(k.getDefaultStressStressImpact()).add(Lang.text("x ").add(Lang.translate("generic.unit.rpm"))).component() : Component.translatable("create.tooltip.stressImpact.medium")).withStyle(IRotate.StressImpact.MEDIUM.getAbsoluteColor()));
                    else
                        tooltip.add(Components.literal(TooltipHelper.makeProgressBar(3, 1)).append(hasGoggles ? Lang.number(k.getDefaultStressStressImpact()).add(Lang.text("x ").add(Lang.translate("generic.unit.rpm"))).component() : Component.translatable("create.tooltip.stressImpact.low")).withStyle(IRotate.StressImpact.LOW.getAbsoluteColor()));
                }
            }

    }



}
