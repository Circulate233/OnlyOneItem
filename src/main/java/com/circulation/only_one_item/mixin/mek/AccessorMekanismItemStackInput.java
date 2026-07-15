package com.circulation.only_one_item.mixin.mek;

import mekanism.common.recipe.inputs.ItemStackInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes Mekanism's derived item-input state for rebuilding recipe indexes after OOI replaces stacks.
 */
@Mixin(value = ItemStackInput.class, remap = false)
public interface AccessorMekanismItemStackInput {

    /** Replaces the ingredient hash cached before item replacement. */
    @Accessor("ingredientHash")
    void ooi$setIngredientHash(int ingredientHash);

    /** Clears the wildcard input derived from the ingredient before item replacement. */
    @Accessor("wildVersion")
    void ooi$setWildVersion(ItemStackInput wildVersion);
}
