package com.kindredgames.wordclues.amazon.purchase;

import com.kindredgames.wordclues.util.KGLog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Used to serialize {@link PurchaseData} into JSON and back again.
 */
public class PurchaseDataJSON {

    private static final String REQUEST_ID = "requestId";
    private static final String REQUEST_STATE = "requestState";
    private static final String PURCHASE_TOKEN = "purchaseToken";
    private static final String SKU = "sku";
    private static final String PURCHASE_TOKEN_FULFILLED = "purchaseTokenFulfilled";

    /**
     * Serializes PurchaseData objects into JSON string.
     */
    public static String toJSON(PurchaseData data) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(REQUEST_ID, data.getRequestId());
            obj.put(REQUEST_STATE, data.getRequestStateAsInt());
            if (data.getPurchaseToken() != null)
                obj.put(PURCHASE_TOKEN, data.getPurchaseToken());
            if (data.getSKU() != null)
                obj.put(SKU, data.getSKU());
            if (data.isPurchaseTokenFulfilled())
                obj.put(PURCHASE_TOKEN_FULFILLED, true);
        } catch (JSONException e) {
            KGLog.e("toJSON: ERROR serializing: " + data);
        }

        return obj.toString();
    }

    /**
     * Deserializes JSON string back into PurchaseData
     * object.
     */
    public static PurchaseData fromJSON(String json) {
        if (json == null)
            return null;
        JSONObject obj = null;
        try {
            obj = new JSONObject(json);
            String requestId = obj.getString(REQUEST_ID);
            int requestState = obj.getInt(REQUEST_STATE);
            String purchaseToken = obj.optString(PURCHASE_TOKEN);
            String sku = obj.optString(SKU);
            boolean purchaseTokenFulfilled = obj.optBoolean(PURCHASE_TOKEN_FULFILLED);

            PurchaseData result = new PurchaseData(requestId);
            result.setRequestState(RequestState.valueOf(requestState));
            result.setPurchaseToken(purchaseToken);
            result.setSKU(sku);
            if (purchaseTokenFulfilled) {
                result.setPurchaseTokenFulfilled();
            }
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
