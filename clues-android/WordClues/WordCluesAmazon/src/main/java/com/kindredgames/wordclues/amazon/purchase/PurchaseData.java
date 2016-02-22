package com.kindredgames.wordclues.amazon.purchase;

/**
 * Represents {@link com.amazon.inapp.purchasing.PurchaseResponse} related data such as requestIds,
 * purchase tokens and sku. This data is saved into SharedPreferences in
 * serialized form.
 */
public class PurchaseData {

    private String requestId;
    private String userId;
    private RequestState requestState;
    private String purchaseToken;
    private String sku;
    private boolean purchaseTokenFulfilled;

    public PurchaseData(String requestId) {
        this.requestId = requestId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRequestState(RequestState requestState) {
        this.requestState = requestState;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getUserId() {
        return userId;
    }

    public RequestState getRequestState() {
        return requestState;
    }

    public int getRequestStateAsInt() {
        return requestState.getState();
    }

    public void setPurchaseToken(String purchaseToken) {
        this.purchaseToken = purchaseToken;
    }

    public void setSKU(String sku) {
        this.sku = sku;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }

    public String getSKU() {
        return sku;
    }

    public void setPurchaseTokenFulfilled() {
        this.purchaseTokenFulfilled = true;
    }

    public boolean isPurchaseTokenFulfilled() {
        return purchaseTokenFulfilled;
    }

    @Override
    public String toString() {
        return "PurchaseData [requestId=" + requestId + ", userId=" + userId + ", requestState=" + requestState + ", purchaseToken=" + purchaseToken + ", sku=" + sku
                + ", purchaseTokenFulfilled=" + purchaseTokenFulfilled + "]";
    }

}
