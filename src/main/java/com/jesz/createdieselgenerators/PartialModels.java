package com.jesz.createdieselgenerators;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.data.Pair;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class PartialModels {
    public static final PartialModel MODULAR_ENGINE_PISTONS_0  = PartialModel.of(new ResourceLocation("createdieselgenerators:block/modular_diesel_engine/pistons/pistons_0"));
    public static final PartialModel MODULAR_ENGINE_PISTONS_1  = PartialModel.of(new ResourceLocation("createdieselgenerators:block/modular_diesel_engine/pistons/pistons_1"));
    public static final PartialModel MODULAR_ENGINE_PISTONS_2  = PartialModel.of(new ResourceLocation("createdieselgenerators:block/modular_diesel_engine/pistons/pistons_2"));
    public static final PartialModel MODULAR_ENGINE_PISTONS_3  = PartialModel.of(new ResourceLocation("createdieselgenerators:block/modular_diesel_engine/pistons/pistons_3"));
    public static final PartialModel MODULAR_ENGINE_PISTONS_4  = PartialModel.of(new ResourceLocation("createdieselgenerators:block/modular_diesel_engine/pistons/pistons_4"));
    public static final PartialModel ENGINE_PISTONS_0          = PartialModel.of(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/pistons_0"));
    public static final PartialModel ENGINE_PISTONS_1          = PartialModel.of(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/pistons_1"));
    public static final PartialModel ENGINE_PISTONS_2          = PartialModel.of(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/pistons_2"));
    public static final PartialModel ENGINE_PISTONS_3          = PartialModel.of(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/pistons_3"));
    public static final PartialModel ENGINE_PISTONS_4          = PartialModel.of(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/pistons_4"));
    public static final PartialModel ENGINE_PISTONS_VERTICAL_0 = PartialModel.of(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/vertical_0"));
    public static final PartialModel ENGINE_PISTONS_VERTICAL_1 = PartialModel.of(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/vertical_1"));
    public static final PartialModel ENGINE_PISTONS_VERTICAL_2 = PartialModel.of(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/vertical_2"));
    public static final PartialModel ENGINE_PISTONS_VERTICAL_3 = PartialModel.of(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/vertical_3"));
    public static final PartialModel ENGINE_PISTONS_VERTICAL_4 = PartialModel.of(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/vertical_4"));

    public static final PartialModel ENGINE_PISTON             = PartialModel.of(new ResourceLocation("createdieselgenerators:block/huge_diesel_engine/piston"));
    public static final PartialModel ENGINE_PISTON_LINKAGE     = PartialModel.of(new ResourceLocation("createdieselgenerators:block/huge_diesel_engine/linkage"));
    public static final PartialModel ENGINE_PISTON_CONNECTOR   = PartialModel.of(new ResourceLocation("createdieselgenerators:block/huge_diesel_engine/shaft_connector"));

    public static final PartialModel PUMPJACK_ROPE             = PartialModel.of(new ResourceLocation("createdieselgenerators:block/pumpjack_rope"));
    public static final PartialModel PUMPJACK_CRANK_SMALL      = PartialModel.of(new ResourceLocation("createdieselgenerators:block/pumpjack_crank/small_counterweight"));
    public static final PartialModel PUMPJACK_CRANK_ROD_SMALL  = PartialModel.of(new ResourceLocation("createdieselgenerators:block/pumpjack_crank/small_rod"));
    public static final PartialModel PUMPJACK_CRANK_LARGE      = PartialModel.of(new ResourceLocation("createdieselgenerators:block/pumpjack_crank/large_counterweight"));
    public static final PartialModel PUMPJACK_CRANK_ROD_LARGE  = PartialModel.of(new ResourceLocation("createdieselgenerators:block/pumpjack_crank/large_rod"));
    public static final PartialModel SMALL_GAUGE_DIAL          = PartialModel.of(new ResourceLocation("createdieselgenerators:block/basin_lid/gauge_dial"));
    public static final PartialModel DISTILLATION_GAUGE        = PartialModel.of(new ResourceLocation("createdieselgenerators:block/distillation_tower/gauge"));
    public static final PartialModel DISTILLATION_GAUGE_DIAL   = PartialModel.of(new ResourceLocation("createdieselgenerators:block/distillation_tower/gauge_dial"));
    public static final PartialModel JEI_DISTILLER_TOP         = PartialModel.of(new ResourceLocation("createdieselgenerators:block/jei_distiller/top"));
    public static final PartialModel JEI_DISTILLER_MIDDLE      = PartialModel.of(new ResourceLocation("createdieselgenerators:block/jei_distiller/middle"));
    public static final PartialModel JEI_DISTILLER_BOTTOM      = PartialModel.of(new ResourceLocation("createdieselgenerators:block/jei_distiller/bottom"));
    public static final PartialModel JEI_ENGINE_PISTON         = PartialModel.of(new ResourceLocation("createdieselgenerators:block/huge_diesel_engine/jei_piston"));
    public static Map<String, Pair<PartialModel, Pair<PartialModel, PartialModel>>> lighterSkinModels = new HashMap<>();
    public static void init(){}
}

