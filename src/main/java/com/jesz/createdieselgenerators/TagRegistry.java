package com.jesz.createdieselgenerators;

import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.fabricators_of_create.porting_lib.tags.Tags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import static com.simibubi.create.AllTags.optionalTag;

public class TagRegistry {

    public enum NameSpace {

        CDG_MOD(CreateDieselGenerators.ID, false, true),
        FORGE("c");

        public final String id;
        public final boolean optionalDefault;
        public final boolean alwaysDatagenDefault;

        NameSpace(String id) {
            this(id, true, false);
        }

        NameSpace(String id, boolean optionalDefault, boolean alwaysDatagenDefault) {
            this.id = id;
            this.optionalDefault = optionalDefault;
            this.alwaysDatagenDefault = alwaysDatagenDefault;
        }
    }

    public enum FluidTags {

        PUMPJACK_OUTPUT(NameSpace.CDG_MOD),
        CRUDE_OIL(NameSpace.FORGE),
        BIODIESEL(NameSpace.FORGE),
        ETHANOL(NameSpace.FORGE),
        GASOLINE(NameSpace.FORGE),
        PLANT_OIL(NameSpace.FORGE);

        public final TagKey<Fluid> tag;
        public final boolean alwaysDatagen;

        FluidTags() {
            this(NameSpace.CDG_MOD);
        }

        FluidTags(NameSpace namespace) {
            this(namespace, namespace.optionalDefault, namespace.alwaysDatagenDefault);
        }

        FluidTags(NameSpace namespace, String path) {
            this(namespace, path, namespace.optionalDefault, namespace.alwaysDatagenDefault);
        }

        FluidTags(NameSpace namespace, boolean optional, boolean alwaysDatagen) {
            this(namespace, null, optional, alwaysDatagen);
        }

        FluidTags(NameSpace namespace, String path, boolean optional, boolean alwaysDatagen) {
            ResourceLocation id = new ResourceLocation(namespace.id, path == null ? CreateLang.asId(name()) : path);
            tag = optionalTag(BuiltInRegistries.FLUID, id);
            this.alwaysDatagen = alwaysDatagen;
        }

        @SuppressWarnings("deprecation")
        public boolean matches(Fluid fluid) {
            return fluid.is(tag);
        }

        public boolean matches(FluidState state) {
            return state.is(tag);
        }

        public static void init() {}
    }
}
