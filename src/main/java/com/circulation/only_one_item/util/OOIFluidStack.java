package com.circulation.only_one_item.util;

import net.minecraftforge.fluids.Fluid;

public interface OOIFluidStack {

    void ooi$ooiInit(Fluid fluid);

    void ooi$replace(Fluid target);
}
