package com.circulation.only_one_item.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@ToString
public final class SimpleItem {

    private final ResourceLocation item;
    @Getter
    private final int meta;
    public static final SimpleItem empty = new SimpleItem(ItemStack.EMPTY);
    private final int hashCode;
    private static final NBTTagCompound NullNbt = new NBTTagCompound() {
        @Override
        public boolean equals(Object nbt) {
            return nbt == this;
        }

        @Override
        public int hashCode() {
            return Integer.MIN_VALUE;
        }
    };
    private static final Map<ResourceLocation, Int2ObjectOpenHashMapS> chane = new ConcurrentHashMap<>();
    private static final Function<ResourceLocation, Int2ObjectOpenHashMapS> intMap = item -> new Int2ObjectOpenHashMapS();
    private final NBTTagCompound nbt;
    private SimpleItem(ResourceLocation item, int meta, NBTTagCompound nbt) {
        this.item = item;
        this.meta = meta;
        this.nbt = nbt;
        this.hashCode = Objects.hash(item, meta, nbt);
    }

    private SimpleItem(ItemStack stack) {
        this(stack.getItem().getRegistryName(), stack.getItemDamage(), stack.getTagCompound());
    }

    public static SimpleItem getInstance(final String rl, final int meta) {
        return getInstance(new ResourceLocation(rl), meta);
    }

    public static SimpleItem getInstance(final ResourceLocation rl, final int meta) {
        return chane.computeIfAbsent(rl, intMap)
                    .computeIfAbsent(meta)
                    .computeIfAbsent(NullNbt, n -> new SimpleItem(rl, meta, null));
    }

    public static SimpleItem getInstance(final ItemStack stack) {
        if (stack.isEmpty()) return empty;
        var nbt = stack.getTagCompound();
        return chane.computeIfAbsent(stack.getItem().getRegistryName(), intMap)
                    .computeIfAbsent(stack.getItemDamage())
                    .computeIfAbsent(nbt == null ? NullNbt : nbt, n -> new SimpleItem(stack));
    }

    public static SimpleItem getNoNBTInstance(final ItemStack stack) {
        if (stack.isEmpty()) return empty;
        return chane.computeIfAbsent(stack.getItem().getRegistryName(), intMap)
                    .computeIfAbsent(stack.getItemDamage())
                    .computeIfAbsent(NullNbt, n -> new SimpleItem(stack));
    }

    public Item getItem() {
        return Item.REGISTRY.getObject(item);
    }

    public ResourceLocation getRegistryName() {
        return item;
    }

    public String getItemID() {
        return item.toString();
    }

    public ItemStack getItemStack(int amount) {
        var i = new ItemStack(getItem(), amount, meta);
        if (nbt != null && !nbt.isEmpty()) {
            i.setTagCompound(nbt);
        }
        return i;
    }

    public boolean isEmpty() {
        return this == empty;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SimpleItem that = (SimpleItem) o;
        return meta == that.meta && Objects.equals(item, that.item) && Objects.equals(nbt, that.nbt);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private static class Int2ObjectOpenHashMapS extends Int2ObjectOpenHashMap<Map<NBTTagCompound, SimpleItem>> {

        public Map<NBTTagCompound, SimpleItem> computeIfAbsent(int key) {
            Map<NBTTagCompound, SimpleItem> v;

            if ((v = get(key)) == null) {
                synchronized (this) {
                    if ((v = get(key)) == null) {
                        v = new ConcurrentHashMap<>();
                        put(key, v);
                    }
                }
            }

            return v;
        }

    }
}