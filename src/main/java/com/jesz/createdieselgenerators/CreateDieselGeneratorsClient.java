package com.jesz.createdieselgenerators;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jesz.createdieselgenerators.blocks.ct.SpriteShifts;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.fluids.FluidRegistry;
import com.jesz.createdieselgenerators.ponder.PonderIndex;
import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.foundation.utility.Pair;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.fml.config.ModConfig;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class CreateDieselGeneratorsClient implements ClientModInitializer {

    public static Map<String, String> lighterSkins = new HashMap<>();

    @Override
    public void onInitializeClient() {
        PartialModels.init();
        SpriteShifts.init();
        //ModLoadingContext.registerConfig(CreateDieselGenerators.ID,  ModConfig.Type.CLIENT, ConfigRegistry.CLIENT_SPEC);
        ForgeConfigRegistry.INSTANCE.register(CreateDieselGenerators.ID, ModConfig.Type.CLIENT, ConfigRegistry.CLIENT_SPEC);
        //ModelLoadingRegistry.INSTANCE.registerModelProvider(CreateDieselGeneratorsClient::onModelRegistry);
        PreparableModelLoadingPlugin.register(
                new PreparableModelLoadingPlugin.DataLoader<Map<String, Pair<ResourceLocation, Pair<ResourceLocation, ResourceLocation>>>>() {
                    @Override
                    public CompletableFuture<Map<String, Pair<ResourceLocation, Pair<ResourceLocation, ResourceLocation>>>> load(ResourceManager resourceManager, Executor executor) {
                        lighterSkins.clear();
                        Minecraft.getInstance().getResourceManager().getNamespaces().stream().toList().forEach(n -> {
                            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(n, "lighter_skins.json"));
                            if (resource.isEmpty())
                                return;
                            JsonParser parser = new JsonParser();
                            try {
                                JsonElement data = parser.parse(resource.get().openAsReader());
                                data.getAsJsonArray().forEach(jsonElement -> {
                                    lighterSkins.put(jsonElement.getAsJsonObject().getAsJsonPrimitive("name").getAsString(), jsonElement.getAsJsonObject().getAsJsonPrimitive("id").getAsString());
                                });
                            } catch (IOException ignored) {
                            }
                        });
                        //Map<String, Pair<PartialModel, Pair<PartialModel, PartialModel>>> loadedLighterSkinModels = new HashMap<>();
                        Map<String, Pair<ResourceLocation, Pair<ResourceLocation, ResourceLocation>>> resourceLocations = new HashMap<>();
                        resourceLocations.put("standard", Pair.of(new ResourceLocation("createdieselgenerators:item/lighter")
                                , Pair.of(new ResourceLocation("createdieselgenerators:item/lighter_open")
                                        , new ResourceLocation("createdieselgenerators:item/lighter_ignited"))));
                        CreateDieselGeneratorsClient.lighterSkins.forEach((name, skinId) -> {
                            resourceLocations.put(skinId, Pair.of(new ResourceLocation("createdieselgenerators:item/lighter/"+skinId)
                                    , Pair.of(new ResourceLocation("createdieselgenerators:item/lighter/"+skinId+"_open")
                                            , new ResourceLocation("createdieselgenerators:item/lighter/"+skinId+"_ignited"))));
                        });
                        return CompletableFuture.supplyAsync(new Supplier<Map<String, Pair<ResourceLocation, Pair<ResourceLocation, ResourceLocation>>>>() {
                            @Override
                            public Map<String, Pair<ResourceLocation, Pair<ResourceLocation, ResourceLocation>>> get() {
                                return resourceLocations;
                            }
                        }, executor);
                    }
                },
                new PreparableModelLoadingPlugin<Map<String, Pair<ResourceLocation, Pair<ResourceLocation, ResourceLocation>>>>() {
                    @Override
                    public void onInitializeModelLoader(Map<String, Pair<ResourceLocation, Pair<ResourceLocation, ResourceLocation>>> resourceLocations, ModelLoadingPlugin.Context pluginContext) {
                        List<ResourceLocation> resourceLocationsAsList = new ArrayList<>();
                        for(String skinId : resourceLocations.keySet()) {
                            resourceLocationsAsList.add(resourceLocations.get(skinId).getFirst());
                            resourceLocationsAsList.add(resourceLocations.get(skinId).getSecond().getFirst());
                            resourceLocationsAsList.add(resourceLocations.get(skinId).getSecond().getSecond());
                        }
                        pluginContext.addModels(resourceLocationsAsList);

                        resourceLocations.forEach((skinId, pair) -> {
                            PartialModels.lighterSkinModels.put(skinId, Pair.of(new PartialModel(pair.getFirst())
                                    , Pair.of(new PartialModel(pair.getSecond().getFirst())
                                            , new PartialModel(pair.getSecond().getSecond()))));
                        });

                        //PartialModels.initSkins();
                    }
                });

        //BuiltinItemRendererRegistry.INSTANCE.register(ItemRegistry.CHEMICAL_SPRAYER, new ChemicalSprayerItemRenderer());
        //BuiltinItemRendererRegistry.INSTANCE.register(ItemRegistry.LIGHTER, new LighterItemRenderer());

        PonderIndex.register();

        BlockRenderLayerMap.INSTANCE.putFluids(RenderType.translucent(), FluidRegistry.ETHANOL.get(), FluidRegistry.ETHANOL.get());
        //consumer.accept(SimpleCustomRenderer.create(this, new ChemicalSprayerItemRenderer()));
    }
}
