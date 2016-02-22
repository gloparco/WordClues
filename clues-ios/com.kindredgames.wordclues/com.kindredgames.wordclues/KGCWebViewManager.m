#import "KGCWebViewManager.h"
#import "KGCResponseData.h"
#import "KGCGameController.h"
#import "KGCUtils.h"
#import "KGCDefine.h"

const int TOPIC_COMMAND_INDEX = 0;
const int TOPIC_FIRST_OPTION_INDEX = 1;
const int TOPIC_SECOND_OPTION_INDEX = 2;
const int TOPIC_THIRD_OPTION_INDEX = 3;

//const int NO_CALLBACK = 0;

@interface KGCWebViewManager ()

@property (nonatomic) BOOL loaded;
@property (nonatomic) NSMutableArray *earlyMessages;

@end


@implementation KGCWebViewManager

//@synthesize webView;
static const NSString *RESPONSE_OK = @"{\"ok\":1}";
static const NSString *RESPONSE_FAILURE = @"{\"ok\":0}";


- (KGCWebViewManager *)initWithView:(UIWebView *)delegatedWebView controller:(UIViewController <KGCGameController> *)$controller {
    self = [super init];
    if (self) {
        // Custom initialization
        self.loaded = NO;
        self.earlyMessages = [[NSMutableArray alloc] init];
        controller = $controller;
        responder = self;

        webView = delegatedWebView;
        webView.delegate = self;
    }
    return self;
}

#pragma mark - UIWebView delegates

- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error {
    if (error.code == NSURLErrorCancelled) {
        KGLog(@"webView cancelled previous loading. Not a problem");
    } else {
        LogError(@"webView didFailLoadWithError: %@", error);
    }
}

- (void)webViewDidFinishLoad:(UIWebView *)webView {
    KGLog(@"Web View Did finish loading");
    [self switchToWebView];
    self.loaded = YES;
    for (NSString *message in self.earlyMessages) {
        KGLog(@"Send early message %@", message);
        [self sendMessage:message];
    }
    [self.earlyMessages removeAllObjects];
    [controller webViewDidLoad];
}

- (void)switchToWebView {
    [controller.view addSubview:webView];
    [controller.view bringSubviewToFront:webView];
}


// This function is called on all location change :
- (BOOL)webView:(UIWebView *)webView2 shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType {
    return [self processUrlRequest:request];
}


#pragma mark - KGCUrlRespondDelegate

- (void)respondWithJavaScript:(NSString *)response {
    [webView stringByEvaluatingJavaScriptFromString:response];
}

//-(void) switchToTester {
//    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Switch to Tester"
//                                                    message:@"Initiated"
//                                                   delegate:nil
//                                          cancelButtonTitle:@"OK"
//                                          otherButtonTitles:nil];
//    [alert show];
//}

#pragma mark - Message handling methods

- (BOOL)processUrlRequest:(NSURLRequest *)request {
    NSString *requestString = [[request URL] absoluteString];
    return [self processUrlPath:requestString];
}

