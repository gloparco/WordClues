package com.kindredgames.wordclues;

import org.json.JSONObject;

public interface Cashier {

    void acceptPayment(String productId, String receipt);

    JSONObject getStoredReceipt(String productId);
}
