#import <Foundation/Foundation.h>

#import "KGCCacheController.h"

@interface KGCCacheUserDefaults : NSObject <KGCCacheController> {

@private
    NSUserDefaults *storage;
}

- (id)init;

- (NSString *)get:(NSString *)key forUser:(NSString *)userId;
- (BOOL)set:(NSString *)key value:(NSString *)value forUser:(NSString *)userId;
- (BOOL)append:(NSString *)key value:(NSString *)value forUser:(NSString *)userId;

//+(NSString*)getCurrentUserId;

@end
