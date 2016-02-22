#import <Foundation/Foundation.h>


@interface KGCResponseData : NSObject {
@public
    int callbackId;
    NSString *topic;
    NSString *json;
}

@end