//
//  KGCAppDelegate.m
//  com.kindredgames.wordclues
//
//  Created by Andrei on 11/22/13.
//  Copyright (c) 2013 Kindred Games Inc. All rights reserved.
//

#import "KGCDefine.h"
#import "KGCAppDelegate.h"
#import "KGCMainViewController.h"

NSString *const SETUP_FILE = @"setup";

@implementation KGCAppDelegate {

}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    self.window.clipsToBounds = true;
    // Override point for customization after application launch.
    self.window.backgroundColor = [UIColor blackColor];
    self.window.autoresizesSubviews = true;

    [self initApplication];

    [self.window makeKeyAndVisible];
    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application {
    // Saves changes in the application's managed object context before the application terminates.
    [self saveContext];
}

- (void)saveContext {
}

#pragma mark - Application's Documents directory

// Returns the URL to the application's Documents directory.
- (NSURL *)applicationDocumentsDirectory {
    return [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask] lastObject];
}

#pragma mark - Application setup

- (void)initApplication {
    // Check if setup is incomplete
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true);
    KGLog(@"Document Directory:");
    for (NSString *path in paths) {
        KGLog(@"%@", path);
    }

    paths = NSSearchPathForDirectoriesInDomains(NSDownloadsDirectory, NSUserDomainMask, true);
    KGLog(@"Downloads Directory:");
    for (NSString *path in paths) {
        KGLog(@"%@", path);
    }

    paths = NSSearchPathForDirectoriesInDomains(NSDocumentationDirectory, NSUserDomainMask, true);
    KGLog(@"Documentation Directory:");
    for (NSString *path in paths) {
        KGLog(@"%@", path);
    }

    paths = NSSearchPathForDirectoriesInDomains(NSApplicationDirectory, NSUserDomainMask, true);
    KGLog(@"Application Directory:");
    for (NSString *path in paths) {
        KGLog(@"%@", path);
    }

    paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true);
    KGLog(@"Caches Directory:");
    for (NSString *path in paths) {
        KGLog(@"%@", path);
    }
    KGLog(@"----- End of directories ------");

    if (![self isSetupComplete]) {
        [self completeSetup];
    }
    [self startApplication];
}

- (BOOL)isSetupComplete {
    NSString *documentFolder = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true) lastObject];
    NSString *setupFile = [documentFolder stringByAppendingPathComponent:SETUP_FILE];
    if (![[NSFileManager defaultManager] fileExistsAtPath:setupFile]) {
        [self completeSetup];
    } else {
        KGLog(@"Setup is complete");
    }
    return true;
}

- (void)completeSetup {
    KGLog(@"Completing setup");
    // Load missing resources

    KGLog(@"Marking complete setup");
    NSString *documentFolder = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true) lastObject];
    NSString *setupFile = [documentFolder stringByAppendingPathComponent:SETUP_FILE];
    NSString *setupContent = @"setup complete";
    [setupContent writeToFile:setupFile atomically:NO encoding:NSStringEncodingConversionAllowLossy error:nil];
}

- (void)startApplication {
    KGLog(@"Starting application");
    //TODO: make local to be selected either from system or application preferences
    NSString *path = [[NSBundle mainBundle] pathForResource:@"en" ofType:@"lproj"];
    NSBundle *languageBundle = [NSBundle bundleWithPath:path];

    KGCMainViewController *mainController = [[KGCMainViewController alloc] initWithNibName:nil bundle:languageBundle];

    //    mainController.view.frame = frameBounds;
    //    mainController.view.layer.borderColor = [UIColor redColor].CGColor;
    //    mainController.view.layer.borderWidth = 3.0f;

    [self.window addSubview:mainController.view];
    [self.window setRootViewController:mainController];
}

//-(void) loadWords
//{
//    NSString *filePath = [[NSBundle mainBundle] pathForResource:@"defaultGames" ofType:@"json"];
//    NSString *games = [NSString stringWithContentsOfFile:filePath encoding:NSUTF8StringEncoding error:nil];
////    KGLog(@"%@", games);
//
//    return (games == nil) ? @"{}" : [games stringByReplacingOccurrencesOfString:@"\n" withString:@""];
//
//}
@end
