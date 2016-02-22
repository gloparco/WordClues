//
//  KGMainViewController.m
//  wmw-ios-ui
//
//  Created by Andrei on 9/23/12.
//  Copyright (c) 2012 Kindred Games. All rights reserved.
//

#import <GameKit/GameKit.h>
#import <Social/Social.h>

#import "KGCMainViewController.h"
#import "KGCCacheFile.h"
#import "KGCWordClues.h"
#import "Reachability.h"
#import "KGCDefine.h"
#import "KGCUtils.h"

@interface KGCMainViewController ()

@property (nonatomic) Reachability *hostReachability;
@property (nonatomic) Reachability *internetReachability;
@property (nonatomic) Reachability *wifiReachability;
@property(nonatomic, retain) NSMutableDictionary *currentUserAchievements;

@end


@implementation KGCMainViewController

static const NSString *RESPONSE_OK = @"{\"ok\":1}";
static const NSString *RESPONSE_FAILURE = @"{\"ok\":0}";
static const NSString *RESPONSE_GAME_CENTER_CLOSED = @"{\"topic\":\"gamecenter.closed\"}";

#ifdef DEBUG_BUILD
//    static const NSString *START_PAGE = @"clues-html-dev/index-dev-ios";
    static const NSString *START_PAGE = @"clues-html-dev/index-dev-android";
#else
    static const NSString *START_PAGE = @"clues-html/index";
#endif

static const NSString *DEFAULT_CACHE_USER = @"default"; // so far we don't switch cache if user ever changes, as it's not supposed to
static const NSString *DEFAULT_USER_DISPLAY_NAME = @"Dude";

static const NSString *LEADERBOARD_ID_BASE = @"com.kindredgames.wordclues.";
static const NSString *ACHIEVEMENT_ID_BASE = @"";
static const NSString *FEEDBACK_EMAIL = @"support@kindredgames.com";

const NSString *NETWORK_HOST_URL =  @"www.apple.com";
const NSString *NETWORK_HOST_ON =  @"{\"topic\":\"network\", \"type\":\"host\", \"on\":true }";
const NSString *NETWORK_HOST_OFF = @"{\"topic\":\"network\", \"type\":\"host\", \"on\":false }";
const NSString *NETWORK_INET_ON =  @"{\"topic\":\"network\", \"type\":\"inet\", \"on\":true }";
const NSString *NETWORK_INET_OFF = @"{\"topic\":\"network\", \"type\":\"inet\", \"on\":false }";
const NSString *NETWORK_WIFI_ON =  @"{\"topic\":\"network\", \"type\":\"wifi\", \"on\":true }";
const NSString *NETWORK_WIFI_OFF = @"{\"topic\":\"network\", \"type\":\"wifi\", \"on\":false }";

//static NSArray *nonCloudKeys;

NSObject <KGCCacheController> *cache;
//NSObject <KGCCacheController> *cloud;

#pragma mark - Lifecycle

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        self.currentUserAchievements = [[NSMutableDictionary alloc] init];
        [self createViews];
        [self initUserCache];
        gameEngine = [[KGCWordClues alloc] initWithCache:cache];
        webViewManager = [[KGCWebViewManager alloc] initWithView:webView controller:self];
        storeManager = [[KGCStoreManager alloc] initWithCashier:self];
        //[storeManager loadPurchases]; // must be done first time only on install - requires login to iTunes
        [self initUser]; // initializing user may also cause chain reaction, and all components must be ready. So the call must be last.
    }
    return self;
}

- (void)initUser {
    if ([GKLocalPlayer localPlayer].isAuthenticated) {
        [self updateCurrentGameCenterUser];
    } else {
        //[self resetDefaultUser];
        [self loadLastUser];
        [self authenticateWithGameCenter];
    }
}

- (void)initUserCache {
    //nonCloudKeys = @[@"game"];

    // File cache is a custom file-based cache solution. As each cached item is in a separate file, cache contents is easy to check or delete. Use it for debugging.
    // Also it should have better performance for appending to cached value
#ifdef DEBUG_BUILD
    KGLog(@"DEBUG BUILD");
#endif
    cache = [[KGCCacheFile alloc] init];

    // UserDefaults cache is embedded iOS cache solution. It uses a SQLLite DB in the same location as FileCache, i.e. Library/Caches/DataDiskCache. Use it for production?
    //cache = [[KGCCacheUserDefaults alloc] init];

    // Cloud storage can be used for all settings, except local, specific for the device, e.g. current game, played clues, etc.
    //cloud = [[KGCCacheCloud alloc] init];
}

