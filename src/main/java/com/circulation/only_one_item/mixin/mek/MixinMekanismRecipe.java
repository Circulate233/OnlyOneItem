package com.circulation.only_one_item.mixin.mek;

import com.circulation.only_one_item.util.OOIMekRecipe;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.outputs.MachineOutput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;

@Mixin(value = RecipeHandler.Recipe.class, remap = false)
public abstract class MixinMekanismRecipe<INPUT extends MachineInput<INPUT>, OUTPUT extends MachineOutput<OUTPUT>, RECIPE extends MachineRecipe<INPUT, OUTPUT, RECIPE>> implements OOIMekRecipe {

    @Shadow
    @Final
    private HashMap<INPUT, RECIPE> recipes;

    @Unique
    @Override
    public void ooi$refresh() {
        Object2ObjectMap<INPUT, RECIPE> map = new Object2ObjectOpenHashMap<>(recipes);
        recipes.clear();
        recipes.putAll(map);
    }
}