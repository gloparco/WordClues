//
//  KGCUtils.m
//
//  Created by Andrei on 10/3/12.
//  Copyright (c) 2012 Kindred Games. All rights reserved.
//

#import <CommonCrypto/CommonDigest.h>
#import "KGCUtils.h"
#import "KGCDefine.h"

@implementation KGCUtils

+ (void)logError:(NSError *)error doing:(NSString *)action {
    if (error != nil && error.code != 0) {
        LogError(@"Error %@ code=%ld: %@", action, (unsigned long)error.code, error.description);
    }
}

+ (double)timestamp {
    return [[NSDate date] timeIntervalSince1970];
}

+ (NSString *)jsonWithDictionary:(NSDictionary *)dictionary {
    NSError *error;
    @try {
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dictionary
                                                           options:0 // NSJSONWritingPrettyPrinted
                                                             error:&error];
        if (jsonData) {
            return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        } else {
            LogError(@"Nil result serializing JSON: %@", error);
        }
    } @catch (NSException *exc) {
        LogError(@"Error serializing JSON: %@", exc);
    }
    return nil;
}

+ (id)jsonData:(NSString *)jsonMessage {
    id messageData = nil;
    if (jsonMessage != nil) {
        NSData *data = [jsonMessage dataUsingEncoding:NSUnicodeStringEncoding];
        NSError *error = nil;
        messageData = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingAllowFragments error:&error];
        if (error != nil) {
            LogError(@"Error parsing json [%@]: %@", jsonMessage, error);
        }
    }
    return messageData;
}

+ (NSString *)dataToJson:(NSDictionary *)jsonData {
    if (jsonData != nil) {
        NSError *error = nil;
        NSData *data = [NSJSONSerialization dataWithJSONObject:jsonData options:0 error:&error];
        if (error == nil) {
            NSString *json = [[NSString alloc] initWithData:data encoding:NSASCIIStringEncoding];
            return json;
        }
    }
    return nil;
}

+ (NSString *)jsonString:(NSString *)jsonString {
    return [jsonString stringByReplacingOccurrencesOfString:@"'" withString:@"\""];
}

+ (NSString *)getStringHash:(NSString *)source {
    return [KGCUtils getStringMd5:source];
}

/**
* Calculate the SHA-256 hash using Common Crypto
*/
+ (NSString *)getStringSHA256:(NSString *)source withSize:(const int)size {
    unsigned char hashedChars[size];
    const char *sourceString = [source UTF8String];
    size_t sourceStringLength = strlen(sourceString);

    // Confirm that the length of the user name is small enough
    // to be recast when calling the hash function.
    if (sourceStringLength > UINT32_MAX) {
        KGLog(@"Account name too long to hash: %@", source);
        return nil;
    }
    CC_SHA256(sourceString, (CC_LONG) sourceStringLength, hashedChars);

    // Convert the array of bytes into a string showing its hex representation.
    NSMutableString *hash = [[NSMutableString alloc] init];
    for (int i = 0; i < size; i++) {
        // Add a dash every four bytes, for readability.
//        if (i != 0 && i % 4 == 0) {
//            [hash appendString:@"-"];
//        }
        [hash appendFormat:@"%02x", hashedChars[i]];
    }
    return [NSString stringWithString:hash];
}

+ (BOOL)isEmptyString:(NSString *)string {
    return string == nil || string.length == 0;
}

+ (NSString *)getStringMd5:(NSString *)string {
    const char *cStr = [string UTF8String];
    unsigned char result[16];
    CC_MD5( cStr, strlen(cStr), result ); // This is the md5 call
    return [NSString stringWithFormat:
            @"%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x",
            result[0], result[1], result[2], result[3],
            result[4], result[5], result[6], result[7],
            result[8], result[9], result[10], result[11],
            result[12], result[13], result[14], result[15]
    ];
}


@end