- (void)loadLastUser {
    NSString *cachedPlayerId = [cache get:@"playerId" forUser:@""];
    if ([KGCUtils isEmptyString:(cachedPlayerId)]) {
        [self resetDefaultUser];
        // don't load UI first time as it will immediately result in reloading once GC responses
    } else {
        currentUserId = cachedPlayerId;
        currentUserCache = [self getUserCacheFolder:currentUserId];
        currentUserDisplayName = [cache get:@"playerName" forUser:@""];
        KGLog(@"Loading cached user: %@", currentUserId);
        [self loadGamePage:NO];
    }
}

- (void)loadLeaderboards {
    if ([GKLocalPlayer localPlayer].isAuthenticated) {
        [GKLeaderboard loadLeaderboardsWithCompletionHandler:^(NSArray *leaderboards, NSError *error) {
            NSMutableDictionary *scores = [[NSMutableDictionary alloc] initWithCapacity:leaderboards.count];
            NSMutableArray *failedLeaderboards = [[NSMutableArray alloc] init];
            for (GKLeaderboard *leaderboard in leaderboards) {
                //[scores addObject:leaderboard.localPlayerScore];
                KGLog(@"Request scores for leaderboard: %@", leaderboard.identifier);
                [leaderboard loadScoresWithCompletionHandler:^(NSArray *lbScores, NSError *error) {
                    GKScore *score = leaderboard.localPlayerScore;
                    if (error == nil && score != nil) {
                        KGLog(@"Loaded scores for leaderboard: %@, value=%lld", score.leaderboardIdentifier, score.value);
                        @synchronized (scores) {
                            [scores setObject:[NSNumber numberWithLongLong:score.value] forKey:[self getLeaderboardName:score.leaderboardIdentifier]];
                        }
                        if (scores.count >= leaderboards.count) {
                            // If here, all leaderboards loaded correctly
                            currentUserLeaderboards = leaderboards;
                            [self notifyAboutLeaderboardScores:scores];
                        }
                    } else {
                        if (error != nil) {
                            KGLog(@"Error loading scores for leaderboard %@: %@", leaderboard.title, error); // to expect - possibly due to network failure
                        } else {
                            KGLog(@"No value for leaderboard %@", leaderboard.identifier);
                        }
                        @synchronized (failedLeaderboards) {
                            if (failedLeaderboards.count == 0) {
                                // only need to notify once about any failed leaderboard
                                [failedLeaderboards addObject:leaderboard];
                                [self notifyAboutLeaderboardScores:nil];
                            }
                        }
                    }
                }];
            }
        }];
    }
}

- (void)notifyAboutLeaderboardScores:(NSDictionary *)scores {
    NSMutableDictionary *message = [[NSMutableDictionary alloc] init];
    [message setObject:@"leaderboards" forKey:@"topic"];
    if (scores != nil) {
        [message setObject:scores forKey:@"scores"];
    }
    [webViewManager sendMessageObject:message];
}

- (void)viewDidLoad {
    webView.opaque = YES;
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self zoomFitContent];
    [self setNeedsStatusBarAppearanceUpdate];
    [self initReachibility];
}

- (void)webViewDidLoad {
    [activityIndicator stopAnimating];
}

- (void)initReachibility {
    /*
     Observe the kNetworkReachabilityChangedNotification. When that notification is posted, the method reachabilityChanged will be called.
     */
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(reachabilityChanged:) name:kReachabilityChangedNotification object:nil];

    //Change the host name here to change the server you want to monitor.
    NSString *remoteHostName = (NSString *)NETWORK_HOST_URL;

    self.hostReachability = [Reachability reachabilityWithHostName:remoteHostName];
    [self.hostReachability startNotifier];
    [self updateInterfaceWithReachability:self.hostReachability];

    self.internetReachability = [Reachability reachabilityForInternetConnection];
    [self.internetReachability startNotifier];
    [self updateInterfaceWithReachability:self.internetReachability];

    self.wifiReachability = [Reachability reachabilityForLocalWiFi];
    [self.wifiReachability startNotifier];
    [self updateInterfaceWithReachability:self.wifiReachability];
}

/*!
 * Called by Reachability whenever status changes.
 */
