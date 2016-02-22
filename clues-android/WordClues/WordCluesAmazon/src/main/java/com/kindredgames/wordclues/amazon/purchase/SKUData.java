package com.kindredgames.wordclues.amazon.purchase;

/**
 * Represents SKU and fulfilled count. Saved in SharedPreferences in serialized form.
 */
public class SKUData {
    private String sku;
    private int fulfilledQuantity;
    private int consumedQuantity;

    public SKUData(String sku) {
        this.sku = sku;
        this.fulfilledQuantity = 0;
        this.consumedQuantity = 0;
    }

    public String getSKU() {
        return sku;
    }

    public void addFulfilledQuantity(int quantity) {
        this.fulfilledQuantity = this.fulfilledQuantity + quantity;
    }

    public void setFulfilledQuantity(int fulfilledQuantity) {
        this.fulfilledQuantity = fulfilledQuantity;
    }

    public void setConsumedQuantity(int consumedQuantity) {
        this.consumedQuantity = consumedQuantity;
    }

    public int getFulfilledQuantity() {
        return fulfilledQuantity;
    }

    public int getConsumedQuantity() {
        return consumedQuantity;
    }

    public int getHaveQuantity() {
        return fulfilledQuantity - consumedQuantity;
    }

    public void consume(int quantity) {
        this.consumedQuantity = this.consumedQuantity + quantity;
    }

    public int getFulfilledCount() {
        return fulfilledQuantity;
    }

    public void setFulfilledCount(int fulfilledCount) {
        this.fulfilledQuantity = fulfilledCount;
    }

    @Override
    public String toString() {
        return "SKUData [sku=" + sku + ", fulfilledQuantity=" + fulfilledQuantity + ", consumedQuantity=" + consumedQuantity + "]";
    }

}
