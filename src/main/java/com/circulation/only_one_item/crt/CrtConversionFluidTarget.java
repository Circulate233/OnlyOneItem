package com.circulation.only_one_item.crt;

import com.circulation.only_one_item.conversion.FluidConversionTarget;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.liquid.ILiquidStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.List;
import java.util.Set;

@ZenRegister
@ZenClass("mods.ooi.ConversionFluid")
@SuppressWarnings("UnusedReturnValue")
public class CrtConversionFluidTarget {

    public static final List<FluidConversionTarget> list = new ObjectArrayList<>();

    private final Set<String> matchFluids = new ObjectOpenHashSet<>();
    private final String targetID;

    public CrtConversionFluidTarget(String id) {
        this.targetID = id;
    }

    @ZenMethod
    public static CrtConversionFluidTarget create(ILiquidStack target) {
        return new CrtConversionFluidTarget(target.getName());
    }

    @ZenMethod
    public CrtConversionFluidTarget addMatchFluid(String name) {
        matchFluids.add(name);
        return this;
    }

    @ZenMethod
    public CrtConversionFluidTarget addMatchFluid(ILiquidStack stack) {
        matchFluids.add(stack.getName());
        return this;
    }

    @ZenMethod
    public CrtConversionFluidTarget addMatchFluid(Object... objs) {
        for (Object obj : objs) {
            if (obj instanceof String name) {
                addMatchFluid(name);
            } else if (obj instanceof ILiquidStack stack) {
                addMatchFluid(stack);
            }
        }
        return this;
    }

    @ZenMethod
    public void register() {
        list.add(new FluidConversionTarget(targetID).setMatchFluids(matchFluids));
    }
}
