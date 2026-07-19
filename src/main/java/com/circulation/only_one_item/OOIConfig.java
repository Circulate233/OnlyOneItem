package com.circulation.only_one_item;

import com.circulation.only_one_item.conversion.FluidConversionTarget;
import com.circulation.only_one_item.conversion.ItemConversionTarget;
import com.circulation.only_one_item.emun.Type;
import com.circulation.only_one_item.util.BlackMatchItem;
import com.circulation.only_one_item.util.MatchItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class OOIConfig {

    public static final List<ItemConversionTarget> items = new ObjectArrayList<>();
    public static final List<FluidConversionTarget> fluids = new ObjectArrayList<>();
    public static final Set<BlackMatchItem> blackList = new ObjectOpenHashSet<>();

    private static Path configPath;

    public static void readConfig() {
        readConfig(Loader.instance().getConfigDir().toPath().resolve("ooi"));
    }

    public static List<ItemConversionTarget> readConfig(Path directory) {
        if (directory == null) {
            throw new IllegalArgumentException("[OOI] Config directory must not be null");
        }

        configPath = directory;
        items.clear();
        fluids.clear();
        blackList.clear();
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            OnlyOneItem.LOGGER.error("[OOI] Failed to create config directory {}", directory, e);
            throw new IllegalStateException("[OOI] Failed to create config directory " + directory, e);
        }

        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        readItems(gson, directory.resolve("ooi_item.json"));
        readFluids(gson, directory.resolve("ooi_fluid.json"));
        readBlackList(gson, directory.resolve("ooi_item_black_list.json"));
        return Collections.unmodifiableList(new ObjectArrayList<>(items));
    }

    public static void writeConfig() {
        if (configPath == null) {
            throw new IllegalStateException("[OOI] Configuration was not loaded before write");
        }

        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        write(configPath.resolve("ooi_item.json"), gson.toJson(items), "item mappings");
        write(configPath.resolve("ooi_fluid.json"), gson.toJson(fluids), "fluid mappings");
        write(configPath.resolve("ooi_item_black_list.json"), gson.toJson(blackList), "item blacklist");
    }

    private static void readItems(Gson gson, Path path) {
        if (!Files.exists(path)) {
            try (InputStream input = OOIConfig.class.getClassLoader().getResourceAsStream("ooi_item.json")) {
                if (input == null) {
                    throw new IllegalStateException("[OOI] Missing bundled ooi_item.json");
                }
                Files.copy(input, path);
            } catch (IOException e) {
                OnlyOneItem.LOGGER.error("[OOI] Failed to create default item config {}", path, e);
                throw new IllegalStateException("[OOI] Failed to create default item config " + path, e);
            }
        }

        try {
            List<ItemConversionTarget> targets = gson.fromJson(
                new String(Files.readAllBytes(path), StandardCharsets.UTF_8),
                new TypeToken<List<ItemConversionTarget>>() { }.getType());
            if (targets == null) {
                throw new IllegalArgumentException("item mappings must be a JSON array");
            }
            for (int index = 0, size = targets.size(); index < size; index++) {
                ItemConversionTarget target = targets.get(index);
                validateItemTarget(target);
                items.add(new ItemConversionTarget(target.getTargetID(), target.getTargetMeta())
                    .setMatchItem(new LinkedHashSet<>(target.getMatchItems())));
            }
        } catch (Exception e) {
            OnlyOneItem.LOGGER.error("[OOI] The config/ooi/ooi_item.json file is incorrect", e);
            throw new IllegalStateException("[OOI] The config/ooi/ooi_item.json file is incorrect", e);
        }
    }

    private static void readFluids(Gson gson, Path path) {
        if (!Files.exists(path)) {
            fluids.add(new FluidConversionTarget(FluidRegistry.WATER.getName()).addMatchFluid(FluidRegistry.WATER.getName()));
            return;
        }

        try {
            List<FluidConversionTarget> targets = gson.fromJson(
                new String(Files.readAllBytes(path), StandardCharsets.UTF_8),
                new TypeToken<List<FluidConversionTarget>>() { }.getType());
            if (targets == null) {
                throw new IllegalArgumentException("fluid mappings must be a JSON array");
            }
            for (int index = 0, size = targets.size(); index < size; index++) {
                FluidConversionTarget target = targets.get(index);
                validateFluidTarget(target);
                fluids.add(new FluidConversionTarget(target.getTargetID())
                    .setMatchFluids(new LinkedHashSet<>(target.getMatchFluids())));
            }
        } catch (Exception e) {
            OnlyOneItem.LOGGER.error("[OOI] The config/ooi/ooi_fluid.json file is incorrect", e);
            throw new IllegalStateException("[OOI] The config/ooi/ooi_fluid.json file is incorrect", e);
        }
    }

    private static void readBlackList(Gson gson, Path path) {
        if (!Files.exists(path)) {
            blackList.add(BlackMatchItem.getInstance("minecraft:gold_ingot", 0));
            blackList.add(BlackMatchItem.getInstance(Type.OreDict, "ingotGold"));
            blackList.add(BlackMatchItem.getInstance(Type.ModID, "minecraft"));
            return;
        }

        try {
            List<BlackMatchItem> targets = gson.fromJson(
                new String(Files.readAllBytes(path), StandardCharsets.UTF_8),
                new TypeToken<List<BlackMatchItem>>() { }.getType());
            if (targets == null) {
                throw new IllegalArgumentException("item blacklist must be a JSON array");
            }
            for (int index = 0, size = targets.size(); index < size; index++) {
                BlackMatchItem target = targets.get(index);
                validateBlackListTarget(target);
                blackList.add(target);
            }
        } catch (Exception e) {
            OnlyOneItem.LOGGER.error("[OOI] The config/ooi/ooi_item_black_list.json file is incorrect", e);
            throw new IllegalStateException("[OOI] The config/ooi/ooi_item_black_list.json file is incorrect", e);
        }
    }

    private static void validateItemTarget(ItemConversionTarget target) {
        if (target == null || target.getMatchItems() == null || target.getMatchItems().isEmpty()) {
            throw new IllegalArgumentException("invalid item mapping");
        }
        resourceLocation(target.getTargetID(), "item targetID");
        for (MatchItem match : target.getMatchItems()) {
            if (match == null || (isNotBlank(match.oreName()) == isNotBlank(match.id()))) {
                throw new IllegalArgumentException("invalid item match in " + target.getTargetID());
            }
            if (isNotBlank(match.id())) {
                resourceLocation(match.id(), "item match id");
            }
        }
    }

    private static void validateFluidTarget(FluidConversionTarget target) {
        if (target == null || !isNotBlank(target.getTargetID())
            || target.getMatchFluids() == null || target.getMatchFluids().isEmpty()) {
            throw new IllegalArgumentException("invalid fluid mapping");
        }
        for (String match : target.getMatchFluids()) {
            if (!isNotBlank(match)) {
                throw new IllegalArgumentException("blank fluid match in " + target.getTargetID());
            }
        }
    }

    private static void validateBlackListTarget(BlackMatchItem target) {
        if (target == null || target.type() == null || !isNotBlank(target.name())) {
            throw new IllegalArgumentException("invalid item blacklist entry");
        }
        if (target.type() == Type.Item) {
            resourceLocation(target.name(), "blacklist item id");
        }
    }

    private static void resourceLocation(String id, String field) {
        if (!isNotBlank(id)) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        try {
            new ResourceLocation(id);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(field + " is not a valid resource location: " + id, e);
        }
    }

    private static void write(Path path, String contents, String name) {
        try {
            Files.write(path, contents.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            OnlyOneItem.LOGGER.error("[OOI] Failed to write {} to {}", name, path, e);
            throw new IllegalStateException("[OOI] Failed to write " + name + " to " + path, e);
        }
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
