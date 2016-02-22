//
// Created by Andrei on 12/14/13.
// Copyright (c) 2013 Kindred Games Inc. All rights reserved.
//


#import <Foundation/Foundation.h>

#import <StoreKit/StoreKit.h>
#import <CommonCrypto/CommonCrypto.h>

#import "KGCCashier.h"
#import "KGCStorePriceCheck.h"

@interface KGCStoreManager : NSObject <SKProductsRequestDelegate, SKPaymentTransactionObserver> {

@private
    NSObject <KGCCashier> *cashier;
    KGCStorePriceCheck *priceCheck;
}

- (id)initWithCashier:(NSObject <KGCCashier> *)myCashier;
//- (void)loadPurchases;

- (BOOL)isEnabled;
- (void)buy:(NSString *)productId;
- (void)price:(NSString *)productId;
- (void)prices:(NSArray *)productIds;
- (void)restorePurchases;

@end