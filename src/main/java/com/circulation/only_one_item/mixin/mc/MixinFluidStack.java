package com.circulation.only_one_item.mixin.mc;

import com.circulation.only_one_item.conversion.FluidConversionTarget;
import com.circulation.only_one_item.handler.MatchFluidHandler;
import com.circulation.only_one_item.util.OOIFluidStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IRegistryDelegate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

@Mixin(FluidStack.class)
public abstract class MixinFluidStack implements OOIFluidStack {

    @Unique
    private static MethodHandle ooi$delegates;
    @Shadow(remap = false)
    private IRegistryDelegate<Fluid> fluidDelegate;

    @Unique
    private static IRegistryDelegate<Fluid> ooi$makeDelegate(Fluid fluid) {
        if (ooi$delegates == null) {
            Class<?> clazz = FluidRegistry.class;
            try {
                ooi$delegates = MethodHandles.lookup().unreflect(clazz.getDeclaredMethod("makeDelegate", Fluid.class));
            } catch (NoSuchMethodException | IllegalAccessException ignored) {

            }
        }
        try {
            if (ooi$delegates != null) {
                return (IRegistryDelegate<Fluid>) ooi$delegates.invoke(fluid);
            }
        } catch (Throwable ignored) {

        }
        return null;
    }

    @Inject(method = "<init>(Lnet/minecraftforge/fluids/Fluid;I)V", at = @At("TAIL"), remap = false)
    private void onInit(Fluid fluid, int amount, CallbackInfo ci) {
        ooi$ooiInit(fluid);
    }

    @Override
    public void ooi$ooiInit(Fluid fluid) {
        FluidConversionTarget conversionTarget = MatchFluidHandler.match(fluid);
        if (conversionTarget == null) {
            return;
        }

        Fluid target = conversionTarget.getTarget();
        if (target == null) {
            MatchFluidHandler.addPreFluidStack(conversionTarget, this);
            return;
        }
        ooi$replace(target);
    }

    @Override
    public void ooi$replace(Fluid target) {
        var delegate = ooi$makeDelegate(target);
        if (delegate != null) {
            fluidDelegate = delegate;
        }
    }
}
