package com.jesz.createdieselgenerators.other;

import com.jesz.createdieselgenerators.blocks.entity.PumpjackHoleBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;


public class OilAmountDisplaySource extends DisplaySource {
    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        if(context.getSourceBlockEntity() instanceof PumpjackHoleBlockEntity sourceBE) {
            return List.of(
                    Component.translatable("createdieselgenerators.display_source.pumpjack_hole_source").append(" : "),
                    CreateLang.number(sourceBE.oilAmount).add(CreateLang.translate("generic.unit.buckets")).component()
            );

        }
        return List.of();
    }

}
