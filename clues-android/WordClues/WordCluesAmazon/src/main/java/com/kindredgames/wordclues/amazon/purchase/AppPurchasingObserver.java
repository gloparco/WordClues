package com.kindredgames.wordclues.amazon.purchase;

import java.util.Set;

import android.app.Activity;
import android.os.AsyncTask;

import com.amazon.inapp.purchasing.BasePurchasingObserver;
import com.amazon.inapp.purchasing.GetUserIdResponse;
import com.amazon.inapp.purchasing.GetUserIdResponse.GetUserIdRequestStatus;
import com.amazon.inapp.purchasing.ItemDataResponse;
import com.amazon.inapp.purchasing.ItemDataResponse.ItemDataRequestStatus;
import com.amazon.inapp.purchasing.Offset;
import com.amazon.inapp.purchasing.PurchaseResponse;
import com.amazon.inapp.purchasing.PurchaseResponse.PurchaseRequestStatus;
import com.amazon.inapp.purchasing.PurchaseUpdatesResponse;
import com.amazon.inapp.purchasing.PurchasingManager;
import com.amazon.inapp.purchasing.Receipt;
import com.kindredgames.wordclues.util.KGLog;

/**
 * Implementation of {@link BasePurchasingObserver} that implements each of the
 * callbacks and calls the methods on the {@link AppPurchasingObserverListener}
 * to notify the listener. In this  app, the {@link com.kindredgames.wordclues.amazon.AmazonStoreManager} is the
 * listener.
 */
public class AppPurchasingObserver extends BasePurchasingObserver {

    private static final String TAG = "SampleIAPConsumablesApp";

    private PurchaseDataStorage purchaseDataStorage;

    // Note: change below to a list if you want more than one listener
    private AppPurchasingObserverListener listener;

    public AppPurchasingObserver(Activity activity, PurchaseDataStorage purchaseDataStorage) {
        super(activity);
        this.purchaseDataStorage = purchaseDataStorage;
    }

    /**
     * Set listener which will get notified through callbacks. This code
     * only uses 1 listener, which is the {@link com.kindredgames.wordclues.amazon.AmazonStoreManager}. If instead
     * multiple listeners are needed, collect listeners with an addListener
     * method and notify all listeners through the callbacks.
     *
     * @param listener
     */
    public void setListener(AppPurchasingObserverListener listener) {
        this.listener = listener;
    }

    /**
     * This callback indicates that the SDK is initialized and whether the app
     * is live or in sandbox (test) mode
     *
     * @param isSandboxMode
     */
    @Override
    public void onSdkAvailable(boolean isSandboxMode) {
        KGLog.i("onSdkAvailable: isSandboxMode: " + isSandboxMode);
    }

    /**
     * This is callback for {@link PurchasingManager#initiateGetUserIdRequest}.
     * For successful case, save the current user from {@link GetUserIdResponse}
     * , and notify listener through
     * {@link AppPurchasingObserverListener#onGetUserIdResponseSuccessful} method
     * and initiate a purchase updates request. For failed case, notify listener
     * through {@link AppPurchasingObserverListener#onGetUserIdResponseFailed}
     * method.
     *
     * @param response
     */
    @Override
    public void onGetUserIdResponse(GetUserIdResponse response) {
        KGLog.i("onGetUserIdResponse: requestId (" + response.getRequestId() + ") userIdRequestStatus: " + response.getUserIdRequestStatus() + ")");

        GetUserIdRequestStatus status = response.getUserIdRequestStatus();
        switch (status) {
            case SUCCESSFUL:
                String userId = response.getUserId();
                KGLog.i("onGetUserIdResponse: save userId (" + userId + ") as current user");
                boolean userChanged = saveCurrentUser(userId);

                KGLog.i("onGetUserIdResponse: call onGetUserIdResponseSuccess for userId (" + userId + ") userChanged (" + userChanged + ")");
                listener.onGetUserIdResponseSuccessful(userId, userChanged);

                Offset offset = purchaseDataStorage.getPurchaseUpdatesOffset();

                KGLog.i("onGetUserIdResponse: call initiatePurchaseUpdatesRequest from offset (" + offset + ")");
                PurchasingManager.initiatePurchaseUpdatesRequest(offset);
                break;

            case FAILED:
                KGLog.i("onGetUserIdResponse: FAILED");
                listener.onGetUserIdResponseFailed(response.getRequestId());
                break;
        }
    }

