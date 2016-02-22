package com.kindredgames.wordclues.amazon.purchase;

import android.app.Activity;
import android.content.SharedPreferences;

import com.amazon.inapp.purchasing.Offset;
import com.amazon.inapp.purchasing.PurchaseResponse;
import com.amazon.inapp.purchasing.Receipt;
import com.kindredgames.wordclues.amazon.AmazonWordCluesSkus;
import com.kindredgames.wordclues.util.KGLog;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Used to store current user, purchase request state and purchase receipts
 * related information into SharedPreferences specific to each user.
 */
public class PurchaseDataStorage {

    private Activity activity;

    private String currentUser;

    private SharedPreferences savedUserRequestsAndPurchaseReceipts;

    public PurchaseDataStorage(Activity activity) {
        this.activity = activity;
    }

    /**
     * Save current user and switch to SharedPreferences specific to the
     * user.
     *
     * @return whether user changed from previously stored user
     */
    public boolean saveCurrentUser(String userId) {
        boolean userChanged = this.currentUser == null ? true : !this.currentUser.equals(userId);

        this.currentUser = userId;
        KGLog.d("saveCurrentUser: %s", userId);

        resetSavedUserRequestsAndPurchaseReceipts();
        return userChanged;
    }

    /**
     * Checks whether userId passed in is same as the currently stored user.
     */
    public boolean isSameAsCurrentUser(String userId) {
        return this.currentUser.equals(userId);
    }

    /**
     * Adds requestId to a list of saved request IDs
     */
    private void addRequestId(String requestId) {
        Set<String> requestIds = getStringSet("REQUEST_IDS");
        requestIds.add(requestId);
        putStringSet("REQUEST_IDS", requestIds);
    }

    /**
     * Removes requestId from the list of saved request IDs
     */
    private void removeRequestId(String requestId) {
        Set<String> requestIds = getStringSet("REQUEST_IDS");
        requestIds.remove(requestId);
        putStringSet("REQUEST_IDS", requestIds);
    }

    /**
     * Get all saved request IDs
     */
    public Set<String> getAllRequestIds() {
        return getStringSet("REQUEST_IDS");
    }

    /**
     * Save userrId and receipt information from PurchaseResponse into
     * SharedPreferences, mark requestId state as having received the
     * purchase response and increment sku fulfill count.
     */
    public PurchaseData savePurchaseResponse(PurchaseResponse response) {
        String requestId = response.getRequestId();
        String userId = response.getUserId();
        Receipt receipt = response.getReceipt();

        // RequestId should match a previously sent requestId
        if (!doesRequestIdMatchSentRequestId(requestId)) {
            KGLog.i("savePurchaseReceipt: requestId (%s) does NOT match any requestId sent before!", requestId);
            return null;
        }

        String purchaseToken = receipt.getPurchaseToken();
        String sku = receipt.getSku();

        PurchaseData purchaseData = getPurchaseData(requestId);
        purchaseData.setUserId(userId);
        purchaseData.setRequestState(RequestState.RECEIVED);
        purchaseData.setPurchaseToken(purchaseToken);
        purchaseData.setSKU(sku);

        KGLog.d("savePurchaseResponse: saving purchaseToken (%s) sku (%s) and request state as (%s)",
                purchaseToken, sku, purchaseData.getRequestState());
        savePurchaseData(purchaseData);

        skuFulfilledQuantityUp(sku);
        return purchaseData;
    }

    /**
     * Checks if requestId matches requestId we sent out previously
     */
    private boolean doesRequestIdMatchSentRequestId(String requestId) {
        PurchaseData purchaseData = getPurchaseData(requestId);
        return purchaseData != null;
    }

    /**
     * Increment SKU fulfilled count and save. Use this fulfilled count to
     * decide whether to fulfill entitlement.
     */
    public void skuFulfilledQuantityUp(String sku) {
        AmazonWordCluesSkus amazonWordCluesSku = AmazonWordCluesSkus.valueForId(sku);
        SKUData skuData = getOrCreateSKUData(sku);
        skuData.addFulfilledQuantity(amazonWordCluesSku.getQuantity());

        KGLog.i("skuFulfilledQuantityUp: fulfilledQuantity increased to (%s) for sku (%s), save SKU data",
                skuData.getFulfilledQuantity(), sku);
        saveSKUData(skuData);
    }

    /**
     * Mark requestId state as fulfilled
     */
    public void setRequestStateFulfilled(String requestId) {
        PurchaseData purchaseData = getPurchaseData(requestId);
        purchaseData.setRequestState(RequestState.FULFILLED);
        savePurchaseData(purchaseData);
        KGLog.i("setRequestStateFulfilled: requestId (%s) setting requestState to (%s)",
                requestId, purchaseData.getRequestState());
    }

    /**
     * Check if requestId state is SENT
     */
    public boolean isRequestStateSent(String requestId) {
        PurchaseData purchaseData = getPurchaseData(requestId);
        return purchaseData != null && RequestState.SENT == purchaseData.getRequestState();
    }

