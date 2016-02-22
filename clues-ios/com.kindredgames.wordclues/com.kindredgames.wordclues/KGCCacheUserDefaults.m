#import "KGCCacheUserDefaults.h"

// It looks like User Defaults are to be used for infrequently changing values (probably accessed via iOS Settings menu).
// Therefore they are not suitable for cache

@implementation KGCCacheUserDefaults {

}

- (id)init {
    self = [super init];
    if (self) {
        storage = [NSUserDefaults standardUserDefaults];
    }
    return self;
}

- (NSString *)getKey:(NSString *)key forUser:(NSString *)userId {
    return [NSString stringWithFormat:@"%@_%@", userId, key];
}

- (NSString *)get:(NSString *)key forUser:(NSString *)userId {
    return [storage objectForKey:[self getKey:key forUser:userId]];
}

- (BOOL)set:(NSString *)key value:(NSString *)value forUser:(NSString *)userId {
    [storage setObject:value forKey:[self getKey:key forUser:userId]];
    [storage synchronize];
    return true;
}

- (BOOL)append:(NSString *)key value:(NSString *)value forUser:(NSString *)userId {
    NSString *oldValue = [self get:key forUser:userId];
    [storage setObject:[oldValue stringByAppendingString:value] forKey:userId];
    [storage synchronize];
    return true;
}


@end
