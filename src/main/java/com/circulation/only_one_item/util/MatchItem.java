package com.circulation.only_one_item.util;

import com.github.bsideup.jabel.Desugar;
import net.minecraft.item.ItemStack;

import java.util.Objects;

@Desugar
public record MatchItem(String oreName, int meta, String id) {

    public MatchItem(String oreName, int meta, String id){
        this.oreName = oreName;
        this.meta = meta;
        if (oreName == null && id == null){
            this.id = "";
        } else {
            this.id = id;
        }
    }

    public static MatchItem[] getInstance(ItemStack[] stacks) {
        var matchItems = new MatchItem[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            matchItems[i] = getInstance(stacks[i]);
        }
        return matchItems;
    }

    public static MatchItem getInstance(String id,int meta) {
        return new MatchItem(null,meta,id);
    }

    public static MatchItem getInstance(String oreName){
        return new MatchItem(oreName,0,null);
    }

    public static MatchItem getInstance(ItemStack stack) {
        var id = stack.getItem().getRegistryName();
        return new MatchItem(null,stack.getMetadata(),id == null ? null : id.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)return true;
        if (o instanceof MatchItem matchItem) {
            if (oreName != null) {
                return oreName.equals(matchItem.oreName);
            } else if (id != null) {
                return id.equals(matchItem.id) && (meta == matchItem.meta || meta == 32767);
            }
        }
        return false;
    }

    @Override
    public int hashCode(){
        return Objects.hash(oreName,meta,id);
    }

}