    /**
     * Mark purchase token as fulfilled
     */
    public void setPurchaseTokenFulfilled(String purchaseToken) {
        PurchaseData purchaseData = getPurchaseDataByPurchaseToken(purchaseToken);
        purchaseData.setPurchaseTokenFulfilled();
        KGLog.i("setPurchaseTokenFulfilled: set purchaseToken (%s) as fulfilled", purchaseToken);
        savePurchaseData(purchaseData);
    }

    /**
     * Checks if purchase for purchase token has been fulfilled
     */
    public boolean isPurchaseTokenFulfilled(String purchaseToken) {
        PurchaseData purchaseData = getPurchaseDataByPurchaseToken(purchaseToken);
        return purchaseData != null && purchaseData.isPurchaseTokenFulfilled();
    }

    /**
     * Adds requestId to list of saved requestIds, creates new
     * {@link PurchaseData} for requestId and sets request
     * state to SENT
     */
    public PurchaseData newPurchaseData(String requestId) {
        addRequestId(requestId);
        PurchaseData purchaseData = new PurchaseData(requestId);
        purchaseData.setRequestState(RequestState.SENT);
        savePurchaseData(purchaseData);
        KGLog.d("newPurchaseData: adding requestId (%s) to saved list and setting request state to (%s)",
                requestId, purchaseData.getRequestState());
        return purchaseData;
    }

    /**
     * Saves PurchaseResponse data into SharedPreferences keyed off of both
     * requestId and purchaseToken.
     */
    public void savePurchaseData(PurchaseData purchaseData) {
        String json = PurchaseDataJSON.toJSON(purchaseData);
        KGLog.d("savePurchaseData: saving for requestId (%s) json: %s", purchaseData.getRequestId(), json);
        String requestId = purchaseData.getRequestId();
        putString(requestId, json);
        String purchaseToken = purchaseData.getPurchaseToken();
        if (purchaseToken != null) {
            KGLog.d("savePurchaseData: saving for purchaseToken (%s) json: %s", purchaseToken, json);
            putString(purchaseToken, json);
        }
    }

    /**
     * Get PurchaseResponse data by requestId
     */
    public PurchaseData getPurchaseData(String requestId) {
        String json = getString(requestId);
        return json == null ? null : PurchaseDataJSON.fromJSON(json);
    }

    /**
     * Get PurchaseResponse data by purchaseToken
     */
    public PurchaseData getPurchaseDataByPurchaseToken(String purchaseToken) {
        String json = getString(purchaseToken);
        return json == null ? null : PurchaseDataJSON.fromJSON(json);
    }

    /**
     * Creates new {@link SKUData} which is used to keep track of fulfilled
     * count
     */
    public SKUData newSKUData(String sku) {
        KGLog.d("newSKUData: creating new SKUData for sku (%s)", sku);
        return new SKUData(sku);
    }

    /**
     * Saves {@link SKUData} to SharedPreferences
     */
    public void saveSKUData(SKUData skuData) {
        String json = SKUDataJSON.toJSON(skuData);
        KGLog.d("saveSKUData: saving for sku (%s) json: %s", skuData.getSKU(), json);
        putString(skuData.getSKU(), json);
    }

    /**
     * Gets {@link SKUData} from SharedPreferences by sku
     */
    public SKUData getSKUData(String sku) {
        String json = getString(sku);
        return json == null ? null : SKUDataJSON.fromJSON(json);
    }

    /**
     * Gets or Creates new {@link SKUData} from SharedPreferences by sku
     */
    public SKUData getOrCreateSKUData(String sku) {
        SKUData skuData = getSKUData(sku);
        if (skuData == null) {
            skuData = newSKUData(sku);
        }
        return skuData;
    }

    /**
     * Saves Offset from Purchase Updates response
     */
    public void savePurchaseUpdatesOffset(Offset offset) {
        putString("PURCHASE_UPDATES_OFFSET", offset.toString());
    }

    /**
     * Get Offset saved from last Purchase Updates response
     */
    public Offset getPurchaseUpdatesOffset() {
        String offsetString = getString("PURCHASE_UPDATES_OFFSET");
        if (offsetString == null) {
            KGLog.i("getPurchaseUpdatesOffset: no previous offset saved, use Offset.BEGINNING");
            return Offset.BEGINNING;
        }
        return Offset.fromString(offsetString);
    }

    /**
     * Remove purchase data for requestId
     */
    public void removePurchaseData(String requestId) {
        remove(requestId);
        removeRequestId(requestId);
    }

    /**
     * Reset SharedPreferences to switch to new SharedPreferences for
     * different user
     */
    private void resetSavedUserRequestsAndPurchaseReceipts() {
        this.savedUserRequestsAndPurchaseReceipts = null;
    }

