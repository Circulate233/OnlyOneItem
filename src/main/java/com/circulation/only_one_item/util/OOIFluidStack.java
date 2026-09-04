package com.circulation.only_one_item.util;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.registries.IRegistryDelegate;

public interface OOIFluidStack {

    void ooi$ooiInit(Fluid fluid);

    void ooi$replace(Fluid target);

    void ooi$inheritReplacementState(Fluid originalFluid, IRegistryDelegate<Fluid> currentDelegate);
}
