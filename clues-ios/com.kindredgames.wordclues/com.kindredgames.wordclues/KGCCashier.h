//
// Created by Andrei on 12/14/13.
// Copyright (c) 2013 Kindred Games Inc. All rights reserved.
//


#import <Foundation/Foundation.h>

@protocol KGCCashier <NSObject>

- (void)acceptPayment:(NSString *)productId receipt:(NSString *)receipt;

- (void)sendMessageObject:(NSDictionary *)response;

@end