- (void)reachabilityChanged:(NSNotification *)note {
    Reachability* curReach = [note object];
    NSParameterAssert([curReach isKindOfClass:[Reachability class]]);
    [self updateInterfaceWithReachability:curReach];
}

- (void)updateInterfaceWithReachability:(Reachability *)reachability {
    NetworkStatus netStatus = [reachability currentReachabilityStatus];
    BOOL connectionRequired = [reachability connectionRequired]; // not clear yet usability of that flag
    BOOL on = NO;
    switch (netStatus) {
        case NotReachable: {
            connectionRequired = NO;
            on = NO;
            break;
        }
        case ReachableViaWWAN: {
            on = YES;
            break;
        }
        case ReachableViaWiFi: {
            on = YES;
            break;
        }
    }

    NSString *message = nil;
    if (reachability == self.hostReachability) {
        message = on ? (NSString *)NETWORK_HOST_ON : (NSString *)NETWORK_HOST_OFF;
    } else if (reachability == self.internetReachability) {
        message = on ? (NSString *)NETWORK_INET_ON : (NSString *)NETWORK_INET_OFF;
    } else if (reachability == self.wifiReachability) {
        message = on ? (NSString *)NETWORK_WIFI_ON : (NSString *)NETWORK_WIFI_OFF;
    }
    [webViewManager sendMessage:message];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (BOOL)canBecomeFirstResponder {
    return true;
}

#pragma mark - Private methods

- (void)createViews {
    self.view.clipsToBounds = YES;
    CGRect viewBounds = self.view.bounds;

//    initializationImage = [[UIImageView alloc] initWithFrame:viewBounds];
//    initializationImage.backgroundColor = [UIColor blackColor];
    //    initializationImage.image = [UIImage imageNamed:@"initializationImage.png"];
    //    [self.view addSubview:initializationImage];

    activityIndicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    activityIndicator.color = [UIColor purpleColor];
    activityIndicator.center = self.view.center;
    activityIndicator.hidesWhenStopped = YES;
    [activityIndicator startAnimating];
    [self.view addSubview:activityIndicator];

    webView = [[UIWebView alloc] initWithFrame:viewBounds];
    webView.contentMode = UIViewContentModeScaleToFill;
    webView.scalesPageToFit = YES;
    webView.clipsToBounds = YES;
    webView.delegate = webViewManager;
    webView.autoresizingMask = UIViewAutoresizingFlexibleHeight + UIViewAutoresizingFlexibleWidth;
    webView.backgroundColor = [UIColor blackColor];
    webView.opaque = NO;

    [[webView scrollView] setScrollEnabled:NO];

    //[self loadGamePage];
    //[self.view addSubview:webView];
}

- (void)loadGamePage:(BOOL)saveNewUser {
    NSString *fileName = [[NSBundle mainBundle] pathForResource:(NSString *)START_PAGE ofType:@"html"];
    KGLog(@"Loading URL: %@", fileName);
    NSURL *fileUrl = [[NSURL alloc] initFileURLWithPath:fileName];

    NSURLRequest *request = [NSURLRequest requestWithURL:fileUrl];
    [webView loadRequest:request];
    // The last loaded user is going to be the next candidate as long as JS doesn't support switching
    if (saveNewUser) {
        [cache set:@"playerId" value:currentUserId forUser:@""];
        [cache set:@"playerName" value:currentUserDisplayName forUser:@""];
    }
}

- (void)zoomFitContent {
    // try to fit web content
    if ([webView respondsToSelector:@selector(scrollView)]) {
        UIScrollView *scroll = [webView scrollView];

        float zoomX = webView.bounds.size.width / scroll.contentSize.width;
        float zoomY = webView.bounds.size.height / scroll.contentSize.height;
        float zoom = MIN(zoomX, zoomY);
        // Looks like ignores scaling below value, making one of sides not covering the entire view
        [scroll setZoomScale:zoom animated:YES];
    }
}

- (UIStatusBarStyle)preferredStatusBarStyle {
    return UIStatusBarStyleLightContent;
}

- (NSString *)generateGames:(int)gamesCount {
    NSString *games = [gameEngine generateGamesJson:gamesCount];
    return [self filterJsonString:games];
}

- (NSString *)generateGame {
    NSString *game = [gameEngine generateGameJson];
    return [self filterJsonString:game];
}

- (NSString *)getUserCacheFolder:(NSString *)userId {
#ifdef DEBUG_BUILD
    return [userId stringByReplacingOccurrencesOfString:@":" withString:@""];
#else
    return [KGCUtils getStringHash:userId];
#endif
}

- (NSString *)getCacheUser {
    return currentUserCache;
}

- (NSString *)getUserCache:(NSString *)cacheName {
    NSString *userId = [self getCacheUser];
    NSString *cachedValue = nil;
//    if ([nonCloudKeys indexOfObject:cacheName] == NSNotFound) {
//        cachedValue = [cloud get:cacheName forUser:userId];
//    }
//    if (cachedValue == nil) {
//        KGLog(@"Nothing in cloud for key=%@", cacheName);
        cachedValue = [cache get:cacheName forUser:userId];
//    }
    return [self filterJsonString:cachedValue];
}

/**
* null data deletes the cache file
*/
- (NSString *)setUserCache:(NSString *)cacheName data:(NSString *)data {
    NSString *userId = [self getCacheUser];
    if ([cache set:cacheName value:data forUser:userId]) {
//        if ([nonCloudKeys indexOfObject:cacheName] == NSNotFound) {
//            [cloud set:cacheName value:data forUser:userId];
//        }
        return (NSString *)RESPONSE_OK;
    } else {
        return (NSString *)RESPONSE_FAILURE;
    }
}

- (NSString *)filterJsonString:(NSString *)json {
//    return (json == nil) ? @"{}" : [json stringByReplacingOccurrencesOfString:@"\n" withString:@""];
    return (json == nil) ? @"null" : [json stringByReplacingOccurrencesOfString:@"\n" withString:@""];
}

#pragma mark - Game Center methods

- (void)authenticateWithGameCenter {
    KGLog(@"authenticateWithGameCenter");
    [GKLocalPlayer localPlayer].authenticateHandler = ^(UIViewController *gameCenterController, NSError *error) {
        if (gameCenterController != nil) {
            if ([GKLocalPlayer localPlayer].isAuthenticated) {
                KGLog(@"authenticateWithGameCenter gcc updateCurrentGameCenterUser");
                [self updateCurrentGameCenterUser];
            } else {
                KGLog(@"authenticateWithGameCenter presentViewController");
                [self presentViewController:gameCenterController animated:YES completion:^(void) {
                    KGLog(@"authenticateWithGameCenter presentViewController completion");
                    [self updateCurrentGameCenterUser];
                }];
            }
        } else if ([GKLocalPlayer localPlayer].isAuthenticated) {
            KGLog(@"authenticateWithGameCenter updateCurrentGameCenterUser");
            [self updateCurrentGameCenterUser];
        } else {
            KGLog(@"authenticateWithGameCenter ???");
            //[self disableGameCenter];
            [self loadGamePage:YES]; // Authenticated or not load the game for the current (default) user
        }
    };
}

- (void)resetDefaultUser {
    currentUserId = (NSString *)DEFAULT_CACHE_USER;
    currentUserCache = [self getUserCacheFolder:currentUserId];
    currentUserDisplayName = (NSString *)DEFAULT_USER_DISPLAY_NAME;
}

- (void)updateCurrentGameCenterUser {
    //currentUserId = [[GKLocalPlayer localPlayer].playerID stringByReplacingOccurrencesOfString:@":" withString:@""];
    NSString *newGameCenterUser = [GKLocalPlayer localPlayer].playerID;
    KGLog(@"GC users new=%@", newGameCenterUser);
//    BOOL changingGameCenterUser = (currentUserId != nil
//            && ![currentUserId isEqualToString:CACHE_USER]
//            && ![currentUserId isEqualToString:newGameCenterUser]);
    BOOL changingGameCenterUser = (currentUserId == nil || ![currentUserId isEqualToString:newGameCenterUser]);
    if (newGameCenterUser == nil) {
        [self resetDefaultUser];
    } else {
        currentUserId = newGameCenterUser;
        currentUserCache = [self getUserCacheFolder:currentUserId];
        currentUserDisplayName = [GKLocalPlayer localPlayer].alias;
    }
    if (changingGameCenterUser) {
        KGLog(@"Changing GC user %@", currentUserId);
        [self loadGamePage:YES];
    }
    if (![KGCUtils isEmptyString:currentUserId] && ![currentUserId isEqualToString:(NSString *)DEFAULT_CACHE_USER]) {
        // Leaderboards have to be refreshed even for the same user, as long as it a valid GameCenter player
        [self loadLeaderboards];
        [self loadAchievements];
    }
}

- (void)displayGameCenter {
    GKGameCenterViewController *gameCenterController = [[GKGameCenterViewController alloc] init];
    if (gameCenterController != nil) {
        gameCenterController.gameCenterDelegate = self;
        gameCenterController.viewState = GKGameCenterViewControllerStateLeaderboards;
        [self presentViewController:gameCenterController animated:YES completion:^() {
            //[webViewManager sendMessage:RESPONSE_GAME_CENTER_CLOSED];
        }];
        [self loadLeaderboards];
    }
}

- (void)gameCenterViewControllerDidFinish:(GKGameCenterViewController *)gameCenterViewController {
//    [gameCenterViewController.presentingViewController dismissViewControllerAnimated:YES completion:^(void){}];
//    [gameCenterViewController.presentedViewController dismissViewControllerAnimated:YES completion:^(void){}];
    [gameCenterViewController dismissViewControllerAnimated:YES completion:^(void){}];
    [webViewManager sendMessage:(NSString *)RESPONSE_GAME_CENTER_CLOSED];
}

- (void)updateLeaderboards:(NSDictionary *)values {
    if (currentUserLeaderboards != nil) {
        NSMutableArray *scores = [[NSMutableArray alloc] init];
        for (GKLeaderboard *leaderboard in currentUserLeaderboards) {
            NSString *shortId = [leaderboard.identifier stringByReplacingOccurrencesOfString:(NSString *)LEADERBOARD_ID_BASE withString:@"" options:NSCaseInsensitiveSearch range:NSMakeRange(0, LEADERBOARD_ID_BASE.length)];
            NSString *value = [values objectForKey:shortId];
            if (value != nil) {
                GKScore *score = [[GKScore alloc] initWithLeaderboardIdentifier:leaderboard.identifier forPlayer:currentUserId];
                score.context = 0;
                score.value = [value intValue];
                [scores addObject:score];
            }
        }
        if (scores.count > 0) {
            [GKScore reportScores:scores withCompletionHandler:^(NSError *error) {
                if (error != nil) {
                    KGLog(@"Error updating leaderboard: %@", error);
                }
            }];
        }
    }
}

- (int)getLeaderboardValue:(NSString *)tokenName {
    NSString *leaderboardId = [self getLeaderboardId:tokenName];
//    if (currentUserLeaderboards == nil) {
//        [self loadLeaderboardInfo];
//    }
    if (currentUserLeaderboards != nil) {
        for (int i = (int)currentUserLeaderboards.count - 1; i >= 0; i--) {
            GKLeaderboard *leaderboard = [currentUserLeaderboards objectAtIndex:i];
            if ([leaderboard.identifier isEqualToString:leaderboardId]) {
                GKScore *score = leaderboard.localPlayerScore;
                return (int)score.value;
            }
        }
    }
    return 0;
}

- (NSDictionary *)getLeaderboardValues {
    NSMutableDictionary *leaderboards = [[NSMutableDictionary alloc] init];
    if (currentUserLeaderboards != nil) {
        for (int i = (int)currentUserLeaderboards.count - 1; i >= 0; i--) {
            GKLeaderboard *leaderboard = [currentUserLeaderboards objectAtIndex:i];
            GKScore *score = leaderboard.localPlayerScore;
            [leaderboards setObject:[NSNumber numberWithLongLong:score.value] forKey:[self getLeaderboardName:leaderboard.identifier]];
        }
    }
    return leaderboards;
}

- (NSDictionary *)getLeaderboardValues:(id)message {
    NSArray *names;
    if ([message isKindOfClass:[NSArray class]]) {
        names = message;
    } else {
        names = [(NSDictionary *) message allKeys];
    }

    NSMutableDictionary *leaderboards = nil;
    if (currentUserLeaderboards != nil) {
        leaderboards = [[NSMutableDictionary alloc] init];
        for (GKLeaderboard *leaderboard in currentUserLeaderboards) {
            NSString *lbName = [self getLeaderboardName:leaderboard.identifier];
            for (NSString *name in names) {
                if ([name isEqualToString:lbName]) {
                    GKScore *score = leaderboard.localPlayerScore;
                    [leaderboards setObject:[NSNumber numberWithLongLong:score.value] forKey:name];
                    break;
                }
            }
        }
    }
    return leaderboards;
}

- (NSString *)getLeaderboardId:(NSString *)tokenName {
    return [NSString stringWithFormat:@"%@%@", LEADERBOARD_ID_BASE, tokenName];
}

- (NSString *)getLeaderboardName:(NSString *)leaderboardId {
    return [leaderboardId stringByReplacingOccurrencesOfString:(NSString *)LEADERBOARD_ID_BASE withString:@"" options:NSCaseInsensitiveSearch range:NSMakeRange(0, ACHIEVEMENT_ID_BASE.length)];
}

- (NSString *)getAchievementId:(NSString *)tokenName {
    return [NSString stringWithFormat:@"%@%@", ACHIEVEMENT_ID_BASE, tokenName];
}

- (NSString *)getAchievementName:(NSString *)achievementId {
    return [achievementId stringByReplacingOccurrencesOfString:(NSString *)ACHIEVEMENT_ID_BASE withString:@"" options:NSCaseInsensitiveSearch range:NSMakeRange(0, ACHIEVEMENT_ID_BASE.length)];
}

- (NSString *)checkGameCenter {
    NSMutableArray *sb = [[NSMutableArray alloc] init];
    [sb addObject:@"{"];
    GKLocalPlayer *player = [GKLocalPlayer localPlayer];
    if (player.isAuthenticated) {
        [sb addObject:[NSString stringWithFormat:@"\"userId\":\"%@\",\"alias\":\"%@\"", player.playerID, player.alias]];
    }
    [sb addObject:@"}"];
    return [sb componentsJoinedByString:@""];
}

- (NSString *)postSocial:(NSString *)service message:(NSString *)message {
    if (![SLComposeViewController isAvailableForServiceType:service]) {
        // The device must be linked to a social service in device's settings for this feature to work
        KGLog(@"Service %@ not available", service);
        return (NSString *)RESPONSE_FAILURE;
    }
    socialController = [[SLComposeViewController alloc] init]; //initiate the Social Controller
    socialController = [SLComposeViewController composeViewControllerForServiceType:service];
    [socialController setInitialText:message]; //the message you want to post
    //[mySLComposerSheet addImage:yourimage]; //an image you could post
    //for more instance methodes, go here:https://developer.apple.com/library/ios/#documentation/NetworkingInternet/Reference/SLComposeViewController_Class/Reference/Reference.html#//apple_ref/doc/uid/TP40012205
    [self presentViewController:socialController animated:YES completion:nil];

    [socialController setCompletionHandler:^(SLComposeViewControllerResult result) {
        NSString *output;
        switch (result) {
            case SLComposeViewControllerResultCancelled:
                output = @"Action Cancelled";
                break;
            case SLComposeViewControllerResultDone:
                output = @"Post Successfull";
                break;
            default:
                break;
        } //check if everythink worked properly. Give out a message on the state.
        //UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Facebook" message:output delegate:nil cancelButtonTitle:@"Ok" otherButtonTitles:nil];
        //[alert show];
        KGLog(@"Social Post result: %@", output);
    }];
    return (NSString *)RESPONSE_OK;
}

- (NSString *)postFacebook:(id)message {
    return [self postSocial:SLServiceTypeFacebook message:[self extractFrom:message field:@"message"]];
}

- (NSString *)postTwitter:(id)message {
    return [self postSocial:SLServiceTypeTwitter message:[self extractFrom:message field:@"message"]];
}

- (void)postEmail:(id)message {
    NSString *subject = [self extractFrom:message field:@"subject"];
    if (subject == nil) {
        subject = @"Word Clues Feedback";
    }
    NSString *body = [self extractFrom:message field:@"body"];
    if (body == nil) {
        body = @"";
    }
    NSString *url = [NSString stringWithFormat:@"mailto:%@?subject=%@&body=%@",
                    FEEDBACK_EMAIL,
                    [subject stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding],
                    [body stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    [self openUrl:url];
}

- (NSString *)extractFrom:(id)message field:(NSString *)field {
    NSDictionary *dict = message;
    return [dict objectForKey:field];
}

- (void)linkStore {
    [self openUrl:@"https://itunes.apple.com/us/app/word-clues-match-game-70s/id770384948?ls=1&mt=8"];
}

- (void)linkTwitter {
    if ([self canOpenTwitterApp]) {
        [self openUrl:@"twitter://user?screen_name=kindredgames"];
    } else {
        [self openUrl:@"https://twitter.com/kindredgames"];
    }
}

- (void)linkFacebook {
    if ([self canOpenFacebookApp]) {
        [self openUrl:@"fb://profile/kindredgames"];
    } else {
        [self openUrl:@"https://www.facebook.com/"];
    }
}

- (BOOL)canOpenFacebookApp {
    return [[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"fb://"]];
}

- (BOOL)canOpenTwitterApp {
    return [[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"twitter://"]];
}

- (void)linkCompany {
    [self openUrl:@"http://www.kindredgames.com"];
}

- (void)openUrl:(NSString *)url {
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
}

- (NSString *)canMakePayments {
    return [storeManager isEnabled] ? @"{\"storeEnabled\":true}" : @"{\"storeEnabled\":false}";
}

- (void)price:(NSDictionary *)invoice {
    if ([storeManager isEnabled]) {
        NSArray *products = (NSArray *)[invoice objectForKey:@"productIds"];
        if (products != nil) {
            [storeManager prices:products];
        } else {
            [storeManager price:(NSString *)[invoice objectForKey:@"productId"]];
        }
    } else {
        KGLog(@"Payments are disabled");
    }
}

- (void)buy:(NSDictionary *)invoice {
    if ([storeManager isEnabled]) {
        [storeManager buy:[invoice objectForKey:@"productId"]];
    } else {
        KGLog(@"Payments are disabled");
    }
}

- (NSString *)owns:(id)productIdMessage {
    if ([productIdMessage isKindOfClass:[NSDictionary class]]) {
        NSDictionary *dict = productIdMessage;
        NSString *productId = [dict objectForKey:@"productId"];
        if (productId != nil) {
            return [cache get:[self getProductCacheName:productId] forUser:@""];
        }
    }
    return nil;
}

- (void)restorePurchases {
    if ([storeManager isEnabled]) {
        [storeManager restorePurchases];
    }
}

- (void)runTest {
}

- (void)acceptPayment:(NSString *)productId receipt:(NSString *)receipt {
    [cache set:[self getProductCacheName:productId] value:receipt forUser:@""];
}

- (NSString *)getProductCacheName:(NSString *)productId {
    return [NSString stringWithFormat:@"product_%@", productId];
}

- (void)sendMessageObject:(NSDictionary *)response {
    [webViewManager sendMessageObject:response];
}

- (NSDictionary *)getAchievementValues
{
    NSMutableDictionary *achievements = nil;
    if (self.currentUserAchievements != nil) {
        achievements = [[NSMutableDictionary alloc] init];
        NSArray *ids = [self.currentUserAchievements allKeys];
        for (NSString *id in ids) {
            GKAchievement *achievement = (GKAchievement *)[self.currentUserAchievements objectForKey:id];
            if (achievement) {
                NSMutableDictionary *node = [[NSMutableDictionary alloc] init];
                [node setObject:[NSNumber numberWithDouble:achievement.percentComplete] forKey:@"value"];
                [achievements setObject:node forKey:id];
            }
        }
    }
    return achievements;
}

- (void)updateAchievements:(id)message
{
    NSDictionary *list = (NSDictionary *)message;
    NSArray *ids = [list allKeys];
    NSMutableArray *achievements = [[NSMutableArray alloc] initWithCapacity:ids.count];
    for (NSString *localId in ids) {
        NSDictionary *doc = [list objectForKey:localId];
        NSString *id = [self getAchievementId:localId];
        GKAchievement *achievement = [[GKAchievement alloc] initWithIdentifier:id];
        if (achievement) {
            achievement.percentComplete = [(NSString *) [doc objectForKey:@"value"] doubleValue];

            [achievements addObject:achievement];
            [GKAchievement reportAchievements:achievements withCompletionHandler:^(NSError *error)
            {
                if (error != nil) {
                    KGLog(@"Error in reporting achievements: %@", error);
                }
            }];
        }
    }
}

- (void)loadAchievements
{
    [GKAchievement loadAchievementsWithCompletionHandler:^(NSArray *achievements, NSError *error)
    {
        if (error == nil) {
            for (GKAchievement* achievement in achievements) {
                [self.currentUserAchievements setObject:achievement forKey:[self getAchievementName:achievement.identifier]];
            }
        }
    }];
}

@end
