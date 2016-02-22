//
//  KGCacheManager.m
//  wmw-ios-ui
//
//  Created by Andrei on 10/3/12.
//  Copyright (c) 2012 Kindred Games. All rights reserved.
//

#import "KGCCacheFile.h"
#import "KGCUtils.h"
#import "KGCDefine.h"

NSString *const CACHE_PREFIX = @"cache";

@implementation KGCCacheFile

- (NSString *)getKeyFileName:(NSString *)key forUser:(NSString *)userId {
    NSString *folder = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true) lastObject];
    NSString *userFolder = [folder stringByAppendingPathComponent:userId];
    if (![[NSFileManager defaultManager] fileExistsAtPath:userFolder isDirectory:nil]) {
        NSError *error;
        [[NSFileManager defaultManager] createDirectoryAtPath:userFolder withIntermediateDirectories:false attributes:nil error:&error];
        [KGCUtils logError:error doing:@"Creating user cache folder"];
    }
    NSString *fileName = [userFolder stringByAppendingPathComponent:[NSString stringWithFormat:@"%@_%@", CACHE_PREFIX, key]];
    KGLog(@"Access cache file %@", fileName);
    return fileName;
}

- (BOOL)set:(NSString *)key value:(NSString *)value forUser:(NSString *)userId {
    NSString *fileName = [self getKeyFileName:key forUser:userId];
    NSError *error;
    @try {
        if (value == nil) {
            NSFileManager *fileManager = [NSFileManager new];
            if ([fileManager fileExistsAtPath:fileName] && [fileManager isDeletableFileAtPath:fileName]) {
                NSError *error;
                [fileManager removeItemAtPath:fileName error:&error];
                if (error != nil) {
                    KGLog(@"Error deleting cache file %@: %@", fileName, error);
                    return false;
                }
            }
        }

        [value writeToFile:fileName atomically:NO encoding:NSStringEncodingConversionAllowLossy error:&error];
        [KGCUtils logError:error doing:@"Set cache"];
    } @catch (NSException *exc) {
        KGLog(@"Exception: %@", exc);
    } @finally {
        // Do we need to close anything?
    }

    return true;
}

- (BOOL)append:(NSString *)key value:(NSString *)value forUser:(NSString *)userId {
    NSString *fileName = [self getKeyFileName:key forUser:userId];
    NSFileHandle *fileHandle = [NSFileHandle fileHandleForWritingAtPath:fileName];
    @try {
        NSFileManager *fileManager = [NSFileManager new];
        if ([fileManager fileExistsAtPath:fileName]) {
            NSFileHandle *fileHandle = [NSFileHandle fileHandleForWritingAtPath:fileName];
            [fileHandle seekToEndOfFile];
            [fileHandle writeData:[value dataUsingEncoding:NSASCIIStringEncoding]]; // only numbers gets stored
        } else {
            [self set:key value:value forUser:userId];
        }
    } @catch (NSException *e) {
        KGLog(@"Error saving progress: %@", e);
    } @finally {
        if (fileHandle != nil) {
            [fileHandle closeFile];
        }
    }

    return true;
}

- (NSString *)get:(NSString *)key forUser:(NSString *)userId {
    NSString *fileName = [self getKeyFileName:key forUser:userId];
    if ([[NSFileManager defaultManager] fileExistsAtPath:fileName]) {
        NSError *error = [[NSError alloc] init];
        NSString *value = [NSString stringWithContentsOfFile:fileName encoding:NSUTF8StringEncoding error:&error];
        [KGCUtils logError:error doing:@"Get cache"];
        return value;
    }
    return nil;
}

//+(NSString*) getCurrentUserId
//{
//    NSString *fileName = [KGCCacheFile getKeyFileName:@"currentUserId" forUser:@""];
//    if ([[NSFileManager defaultManager] fileExistsAtPath:fileName]) {
//        NSError *error = [[NSError alloc] init];
//        NSString *value = [NSString stringWithContentsOfFile:fileName encoding:NSUTF8StringEncoding error:&error];
//        [KGCUtils logError:error doing:@"Get currentUserId"];
//        return value;
//    }
//    return nil;
//}

@end
