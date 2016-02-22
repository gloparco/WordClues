package com.kindredgames.wordclues;

import java.util.List;

public interface StoreManager {

    public void init();
    boolean isEnabled();
    void buy(String productId);
//    String owns(String productId);
    void prices(List<String> productNames);
    void restorePurchases();
    String getStoreUrl();
}