    private boolean saveCurrentUser(String userId) {
        return purchaseDataStorage.saveCurrentUser(userId);
    }

    /**
     * This is callback for {@link PurchasingManager#initiateItemDataRequest}.
     * For unavailable SKUs, notify listener through
     * {@link AppPurchasingObserverListener#onItemDataResponseSuccessfulWithUnavailableSkus(java.util.Set)} }
     * method.
     */
    @Override
    public void onItemDataResponse(ItemDataResponse response) {
        final ItemDataRequestStatus status = response.getItemDataRequestStatus();
        KGLog.i("onItemDataResponse: itemDataRequestStatus (" + status + ")");

        switch (status) {
            case SUCCESSFUL_WITH_UNAVAILABLE_SKUS:
                Set<String> unavailableSkus = response.getUnavailableSkus();
                KGLog.i("onItemDataResponse: " + unavailableSkus.size() + " unavailable skus");
                if (!unavailableSkus.isEmpty()) {
                    KGLog.i("onItemDataResponse: call onItemDataResponseUnavailableSkus");
                    listener.onItemDataResponseSuccessfulWithUnavailableSkus(unavailableSkus);
                }
            case SUCCESSFUL:
                KGLog.d("onItemDataResponse: successful. The item data map in this response includes the valid SKUs");
                listener.onItemDataResponseSuccessful(response.getItemData());
                break;
            case FAILED:
                KGLog.d("onItemDataResponse: failed, should retry request");
                listener.onItemDataResponseFailed(response.getRequestId());
                break;
        }
    }

