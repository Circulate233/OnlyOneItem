package com.circulation.only_one_item.handler;

import com.circulation.only_one_item.conversion.FluidConversionTarget;
import com.circulation.only_one_item.util.OOIFluidStack;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IRegistryDelegate;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

public class MatchFluidHandler {

    private static final Map<String, Fluid> fluidNameToTargetMap = new Object2ReferenceOpenHashMap<>();
    private static List<WeakReference<OOIFluidStack>> list = new ObjectArrayList<>();

    public static void preFluidStackInit() {
        if (list == null)
            throw new RuntimeException("[OOI] Initialization should not be performed multiple times");
        ((OOIFluidStack) new FluidStack(FluidRegistry.WATER, 1)).ooi$init();
        list.parallelStream()
            .forEach(ref -> {
                var fluid = ref.get();
                if (fluid != null) {
                    IRegistryDelegate<Fluid> stack;
                    if ((stack = fluid.ooi$getFluidDelegate()) != null) {
                        fluid.ooi$ooiInit(stack.get());
                    }
                }
            });
        list.clear();
        list = null;
    }

    public static synchronized void addPreFluidStack(OOIFluidStack i) {
        if (list == null)
            throw new RuntimeException("[OOI] It should not be added again after initialization");
        list.add(new WeakReference<>(i));
    }

    public static Fluid match(Object obj) {
        if (!(obj instanceof Fluid fluid)) return null;
        return fluidNameToTargetMap.get(fluid.getName());
    }

    public static void Init(List<FluidConversionTarget> fluids) {
        for (FluidConversionTarget t : fluids) {
            for (String fluid : t.getMatchFluids()) {
                if (t.getTarget() != null) {
                    fluidNameToTargetMap.put(fluid, t.getTarget());
                }
            }
        }
        fluids.clear();
    }
}
