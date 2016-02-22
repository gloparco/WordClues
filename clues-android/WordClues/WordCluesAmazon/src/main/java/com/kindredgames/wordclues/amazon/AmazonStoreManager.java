package com.kindredgames.wordclues.amazon;

import android.app.Activity;

import com.amazon.inapp.purchasing.GetUserIdResponse;
import com.amazon.inapp.purchasing.Item;
import com.amazon.inapp.purchasing.Offset;
import com.amazon.inapp.purchasing.PurchasingManager;

import com.kindredgames.wordclues.Cashier;
import com.kindredgames.wordclues.MessageDispatcher;
import com.kindredgames.wordclues.StoreManager;
import com.kindredgames.wordclues.amazon.purchase.AppPurchasingObserver;
import com.kindredgames.wordclues.amazon.purchase.AppPurchasingObserverListener;
import com.kindredgames.wordclues.amazon.purchase.PurchaseData;
import com.kindredgames.wordclues.amazon.purchase.PurchaseDataStorage;
import com.kindredgames.wordclues.util.KGLog;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AmazonStoreManager implements StoreManager, AppPurchasingObserverListener {

    public boolean isEnabled() {
        return true;
    }

    private Activity activity;
    private Cashier cashier;

    // Wrapper around SharedPreferences to save request state
    // and purchase receipt data
    private PurchaseDataStorage purchaseDataStorage;

    public MessageDispatcher getMessageDispatcher() {
        return (MessageDispatcher) activity;
    }

    public AmazonStoreManager(Activity activity, Cashier cashier) {
        this.activity = activity;
        this.cashier = cashier;
        setup();
    }

    @Override
    public String getStoreUrl() {
        return "amzn://apps/android?asin=M3PQI3OWGSB28Y";
        //return "http://www.amazon.com/gp/mas/dl/android?asin=M3PQI3OWGSB28Y";
    }

    public void setup() {
        purchaseDataStorage = new PurchaseDataStorage(activity);

        AppPurchasingObserver purchasingObserver = new AppPurchasingObserver(activity, purchaseDataStorage);
        purchasingObserver.setListener(this);

        KGLog.i("onCreate: registering AppPurchasingObserver");
        PurchasingManager.registerObserver(purchasingObserver);
    }

    public void init() {
        KGLog.i("onResume: call initiateGetUserIdRequest");
        PurchasingManager.initiateGetUserIdRequest();

//        KGLog.i("onResume: call initiateItemDataRequest for skus: " + AmazonWordCluesSkus.getAll());
//        PurchasingManager.initiateItemDataRequest(AmazonWordCluesSkus.getAll());
    }

    public void buy(String productId) {
        String requestId = PurchasingManager.initiatePurchaseRequest(productId);
        PurchaseData purchaseData = purchaseDataStorage.newPurchaseData(requestId);
        KGLog.i("BUY: requestId (%s) requestState (%s)", requestId, purchaseData.getRequestState());
    }

    public void prices(List<String> productNames) {
        Set<String> productIds = new HashSet<String>();
        for (String productName : productNames) {
            productIds.add(AmazonWordCluesSkus.valueForName(productName).getId());
        }
        KGLog.i("price: call initiateItemDataRequest for skus: " + productIds);
        PurchasingManager.initiateItemDataRequest(productIds); // handle response in onItemDataResponseSuccessful
    }

//    public String owns(String productId) {
//
//    }

    public void restorePurchases() {
        PurchasingManager.initiatePurchaseUpdatesRequest(Offset.BEGINNING);
    }

    /**
     * Callback for a successful get user id response
     * {@link GetUserIdResponseStatus#SUCCESSFUL}.
     * <p/>
     * In this sample app, if the user changed from the previously stored user,
     * this method updates the display based on purchase data stored for the
     * user in SharedPreferences.  The level 2 entitlement is fulfilled
     * if a stored purchase token was found to NOT be fulfilled or if the SKU
     * should be fulfilled.
     *
     * @param userId      returned from {@link GetUserIdResponse#getUserId()}.
     * @param userChanged - whether user changed from previously stored user.
     */
    @Override
    public void onGetUserIdResponseSuccessful(String userId, boolean userChanged) {
        KGLog.i("onGetUserIdResponseSuccessful: update display if userId (%s) user changed from previous stored user (%s)", userId, userChanged);

        if (!userChanged) {
            return;
        }

        // Reset to original setting where level2 is disabled
        //disableLevel2InView();

        Set<String> requestIds = purchaseDataStorage.getAllRequestIds();
        KGLog.i("onGetUserIdResponseSuccessful: (%s) saved requestIds", requestIds.size());
        for (String requestId : requestIds) {
            PurchaseData purchaseData = purchaseDataStorage.getPurchaseData(requestId);
            if (purchaseData == null) {
                KGLog.i("onGetUserIdResponseSuccessful: could NOT find purchaseData for requestId (%s), skipping", requestId);
                continue;
            }
            if (purchaseDataStorage.isRequestStateSent(requestId)) {
                KGLog.i("onGetUserIdResponseSuccessful: have not received purchase response for requestId still in SENT status: requestId (%s), skipping", requestId);
                continue;
            }

            KGLog.d("onGetUserIdResponseSuccessful: requestId (%s) %s", requestId, purchaseData);

            String purchaseToken = purchaseData.getPurchaseToken();
            String sku = purchaseData.getSKU();
            if (!purchaseData.isPurchaseTokenFulfilled()) {
                KGLog.i("onGetUserIdResponseSuccess: requestId (%s) userId (%s) sku (%s) purchaseToken (%s) was NOT fulfilled, fulfilling purchase now",
                        requestId, userId, sku);
                onPurchaseResponseSuccess(userId, sku, purchaseToken);

                purchaseDataStorage.setPurchaseTokenFulfilled(purchaseToken);
                purchaseDataStorage.setRequestStateFulfilled(requestId);
            } else {
                boolean shouldFulfillSKU = purchaseDataStorage.shouldFulfillSKU(sku);
                if (shouldFulfillSKU) {
                    KGLog.i("onGetUserIdResponseSuccess: should fulfill sku ("
                            + sku + ") is true, so fulfilling purchasing now");
                    onPurchaseUpdatesResponseSuccess(userId, sku, purchaseToken);
                }
            }
        }
    }

    /**
     * Callback for a failed get user id response
     * {@link GetUserIdRequestStatus#FAILED}
     *
     * @param requestId returned from {@link GetUserIdResponsee#getRequestId()} that
     *                  can be used to correlate with original request sent with
     *                  {@link PurchasingManager#initiateGetUserIdRequest()}.
     */
    @Override
    public void onGetUserIdResponseFailed(String requestId) {
        KGLog.i("onGetUserIdResponseFailed for requestId (%s)", requestId);
    }

    /**
     * Callback for successful item data response with unavailable SKUs
     * {@link ItemDataRequestStatus#SUCCESSFUL_WITH_UNAVAILABLE_SKUS}. This
     * means that these unavailable SKUs are NOT accessible in developer portal.
     * <p/>
     * In this sample app, we disable the buy button for these SKUs.
     *
     * @param unavailableSkus - skus that are not valid in developer portal
     */
    @Override
    public void onItemDataResponseSuccessfulWithUnavailableSkus(Set<String> unavailableSkus) {
        KGLog.i("onItemDataResponseSuccessfulWithUnavailableSkus: for (%s) unavailableSkus", unavailableSkus.size());
        //We don't disable anything yet
        //disableButtonsForUnavailableSkus(unavailableSkus);
    }

    /**
     * Callback for successful item data response
     * {@link ItemDataRequestStatus#SUCCESSFUL} with item data
     *
     * @param itemData - map of valid SKU->Items
     */
    @Override
    public void onItemDataResponseSuccessful(Map<String, Item> itemData) {
        for (Map.Entry<String, Item> entry : itemData.entrySet()) {
            String productId = entry.getKey();
            AmazonWordCluesSkus sku = AmazonWordCluesSkus.valueForId(productId);
            if (sku != null) {
                Item item = entry.getValue();
                KGLog.i("onItemDataResponseSuccessful: sku (%s) item (%s)", sku, item);
                try {
                    JSONObject message = new JSONObject();
                    message.put("topic", "store.priced");
                    message.put("productId", sku.getName());
                    message.put("price", item.getPrice());
                    getMessageDispatcher().dispatchMessage(message);
                } catch (JSONException exc) {
                    KGLog.e("Error creating JSON: %s", exc);
                }
            } else {
                KGLog.e("Unknown SKU productId: %s", productId);
            }
        }
    }

    /**
     * Callback for failed item data response
     * {@link ItemDataRequestStatus#FAILED}.
     *
     * @param requestId
     */
    public void onItemDataResponseFailed(String requestId) {
        KGLog.i("onItemDataResponseFailed: for requestId (%s)", requestId);
    }

    /**
     * Callback on successful purchase response
     * {@link PurchaseRequestStatus#SUCCESSFUL}. In this sample app, we show
     * level 2 as enabled
     *
     * @param sku
     */
    @Override
    public void onPurchaseResponseSuccess(String userId, String sku, String purchaseToken) {
        KGLog.i("onPurchaseResponseSuccess: for userId (%s) sku (%s) purchaseToken (%s)", userId, sku, purchaseToken);
        paidForSku(userId, sku, purchaseToken);
    }

    /**
     * Callback when user is already entitled
     * {@link PurchaseRequestStatus#ALREADY_ENTITLED} to sku passed into
     * initiatePurchaseRequest.
     *
     * @param userId
     */
    @Override
    public void onPurchaseResponseAlreadyEntitled(String userId, String sku) {
        KGLog.i("onPurchaseResponseAlreadyEntitled: for userId (%s) sku (%s)", userId, sku);
        // For entitlements, even if already entitled, make sure to enable.
        paidForSku(userId, sku, null);
    }

    /**
     * Callback when sku passed into
     * {@link PurchasingManager#initiatePurchaseRequest} is not valid
     * {@link PurchaseRequestStatus#INVALID_SKU}.
     *
     * @param userId
     * @param sku
     */
    @Override
    public void onPurchaseResponseInvalidSKU(String userId, String sku) {
        KGLog.i("onPurchaseResponseInvalidSKU: for userId (%s) sku (%s)", userId, sku);
    }

    /**
     * Callback on failed purchase response {@link PurchaseRequestStatus#FAILED}
     * .
     *
     * @param requestId
     * @param sku
     */
    @Override
    public void onPurchaseResponseFailed(String requestId, String sku) {
        KGLog.i("onPurchaseResponseFailed: for requestId (%s) sku (%s)", requestId, sku);
    }

    /**
     * Callback on successful purchase updates response
     * {@link PurchaseUpdatesRequestStatus#SUCCESSFUL} for each receipt.
     * <p/>
     * In this sample app, we show level 2 as enabled.
     *
     * @param userId
     * @param sku
     * @param purchaseToken
     */
    @Override
    public void onPurchaseUpdatesResponseSuccess(String userId, String sku, String purchaseToken) {
        KGLog.i("onPurchaseUpdatesResponseSuccess: for userId (%s) sku (%s) purchaseToken (%s)",
                userId, sku, purchaseToken);
        paidForSku(userId, sku, purchaseToken);
    }

    /**
     * Callback on successful purchase updates response
     * {@link PurchaseUpdatesRequestStatus#SUCCESSFUL} for revoked SKU.
     * <p/>
     * In this sample app, we revoke fulfillment if level 2 sku has been revoked
     * by showing level 2 as disabled
     *
     * @param userId
     * @param revokedSKU
     */
    @Override
    public void onPurchaseUpdatesResponseSuccessRevokedSku(String userId, String revokedSku) {
        KGLog.i("onPurchaseUpdatesResponseSuccessRevokedSku: for userId (%s) revokedSku (%s)", userId, revokedSku);
//        if (!MySKU.LEVEL2.getSku().equals(revokedSku))
//            return;
//
//        KGLog.i("onPurchaseUpdatesResponseSuccessRevokedSku: disabling play level 2 button");
//        disableLevel2InView();
//
//        KGLog.i("onPurchaseUpdatesResponseSuccessRevokedSku: fulfilledCountDown for revokedSKU (%s)", revokedSku);
    }

    /**
     * Callback on failed purchase updates response
     * {@link PurchaseUpdatesRequestStatus#FAILED}
     *
     * @param requestId
     */
    public void onPurchaseUpdatesResponseFailed(String requestId) {
        KGLog.i("onPurchaseUpdatesResponseFailed: for requestId (%s)", requestId);
    }

    private void paidForSku(String userId, String sku, String purchaseToken) {
        paymentReceived(sku, purchaseToken);
    }


    private boolean isValidReceipt(JSONObject receipt, String sku) {
        try {
            return receipt != null && sku != null
                    //&& StringUtils.isNotBlank(receipt.getString("transactionId"))
                    && sku.equalsIgnoreCase(receipt.getString("productId"));
        } catch (JSONException exc) {
            KGLog.e("Error parsing stored receipt for sku %s: %s", sku, exc);
            return false;
        }
    }

    private void paymentReceived(String sku, String purchaseToken) {
        try {
            JSONObject oldReceipt = cashier.getStoredReceipt(sku);
            if (!isValidReceipt(oldReceipt, sku)) {
                JSONObject receipt = new JSONObject();
                //receipt.put("date", transaction.transactionDate description);
                //receipt.put("dateTimeInterval", transaction.transactionDate timeIntervalSinceReferenceDate);
                receipt.put("transactionId", StringUtils.isNotEmpty(purchaseToken) ? purchaseToken : "purchaseToken"); // On restore transaction id may be not provided by Amazon
                receipt.put("productId", sku);
                receipt.put("quantity", 1);

                cashier.acceptPayment(sku, receipt.toString());
                notifyAboutPaidProduct(sku);
            } else {
                KGLog.d("Payment receipt is there already for %s. Not updating user", sku);
            }
        } catch (JSONException exc) {
            KGLog.e("Payment received JSON exception: %s", exc);
        }
    }

    private void notifyAboutPaidProduct(String productId) throws JSONException {
        JSONObject message = new JSONObject();
        message.put("topic", "store.paid");
        message.put("productId", productId);
        //[cashier sendMessage:[NSString stringWithFormat:@"{\"topic\":\"store.paid\",\"productId\":\"%@\"}", transaction.payment.productIdentifier]];
        getMessageDispatcher().dispatchMessage(message);
        KGLog.d("Purchase confirmation for %s", productId);
    }

}
