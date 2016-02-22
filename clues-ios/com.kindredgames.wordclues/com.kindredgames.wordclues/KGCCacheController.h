//
// Created by Andrei on 12/14/13.
// Copyright (c) 2013 Kindred Games Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol KGCCacheController <NSObject>

- (NSString *)get:(NSString *)key forUser:(NSString *)userId;
- (BOOL)set:(NSString *)key value:(NSString *)value forUser:(NSString *)userId;
- (BOOL)append:(NSString *)key value:(NSString *)value forUser:(NSString *)userId;

@end