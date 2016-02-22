#import <Foundation/Foundation.h>

#import "KGCCacheController.h"

@interface KGCCacheFile : NSObject <KGCCacheController>

- (NSString *)get:(NSString *)key forUser:(NSString *)userId;
- (BOOL)set:(NSString *)key value:(NSString *)value forUser:(NSString *)userId;
- (BOOL)append:(NSString *)key value:(NSString *)value forUser:(NSString *)userId;

//+(NSString*)getCurrentUserId;

@end
