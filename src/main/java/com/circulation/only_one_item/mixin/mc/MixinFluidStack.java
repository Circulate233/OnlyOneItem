package com.circulation.only_one_item.mixin.mc;

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Mixin(FluidStack.class)

public abstract class MixinFluidStack implements OOIFluidStack {

    @Unique
    private static boolean ooi$init = false;
    @Unique
    private static Method ooi$delegates;
    @Shadow(remap = false)
    private IRegistryDelegate<Fluid> fluidDelegate;

    @Unique
    private static IRegistryDelegate<Fluid> ooi$makeDelegate(Fluid fluid) {
        if (ooi$delegates == null) {
            Class<?> clazz = FluidRegistry.class;
            try {
                ooi$delegates = clazz.getDeclaredMethod("makeDelegate", Fluid.class);
            } catch (NoSuchMethodException ignored) {

            }
        }
        try {
            if (ooi$delegates != null) {
                return (IRegistryDelegate<Fluid>) ooi$delegates.invoke(null, fluid);
            }
        } catch (InvocationTargetException | IllegalAccessException ignored) {
        }
        return null;
    }

    @Inject(method = "<init>(Lnet/minecraftforge/fluids/Fluid;I)V", at = @At("TAIL"), remap = false)
    private void onInit(Fluid fluid, int amount, CallbackInfo ci) {
        if (ooi$init) {
            ooi$ooiInit(fluid);
        } else {
            MatchFluidHandler.addPreFluidStack(this);
        }
    }

    @Override
    public void ooi$init() {
        ooi$init = true;
    }

    @Override
    public void ooi$ooiInit(Fluid fluid) {
        Fluid target = MatchFluidHandler.match(fluid);

        if (target != null) {
            var d = ooi$makeDelegate(target);
            if (d != null) {
                fluidDelegate = d;
            }
        }
    }

    @Override
    public IRegistryDelegate<Fluid> ooi$getFluidDelegate() {
        return this.fluidDelegate;
    }
}
