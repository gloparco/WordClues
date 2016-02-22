package com.kindredgames.wordclues.amazon.purchase;

import com.amazon.inapp.purchasing.Receipt;

import java.util.Set;

/**
 * Represents {@link com.amazon.inapp.purchasing.PurchaseUpdatesResponse} related data such as userId,
 * receipts and revoked skus.
 */
public class PurchaseUpdatesData {
    private final String userId;
    private final Set<Receipt> receipts;
    private final Set<String> revokedSkus;

    public PurchaseUpdatesData(String userId, Set<Receipt> receipts, Set<String> revokedSkus) {
        this.userId = userId;
        this.receipts = receipts;
        this.revokedSkus = revokedSkus;
    }

    public Set<Receipt> getReceipts() {
        return receipts;
    }

    public Set<String> getRevokedSkus() {
        return revokedSkus;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "PurchaseUpdatesData [userId=" + userId + ", receipts=" + receipts + ", revokedSkus=" + revokedSkus + "]";
    }

}
