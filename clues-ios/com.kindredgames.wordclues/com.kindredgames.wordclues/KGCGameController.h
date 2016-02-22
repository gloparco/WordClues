//
// Created by Andrei on 12/12/13.
// Copyright (c) 2013 Kindred Games Inc. All rights reserved.
//


#import <Foundation/Foundation.h>

@protocol KGCGameController <NSObject>

- (void)webViewDidLoad;

- (NSString *)generateGames:(int)gamesCount;
- (NSString *)generateGame;
- (NSString *)getUserCache:(NSString *)cacheName;
- (NSString *)setUserCache:(NSString *)cacheName data:(NSString *)data;

- (BOOL)canOpenFacebookApp;
- (BOOL)canOpenTwitterApp;

- (NSString *)postFacebook:(NSString *)message;
- (NSString *)postTwitter:(NSString *)message;
- (void)postEmail:(id)message;

- (void)linkStore;
- (void)linkTwitter;
- (void)linkFacebook;
- (void)linkCompany;

//-(NSString*) loginGameCenter:(NSString*)messageJson newTopic:(NSString**)newTopic callbackId:(int*)callbackId;
- (void)displayGameCenter;
- (void)loadLeaderboards;
- (void)updateLeaderboards:(NSDictionary *)values;
- (int)getLeaderboardValue:(NSString *)tokenName;
- (NSDictionary *)getLeaderboardValues;
- (NSDictionary *)getLeaderboardValues:(id)message;
- (NSString *)checkGameCenter;
- (NSString *)canMakePayments;

- (void)price:(NSDictionary *)invoice;
- (void)buy:(NSDictionary *)invoice;
- (NSString *)owns:(id)productIdMessage;
- (void)restorePurchases;

- (NSDictionary *)getAchievementValues;
- (void)updateAchievements:(id)message;

/**
* A helping hand method while developing
*/
- (void)runTest;

@end