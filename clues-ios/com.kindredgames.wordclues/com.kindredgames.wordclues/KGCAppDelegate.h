//
//  KGCAppDelegate.h
//  com.kindredgames.wordclues
//
//  Created by Andrei on 11/22/13.
//  Copyright (c) 2013 Kindred Games Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface KGCAppDelegate : UIResponder <UIApplicationDelegate>

@property(strong, nonatomic) UIWindow *window;

- (void)saveContext;

- (NSURL *)applicationDocumentsDirectory;

@end