- (BOOL)processUrlPath:(NSString *)path {
    NSString *requestString = path;

    if ([requestString hasPrefix:@"wmw:"]) {

        NSArray *components = [requestString componentsSeparatedByString:@":"];

        // Topic format: command.name/do|destination/some/other/parameters
        NSString *topic = (NSString *) [components objectAtIndex:1];
        NSArray *topicParams = [topic componentsSeparatedByString:@"."];

        int callbackId = [((NSString *) [components objectAtIndex:2]) intValue];
        NSString *argsAsString = [(NSString *) [components objectAtIndex:3] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        NSString *trimmedString = [argsAsString stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
        KGLog(@"Received JS topic=[%@] message[%ld]: %@", topic, (unsigned long)trimmedString.length, trimmedString);

        [self handleCall:topic topicParams:topicParams callbackId:callbackId message:trimmedString];
        return NO;
    }

    // Accept this location change
    return YES;
}

// Implements all you native function in this one, by matching 'topic' and parsing 'messageJson'
// Use 'callbackId' with 'returnResult' selector when you get some results to send back to javascript
- (void)handleCall:(NSString *)topic topicParams:(NSArray *)topicParams callbackId:(int)callbackId message:(NSString *)messageJson {
    NSString *response = nil;
    NSString *command = topicParams[TOPIC_COMMAND_INDEX];
    NSString *firstOption = topicParams.count - 1 >= TOPIC_FIRST_OPTION_INDEX ? topicParams[TOPIC_FIRST_OPTION_INDEX] : nil;
    NSString *function = firstOption; // only for readability
    NSString *secondOption = topicParams.count - 1 >= TOPIC_SECOND_OPTION_INDEX ? topicParams[TOPIC_SECOND_OPTION_INDEX] : nil;
    NSString *functionArg = secondOption; // only for readability
    //NSString *thirdOption = topicParams.count - 1 >= TOPIC_THIRD_OPTION_INDEX ? topicParams[TOPIC_THIRD_OPTION_INDEX] : nil;
    //NSString *functionSecondArg = thirdOption; // only for readability

    if ([command isEqualToString:@"get"]) {
        response = [controller getUserCache:firstOption];
    } else if ([command isEqualToString:@"set"]) {
        response = [controller setUserCache:firstOption data:messageJson];
//    } else if ([topic isEqualToString:@"login.GameCenter/do"]) {
//        response = [self respondLoginGameCenter:messageJson newTopic:&newTopic callbackId:&callbackId];
    } else if ([command isEqualToString:@"generate"]) {
        if ([function isEqualToString:@"game"]) {
            response = [controller generateGames:1];
            [controller setUserCache:@"game" data:response];
        } else if ([function isEqualToString:@"games"]) {
            response = [controller generateGames:[functionArg intValue]];
            [controller setUserCache:@"game" data:response];
        }
    } else if ([command isEqualToString:@"post"]) {
        if ([function isEqualToString:@"facebook"]) {
            response = [controller postFacebook:[KGCUtils jsonData:messageJson]];
        } else if ([function isEqualToString:@"twitter"]) {
            response = [controller postTwitter:[KGCUtils jsonData:messageJson]];
        }
    } else if ([command isEqualToString:@"display"]) {
        if ([function isEqualToString:@"gamecenter"]) {
            [controller displayGameCenter];
        }
    } else if ([command isEqualToString:@"link"]) {
        if ([function isEqualToString:@"store"]) {
            [controller linkStore];
        } else if ([function isEqualToString:@"twitter"]) {
            [controller linkTwitter];
        } else if ([function isEqualToString:@"facebook"]) {
            [controller linkFacebook];
        } else if ([function isEqualToString:@"company"]) {
            [controller linkCompany];
        } else if ([function isEqualToString:@"email"]) {
            [controller postEmail:[KGCUtils jsonData:messageJson]];
        }
    } else if ([command isEqualToString:@"store"]) {
        if ([function isEqualToString:@"enabled"]) {
            response = [controller canMakePayments];
        } else if ([function isEqualToString:@"price"]) {
            [controller price:[KGCUtils jsonData:messageJson]];
        } else if ([function isEqualToString:@"buy"]) {
            [controller buy:[KGCUtils jsonData:messageJson]];
        } else if ([function isEqualToString:@"owns"]) {
            response = [controller owns:[KGCUtils jsonData:messageJson]];
        } else if ([function isEqualToString:@"restore"]) {
            [controller restorePurchases];
        }
    } else if ([command isEqualToString:@"leaderboard"]) {
        if ([function isEqualToString:@"get"]) {
            response = [NSString stringWithFormat:@"{\"value\":\"%d\"}", [controller getLeaderboardValue:functionArg]];
        } else if ([function isEqualToString:@"set"]) {
            [controller updateLeaderboards:[KGCUtils jsonData:messageJson]];
        }
    } else if ([command isEqualToString:@"leaderboards"]) {
        if ([function isEqualToString:@"get"]) {
            response = [KGCUtils dataToJson:[controller getLeaderboardValues:[KGCUtils jsonData:messageJson]]];
            //response = [self dataToJson:[controller getLeaderboardValues]];
        } else if ([function isEqualToString:@"set"]) {
            [controller updateLeaderboards:[KGCUtils jsonData:messageJson]];
        }
    } else if ([command isEqualToString:@"achievements"]) {
        if ([function isEqualToString:@"get"]) {
            response = [KGCUtils dataToJson:[controller getAchievementValues]];
        } else if ([function isEqualToString:@"set"]) {
            [controller updateAchievements:[KGCUtils jsonData:messageJson]];
        }
    } else if ([command isEqualToString:@"check"]) {
        if ([function isEqualToString:@"gamecenter"]) {
            response = [controller checkGameCenter];
        } else if ([function isEqualToString:@"facebook"]) {
            response = [controller canOpenFacebookApp] ? (NSString *)RESPONSE_OK : (NSString *)RESPONSE_FAILURE;
        } else if ([function isEqualToString:@"twitter"]) {
            response = [controller canOpenTwitterApp] ? (NSString *)RESPONSE_OK : (NSString *)RESPONSE_FAILURE;
        }
    } else if ([command isEqualToString:@"error"]) {
        NSDictionary *errorMessage = [KGCUtils jsonData:messageJson];
        LogError(@"JS Error: %@", [errorMessage objectForKey:@"message"]);
    } else {
        KGLog(@"Unsupported topic: '%@'", topic);
        return;
    }

//    if (callbackId > 0 && response != null) {
    if (callbackId > 0) { // Null is also a valid response
        KGLog(@"Returning to JS response: '%@'", response);
        [self returnResult:callbackId topic:topic json:response];
    }
}

- (NSMutableDictionary *)createMessageResponse:(NSString *)topic {
    NSMutableDictionary *response = [[NSMutableDictionary alloc] init];
    [response setObject:topic forKey:@"topic"];
    return response;
}

- (NSString *)formatStringParameter:(NSString *)param {
    //TODO: need to cancel '
    return param == nil ? @"null" : [NSString stringWithFormat:@"'%@'", [param stringByReplacingOccurrencesOfString:@"'" withString:@"\\'"]];
}

- (void)returnResult:(int)callbackId topic:(NSString *)topic json:(NSString *)json {
    KGLog(@"Response to JS: topic=%@: %@", topic, json);
    [responder respondWithJavaScript:[NSString stringWithFormat:@"GAME.bridge.resultForCallback(%d,[%@,%@]);", callbackId, [self formatStringParameter:topic], [self formatStringParameter:json]]];
}

- (void)returnResultResponse:(KGCResponseData *)response {
    if (![[NSThread currentThread] isMainThread]) {
        KGLog(@"Response to JS - switching to main thread");
        [self performSelectorOnMainThread:@selector(returnResultResponse:) withObject:response waitUntilDone:false];
        return;
    }
    KGLog(@"Response to JS: topic=%@: %@", response->topic, response->json);
    [responder respondWithJavaScript:[NSString stringWithFormat:@"GAME.bridge.resultForCallback(%d,[%@,%@]);",
                    response->callbackId,
                    [self formatStringParameter:response->topic],
                    [self formatStringParameter:response->json]]];
}

- (void)sendMessage:(NSString *)response {
    if (![[NSThread currentThread] isMainThread]) {
        KGLog(@"Message to JS - switching to main thread");
        [self performSelectorOnMainThread:@selector(sendMessage:) withObject:response waitUntilDone:false];
        return;
    }
    if (self.loaded) {
        KGLog(@"Message to JS: %@", response);
        [responder respondWithJavaScript:[NSString stringWithFormat:@"GAME.bridge.response(%@,%@);",
                                                                    [self formatStringParameter:nil],
                                                                    [self formatStringParameter:response]]];
    } else {
        KGLog(@"Cached message to JS: %@", response);
        [self.earlyMessages addObject:response];
    }
}

- (void)sendMessageObject:(NSDictionary *)response {
    [self sendMessage:[KGCUtils jsonWithDictionary:response]];
}


#pragma mark - Respond methods

//- (void) clientLog:(NSString *)topic message:(NSString*)jsonString
//{
//    NSDictionary *message = [self parseMessage:jsonString];
//    NSString *logLevel = [[topic substringWithRange:[topicLogLevel rangeOfFirstMatchInString:topic options:0 range:NSMakeRange(0, [topic length])]] uppercaseString];
//    KGLog(@"JS %@: %@", logLevel, message[@"message"]);
//}

//-(void) responseFromCache:(NSMutableDictionary *)response forKey:(NSString *)key
//{
//    //TODO: keep current userId in a separate cache
//    NSString *cachedValue = (NSString *)[KGCCacheFile get:key forUser:currentUserId];
//    if (cachedValue) {
//        [response setObject:cachedValue forKey:key];
//    }
//}

//-(void) parseCacheTopic:(NSArray *)topicParams cacheName:(NSString**)cacheName userId:(NSString**)userId {
//    *cacheName = @"cache";
//    *userId = currentUserId;
//    if (topicParams.count > TOPIC_FIRST_OPTION_INDEX && topicParams[TOPIC_FIRST_OPTION_INDEX]) {
//        *cacheName = topicParams[TOPIC_FIRST_OPTION_INDEX];
//        if (topicParams.count > TOPIC_FIRST_OPTION_INDEX + 1 && topicParams[TOPIC_FIRST_OPTION_INDEX]) {
//            *userId = topicParams[TOPIC_FIRST_OPTION_INDEX + 1];
//        }
//    }
//}

@end
