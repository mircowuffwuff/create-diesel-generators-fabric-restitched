package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.fluids.FluidRegistry;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import com.simibubi.create.foundation.utility.Components;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * rewritten using <a href="https://github.com/davioliva16/create-aquatic-ambitions/pull/10/files#diff-676ca02226dc30d2db262abc5c5a66d457c8c45a0080268f5a6cd7532485bd17">this snippet</a> by <a href="https://github.com/ninjaguardian">ninjaguardian</a> as a template.
 * thank you ninjaguardian!
 */
public class CreativeTab {

    public static final CreativeModeTab CDG_CREATIVE_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            new ResourceLocation(CreateDieselGenerators.ID, "s"), /*i dont have a clue what this s does here, but it works*/
            FabricItemGroup.builder()
                    .title(Components.translatable("itemGroup.cdg_creative_tab"))
                    .icon(() -> new ItemStack(BlockRegistry.DIESEL_ENGINE))
                    .displayItems(((itemDisplayParameters, output) -> {
                        output.accept(ItemRegistry.ENGINE_PISTON);
                        output.accept(ItemRegistry.ENGINE_SILENCER);
                        output.accept(ItemRegistry.ENGINE_TURBO);
                        output.accept(BlockRegistry.DIESEL_ENGINE);
                        output.accept(BlockRegistry.MODULAR_DIESEL_ENGINE);
                        output.accept(BlockRegistry.HUGE_DIESEL_ENGINE);
                        output.accept(ItemRegistry.DISTILLATION_CONTROLLER);
                        output.accept(ItemRegistry.OIL_SCANNER);
                        output.accept(BlockRegistry.PUMPJACK_BEARING);
                        output.accept(BlockRegistry.PUMPJACK_CRANK);
                        output.accept(BlockRegistry.PUMPJACK_HEAD);
                        output.accept(BlockRegistry.PUMPJACK_HOLE);
                        output.accept(ItemRegistry.WOOD_CHIPS);
                        output.accept(BlockRegistry.CHIP_WOOD_BEAM);
                        output.accept(BlockRegistry.CHIP_WOOD_BLOCK);
                        output.accept(BlockRegistry.CHIP_WOOD_STAIRS);
                        output.accept(BlockRegistry.CHIP_WOOD_SLAB);
                        output.accept(BlockRegistry.CANISTER);
                        output.accept(BlockRegistry.OIL_BARREL);
                        output.accept(BlockRegistry.BASIN_LID);
                        output.accept(BlockRegistry.ASPHALT_BLOCK);
                        output.accept(BlockRegistry.ASPHALT_STAIRS);
                        output.accept(BlockRegistry.ASPHALT_SLAB);
                        output.accept(FluidRegistry.CRUDE_OIL.get().getBucket());
                        output.accept(FluidRegistry.BIODIESEL.get().getBucket());
                        output.accept(FluidRegistry.DIESEL.get().getBucket());
                        output.accept(FluidRegistry.GASOLINE.get().getBucket());
                        output.accept(FluidRegistry.PLANT_OIL.get().getBucket());
                        output.accept(FluidRegistry.ETHANOL.get().getBucket());
                        output.accept(ItemRegistry.KELP_HANDLE);
                        output.accept(ItemRegistry.LIGHTER);
                        output.accept(ItemRegistry.CHEMICAL_SPRAYER);
                        output.accept(ItemRegistry.CHEMICAL_SPRAYER_LIGHTER);
                    }))
                    .build()
    );

    public static void registerItemGroups() {
        CreateDieselGenerators.LOGGER.info("Registering Item Groups for " + CreateDieselGenerators.ID);
    }
}
