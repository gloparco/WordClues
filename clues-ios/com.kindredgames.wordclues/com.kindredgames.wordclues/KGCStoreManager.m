//
// Created by Andrei on 12/14/13.
// Copyright (c) 2013 Kindred Games Inc. All rights reserved.
//

#import <StoreKit/StoreKit.h>

#import "KGCStoreManager.h"
#import "KGCUtils.h"
#import "KGCDefine.h"

@implementation KGCStoreManager {

}

//static const NSString *STORE_PRODUCT_ID_PREFIX = @"com.kindredgames.wordclues.";
static const NSString *STORE_PRODUCT_ID_PREFIX = @"";

- (id)initWithCashier:(NSObject <KGCCashier> *)myCashier {
    self = [super init];
    if (self) {
        cashier = myCashier;
        priceCheck = [[KGCStorePriceCheck alloc] initWithCashier:cashier];
        [[SKPaymentQueue defaultQueue] addTransactionObserver:self];
    }
    return self;
}

- (BOOL)isEnabled {
    return [SKPaymentQueue canMakePayments];
}

- (void)price:(NSString *)productId {
    [priceCheck price:productId];
}

- (void)prices:(NSArray *)productIds {
    [priceCheck prices:productIds];
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
        if (response.products.count > 0) {
            SKProduct *product = [response.products objectAtIndex:0];
            SKMutablePayment *payment = [SKMutablePayment paymentWithProduct:product];
            payment.quantity = 1;
            [[SKPaymentQueue defaultQueue] addPayment:payment];
            KGLog(@"Payment requested");
        }
    } else {
        LogError(@"Invalid product identifier: %@", [response.invalidProductIdentifiers objectAtIndex:0]); //It's never more than 1 for the game
    }
    //[self displayStoreUI]; // Custom method
}

- (void)request:(SKRequest *)request didFailWithError:(NSError *)error {
    LogError(@"Product request error: %@", error);
}

//- (void)loadPurchases {
//    [[SKPaymentQueue defaultQueue] restoreCompletedTransactions];
//}


#pragma mark - SKPaymentTransactionObserver

// Sent when the transaction array has changed (additions or state changes).  Client should check state of transactions and finish as appropriate.
- (void)paymentQueue:(SKPaymentQueue *)queue updatedTransactions:(NSArray *)transactions {
    if (transactions != nil) {
        for (SKPaymentTransaction *transaction in transactions) {
            switch (transaction.transactionState) {
                // Call the appropriate custom method.
                case SKPaymentTransactionStatePurchasing:
                    KGLog(@"Transaction State: Purchasing (%ld)", (unsigned long)transaction.transactionState);
                    //[self restoreTransaction:transaction];
                    break;
                case SKPaymentTransactionStatePurchased:
                    KGLog(@"Transaction State: Purchased (%ld)", (unsigned long)transaction.transactionState);
                    [self download:transaction];
                    [self paymentReceived:transaction];
                    break;
                case SKPaymentTransactionStateFailed:
                    KGLog(@"Transaction State: Failed (%ld)", (unsigned long)transaction.transactionState);
                    //[self failedTransaction:transaction];
                    [self download:transaction];
                    [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
                    break;
                case SKPaymentTransactionStateRestored:
                    KGLog(@"Transaction State: Restored (%ld)", (unsigned long)transaction.transactionState);
                    [self download:transaction];
                    [self paymentReceived:transaction];
                    break;
                default:
                    break;
            }
        }
    }
}

- (void)download:(SKPaymentTransaction *)transaction {
    [[SKPaymentQueue defaultQueue] startDownloads:transaction.downloads];
}

- (void)paymentReceived:(SKPaymentTransaction *)transaction {
    [[SKPaymentQueue defaultQueue] finishTransaction:transaction];

    NSMutableDictionary *receipt = [[NSMutableDictionary alloc] init];
    [receipt setObject:[transaction.transactionDate description] forKey:@"date"];
    [receipt setObject:[NSNumber numberWithDouble:[transaction.transactionDate timeIntervalSinceReferenceDate]] forKey:@"dateTimeInterval"];
    [receipt setObject:transaction.transactionIdentifier forKey:@"transactionId"];
    [receipt setObject:transaction.payment.productIdentifier forKey:@"productId"];
    if (transaction.payment.applicationUsername) {
        [receipt setObject:transaction.payment.applicationUsername forKey:@"applicationUsername"];
    }
    if (transaction.originalTransaction) {
        [receipt setObject:transaction.originalTransaction.transactionIdentifier forKey:@"originalTransaction"];
    }
    [receipt setObject:[NSNumber numberWithInteger:transaction.payment.quantity] forKey:@"quantity"];

    [cashier acceptPayment:transaction.payment.productIdentifier receipt:[KGCUtils jsonWithDictionary:receipt]];
    [self notifyAboutPaidProduct:transaction.payment.productIdentifier];
}

- (void)notifyAboutPaidProduct:(NSString *)productId {
    NSMutableDictionary *message = [[NSMutableDictionary alloc] init];
    [message setObject:@"store.paid" forKey:@"topic"];
    [message setObject:productId forKey:@"productId"];
    //[cashier sendMessage:[NSString stringWithFormat:@"{\"topic\":\"store.paid\",\"productId\":\"%@\"}", transaction.payment.productIdentifier]];
    [cashier sendMessageObject:message];
    KGLog(@"Purchase confirmation for %@", productId);
}

// Sent when transactions are removed from the queue (via finishTransaction:).
- (void)paymentQueue:(SKPaymentQueue *)queue removedTransactions:(NSArray *)transactions {
    KGLog(@"transactions are removed from the queue (via finishTransaction:)");
}

// Sent when an error is encountered while adding transactions from the user's purchase history back to the queue.
- (void)paymentQueue:(SKPaymentQueue *)queue restoreCompletedTransactionsFailedWithError:(NSError *)error {
    KGLog(@"An error is encountered while adding transactions from the user's purchase history back to the queue: %@", error);
}

// Sent when all transactions from the user's purchase history have successfully been added back to the queue.
- (void)paymentQueueRestoreCompletedTransactionsFinished:(SKPaymentQueue *)queue {
    KGLog(@"All transactions from the user's purchase history have successfully been added back to the queue");
}

// Sent when the download state has changed.
- (void)paymentQueue:(SKPaymentQueue *)queue updatedDownloads:(NSArray *)downloads {
    KGLog(@"The download state has changed");
}

- (void)restorePurchases {
    [[SKPaymentQueue defaultQueue] restoreCompletedTransactions];
}

@end