    /**
     * This is callback for
     * {@link PurchasingManager#initiatePurchaseUpdatesRequest}.
     * <p/>
     * For consumables we do not receive any info through this callback.
     * We just need to call {@link PurchasingManager#initiatePurchaseUpdatesRequest}
     * to ensure we received the callback in onPurchaseResponse.
     */
    @Override
    public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse response) {
        KGLog.i("onPurchaseUpdatesResponse: requestId (%s) purchaseUpdatesResponseStatus (%s) userId (%s)",
                response.getRequestId(), response.getPurchaseUpdatesRequestStatus(), response.getUserId());
        Set<Receipt> receipts = response.getReceipts();
        //Set<String> revokedSkus = response.getRevokedSkus();
        String userId = response.getUserId();
        for (Receipt receipt : receipts) {
            listener.onPurchaseResponseAlreadyEntitled(userId, receipt.getSku());
        }
    }

    /**
     * This is callback for {@link PurchasingManager#initiatePurchaseRequest}.
     * Save purchase response including receipt and then fire off async task to
     * fulfill purchase. For the other response cases: already entitled, invalid
     * sku and failed, notify {@link AppPurchasingObserverListener} through
     * appropriate method.
     */
    @Override
    public void onPurchaseResponse(PurchaseResponse response) {
        String requestId = response.getRequestId();
        String userId = response.getUserId();
        PurchaseRequestStatus status = response.getPurchaseRequestStatus();
        KGLog.i("onPurchaseResponse: requestId (%s) userId (%s) purchaseRequestStatus (%s)",
                requestId, userId, status);
        if (!purchaseDataStorage.isSameAsCurrentUser(userId)) {
            // In most cases UserId in PurchaseResponse should be the
            // same as UserId from GetUserIdResponse
            KGLog.i("onPurchaseResponse: userId (%s) in response is NOT the same as current user!", userId);
            return;
        }

        PurchaseData purchaseDataForRequestId = null;
        String sku = null;

        switch (status) {
            case SUCCESSFUL:
                Receipt receipt = response.getReceipt();
                KGLog.i("onPurchaseResponse: receipt itemType (%s) SKU (%s) purchaseToken (%s)",
                        receipt.getItemType(), receipt.getSku(), receipt.getPurchaseToken());

                KGLog.i("onPurchaseResponse: call savePurchaseReceipt for requestId (%s)", response.getRequestId());
                PurchaseData purchaseData = purchaseDataStorage.savePurchaseResponse(response);
                if (purchaseData == null) {
                    KGLog.i("onPurchaseResponse: could not save purchase receipt for requestId (%s), skipping fulfillment", response.getRequestId());
                    break;
                }

                KGLog.i("onPurchaseResponse: fulfill purchase with AsyncTask");
                new PurchaseResponseSuccessAsyncTask().execute(purchaseData);

                break;
            case ALREADY_ENTITLED:
                KGLog.i("onPurchaseResponse: already entitled, should never get here for a consumable.");
                // Should never get here for a consumable
                purchaseDataForRequestId = purchaseDataStorage.getPurchaseData(requestId);
                purchaseDataStorage.removePurchaseData(requestId);
                if (purchaseDataForRequestId != null) {
                    sku = purchaseDataForRequestId.getSKU();
                }
                listener.onPurchaseResponseAlreadyEntitled(userId, sku);
                break;
            case INVALID_SKU:
                KGLog.i("onPurchaseResponse: invalid SKU! Should never get here, onItemDataResponse should have disabled buy button already.");
                // We should never get here because onItemDataResponse should have
                // taken care of invalid skus already and disabled the buy button
                purchaseDataForRequestId = purchaseDataStorage.getPurchaseData(requestId);
                purchaseDataStorage.removePurchaseData(requestId);
                if (purchaseDataForRequestId != null) {
                    sku = purchaseDataForRequestId.getSKU();
                }
                listener.onPurchaseResponseInvalidSKU(userId, sku);
                break;
            case FAILED:
                KGLog.i("onPurchaseResponse: failed so remove purchase request from local storage");
                purchaseDataForRequestId = purchaseDataStorage.getPurchaseData(requestId);
                purchaseDataStorage.removePurchaseData(requestId);
                if (purchaseDataForRequestId != null) {
                    sku = purchaseDataForRequestId.getSKU();
                }
                listener.onPurchaseResponseFailed(requestId, sku);
                // May want to retry request
                break;
        }

    }

    /**
     * AsyncTask to fulfill purchase which is kicked off from
     * onPurchaseResponse. Notify listener through
     * {@link AppPurchasingObserverListener#onPurchaseResponseSuccess} method.
     * Save the fact that purchase token and requestId are fulfilled.
     */
    private class PurchaseResponseSuccessAsyncTask extends AsyncTask<PurchaseData, Void, Boolean> {

        @Override
        protected Boolean doInBackground(PurchaseData... args) {
            PurchaseData purchaseData = args[0];

            String requestId = purchaseData.getRequestId();

            String userId = purchaseData.getUserId();
            String sku = purchaseData.getSKU();
            String purchaseToken = purchaseData.getPurchaseToken();

            KGLog.i("PurchaseResponseSuccessAsyncTask.doInBackground: call listener's onPurchaseResponseSucccess for sku (" + sku + ")");
            listener.onPurchaseResponseSuccess(userId, sku, purchaseToken);

            KGLog.d("PurchaseResponseSuccessAsyncTask.doInBackground: fulfilled SKU (" + sku + ") purchaseToken (" + purchaseToken + ")");
            purchaseDataStorage.setPurchaseTokenFulfilled(purchaseToken);

            purchaseDataStorage.setRequestStateFulfilled(requestId);
            KGLog.d("PurchaseResponseSuccessAsyncTask.doInBackground: completed for requestId (" + requestId + ")");
            return true;
        }
    }

}
