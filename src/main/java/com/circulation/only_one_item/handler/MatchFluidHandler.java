package com.circulation.only_one_item.handler;

import com.circulation.only_one_item.conversion.FluidConversionTarget;
import com.circulation.only_one_item.util.OOIFluidStack;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import lombok.Synchronized;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IRegistryDelegate;

import java.util.List;
import java.util.Map;

public class MatchFluidHandler {

    private static final Map<String, Fluid> fluidNameToTargetMap = new Object2ReferenceOpenHashMap<>();
    private static final Reference2ReferenceMap<Fluid, Fluid> fluidToTargetMap = new Reference2ReferenceOpenHashMap<>();
    private static List<OOIFluidStack> list = new ObjectArrayList<>();

    public static void preFluidStackInit() {
        if (list == null)
            throw new RuntimeException("[OOI] Initialization should not be performed multiple times");
        ((OOIFluidStack) new FluidStack(FluidRegistry.WATER, 1)).ooi$init();
        list.parallelStream()
            .forEach(fluid -> {
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

    @Synchronized("list")
    public static void addPreFluidStack(OOIFluidStack i) {
        if (list == null)
            throw new RuntimeException("[OOI] It should not be added again after initialization");
        list.add(i);
    }

    public static Fluid match(Object obj) {
        if (!(obj instanceof Fluid fluid)) return null;
        return fluidToTargetMap.get(fluid);
    }

    public static synchronized void Init(List<FluidConversionTarget> fluids) {
        for (FluidConversionTarget t : fluids) {
            if (t == null || t.getMatchFluids() == null) {
                continue;
            }
            Fluid target = t.getTarget();
            if (target == null) {
                continue;
            }
            for (String fluid : t.getMatchFluids()) {
                if (fluid != null) {
                    fluidNameToTargetMap.put(fluid, target);
                }
            }
        }
        fluids.clear();
    }

    public static synchronized void lock() {
        fluidNameToTargetMap.forEach((key, fluid) -> {
            var f = FluidRegistry.getFluid(key);
            if (f != null) {
                fluidToTargetMap.put(f, fluid);
            }
        });
        fluidNameToTargetMap.clear();
    }
}
