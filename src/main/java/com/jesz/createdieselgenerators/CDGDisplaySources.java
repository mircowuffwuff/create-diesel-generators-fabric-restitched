package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.other.OilAmountDisplaySource;
import com.tterrag.registrate.util.entry.RegistryEntry;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;

public class CDGDisplaySources {
    public static final RegistryEntry<OilAmountDisplaySource> OIL_AMOUNT = REGISTRATE.displaySource("oil_amount", OilAmountDisplaySource::new).register();
}
