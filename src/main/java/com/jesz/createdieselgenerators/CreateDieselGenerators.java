package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.compat.EveryCompatCompat;
import com.jesz.createdieselgenerators.compat.computercraft.CCProxy;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.entity.EntityRegistry;
import com.jesz.createdieselgenerators.fluids.FluidRegistry;
import com.jesz.createdieselgenerators.items.FluidStorageItem;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.jesz.createdieselgenerators.other.SpoutCanisterFilling;
import com.jesz.createdieselgenerators.recipes.RecipeRegistry;
import com.jesz.createdieselgenerators.sounds.SoundRegistry;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.foundation.data.CreateRegistrate;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import io.github.fabricators_of_create.porting_lib.event.common.ExplosionEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class CreateDieselGenerators implements ModInitializer {
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create("createdieselgenerators");
    public static final String ID = "createdieselgenerators";
    public static final String NAME = "Create: Diesel Generators";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);


    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(ID, path);
    }

    @Override
    public void onInitialize() {
        TagRegistry.FluidTags.init();
        SoundRegistry.prepare();
        ItemRegistry.register();
        BlockRegistry.register();
        FluidRegistry.register();
        BlockEntityRegistry.register();
        EntityRegistry.register();
        SoundRegistry.register();
        RecipeRegistry.register();
        CreativeTab.registerItemGroups();

        REGISTRATE.register();

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(FuelTypeManager.ReloadListener.INSTANCE);

        ExplosionEvents.DETONATE.register(Events::onExplosion);
        CommandRegistrationCallback.EVENT.register(Events::onCommandRegister);

        FluidStorage.ITEM.registerFallback((itemStack, context) -> {
            if (itemStack.getItem() instanceof FluidStorageItem storageItem)
                return storageItem.getFluidStorage(itemStack, context);
            return null;
        });

        if(FabricLoader.getInstance().isModLoaded("moonlight")) {
            EveryCompatCompat.init();
        }

        Mods.COMPUTERCRAFT.executeIfInstalled(() -> CCProxy::register);

        BlockSpoutingBehaviour.BY_BLOCK_ENTITY.register(BlockEntityRegistry.CANISTER.get(), new SpoutCanisterFilling());
        //ForgeConfigRegistry.INSTANCE.register()
        //ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigRegistry.SERVER_SPEC, "createdieselgenerators-server.toml");
        //ConfigRegistry.register();
        ForgeConfigRegistry.INSTANCE.register(ID,  ModConfig.Type.SERVER, ConfigRegistry.SERVER_SPEC);
        //ModLoadingContext.registerConfig(ID,  ModConfig.Type.SERVER, ConfigRegistry.SERVER_SPEC);
    }

    public static int getOilAmount(ServerLevel serverLevel, Holder<Biome> biome, int x, int z, long seed){
        Random random = new Random(new Random(seed).nextLong() + (long) x * z);
        int amount = Math.abs(random.nextInt());
        var reg = serverLevel.registryAccess().registry(Registries.BIOME);
        if (reg.isPresent()) {

            TagKey<Biome> key = AllTags.optionalTag(reg.get(), CreateDieselGenerators.asResource("oil_biomes"));

            boolean isHighInOil = biome == null || biome.is(key);
            if(biome != null && biome.is(AllTags.optionalTag(reg.get(), new ResourceLocation("createdieselgenerators:deny_oil_biomes")))) {
                return 0;
            }

            if(isHighInOil ? (random.nextFloat(0, 100) >= ConfigRegistry.HIGH_OIL_PERCENTAGE.get()) : (amount % 100 >= ConfigRegistry.OIL_PERCENTAGE.get())) {
                return 0;
            }
            if(ConfigRegistry.OIL_DEPOSITS_INFINITE.get()) {
                return Integer.MAX_VALUE;
            }

            if(isHighInOil) {
                return (int) (Mth.clamp(amount % 400000, 8000, 400000)*ConfigRegistry.HIGH_OIL_MULTIPLIER.get());
            }
            return (int) (Mth.clamp(amount % 15000, 0, 12000)*ConfigRegistry.OIL_MULTIPLIER.get());
        }
        return 0;
    }


}
