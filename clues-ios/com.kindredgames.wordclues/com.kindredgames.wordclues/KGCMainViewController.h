//
//  KGMainViewController.h
//  wmw-ios-ui
//
//  Created by Andrei on 9/23/12.
//  Copyright (c) 2012 Kindred Games. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <GameKit/GKFriendRequestComposeViewController.h>
#import <GameKit/GKLeaderboardViewController.h>
#import <Social/Social.h>
#import <Accounts/Accounts.h>
#import <StoreKit/StoreKit.h>
#import <StoreKit/SKProductsRequest.h>

#import "KGCWebViewManager.h"
#import "KGCGameController.h"
#import "KGCCacheController.h"
#import "KGCStoreManager.h"

@class KGCWordClues;
@class KGCCacheController;
@class SLComposeViewController;

@interface KGCMainViewController : UIViewController <KGCGameController, KGCCashier, GKGameCenterControllerDelegate> {

@private
    KGCWordClues *gameEngine;
    KGCStoreManager *storeManager;

    UIWebView *webView;
    UIActivityIndicatorView *activityIndicator;
    //UIImageView *initializationImage;

    KGCWebViewManager *webViewManager;
    SLComposeViewController *socialController;

    NSString *currentUserId; // GameCenter PlayerID or default value
    NSString *currentUserDisplayName; // GameCenter display name or default name
    NSArray *currentUserLeaderboards;
    NSString *currentUserCache; // Name of cache to be used for the user. Precalculated for performance
}

//-(id) initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil;

- (void)createViews;
- (void)zoomFitContent;

- (void)runTest;

@end
