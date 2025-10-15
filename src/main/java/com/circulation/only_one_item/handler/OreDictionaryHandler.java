package com.circulation.only_one_item.handler;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

public class OreDictionaryHandler {

    @SubscribeEvent
    public void onOreRegister(OreDictionary.OreRegisterEvent event) {
        MatchItemHandler.onOreRegister(event);
    }
}
