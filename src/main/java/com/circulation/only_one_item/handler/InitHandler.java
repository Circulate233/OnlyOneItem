package com.circulation.only_one_item.handler;

import com.circulation.only_one_item.util.OOIMekRecipe;
import mekanism.common.recipe.RecipeHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

public class InitHandler {

    public static void allPreInit() {
        MatchFluidHandler.lock();
        MatchItemHandler.preItemStackInit();
        MatchFluidHandler.preFluidStackInit();

        if (Loader.isModLoaded("mekanism")) MekInit();
    }

    @Optional.Method(modid = "mekanism")
    private static void MekInit() {
        RecipeHandler.Recipe.values().forEach(recipe -> ((OOIMekRecipe) recipe).ooi$refresh());
    }
}
