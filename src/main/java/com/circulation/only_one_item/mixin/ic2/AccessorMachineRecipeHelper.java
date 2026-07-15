package com.circulation.only_one_item.mixin.ic2;

import ic2.api.recipe.MachineRecipe;
import ic2.core.recipe.MachineRecipeHelper;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Map;

/**
 * Exposes IC2's machine recipes and their derived lookup collections so OOI can rebuild stale indexes.
 */
@Mixin(value = MachineRecipeHelper.class, remap = false)
public interface AccessorMachineRecipeHelper {

    /** Returns the authoritative recipe table. */
    @Accessor("recipes")
    Map<?, MachineRecipe<?, ?>> ooi$getRecipes();

    /** Returns IC2's item-keyed recipe cache. */
    @Accessor("recipeCache")
    Map<Item, List<MachineRecipe<?, ?>>> ooi$getRecipeCache();

    /** Returns recipes that IC2 could not place in the item-keyed cache. */
    @Accessor("uncacheableRecipes")
    List<MachineRecipe<?, ?>> ooi$getUncacheableRecipes();

    /** Reindexes one authoritative recipe using IC2's own cache-building logic. */
    @Invoker("addToCache")
    void ooi$addToCache(MachineRecipe<?, ?> recipe);
}
