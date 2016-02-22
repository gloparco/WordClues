package com.kindredgames.wordclues.amazon.purchase;

import com.kindredgames.wordclues.util.KGLog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Used to serialize SKUData into JSON and back again.
 */
public class SKUDataJSON {

    private static final String SKU = "sku";
    private static final String FULFILLED_QTY = "fulfilledQty";
    private static final String CONSUMED_QTY = "consumedQty";

    /**
     * Serializes SKUData into JSON string.
     */
    public static String toJSON(SKUData data) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(SKU, data.getSKU());
            obj.put(FULFILLED_QTY, data.getFulfilledQuantity());
            obj.put(CONSUMED_QTY, data.getConsumedQuantity());
        } catch (JSONException e) {
            KGLog.e("toJSON: ERROR serializing: " + data);
        }

        // KGLog.i("toJSON: "+obj.toString());
        return obj.toString();
    }

    /**
     * Deserializes JSON string back into SKUData object.
     */
    public static SKUData fromJSON(String json) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(json);
            String sku = obj.getString(SKU);
            int fulfilledQuantity = obj.getInt(FULFILLED_QTY);
            int consumedQuantity = obj.getInt(CONSUMED_QTY);
            SKUData result = new SKUData(sku);
            result.setFulfilledQuantity(fulfilledQuantity);
            result.setConsumedQuantity(consumedQuantity);
            // KGLog.i("fromJSON: " + result);
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
