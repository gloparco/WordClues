package com.kindredgames.wordclues.google;

import android.app.Activity;

import com.kindredgames.wordclues.Cashier;
import com.kindredgames.wordclues.StoreManager;

import java.util.List;

public class GoogleStoreManager implements StoreManager {

    private Activity activity;
    private Cashier cashier;

    public GoogleStoreManager(Activity activity, Cashier cashier) {
        this.activity = activity;
        this.cashier = cashier;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getStoreUrl() {
        return "";
    }

    public void init() {
    }

    @Override
    public void buy(String productId) {

    }

    @Override
    public void prices(List<String> productNames) {

    }

    @Override
    public void restorePurchases() {

    }

}
