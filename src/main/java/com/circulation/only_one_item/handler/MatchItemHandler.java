package com.circulation.only_one_item.handler;

import com.circulation.only_one_item.OOIConfig;
import com.circulation.only_one_item.OnlyOneItem;
import com.circulation.only_one_item.conversion.ItemConversionTarget;
import com.circulation.only_one_item.emun.Type;
import com.circulation.only_one_item.util.BlackMatchItem;
import com.circulation.only_one_item.util.MatchItem;
import com.circulation.only_one_item.util.OOIItemStack;
import com.circulation.only_one_item.util.RecipeSignature;
import com.circulation.only_one_item.util.SimpleItem;
import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.RegistryManager;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MatchItemHandler {
    private static final Map<ResourceLocation, Int2ObjectMap<ItemConversionTarget>> itemIdToTargetMap = new Object2ObjectOpenHashMap<>();
    private static final Map<String, ItemConversionTarget> odToTargetMap = new Object2ObjectLinkedOpenHashMap<>();
    private static final Map<ResourceLocation, IntSet> finalItemBlackMap = new Object2ObjectOpenHashMap<>();
    private static final Set<String> finalODBlackSet = new ObjectOpenHashSet<>();
    private static final Set<String> finalMODIDBlackSet = new ObjectOpenHashSet<>();
    private static final Set<SimpleItem> allTarget = new ObjectOpenHashSet<>();
    private static final Int2ObjectMap<ItemConversionTarget> defaultMap = Int2ObjectMaps.emptyMap();
    private static Reference2ObjectMap<ItemConversionTarget, ObjectArrayList<OOIItemStack>> map =
        new Reference2ObjectOpenHashMap<>();

    private static final Hash.Strategy<ItemStack> hashItemStack = new Hash.Strategy<>() {
        @Override
        public int hashCode(ItemStack o) {
            return 31 * o.getItem().hashCode();
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            if (a == null || b == null) {
                return false;
            }
            return a.getItem() == b.getItem() && (a.getMetadata() == b.getMetadata() || b.getMetadata() == Short.MAX_VALUE);
        }
    };

    public static void preItemStackInit() {
        odToTargetMap.forEach((od, i) -> {
            var ods = OreDictionary.getOres(od);
            var listC = new ObjectArrayList<>(ods);
            ods.clear();
            for (int index = 0, size = listC.size(); index < size; index++) {
                ItemStack stack = listC.get(index);
                Item item = stack.getItem();
                ResourceLocation rl = item.getRegistryName();
                int meta = stack.getMetadata();
                if (rl == null) continue;
                if ((finalItemBlackMap.containsKey(rl) && finalItemBlackMap.get(rl).contains(meta))
                    || finalMODIDBlackSet.contains(rl.getNamespace())
                    || allTarget.contains(SimpleItem.getInstance(stack))) {
                    ods.add(stack);
                }
            }
            ods.add(i.getItemStack());
        });

        processPendingStacks();
        if (map == null) {
            throw new IllegalStateException("[OOI] Item pending targets were already finalized");
        }
        for (Reference2ObjectMap.Entry<ItemConversionTarget, ObjectArrayList<OOIItemStack>> entry
            : map.reference2ObjectEntrySet()) {
            ItemConversionTarget target = entry.getKey();
            OnlyOneItem.LOGGER.error(
                "[OOI] Dropping pending item stacks because target item is not registered: targetID={}, targetMeta={}, stacks={}",
                target.getTargetID(), target.getTargetMeta(), entry.getValue().size());
        }
        map.clear();
        map = null;

        var f = FurnaceRecipes.instance();
        f.smeltingList = new Object2ObjectOpenCustomHashMap<>(f.smeltingList, hashItemStack);
        f.experienceList = new Object2FloatOpenCustomHashMap<>(f.experienceList, hashItemStack);
    }

    public static void processPendingStacks() {
        if (map == null || map.isEmpty()) {
            return;
        }

        ObjectArrayList<ItemConversionTarget> initializedTargets = new ObjectArrayList<>();
        for (Reference2ObjectMap.Entry<ItemConversionTarget, ObjectArrayList<OOIItemStack>> entry
            : map.reference2ObjectEntrySet()) {
            Item targetItem = entry.getKey().getTarget();
            if (targetItem == null) {
                continue;
            }
            ObjectArrayList<OOIItemStack> stacks = entry.getValue();
            for (int index = 0, size = stacks.size(); index < size; index++) {
                stacks.get(index).ooi$replace(entry.getKey(), targetItem);
            }
            initializedTargets.add(entry.getKey());
        }
        for (int index = 0, size = initializedTargets.size(); index < size; index++) {
            map.remove(initializedTargets.get(index));
        }
    }

    public static void addPreItemStack(ItemConversionTarget target, OOIItemStack stack) {
        if (map == null) {
            return;
        }
        for (ObjectArrayList<OOIItemStack> stacks : map.values()) {
            for (int index = 0, size = stacks.size(); index < size; index++) {
                if (stacks.get(index) == stack) {
                    return;
                }
            }
        }
        map.computeIfAbsent(target, key -> new ObjectArrayList<>()).add(stack);
    }

    public static boolean isModify(String odName) {
        return odToTargetMap.containsKey(odName);
    }

    public static boolean isModify(Multiset<SimpleItem> set) {
        for (SimpleItem item : set) {
            ResourceLocation ii;
            if ((itemIdToTargetMap.containsKey(ii = item.getRegistryName())
                && itemIdToTargetMap.get(ii).containsKey(item.getMeta()))
                || allTarget.contains(item)) {
                return true;
            }
        }
        return false;
    }

    public static void clearRecipe() {
        Map<RecipeSignature, List<IRecipe>> recipes = new Object2ObjectOpenHashMap<>();
        Set<RecipeSignature> recipes0 = new ObjectOpenHashSet<>();
        List<ResourceLocation> cleanRecipes = new ObjectArrayList<>();
        final var a = RegistryManager.ACTIVE.<IRecipe>getRegistry(GameData.RECIPES);

        for (Map.Entry<ResourceLocation, IRecipe> s : a.getEntries()) {
            var recipe = s.getValue();

            if (((OOIItemStack) (Object) recipe.getRecipeOutput()).ooi$isBeReplaced() && recipe.getRecipeOutput().isEmpty()) {
                cleanRecipes.add(recipe.getRegistryName());
                continue;
            }

            if (recipe.isDynamic()) continue;
            if (!isPotentiallyModifiedRecipe(recipe)) continue;

            var rs = new RecipeSignature(recipe);
            if (rs.getOutputSignature().isEmpty() || !rs.isModify()) continue;

            recipes.computeIfAbsent(rs, v -> new ObjectArrayList<>())
                   .add(recipe);
        }

        recipes.forEach((r, recipe) -> {
            if (recipe.size() > 1) {
                for (int index = 0, size = recipe.size(); index < size; index++) {
                    IRecipe iRecipe = recipe.get(index);
                    if (iRecipe != null) {
                        a.remove(iRecipe.getRegistryName());
                    }
                }
                recipes0.add(r);
            }
        });

        recipes.clear();
        cleanRecipes.forEach(a::remove);
        cleanRecipes.clear();
        recipes0.forEach(RecipeSignature::rebuildRecipe);
        recipes0.clear();
    }

    public static synchronized void Clear() {
        itemIdToTargetMap.clear();
        odToTargetMap.clear();
        allTarget.clear();
        finalMODIDBlackSet.clear();
        finalItemBlackMap.clear();
        finalODBlackSet.clear();
    }

    public static synchronized void InitTarget() {
        Clear();
        BlackInit();
        Init();
        var f = FurnaceRecipes.instance();
        f.smeltingList = new Object2ObjectOpenCustomHashMap<>(f.smeltingList, hashItemStack);
        f.experienceList = new Object2FloatOpenCustomHashMap<>(f.experienceList, hashItemStack);
    }

    public static synchronized void registerTarget(ItemConversionTarget target) {
        validateTarget(target);
        OOIConfig.items.add(target);
        Init(target);
    }

    public static synchronized void registerBlackList(BlackMatchItem target) {
        if (target == null) {
            OnlyOneItem.LOGGER.error("[OOI] Invalid item blacklist entry");
            throw new IllegalArgumentException("[OOI] Invalid item blacklist entry");
        }
        if (OOIConfig.blackList.add(target)) {
            BlackInit(target);
        }
    }

    public static synchronized void finalizeTargets() {
        Map<String, ItemConversionTarget> targetsByMatch = new Object2ObjectLinkedOpenHashMap<>();
        Map<String, MatchItem> matchesByKey = new Object2ObjectLinkedOpenHashMap<>();

        for (int index = 0, size = OOIConfig.items.size(); index < size; index++) {
            ItemConversionTarget target = OOIConfig.items.get(index);
            validateTarget(target);
            for (MatchItem matchItem : target.getMatchItems()) {
                if (matchItem == null) {
                    OnlyOneItem.LOGGER.error("[OOI] Null match item in mapping {}", target.getTargetID());
                    throw new IllegalStateException("[OOI] Null match item in mapping " + target.getTargetID());
                }
                String key = matchItem.oreName() != null
                    ? "ore:" + matchItem.oreName()
                    : "item:" + matchItem.id() + ':' + matchItem.meta();
                targetsByMatch.put(key, target);
                matchesByKey.put(key, matchItem);
            }
        }

        Map<String, ItemConversionTarget> finalTargets = new Object2ObjectLinkedOpenHashMap<>();
        for (Map.Entry<String, ItemConversionTarget> entry : targetsByMatch.entrySet()) {
            ItemConversionTarget target = entry.getValue();
            if (target.getTarget() == null) {
                OnlyOneItem.LOGGER.error(
                    "[OOI] Dropping item mapping because target item is not registered: targetID={}, targetMeta={}, match={}",
                    target.getTargetID(), target.getTargetMeta(), entry.getKey());
                continue;
            }
            String key = target.getTargetID() + '#' + target.getTargetMeta();
            ItemConversionTarget finalTarget = finalTargets.get(key);
            if (finalTarget == null) {
                finalTarget = new ItemConversionTarget(target.getTargetID(), target.getTargetMeta())
                    .setMatchItem(new ObjectLinkedOpenHashSet<>());
                finalTargets.put(key, finalTarget);
            }
            finalTarget.getMatchItems().add(matchesByKey.get(entry.getKey()));
        }

        OOIConfig.items.clear();
        OOIConfig.items.addAll(finalTargets.values());
        Clear();
        BlackInit();
        Init();
    }

    public static void addTargetItem(ResourceLocation rl, int meta, ItemConversionTarget t) {
        if (rl == null) return;
        if (rl.toString().equals(t.getTargetID()) && meta == t.getTargetMeta()) return;
        if (allTarget.contains(SimpleItem.getInstance(rl, meta))) return;
        itemIdToTargetMap
            .computeIfAbsent(rl, k -> new Int2ObjectOpenHashMap<>())
            .put(meta, t);
        allTarget.add(SimpleItem.getInstance(t.getTargetID(), t.getTargetMeta()));
    }

    public static void onOreRegister(OreDictionary.OreRegisterEvent event) {
        var od = event.getName();
        var ore = event.getOre();
        if (finalODBlackSet.contains(od)) {
            finalItemBlackMap
                .computeIfAbsent(ore.getItem().getRegistryName(), item -> new IntOpenHashSet())
                .add(ore.getMetadata());
            return;
        }
        var rl = ore.getItem().getRegistryName();
        if (rl != null) {
            if (finalMODIDBlackSet.contains(rl.getNamespace())) {
                finalItemBlackMap
                    .computeIfAbsent(ore.getItem().getRegistryName(), item -> new IntOpenHashSet())
                    .add(ore.getMetadata());
                return;
            }
        }
        if (odToTargetMap.containsKey(od)) {
            addTargetItem(rl, ore.getMetadata(), odToTargetMap.get(od));
        }
    }

    public static void postODProcess() {
        Map<ResourceLocation, IntSet> sourcesBeforeOreScan = new Object2ObjectOpenHashMap<>();
        for (Map.Entry<ResourceLocation, Int2ObjectMap<ItemConversionTarget>> entry : itemIdToTargetMap.entrySet()) {
            sourcesBeforeOreScan.put(entry.getKey(), new IntOpenHashSet(entry.getValue().keySet()));
        }
        for (Map.Entry<String, ItemConversionTarget> entry : odToTargetMap.entrySet()) {
            var od = entry.getKey();
            ItemConversionTarget oreTarget = entry.getValue();
            if (oreTarget == null || oreTarget.getTarget() == null) {
                String targetID = oreTarget == null ? "null" : oreTarget.getTargetID();
                int targetMeta = oreTarget == null ? 0 : oreTarget.getTargetMeta();
                OnlyOneItem.LOGGER.error(
                    "[OOI] Cannot finalize ore dictionary mapping: oreName={}, targetID={}, targetMeta={}",
                    od, targetID, targetMeta);
                throw new IllegalStateException("[OOI] Invalid final ore dictionary target " + targetID + ':' + targetMeta);
            }
            var list = OreDictionary.getOres(od);
            var blackList = new ReferenceArrayList<ItemStack>();
            for (int index = 0, size = list.size(); index < size; index++) {
                ItemStack ore = list.get(index);
                if (finalODBlackSet.contains(od)) {
                    finalItemBlackMap
                        .computeIfAbsent(ore.getItem().getRegistryName(), item -> new IntOpenHashSet())
                        .add(ore.getMetadata());
                    blackList.add(ore);
                    continue;
                }
                var rl = ore.getItem().getRegistryName();
                if (rl != null) {
                    if (finalMODIDBlackSet.contains(rl.getNamespace())) {
                        finalItemBlackMap
                            .computeIfAbsent(rl, item -> new IntOpenHashSet())
                            .add(ore.getMetadata());
                        blackList.add(ore);
                        continue;
                    }
                }
                if (rl != null) {
                    IntSet protectedMetas = sourcesBeforeOreScan.get(rl);
                    if (protectedMetas == null || !protectedMetas.contains(ore.getMetadata())) {
                        addTargetItem(rl, ore.getMetadata(), oreTarget);
                    }
                }
            }
            list.clear();
            list.add(oreTarget.getItemStack());
            list.addAll(blackList);
        }

        rebuildConfigFromRuntimeMaps(
            itemIdToTargetMap,
            odToTargetMap,
            finalItemBlackMap,
            finalODBlackSet,
            finalMODIDBlackSet);
    }

    static void rebuildConfigFromRuntimeMaps(
        Map<ResourceLocation, Int2ObjectMap<ItemConversionTarget>> itemTargets,
        Map<String, ItemConversionTarget> oreTargets,
        Map<ResourceLocation, IntSet> itemBlackList,
        Set<String> oreBlackList,
        Set<String> modBlackList) {
        Map<String, ItemConversionTarget> exportedTargets = new Object2ObjectLinkedOpenHashMap<>();
        for (int index = 0, size = OOIConfig.items.size(); index < size; index++) {
            ItemConversionTarget target = OOIConfig.items.get(index);
            validateTarget(target);
            if (target.getTarget() == null) {
                OnlyOneItem.LOGGER.error(
                    "[OOI] Cannot establish final target order: targetID={}, targetMeta={}",
                    target.getTargetID(), target.getTargetMeta());
                throw new IllegalStateException(
                    "[OOI] Invalid configured item target " + target.getTargetID() + ':' + target.getTargetMeta());
            }
            String targetKey = target.getTargetID() + '#' + target.getTargetMeta();
            if (!exportedTargets.containsKey(targetKey)) {
                exportedTargets.put(targetKey, new ItemConversionTarget(target.getTargetID(), target.getTargetMeta())
                    .setMatchItem(new ObjectLinkedOpenHashSet<>()));
            }
        }

        ObjectArrayList<ResourceLocation> itemIDs = new ObjectArrayList<>(itemTargets.keySet());
        for (int index = 0, size = itemIDs.size(); index < size; index++) {
            if (itemIDs.get(index) == null) {
                OnlyOneItem.LOGGER.error("[OOI] Cannot export item mapping with a null source item ID");
                throw new IllegalStateException("[OOI] Cannot export item mapping with a null source item ID");
            }
        }
        itemIDs.sort(Comparator.comparing(ResourceLocation::toString));
        Map<String, ItemConversionTarget> runtimeTargets = new Object2ObjectOpenHashMap<>();
        for (int idIndex = 0, idSize = itemIDs.size(); idIndex < idSize; idIndex++) {
            ResourceLocation itemID = itemIDs.get(idIndex);
            Int2ObjectMap<ItemConversionTarget> metaTargets = itemTargets.get(itemID);
            IntArrayList metas = new IntArrayList(metaTargets.keySet());
            metas.sort(null);
            for (int metaIndex = 0, metaSize = metas.size(); metaIndex < metaSize; metaIndex++) {
                int meta = metas.getInt(metaIndex);
                ItemConversionTarget target = metaTargets.get(meta);
                if (target == null || target.getTarget() == null) {
                    String targetID = target == null ? "null" : target.getTargetID();
                    int targetMeta = target == null ? 0 : target.getTargetMeta();
                    OnlyOneItem.LOGGER.error(
                        "[OOI] Cannot export item mapping: sourceID={}, sourceMeta={}, targetID={}, targetMeta={}",
                        itemID, meta, targetID, targetMeta);
                    throw new IllegalStateException("[OOI] Invalid final item target " + targetID + ':' + targetMeta);
                }
                String targetKey = target.getTargetID() + '#' + target.getTargetMeta();
                runtimeTargets.put(targetKey, target);
            }
        }

        ObjectArrayList<String> oreNames = new ObjectArrayList<>(oreTargets.keySet());
        oreNames.sort(String::compareTo);
        for (int index = 0, size = oreNames.size(); index < size; index++) {
            String oreName = oreNames.get(index);
            ItemConversionTarget target = oreTargets.get(oreName);
            if (target == null || target.getTarget() == null) {
                String targetID = target == null ? "null" : target.getTargetID();
                int targetMeta = target == null ? 0 : target.getTargetMeta();
                OnlyOneItem.LOGGER.error(
                    "[OOI] Cannot export ore dictionary mapping: oreName={}, targetID={}, targetMeta={}",
                    oreName, targetID, targetMeta);
                throw new IllegalStateException("[OOI] Invalid final ore dictionary target " + targetID + ':' + targetMeta);
            }
            String targetKey = target.getTargetID() + '#' + target.getTargetMeta();
            runtimeTargets.put(targetKey, target);
        }

        ObjectArrayList<ItemConversionTarget> additionalTargets = new ObjectArrayList<>();
        for (Map.Entry<String, ItemConversionTarget> entry : runtimeTargets.entrySet()) {
            if (!exportedTargets.containsKey(entry.getKey())) {
                additionalTargets.add(entry.getValue());
            }
        }
        additionalTargets.sort(
            Comparator.comparing(ItemConversionTarget::getTargetID)
                .thenComparingInt(ItemConversionTarget::getTargetMeta));
        for (int index = 0, size = additionalTargets.size(); index < size; index++) {
            ItemConversionTarget target = additionalTargets.get(index);
            String targetKey = target.getTargetID() + '#' + target.getTargetMeta();
            exportedTargets.put(targetKey, new ItemConversionTarget(target.getTargetID(), target.getTargetMeta())
                .setMatchItem(new ObjectLinkedOpenHashSet<>()));
        }

        for (int idIndex = 0, idSize = itemIDs.size(); idIndex < idSize; idIndex++) {
            ResourceLocation itemID = itemIDs.get(idIndex);
            Int2ObjectMap<ItemConversionTarget> metaTargets = itemTargets.get(itemID);
            IntArrayList metas = new IntArrayList(metaTargets.keySet());
            metas.sort(null);
            for (int metaIndex = 0, metaSize = metas.size(); metaIndex < metaSize; metaIndex++) {
                int meta = metas.getInt(metaIndex);
                ItemConversionTarget target = metaTargets.get(meta);
                exportedTargets.get(target.getTargetID() + '#' + target.getTargetMeta())
                    .getMatchItems()
                    .add(MatchItem.getInstance(itemID.toString(), meta));
            }
        }

        for (int index = 0, size = oreNames.size(); index < size; index++) {
            String oreName = oreNames.get(index);
            ItemConversionTarget target = oreTargets.get(oreName);
            exportedTargets.get(target.getTargetID() + '#' + target.getTargetMeta())
                .getMatchItems()
                .add(MatchItem.getInstance(oreName));
        }

        OOIConfig.items.clear();
        for (ItemConversionTarget target : exportedTargets.values()) {
            if (!target.getMatchItems().isEmpty()) {
                OOIConfig.items.add(target);
            }
        }

        OOIConfig.blackList.clear();
        ObjectArrayList<ResourceLocation> blackItemIDs = new ObjectArrayList<>(itemBlackList.keySet());
        for (int index = 0, size = blackItemIDs.size(); index < size; index++) {
            if (blackItemIDs.get(index) == null) {
                OnlyOneItem.LOGGER.error("[OOI] Cannot export item blacklist with a null item ID");
                throw new IllegalStateException("[OOI] Cannot export item blacklist with a null item ID");
            }
        }
        blackItemIDs.sort(Comparator.comparing(ResourceLocation::toString));
        for (int idIndex = 0, idSize = blackItemIDs.size(); idIndex < idSize; idIndex++) {
            ResourceLocation itemID = blackItemIDs.get(idIndex);
            IntArrayList metas = new IntArrayList(itemBlackList.get(itemID));
            metas.sort(null);
            for (int metaIndex = 0, metaSize = metas.size(); metaIndex < metaSize; metaIndex++) {
                OOIConfig.blackList.add(BlackMatchItem.getInstance(itemID.toString(), metas.getInt(metaIndex)));
            }
        }

        ObjectArrayList<String> blackOreNames = new ObjectArrayList<>(oreBlackList);
        blackOreNames.sort(String::compareTo);
        for (int index = 0, size = blackOreNames.size(); index < size; index++) {
            OOIConfig.blackList.add(BlackMatchItem.getInstance(Type.OreDict, blackOreNames.get(index)));
        }

        ObjectArrayList<String> blackModIDs = new ObjectArrayList<>(modBlackList);
        blackModIDs.sort(String::compareTo);
        for (int index = 0, size = blackModIDs.size(); index < size; index++) {
            OOIConfig.blackList.add(BlackMatchItem.getInstance(Type.ModID, blackModIDs.get(index)));
        }
    }

    public static ItemConversionTarget match(Item item, int meta) {
        if (item == null) return null;
        ResourceLocation rl = item.getRegistryName();
        if (rl == null) return null;
        IntSet s;
        if (((s = finalItemBlackMap.get(rl)) != null && s.contains(meta))
            || finalMODIDBlackSet.contains(rl.getNamespace())) {
            return null;
        }

        return itemIdToTargetMap
            .getOrDefault(rl, defaultMap)
            .get(meta);
    }

    private static boolean isPotentiallyModifiedRecipe(IRecipe recipe) {
        List<Ingredient> ingredients = recipe.getIngredients();
        for (int index = 0, size = ingredients.size(); index < size; index++) {
            Ingredient ingredient = ingredients.get(index);
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                if (isPotentiallyModifiedStack(stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isPotentiallyModifiedStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (match(stack.getItem(), stack.getMetadata()) != null) return true;
        return allTarget.contains(SimpleItem.getNoNBTInstance(stack));
    }

    private static void Init() {
        for (int index = 0, size = OOIConfig.items.size(); index < size; index++) {
            Init(OOIConfig.items.get(index));
        }
    }

    private static void Init(ItemConversionTarget target) {
        validateTarget(target);
        for (MatchItem matchItem : target.getMatchItems()) {
            if (matchItem.oreName() != null) {
                var list = OreDictionary.getOres(matchItem.oreName(), false);
                for (int index = 0, size = list.size(); index < size; index++) {
                    ItemStack stack = list.get(index);
                    Item item = stack.getItem();
                    ResourceLocation rl = item.getRegistryName();
                    int meta = stack.getMetadata();
                    if (rl == null) continue;
                    if (allTarget.contains(SimpleItem.getNoNBTInstance(stack))
                        || (finalItemBlackMap.containsKey(rl) && finalItemBlackMap.get(rl).contains(meta))
                        || finalMODIDBlackSet.contains(rl.getNamespace())) {
                        continue;
                    }
                    addTargetItem(rl, meta, target);
                }
                odToTargetMap.put(matchItem.oreName(), target);
            } else if (matchItem.id() != null) {
                if (!allTarget.contains(SimpleItem.getInstance(matchItem.id(), matchItem.meta()))) {
                    addTargetItem(new ResourceLocation(matchItem.id()), matchItem.meta(), target);
                }
            }
        }
    }

    private static void BlackInit() {
        for (BlackMatchItem matchItem : OOIConfig.blackList) {
            BlackInit(matchItem);
        }
    }

    private static void BlackInit(BlackMatchItem matchItem) {
        switch (matchItem.type()) {
            case Item -> finalItemBlackMap
                .computeIfAbsent(new ResourceLocation(matchItem.name()), k -> new IntOpenHashSet())
                .add(matchItem.meta());
            case ModID -> finalMODIDBlackSet.add(matchItem.name());
            case OreDict -> {
                String od = matchItem.name();
                List<ItemStack> stacks = OreDictionary.getOres(od);
                for (int index = 0, size = stacks.size(); index < size; index++) {
                    ItemStack stack = stacks.get(index);
                    Item item = stack.getItem();
                    ResourceLocation rl = item.getRegistryName();
                    int meta = stack.getMetadata();
                    if (rl != null) {
                        finalItemBlackMap
                            .computeIfAbsent(rl, k -> new IntOpenHashSet())
                            .add(meta);
                    }
                }
                finalODBlackSet.add(od);
            }
        }
    }

    private static void validateTarget(ItemConversionTarget target) {
        if (target == null || target.getMatchItems() == null || target.getMatchItems().isEmpty()) {
            OnlyOneItem.LOGGER.error("[OOI] Invalid item mapping");
            throw new IllegalStateException("[OOI] Invalid item mapping");
        }
    }
}
