package com.circulation.only_one_item.handler;

import com.circulation.only_one_item.OOIConfig;
import com.circulation.only_one_item.OnlyOneItem;
import com.circulation.only_one_item.conversion.FluidConversionTarget;
import com.circulation.only_one_item.util.OOIFluidStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.Map;

public class MatchFluidHandler {

    private static final Map<String, FluidConversionTarget> fluidNameToTargetMap = new Object2ObjectOpenHashMap<>();
    private static final Reference2ReferenceMap<Fluid, FluidConversionTarget> fluidToTargetMap = new Reference2ReferenceOpenHashMap<>();
    private static Reference2ObjectMap<FluidConversionTarget, ObjectArrayList<OOIFluidStack>> map =
        new Reference2ObjectOpenHashMap<>();

    public static void preFluidStackInit() {
        processPendingStacks();
        if (map == null) {
            throw new IllegalStateException("[OOI] Fluid pending targets were already finalized");
        }
        for (Reference2ObjectMap.Entry<FluidConversionTarget, ObjectArrayList<OOIFluidStack>> entry
            : map.reference2ObjectEntrySet()) {
            OnlyOneItem.LOGGER.error(
                "[OOI] Dropping pending fluid stacks because target fluid is not registered: targetID={}, stacks={}",
                entry.getKey().getTargetID(), entry.getValue().size());
        }
        map.clear();
        map = null;
    }

    public static void processPendingStacks() {
        if (map == null || map.isEmpty()) {
            return;
        }

        ObjectArrayList<FluidConversionTarget> initializedTargets = new ObjectArrayList<>();
        for (Reference2ObjectMap.Entry<FluidConversionTarget, ObjectArrayList<OOIFluidStack>> entry
            : map.reference2ObjectEntrySet()) {
            Fluid target = entry.getKey().getTarget();
            if (target == null) {
                continue;
            }
            ObjectArrayList<OOIFluidStack> stacks = entry.getValue();
            for (int index = 0, size = stacks.size(); index < size; index++) {
                stacks.get(index).ooi$replace(target);
            }
            initializedTargets.add(entry.getKey());
        }
        for (int index = 0, size = initializedTargets.size(); index < size; index++) {
            map.remove(initializedTargets.get(index));
        }
    }

    public static void addPreFluidStack(FluidConversionTarget target, OOIFluidStack stack) {
        if (map == null) {
            return;
        }
        for (ObjectArrayList<OOIFluidStack> stacks : map.values()) {
            for (int index = 0, size = stacks.size(); index < size; index++) {
                if (stacks.get(index) == stack) {
                    return;
                }
            }
        }
        map.computeIfAbsent(target, key -> new ObjectArrayList<>()).add(stack);
    }

    public static FluidConversionTarget match(Object obj) {
        if (!(obj instanceof Fluid fluid)) return null;
        FluidConversionTarget target = fluidToTargetMap.get(fluid);
        return target == null ? fluidNameToTargetMap.get(fluid.getName()) : target;
    }

    public static synchronized void Clear() {
        fluidNameToTargetMap.clear();
        fluidToTargetMap.clear();
    }

    public static synchronized void Init() {
        Clear();
        for (int index = 0, size = OOIConfig.fluids.size(); index < size; index++) {
            addTarget(OOIConfig.fluids.get(index));
        }
    }

    public static synchronized void registerTarget(FluidConversionTarget target) {
        validateTarget(target);
        OOIConfig.fluids.add(target);
        addTarget(target);
    }

    public static synchronized void finalizeTargets() {
        Map<String, FluidConversionTarget> targetsByMatch = new Object2ObjectLinkedOpenHashMap<>();
        for (int index = 0, size = OOIConfig.fluids.size(); index < size; index++) {
            FluidConversionTarget target = OOIConfig.fluids.get(index);
            validateTarget(target);
            for (String matchFluid : target.getMatchFluids()) {
                if (matchFluid == null || matchFluid.trim().isEmpty()) {
                    OnlyOneItem.LOGGER.error("[OOI] Blank match fluid in mapping {}", target.getTargetID());
                    throw new IllegalStateException("[OOI] Blank match fluid in mapping " + target.getTargetID());
                }
                targetsByMatch.put(matchFluid, target);
            }
        }

        Map<String, FluidConversionTarget> finalTargets = new Object2ObjectLinkedOpenHashMap<>();
        for (Map.Entry<String, FluidConversionTarget> entry : targetsByMatch.entrySet()) {
            FluidConversionTarget target = entry.getValue();
            Fluid targetFluid = target.getTarget();
            if (targetFluid == null) {
                OnlyOneItem.LOGGER.error(
                    "[OOI] Dropping fluid mapping because target fluid is not registered: targetID={}, match={}",
                    target.getTargetID(), entry.getKey());
                continue;
            }
            FluidConversionTarget finalTarget = finalTargets.get(target.getTargetID());
            if (finalTarget == null) {
                finalTarget = new FluidConversionTarget(target.getTargetID())
                    .setMatchFluids(new ObjectLinkedOpenHashSet<>());
                finalTargets.put(target.getTargetID(), finalTarget);
            }
            finalTarget.getMatchFluids().add(entry.getKey());
        }

        OOIConfig.fluids.clear();
        OOIConfig.fluids.addAll(finalTargets.values());
        Clear();
        for (int index = 0, size = OOIConfig.fluids.size(); index < size; index++) {
            addTarget(OOIConfig.fluids.get(index));
        }
    }

    private static void addTarget(FluidConversionTarget target) {
        validateTarget(target);
        for (String matchFluid : target.getMatchFluids()) {
            if (matchFluid == null || matchFluid.trim().isEmpty()) {
                OnlyOneItem.LOGGER.error("[OOI] Blank match fluid in mapping {}", target.getTargetID());
                throw new IllegalStateException("[OOI] Blank match fluid in mapping " + target.getTargetID());
            }
            fluidNameToTargetMap.put(matchFluid, target);
        }
    }

    private static void validateTarget(FluidConversionTarget target) {
        if (target == null || target.getMatchFluids() == null || target.getMatchFluids().isEmpty()) {
            OnlyOneItem.LOGGER.error("[OOI] Invalid fluid mapping");
            throw new IllegalStateException("[OOI] Invalid fluid mapping");
        }
    }

    public static synchronized void lock() {
        fluidNameToTargetMap.forEach((key, target) -> {
            var f = FluidRegistry.getFluid(key);
            if (f != null) {
                fluidToTargetMap.put(f, target);
            }
        });
        fluidNameToTargetMap.clear();
    }
}
