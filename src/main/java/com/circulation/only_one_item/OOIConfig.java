package com.circulation.only_one_item;

import com.circulation.only_one_item.conversion.FluidConversionTarget;
import com.circulation.only_one_item.conversion.ItemConversionTarget;
import com.circulation.only_one_item.emun.Type;
import com.circulation.only_one_item.handler.MatchFluidHandler;
import com.circulation.only_one_item.handler.MatchItemHandler;
import com.circulation.only_one_item.util.BlackMatchItem;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
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

    public static void readConfig() throws IOException {
        var configPath = Loader.instance().getConfigDir().toPath().resolve("ooi");
        var ooiPath = configPath.resolve("ooi_item.json");
        var blackPath = configPath.resolve("ooi_item_black_list.json");
        var ooiFluidPath = configPath.resolve("ooi_fluid.json");

        Files.createDirectories(configPath);

        var config = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

        if (Files.exists(ooiPath)) {
            try {
                items.addAll(config.fromJson(new String(Files.readAllBytes(ooiPath), StandardCharsets.UTF_8), (new TypeToken<Set<ItemConversionTarget>>() {
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
            }
        }

        if (Files.exists(ooiFluidPath)) {
            try {
                fluids.addAll(config.fromJson(new String(Files.readAllBytes(ooiFluidPath), StandardCharsets.UTF_8), (new TypeToken<Set<FluidConversionTarget>>() {
                }).getType()));
            } catch (Exception ignored) {
                OnlyOneItem.LOGGER.error("[OOI]The config/ooi/ooi_fluid.json file is incorrect!");
            }
        } else {
            fluids.add(new FluidConversionTarget(FluidRegistry.WATER.getName()).addMatchFluid(FluidRegistry.WATER.getName()));
            Files.write(ooiFluidPath, config.toJson(fluids).getBytes(StandardCharsets.UTF_8));
        }

        if (Files.exists(blackPath)) {
            try {
                blackList.addAll(config.fromJson(new String(Files.readAllBytes(blackPath), StandardCharsets.UTF_8), (new TypeToken<Set<BlackMatchItem>>() {
                }).getType()));
            } catch (Exception ignored) {
                OnlyOneItem.LOGGER.error("[OOI]The config/ooi/ooi_item_black_list.json file is incorrect!");
            }
        } else {
            blackList.add(BlackMatchItem.getInstance("minecraft:gold_ingot", 0));
            blackList.add(BlackMatchItem.getInstance(Type.OreDict, "ingotGold"));
            blackList.add(BlackMatchItem.getInstance(Type.ModID, "minecraft"));
            Files.write(blackPath, config.toJson(blackList).getBytes(StandardCharsets.UTF_8));
        }
        MatchItemHandler.InitTarget();
        MatchFluidHandler.Init(OOIConfig.fluids);
    }
}