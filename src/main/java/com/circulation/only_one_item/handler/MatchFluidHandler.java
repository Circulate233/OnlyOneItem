package com.circulation.only_one_item.handler;

import com.circulation.only_one_item.conversion.FluidConversionTarget;
import com.circulation.only_one_item.OnlyOneItem;
import com.circulation.only_one_item.crt.CrtConversionFluidTarget;
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
import net.minecraftforge.fml.common.Optional;

import java.util.List;
import java.util.LinkedHashMap;
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

    public static synchronized void Clear() {
        fluidNameToTargetMap.clear();
        fluidToTargetMap.clear();
    }

    public static synchronized void Init(List<FluidConversionTarget> fluids) {
        Clear();
        Map<String, FluidConversionTarget> targetsByMatch = new LinkedHashMap<>();
        Map<String, String> sourcesByMatch = new LinkedHashMap<>();
        for (int index = 0; index < 2; index++) {
            List<FluidConversionTarget> targets = index == 0 ? fluids : CrtConversionFluidTarget.list;
            String source = index == 0 ? "JSON" : "CRT";
            for (FluidConversionTarget target : targets) {
                if (target == null || target.getMatchFluids() == null || target.getMatchFluids().isEmpty()) {
                    OnlyOneItem.LOGGER.error("[OOI] Invalid {} fluid mapping", source);
                    throw new IllegalStateException("[OOI] Invalid " + source + " fluid mapping");
                }
                for (String matchFluid : target.getMatchFluids()) {
                    if (matchFluid == null || matchFluid.trim().isEmpty()) {
                        OnlyOneItem.LOGGER.error("[OOI] Blank match fluid in {} mapping {}", source, target.getTargetID());
                        throw new IllegalStateException("[OOI] Blank match fluid in " + source + " mapping");
                    }
                    targetsByMatch.put(matchFluid, target);
                    sourcesByMatch.put(matchFluid, source);
                }
            }
        }

        Map<String, FluidConversionTarget> finalTargets = new LinkedHashMap<>();
        for (Map.Entry<String, FluidConversionTarget> entry : targetsByMatch.entrySet()) {
            FluidConversionTarget target = entry.getValue();
            Fluid targetFluid = target.getTarget();
            if (targetFluid == null) {
                OnlyOneItem.LOGGER.error(
                    "[OOI] Dropping {} fluid mapping because target fluid is not registered: targetID={}, match={}",
                    sourcesByMatch.get(entry.getKey()), target.getTargetID(), entry.getKey());
                continue;
            }
            FluidConversionTarget finalTarget = finalTargets.get(target.getTargetID());
            if (finalTarget == null) {
                finalTarget = new FluidConversionTarget(target.getTargetID())
                    .setMatchFluids(new java.util.LinkedHashSet<>());
                finalTargets.put(target.getTargetID(), finalTarget);
            }
            finalTarget.getMatchFluids().add(entry.getKey());
        }

        fluids.clear();
        fluids.addAll(finalTargets.values());
        for (FluidConversionTarget target : fluids) {
            Fluid targetFluid = target.getTarget();
            for (String matchFluid : target.getMatchFluids()) {
                fluidNameToTargetMap.put(matchFluid, targetFluid);
            }
        }
    }

    @Optional.Method(modid = "crafttweaker")
    public static void CrtInit() {
        OnlyOneItem.LOGGER.debug("[OOI] CRT fluid mappings staged: {}", CrtConversionFluidTarget.list.size());
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
