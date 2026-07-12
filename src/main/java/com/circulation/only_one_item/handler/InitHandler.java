package com.circulation.only_one_item.handler;

public class InitHandler {

    public static void allPreInit() {
        MatchFluidHandler.lock();
        MatchItemHandler.preItemStackInit();
        MatchFluidHandler.preFluidStackInit();
    }

}
