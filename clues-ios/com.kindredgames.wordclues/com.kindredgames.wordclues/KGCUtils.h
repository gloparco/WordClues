//
//  KGCUtils.h
//
//  Created by Andrei on 10/3/12.
//  Copyright (c) 2012 Kindred Games. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface KGCUtils : NSObject

+ (void)logError:(NSError *)error doing:(NSString *)action;

+ (double)timestamp;

+ (NSString *)jsonWithDictionary:(NSDictionary *)dictionary;

+ (id)jsonData:(NSString *)jsonMessage;
+ (NSString *)dataToJson:(NSDictionary *)jsonData;

/**
* Convenience method, just replacing single quotes with double quotes for easier coding of JSON objects
*/
+ (NSString *)jsonString:(NSString *)jsonString;

+ (NSString *)getStringHash:(NSString *)source;

+ (BOOL)isEmptyString:(NSString *)string;

@end
