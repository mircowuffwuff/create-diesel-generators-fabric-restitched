package com.jesz.createdieselgenerators.ponder;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class CDGPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return "createdieselgenerators";
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderPlugin.super.registerScenes(helper);
        PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
        HELPER.forComponents(ItemRegistry.DISTILLATION_CONTROLLER)
                .addStoryBoard("distillation_tower", DistillationScenes::distillation);
        HELPER.forComponents(BlockRegistry.DIESEL_ENGINE)
                .addStoryBoard("diesel_engine", DieselEngineScenes::small);
        HELPER.forComponents(BlockRegistry.DIESEL_ENGINE)
                .addStoryBoard("engine_silencer", DieselEngineScenes::silencer);
        HELPER.forComponents(BlockRegistry.MODULAR_DIESEL_ENGINE)
                .addStoryBoard("large_diesel_engine", DieselEngineScenes::modular);
        HELPER.forComponents(BlockRegistry.MODULAR_DIESEL_ENGINE)
                .addStoryBoard("engine_silencer", DieselEngineScenes::silencer);
        HELPER.forComponents(ItemRegistry.ENGINE_SILENCER)
                .addStoryBoard("engine_silencer", DieselEngineScenes::silencer);
        HELPER.forComponents(BlockRegistry.BASIN_LID)
                .addStoryBoard("basin_fermenting_station", BasinScenes::basin_lid);
        HELPER.forComponents(BlockRegistry.HUGE_DIESEL_ENGINE)
                .addStoryBoard("huge_diesel_engine", DieselEngineScenes::huge);
        HELPER.forComponents(BlockRegistry.PUMPJACK_BEARING, BlockRegistry.PUMPJACK_CRANK, BlockRegistry.PUMPJACK_HEAD)
                .addStoryBoard("pumpjack", OilScenes::pumpjack);
    }
        @Override
        public void registerTags (PonderTagRegistrationHelper < ResourceLocation > helper) {
            PonderPlugin.super.registerTags(helper);

            PonderTagRegistrationHelper<RegistryEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
            HELPER.addToTag(AllCreatePonderTags.KINETIC_SOURCES)
                    .add(BlockRegistry.DIESEL_ENGINE)
                    .add(BlockRegistry.MODULAR_DIESEL_ENGINE)
                    .add(BlockRegistry.HUGE_DIESEL_ENGINE);
            HELPER.addToTag(AllCreatePonderTags.KINETIC_APPLIANCES)
                    .add(BlockRegistry.BASIN_LID)
                    .add(BlockRegistry.PUMPJACK_BEARING);
            HELPER.addToTag(AllCreatePonderTags.DISPLAY_SOURCES)
                    .add(BlockRegistry.DIESEL_ENGINE)
                    .add(BlockRegistry.MODULAR_DIESEL_ENGINE);
        }
    }