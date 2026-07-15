package com.circulation.only_one_item.handler;

import com.circulation.only_one_item.OnlyOneItem;
import com.circulation.only_one_item.mixin.ic2.AccessorMachineRecipeHelper;
import com.circulation.only_one_item.mixin.ic2.AccessorUuIndex;
import com.circulation.only_one_item.mixin.mek.AccessorMekanismItemStackInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.Recipes;
import ic2.core.uu.UuIndex;
import mekanism.common.OreDictCache;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.outputs.MachineOutput;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public class InitHandler {

    public static void allPreInit() {
        boolean ic2Loaded = Loader.isModLoaded("ic2");
        boolean mekanismLoaded = Loader.isModLoaded("mekanism");
        if (ic2Loaded) {
            IC2RecipeCaches.beforeItemReplacement();
        }

        MatchFluidHandler.lock();
        MatchItemHandler.preItemStackInit();
        MatchFluidHandler.preFluidStackInit();

        if (mekanismLoaded) {
            MekanismRecipeCaches.afterStackReplacement();
        }
        if (ic2Loaded) {
            IC2RecipeCaches.afterItemReplacement();
        }
    }

    private static final class MekanismRecipeCaches {

        @SuppressWarnings({"rawtypes"})
        private static void afterStackReplacement() {
            int recipeMapCount = 0;
            int recipeCount = 0;
            int collapsedRecipeCount = 0;

            for (RecipeHandler.Recipe recipeType : RecipeHandler.Recipe.values()) {
                int originalRecipeCount = recipeType.get().size();
                rebuild(recipeType);
                recipeMapCount++;
                recipeCount += originalRecipeCount;
                int collapsed = originalRecipeCount - recipeType.get().size();
                collapsedRecipeCount += collapsed;
                if (collapsed > 0) {
                    OnlyOneItem.LOGGER.warn(
                        "[OOI] Mekanism recipe map {} collapsed {} duplicate inputs after replacement",
                        recipeType.getRecipeName(), collapsed);
                }
            }

            OreDictCache.cachedKeys.clear();
            OreDictCache.oreDictStacks.clear();
            OreDictCache.modIDStacks.clear();

            OnlyOneItem.LOGGER.info(
                "[OOI] Rebuilt {} Mekanism recipe maps with {} recipes after stack replacement; collapsed={}",
                recipeMapCount, recipeCount, collapsedRecipeCount);
        }

        private static <
            INPUT extends MachineInput<INPUT>,
            OUTPUT extends MachineOutput<OUTPUT>,
            RECIPE extends mekanism.common.recipe.machines.MachineRecipe<INPUT, OUTPUT, RECIPE>>
        void rebuild(RecipeHandler.Recipe<INPUT, OUTPUT, RECIPE> recipeType) {
            List<RECIPE> recipes = new ArrayList<>(recipeType.get().values());
            HashMap<INPUT, RECIPE> rebuiltRecipes = new HashMap<>(recipes.size());

            for (RECIPE recipe : recipes) {
                INPUT input = recipe.getInput();
                if (input instanceof ItemStackInput itemStackInput) {
                    AccessorMekanismItemStackInput accessor = (AccessorMekanismItemStackInput) itemStackInput;
                    accessor.ooi$setIngredientHash(itemStackInput.hashIngredients());
                    accessor.ooi$setWildVersion(null);
                }
                rebuiltRecipes.put(input, recipe);
            }

            recipeType.get().clear();
            recipeType.get().putAll(rebuiltRecipes);
        }
    }

    private static final class IC2RecipeCaches {

        private static boolean uuIndexInitialized;

        private static void beforeItemReplacement() {
            AccessorUuIndex accessor = (AccessorUuIndex) UuIndex.instance;
            uuIndexInitialized = !accessor.ooi$getResolvers().isEmpty();
            if (uuIndexInitialized) {
                UuIndex.instance.get(ItemStack.EMPTY);
            }
        }

        private static void afterItemReplacement() {
            Object[] recipeManagers = {
                Recipes.macerator,
                Recipes.extractor,
                Recipes.compressor,
                Recipes.centrifuge,
                Recipes.blockcutter,
                Recipes.blastfurnace,
                Recipes.metalformerExtruding,
                Recipes.metalformerCutting,
                Recipes.metalformerRolling,
                Recipes.oreWashing,
                Recipes.recyclerBlacklist,
                Recipes.recyclerWhitelist
            };
            Set<Object> rebuiltManagers = Collections.newSetFromMap(new IdentityHashMap<>());
            int recipeCount = 0;

            for (Object recipeManager : recipeManagers) {
                if (!(recipeManager instanceof AccessorMachineRecipeHelper accessor)
                    || !rebuiltManagers.add(recipeManager)) {
                    continue;
                }

                accessor.ooi$getRecipeCache().clear();
                accessor.ooi$getUncacheableRecipes().clear();
                for (MachineRecipe<?, ?> recipe : accessor.ooi$getRecipes().values()) {
                    accessor.ooi$addToCache(recipe);
                    recipeCount++;
                }
            }

            if (rebuiltManagers.isEmpty()) {
                OnlyOneItem.LOGGER.error("[OOI] IC2 is loaded, but no IC2 recipe cache was rebuilt");
                throw new IllegalStateException("[OOI] Failed to rebuild IC2 recipe caches");
            }

            if (uuIndexInitialized) {
                UuIndex.instance.refresh(true);
            }

            OnlyOneItem.LOGGER.info(
                "[OOI] Rebuilt {} IC2 recipe caches with {} recipes after item replacement; UU index refreshed={}",
                rebuiltManagers.size(), recipeCount, uuIndexInitialized);
        }
    }
}
