//
// Created by Andrei on 12/14/13.
// Copyright (c) 2013 Kindred Games Inc. All rights reserved.
//


#import <Foundation/Foundation.h>

#import <StoreKit/StoreKit.h>
#import <CommonCrypto/CommonCrypto.h>

#import "KGCCashier.h"

@interface KGCStorePriceCheck : NSObject <SKProductsRequestDelegate> {

@private
    NSObject <KGCCashier> *cashier;

}

- (id)initWithCashier:(NSObject <KGCCashier> *)myCashier;

- (void)price:(NSString *)productId;
- (void)prices:(NSArray *)productIds;

@end