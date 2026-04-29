package com.circulation.only_one_item;

import com.circulation.only_one_item.conversion.FluidConversionTarget;
import com.circulation.only_one_item.conversion.ItemConversionTarget;
import com.circulation.only_one_item.emun.Type;
import com.circulation.only_one_item.handler.MatchFluidHandler;
import com.circulation.only_one_item.handler.MatchItemHandler;
import com.circulation.only_one_item.util.BlackMatchItem;
import com.circulation.only_one_item.util.MatchItem;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

public class OOIConfig {

    public static final List<ItemConversionTarget> items = new ObjectArrayList<>();
    public static final List<FluidConversionTarget> fluids = new ObjectArrayList<>();
    public static final Set<BlackMatchItem> blackList = new ObjectOpenHashSet<>();

    public static void readConfig() {
        var configPath = Loader.instance().getConfigDir().toPath().resolve("ooi");
        var ooiPath = configPath.resolve("ooi_item.json");
        var blackPath = configPath.resolve("ooi_item_black_list.json");
        var ooiFluidPath = configPath.resolve("ooi_fluid.json");

        try {
            Files.createDirectories(configPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var config = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

        if (Files.exists(ooiPath)) {
            try {
                addItemTargets(config.fromJson(new String(Files.readAllBytes(ooiPath), StandardCharsets.UTF_8), (new TypeToken<Set<ItemConversionTarget>>() {
                }).getType()));
            } catch (Exception ignored) {
                OnlyOneItem.LOGGER.error("[OOI]The config/ooi/ooi_item.json file is incorrect!");
            }
        } else {
            ClassLoader classLoader = OOIConfig.class.getClassLoader();

            try (InputStream inputStream = classLoader.getResourceAsStream("ooi_item.json")) {
                if (inputStream != null) {
                    Files.copy(inputStream, ooiPath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (Files.exists(ooiFluidPath)) {
            try {
                addFluidTargets(config.fromJson(new String(Files.readAllBytes(ooiFluidPath), StandardCharsets.UTF_8), (new TypeToken<Set<FluidConversionTarget>>() {
                }).getType()));
            } catch (Exception ignored) {
                OnlyOneItem.LOGGER.error("[OOI]The config/ooi/ooi_fluid.json file is incorrect!");
            }
        } else {
            fluids.add(new FluidConversionTarget(FluidRegistry.WATER.getName()).addMatchFluid(FluidRegistry.WATER.getName()));
            try {
                Files.write(ooiFluidPath, config.toJson(fluids).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (Files.exists(blackPath)) {
            try {
                addBlackList(config.fromJson(new String(Files.readAllBytes(blackPath), StandardCharsets.UTF_8), (new TypeToken<Set<BlackMatchItem>>() {
                }).getType()));
            } catch (Exception ignored) {
                OnlyOneItem.LOGGER.error("[OOI]The config/ooi/ooi_item_black_list.json file is incorrect!");
            }
        } else {
            blackList.add(BlackMatchItem.getInstance("minecraft:gold_ingot", 0));
            blackList.add(BlackMatchItem.getInstance(Type.OreDict, "ingotGold"));
            blackList.add(BlackMatchItem.getInstance(Type.ModID, "minecraft"));
            try {
                Files.write(blackPath, config.toJson(blackList).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        MatchItemHandler.InitTarget();
        MatchFluidHandler.Init(OOIConfig.fluids);
    }

    private static void addItemTargets(Set<ItemConversionTarget> targets) {
        if (targets == null) return;
        for (ItemConversionTarget target : targets) {
            if (target == null || !isRegisteredItem(target.getTargetID()) || target.getMatchItems() == null) {
                continue;
            }
            Set<MatchItem> validMatches = new ObjectOpenHashSet<>();
            for (MatchItem matchItem : target.getMatchItems()) {
                if (isValidMatchItem(matchItem)) {
                    validMatches.add(matchItem);
                }
            }
            if (!validMatches.isEmpty()) {
                items.add(target.setMatchItem(validMatches));
            }
        }
    }

    private static void addFluidTargets(Set<FluidConversionTarget> targets) {
        if (targets == null) return;
        for (FluidConversionTarget target : targets) {
            if (target == null || target.getTarget() == null || target.getMatchFluids() == null) {
                continue;
            }
            Set<String> validMatches = new ObjectOpenHashSet<>();
            for (String fluid : target.getMatchFluids()) {
                if (isNotBlank(fluid)) {
                    validMatches.add(fluid);
                }
            }
            if (!validMatches.isEmpty()) {
                fluids.add(target.setMatchFluids(validMatches));
            }
        }
    }

    private static void addBlackList(Set<BlackMatchItem> targets) {
        if (targets == null) return;
        for (BlackMatchItem target : targets) {
            if (isValidBlackList(target)) {
                blackList.add(target);
            }
        }
    }

    private static boolean isValidMatchItem(MatchItem matchItem) {
        if (matchItem == null) return false;
        if (isNotBlank(matchItem.oreName())) return true;
        return isValidResourceLocation(matchItem.id());
    }

    private static boolean isValidBlackList(BlackMatchItem target) {
        if (target == null || target.type() == null || !isNotBlank(target.name())) return false;
        return switch (target.type()) {
            case Item -> isValidResourceLocation(target.name());
            case OreDict, ModID -> true;
        };
    }

    private static boolean isRegisteredItem(String id) {
        if (!isValidResourceLocation(id)) return false;
        return Item.getByNameOrId(id) != null;
    }

    private static boolean isValidResourceLocation(String id) {
        if (!isNotBlank(id)) return false;
        try {
            new ResourceLocation(id);
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
