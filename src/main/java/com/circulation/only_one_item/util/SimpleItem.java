package com.circulation.only_one_item.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.Objects;

public final class SimpleItem {
    private static final Map<ResourceLocation, Int2ObjectMap<SimpleItem>> chace = new Object2ObjectOpenHashMap<>();
    public static final SimpleItem EMPTY = SimpleItem.getInstance(ItemStack.EMPTY);
    @NonNull
    private final ResourceLocation item;
    @Getter
    private final int meta;
    private final int hashCode;

    private SimpleItem(@NonNull ResourceLocation item, int meta) {
        this.item = item;
        this.meta = meta;
        this.hashCode = Objects.hash(item, meta);
    }

    public static SimpleItem getInstance(@NonNull String item, int meta) {
        return getInstance(new ResourceLocation(item), meta);
    }

    public static SimpleItem getInstance(ResourceLocation item, int meta) {
        var map = chace.computeIfAbsent(item, rl -> new Int2ObjectOpenHashMap<>());
        if (map.containsKey(meta)) {
            return map.get(meta);
        }
        var value = new SimpleItem(item, meta);
        map.put(meta, value);
        return value;
    }

    public static SimpleItem getInstance(@NonNull ItemStack stack) {
        return getInstance(stack.getItem().getRegistryName(), stack.getMetadata());
    }

    public boolean isEmpty() {
        return this == EMPTY || Objects.equals(this.item, ItemStack.EMPTY.getItem().getRegistryName());
    }

    public Item getItem() {
        return Item.getByNameOrId(item.toString());
    }

    public String getItemID() {
        return item.toString();
    }

    public ResourceLocation getItemRL() {
        return item;
    }

    public ItemStack getItemStack(int amount) {
        var stack = getItem();
        if (stack != null && stack != Items.AIR) {
            return new ItemStack(stack, amount, meta);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SimpleItem that = (SimpleItem) o;
        return meta == that.meta && Objects.equals(item, that.item);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return item + ":" + meta;
    }
}