package com.jesz.createdieselgenerators.recipes;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.foundation.utility.CreateLang;
import com.tterrag.registrate.fabric.RegistryObject;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Supplier;

public enum RecipeRegistry implements IRecipeTypeInfo {

    BASIN_FERMENTING(BasinFermentingRecipe::new),
    DISTILLATION(DistillationRecipe::new);
    private final ResourceLocation id;
    private final RecipeSerializer<?> serializerObject;
    @Nullable
    private final RecipeType<?> typeObject;
    private final Supplier<RecipeType<?>> type;

    RecipeRegistry(Supplier<RecipeSerializer<?>> serializerSupplier) {
        String name = CreateLang.asId(name());
        id = CreateDieselGenerators.asResource(name);
        serializerObject = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializerSupplier.get());
        typeObject = simpleType(id);
        Registry.register(BuiltInRegistries.RECIPE_TYPE, id, typeObject);
        type = () -> typeObject;
    }

    RecipeRegistry(ProcessingRecipeBuilder.ProcessingRecipeFactory<?> processingFactory) {
        this(() -> new ProcessingRecipeSerializer<>(processingFactory));
    }

    public static void register() {

    }

    public static <T extends Recipe<?>> RecipeType<T> simpleType(ResourceLocation id) {
        String stringId = id.toString();
        return new RecipeType<T>() {
            @Override
            public String toString() {
                return stringId;
            }
        };
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public <T extends RecipeSerializer<?>> T getSerializer() {
        return (T) serializerObject;
    }

    @Override
    public <T extends RecipeType<?>> T getType() {
        return (T) type.get();
    }
}
