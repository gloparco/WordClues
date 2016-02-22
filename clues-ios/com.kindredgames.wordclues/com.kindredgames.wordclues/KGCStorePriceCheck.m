//
// Created by Andrei on 12/14/13.
// Copyright (c) 2013 Kindred Games Inc. All rights reserved.
//

#import <StoreKit/StoreKit.h>

#import "KGCStorePriceCheck.h"
#import "KGCUtils.h"
#import "KGCDefine.h"

@implementation KGCStorePriceCheck {

}

//static const NSString *STORE_PRODUCT_ID_PREFIX = @"com.kindredgames.wordclues.";
static const NSString *STORE_PRODUCT_ID_PREFIX = @"";

- (id)initWithCashier:(NSObject <KGCCashier> *)myCashier {
    self = [super init];
    if (self) {
        cashier = myCashier;
    }
    return self;
}

- (void)price:(NSString *)productId {
    //[self validateProductIdentifiers:@[[STORE_PRODUCT_ID_PREFIX stringByAppendingString:productId]]];
    [self validateProductIdentifiers:@[productId]];
}

- (void)prices:(NSArray *)productIds {
    NSMutableArray *fullProductIds = [[NSMutableArray alloc] initWithCapacity:productIds.count];
    for (NSString *productId in productIds) {
        [fullProductIds addObject:[STORE_PRODUCT_ID_PREFIX stringByAppendingString:productId]];
    }
    [self validateProductIdentifiers:fullProductIds];
}

- (void)buy:(NSString *)productId {
    [self validateProductIdentifiers:@[[STORE_PRODUCT_ID_PREFIX stringByAppendingString:productId]]];
}

- (void)validateProductIdentifiers:(NSArray *)productIdentifiers {
    SKProductsRequest *productsRequest = [[SKProductsRequest alloc] initWithProductIdentifiers:[NSSet setWithArray:productIdentifiers]];
    productsRequest.delegate = self;
    [productsRequest start];
}

// SKProductsRequestDelegate protocol method
- (void)productsRequest:(SKProductsRequest *)request didReceiveResponse:(SKProductsResponse *)response {
    //self.products = response.products;
    if (response.invalidProductIdentifiers.count == 0) {
        for (int index = 0; index < response.products.count; index++) {
            SKProduct *product = [response.products objectAtIndex:index];

            NSNumberFormatter *numberFormatter = [[NSNumberFormatter alloc] init];
            [numberFormatter setFormatterBehavior:NSNumberFormatterBehavior10_4];
            [numberFormatter setNumberStyle:NSNumberFormatterCurrencyStyle];
            [numberFormatter setLocale:product.priceLocale];

            NSString *price = [numberFormatter stringFromNumber:product.price];
#if DEBUG_BUILD
            NSLocale* storeLocale = product.priceLocale;
            NSString *storeCountry = (NSString*)CFLocaleGetValue((__bridge CFLocaleRef)storeLocale, kCFLocaleCountryCode);
            KGLog(@"App Store Country %@. Price=%@%@", storeCountry, price, product.isDownloadable ? @", Downloadable" : @"");
#endif
            [self notifyAboutProductPrice:product.productIdentifier price:price];
        }
    } else {
        LogError(@"Invalid product identifier: %@", [response.invalidProductIdentifiers objectAtIndex:0]); //It's never more than 1 for the game
    }
}

- (void)request:(SKRequest *)request didFailWithError:(NSError *)error {
    LogError(@"Product request error: %@", error);
}

#pragma mark - SKPaymentTransactionObserver


- (void)notifyAboutProductPrice:(NSString *)productId price:(NSString *)price {
    NSMutableDictionary *message = [[NSMutableDictionary alloc] init];
    [message setObject:@"store.priced" forKey:@"topic"];
    [message setObject:productId forKey:@"productId"];
    [message setObject:price forKey:@"price"];
    [cashier sendMessageObject:message];
    KGLog(@"Price for %@ = %@", productId, price);
}

@end