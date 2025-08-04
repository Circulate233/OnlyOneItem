package com.circulation.only_one_item.util;

import com.circulation.only_one_item.emun.Type;
import com.github.bsideup.jabel.Desugar;
import net.minecraft.item.ItemStack;

import java.util.Objects;

@Desugar
public record BlackMatchItem(Type type,String name,int meta) {

    public static BlackMatchItem getInstance(ItemStack stack){
        var id = stack.getItem().getRegistryName();
        return new BlackMatchItem(Type.Item,id == null ? null : id.toString(),stack.getMetadata());
    }

    public static BlackMatchItem getInstance(String id,int meta){
        return new BlackMatchItem(Type.Item,id,meta);
    }

    public static BlackMatchItem getInstance(Type type,String odOrmMdID){
        return new BlackMatchItem(type,odOrmMdID,-1);
    }

    public static BlackMatchItem getInstance(MatchItem mi){
        if (mi.oreName() != null){
            return new BlackMatchItem(Type.OreDict, mi.oreName(), -1);
        } else {
            return new BlackMatchItem(Type.Item, mi.id(), mi.meta());
        }
    }

    public static BlackMatchItem getModIDInstance(MatchItem mi){
        return new BlackMatchItem(Type.ModID,mi.id().split(":")[0], -1);
    }

    public static BlackMatchItem getModIDInstance(ItemStack stack){
        var id = stack.getItem().getRegistryName();
        return new BlackMatchItem(Type.ModID,id == null ? "" : id.getNamespace(), -1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)return true;
        if (!(o instanceof BlackMatchItem that)) return false;
        if (this.type != that.type)return false;
        return switch (this.type){
            case OreDict,ModID -> Objects.equals(name, that.name);
            case Item -> Objects.equals(name, that.name) && meta == that.meta;
        };
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, meta);
    }
}
