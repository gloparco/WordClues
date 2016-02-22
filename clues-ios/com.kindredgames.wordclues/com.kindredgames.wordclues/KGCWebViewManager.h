//
//  KGCWebViewManager.h
//
//  Created by Andrei on 9/27/12.
//  Copyright (c) 2012 Kindred Games. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "KGCCashier.h"

@protocol KGCGameController;

@protocol KGCUrlRespondDelegate <NSObject>

//@optional
- (void)respondWithJavaScript:(NSString *)response;

@end

@interface KGCWebViewManager : NSObject <UIWebViewDelegate, KGCUrlRespondDelegate> {
    UIViewController <KGCGameController> *controller;
    UIWebView *webView;
    NSObject <KGCUrlRespondDelegate> *responder;
}


- (KGCWebViewManager *)initWithView:(UIWebView *)delegatedWebView controller:(UIViewController *)$ontroller;
- (void)respondWithJavaScript:(NSString *)response;
//-(void) switchToTester;
- (void)sendMessage:(NSString *)response;
- (void)sendMessageObject:(NSDictionary *)response;

@end