    /**
     * Gets user specific SharedPreferences where we save requestId state
     * and purchase receipt data into.
     */
    private SharedPreferences getSavedUserRequestsAndPurchaseReceipts() {
        if (savedUserRequestsAndPurchaseReceipts != null) {
            return savedUserRequestsAndPurchaseReceipts;
        }
        savedUserRequestsAndPurchaseReceipts = activity.getSharedPreferences(currentUser, Activity.MODE_PRIVATE);
        return savedUserRequestsAndPurchaseReceipts;
    }

    /**
     * Convenience method to getStringSet for key from SharedPreferences
     */
    protected Set<String> getStringSet(String key) {
        Set<String> emptySet = new HashSet<String>();
        return getStringSet(key, emptySet);
    }

    /**
     * Convenience method to getStringSet for key from SharedPreferences,
     * specifying default values if null.
     */
    protected Set<String> getStringSet(String key, Set<String> defValues) {
        savedUserRequestsAndPurchaseReceipts = getSavedUserRequestsAndPurchaseReceipts();
        // If you're only targeting devices with Android API Level 11 or above
        // you can just use the getStringSet method
        String pipeDelimitedValues = getString(key);
        return convertPipeDelimitedToList(pipeDelimitedValues);
    }

    private Set<String> convertPipeDelimitedToList(String pipeDelimitedValues) {
        Set<String> result = new HashSet<String>();
        if (pipeDelimitedValues == null || "".equals(pipeDelimitedValues))
            return result;

        StringTokenizer stk = new StringTokenizer(pipeDelimitedValues, "|");
        while (stk.hasMoreTokens()) {
            String token = stk.nextToken();
            result.add(token);
        }
        return result;
    }

    /**
     * Convenience method to save string set by key into SharedPreferences
     */
    protected void putStringSet(String key, Set<String> valuesSet) {
        SharedPreferences.Editor editor = savedUserRequestsAndPurchaseReceipts.edit();
        // If you're only targeting devices with Android API Level 11 or above
        // you can just use the putStringSet method
        String pipeDelimitedValues = convertListToPipeDelimited(valuesSet);
        editor.putString(key, pipeDelimitedValues);
        editor.apply();
    }

    private String convertListToPipeDelimited(Set<String> values) {
        if (values == null || values.isEmpty())
            return "";
        StringBuilder result = new StringBuilder();
        for (Iterator<String> iter = values.iterator(); iter.hasNext(); ) {
            result.append(iter.next());
            if (iter.hasNext()) {
                result.append("|");
            }
        }
        return result.toString();
    }

    /**
     * Convenience method to save string by key into SharedPreferences
     */
    protected void putString(String key, String value) {
        savedUserRequestsAndPurchaseReceipts = getSavedUserRequestsAndPurchaseReceipts();
        SharedPreferences.Editor editor = savedUserRequestsAndPurchaseReceipts.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Convenience method to get int by key from SharedPreferences
     */
    protected int getInt(String key, int defValue) {
        savedUserRequestsAndPurchaseReceipts = getSavedUserRequestsAndPurchaseReceipts();
        return savedUserRequestsAndPurchaseReceipts.getInt(key, defValue);
    }

    /**
     * Convenience method to save int by key into SharedPreferences
     */
    protected void putInt(String key, int value) {
        SharedPreferences.Editor editor = savedUserRequestsAndPurchaseReceipts.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * Convenience method to get string by key from SharedPreferences
     */
    protected String getString(String key) {
        return getString(key, null);
    }

    /**
     * Convenience method to get string by key from SharedPreferences
     * returning default value if null
     */
    protected String getString(String key, String defValue) {
        savedUserRequestsAndPurchaseReceipts = getSavedUserRequestsAndPurchaseReceipts();
        return savedUserRequestsAndPurchaseReceipts.getString(key, defValue);
    }

    /**
     * Convenience method to get boolean by key from SharedPreferences
     * returning false as default value if null
     */
    protected boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * Convenience method to get boolean by key from SharedPreferences
     * returning default value if null
     */
    protected boolean getBoolean(String key, boolean defValue) {
        savedUserRequestsAndPurchaseReceipts = getSavedUserRequestsAndPurchaseReceipts();
        return savedUserRequestsAndPurchaseReceipts.getBoolean(key, defValue);
    }

    /**
     * Convenience method to save boolean by key into SharedPreferences
     */
    protected void putBoolean(String key, boolean value) {
        savedUserRequestsAndPurchaseReceipts = getSavedUserRequestsAndPurchaseReceipts();
        SharedPreferences.Editor editor = savedUserRequestsAndPurchaseReceipts.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * Convenience method to remove data in SharedPreferences by key
     */
    protected void remove(String key) {
        savedUserRequestsAndPurchaseReceipts = getSavedUserRequestsAndPurchaseReceipts();
        SharedPreferences.Editor editor = savedUserRequestsAndPurchaseReceipts.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * Checks whether we should fulfill for this SKU by checking SKU's fulfill count
     */
    public boolean shouldFulfillSKU(String sku) {
        SKUData skuData = getSKUData(sku);
        return skuData == null ? false : skuData.getFulfilledCount() > 0;
    }

}
