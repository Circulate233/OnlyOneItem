package com.circulation.only_one_item.mixin.ic2;

import ic2.core.uu.IRecipeResolver;
import ic2.core.uu.UuIndex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Exposes the IC2 UU resolver list used to detect whether its recipe graph has already been initialized.
 */
@Mixin(value = UuIndex.class, remap = false)
public interface AccessorUuIndex {

    /** Returns the resolvers backing the current UU recipe graph. */
    @Accessor("resolvers")
    List<IRecipeResolver> ooi$getResolvers();
}
