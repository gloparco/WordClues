//
// Created by Andrei on 12/11/13.
// Copyright (c) 2013 Kindred Games Inc. All rights reserved.
//


#import "KGCWordClues.h"
#import "KGCMainViewController.h"
#import "KGCDefine.h"

const NSString *DEFAULT_LINES_SET_FILE = @"english";
const NSString *DEFAULT_LINES_SET_FILE_EXT = @"txt";
const NSString *PLAYED_CLUES_CACHE = @"played";

const int CLUES_SIZE = 5;
const int GAME_SIZE = 10;
const int REVERSE_ROUND_CLUE_MIN_SIZE = 4;
const int MIN_LONG_CLUES_COUNT = 2;
const int LONG_CLUE_ROUNDS[MIN_LONG_CLUES_COUNT] = { 4, 9 };

@implementation KGCWordClues {

}

- (KGCWordClues *)initWithCache:(NSObject <KGCCacheController> *)$cache {
    self = [super init];
    if (self) {
        cache = $cache;
        [self loadPlayableSets];
        [self initGameState:0];
    }
    return self;
}

/**
* Loads at least the default set and any purchased
*/
- (BOOL)loadPlayableSets {
    // Free default only for the very beginning
    lineSets = [[NSMutableArray alloc] init];
    NSMutableArray *lines = [self loadLines:(NSString *)DEFAULT_LINES_SET_FILE extension:(NSString *)DEFAULT_LINES_SET_FILE_EXT];
    [lineSets addObject:lines];
    // If need to load more lines, load them and add to the main lines
    return YES;
}

- (NSMutableArray *)loadLines:(NSString *)fileName extension:(NSString *)extension {
    NSError *error = nil;
    NSString *filePath = [[NSBundle mainBundle] pathForResource:fileName ofType:extension];
    NSString *fileContents = [NSString stringWithContentsOfFile:filePath encoding:NSUnicodeStringEncoding error:&error];
    if (error != nil) {
        LogError(@"Error reading file %@: %@", filePath, error);
        return nil;
    }
    NSArray *lineTexts = [fileContents componentsSeparatedByCharactersInSet:[NSCharacterSet newlineCharacterSet]];
    NSCharacterSet *delimiters = [NSCharacterSet characterSetWithCharactersInString:@"=,"];
    NSMutableArray *lines = [[NSMutableArray alloc] init];
    for (int i = (int)lineTexts.count - 1; i >= 0; i--) {
        NSString *lineText = [lineTexts objectAtIndex:i];
        lineText = [lineText stringByReplacingOccurrencesOfString:@" " withString:@""];
        NSArray *line = [lineText componentsSeparatedByCharactersInSet:delimiters];
        if (line.count > CLUES_SIZE) {
            [lines addObject:line];
        } else {
            KGLog(@"Too short (%ld) clue line: %@", (unsigned long)line.count, lineText);
        }
    }
    return lines;
}

- (void)initGameState:(int)trimHistoricalClues {
    // We know that clues are played in CLUE_SIZE packs per answer, avoiding
    NSTimeInterval duration = [NSDate timeIntervalSinceReferenceDate];
    [self loadPlayedClues:trimHistoricalClues];
    [self generateAvailableAnswersAndClues];
    duration = [NSDate timeIntervalSinceReferenceDate] - duration;
    KGLog(@"Reloaded clues in %f", duration);
}


/**
* Loads playable clues. If no clues exists, generates a new one, populating with all playable sets.
* Use it to optionally trim history, as afterwards it has to be reloaded anyway.
*/
- (void)loadPlayedClues:(int)trimHistoricalClues {
    // There is only one file for that.
    playedCluesHistory = [[NSHashTable alloc] init];
    NSString *fileContents = [cache get:(NSString *)PLAYED_CLUES_CACHE forUser:@""];
    if (fileContents != nil) {
        NSArray *clueTexts = [fileContents componentsSeparatedByCharactersInSet:[NSCharacterSet newlineCharacterSet]];
        if (trimHistoricalClues < 0) {
            trimHistoricalClues = 0;
        }
        for (int i = trimHistoricalClues; i < clueTexts.count; i++) {
            NSString *clueText = [clueTexts objectAtIndex:i];
            if (clueText.length > 0) {
                NSNumber *clue = [[NSNumber alloc] initWithInt:[clueText intValue]];
                if (clue.intValue > 0) {
                    [playedCluesHistory addObject:clue];
                }
            }
        }
        if (trimHistoricalClues > 0) {
            NSMutableArray *trimmedHistory = [[NSMutableArray alloc] initWithArray:clueTexts copyItems:NO];
            [trimmedHistory removeObjectsInRange:NSMakeRange(0, MIN(trimHistoricalClues, clueTexts.count))];
            if (![[trimmedHistory lastObject] isEqualToString:@""]) {
                [trimmedHistory addObject:@""]; // that to always have \n at the end
            }
            [cache set:(NSString *)PLAYED_CLUES_CACHE value:[trimmedHistory componentsJoinedByString:@"\n"] forUser:@""];
        }
    }
}

- (void)generateAvailableAnswersAndClues {
    playableAnswers = [[NSMutableArray alloc] init];
    availableClues = [[NSMutableDictionary alloc] init];

    for (int iSet = 0; iSet < lineSets.count; iSet++) {
        NSMutableArray *lines = [lineSets objectAtIndex:iSet];
        for (int iLine = 0; iLine < lines.count; iLine++) {
            NSArray *line = [lines objectAtIndex:iLine];
            NSString *answer = [line objectAtIndex:0];

            NSMutableArray *clues = [availableClues objectForKey:answer];
            if (clues == nil) {
                clues = [[NSMutableArray alloc] init];
                [availableClues setObject:clues forKey:answer];
            }
            for (int iClue = 1; iClue < line.count; iClue++) {
                // we don't need clue's text here, we build a "reference" only
                NSNumber *clueIndex = [[NSNumber alloc] initWithInt:[self getClueIndexWithSet:iSet line:iLine clue:iClue]];
                if (![playedCluesHistory containsObject:clueIndex]) {
                    [clues addObject:clueIndex];
                    [playableAnswers addObject:answer];
                }
            }
        }
    }
}

- (void)saveProgress:(NSMutableArray *)playedClues {
    if (playedClues != nil && playedClues.count > 0) {
        NSString *data = [playedClues componentsJoinedByString:@""];
        [cache append:(NSString *)PLAYED_CLUES_CACHE value:data forUser:@""];
    }
}

- (NSString *)generateGamesJson:(int)gamesCount {
    @try {
        NSMutableArray *sb = [[NSMutableArray alloc] init];
        NSMutableArray *playedClues = [[NSMutableArray alloc] init];
        [sb addObject:@"["];
        int iGame = 0;
        [self checkEnoughAnswers:gamesCount * GAME_SIZE];
        while (iGame < gamesCount && playableAnswers.count >= GAME_SIZE) {
            [self generateGame:sb playedClues:playedClues];
            if ([[sb lastObject] isEqualToString:@","]) {
                // can happen when less than required games are available
                [sb removeLastObject];
            }
            iGame++;
            if (iGame < gamesCount) {
                [sb addObject:@","];
            }
        }
        if ([[sb lastObject] isEqualToString:@","]) {
            // can happen when less than required games are available
            [sb removeLastObject];
        }
        [sb addObject:@"]"];
        [self saveProgress:playedClues];
        NSString *json = [sb componentsJoinedByString:@""];
        KGLog(@"Generated json: %@", json);
        return json;
    } @catch (NSException *e) {
        LogError(@"Error generating games: %@", e);
        return nil;
    }
}

- (NSString *)generateGameJson {
    @try {
        NSMutableArray *sb = [[NSMutableArray alloc] init];
        NSMutableArray *playedClues = [[NSMutableArray alloc] init];
        [self checkEnoughAnswers:GAME_SIZE];
        [self generateGame:sb playedClues:playedClues];
        [self saveProgress:playedClues];
        NSString *json = [sb componentsJoinedByString:@""];
        KGLog(@"Generated json: %@", json);
        return json;
    } @catch (NSException *e) {
        LogError(@"Error generating games: %@", e);
        return nil;
    }
}

- (void)generateGame:(NSMutableArray *)sbGames playedClues:(NSMutableArray *)playedClues {
    // Check if we have sufficient answers and clues
    [sbGames addObject:@"["];
    int iAnswer = 0;
    NSMutableArray *longClueRounds = [[NSMutableArray alloc] init];
    NSMutableArray *shortClueRounds = [[NSMutableArray alloc] init];

    //TODO: add check if the same answer get played in the same game
    while (iAnswer < GAME_SIZE && playableAnswers.count > 0) {
        int answerIndex = arc4random() % playableAnswers.count;
        // alternatively position can be calculated within availableClues by navigating through keys - more calc intense vs. memory intense
        NSString *answer = [playableAnswers objectAtIndex:answerIndex];
        NSMutableArray *clues = [availableClues objectForKey:answer];

        if (clues.count >= CLUES_SIZE) {
            NSMutableArray *sb = [[NSMutableArray alloc] init];
            [sb addObject:[NSString stringWithFormat:@"{\"answer\":\"%@\",\"clues\":[", answer]];
            int minClueSize = NSIntegerMax;
            for (int iClue = 0; iClue < CLUES_SIZE; iClue++) {
                int clueIndex = arc4random() % clues.count;
                NSNumber *clueSetIndex = [clues objectAtIndex:clueIndex];
                NSString *clue = [self getSetClue:[clueSetIndex unsignedIntValue]];
                if (clue.length < minClueSize) {
                    minClueSize = clue.length;
                }
                [playedClues addObject:[NSString stringWithFormat:@"%@\n", clueSetIndex]];
                [sb addObject:[NSString stringWithFormat:@"\"%@\"", clue]];
                if (iClue < CLUES_SIZE - 1) {
                    [sb addObject:[NSString stringWithFormat:@","]];
                }
                // remove used clue
                [clues removeObjectAtIndex:clueIndex];
                if (answerIndex >= 0 && answerIndex < playableAnswers.count) {
                    [playableAnswers removeObjectAtIndex:answerIndex];
                    if (answerIndex >= playableAnswers.count || [playableAnswers objectAtIndex:answerIndex] != answer) {
                        answerIndex--;
                        // answers in playableAnswers are sorted sequentially and take as many spots as there are clues for the same answer.
                        // If next index doesn't contain the same answer, search for any first index with the same answer
                        if (answerIndex < 0 || [playableAnswers objectAtIndex:answerIndex] != answer) {
                            answerIndex = (int)[playableAnswers indexOfObject:answer];
                            if ((signed long)answerIndex == NSNotFound && (iClue < CLUES_SIZE - 1)) {
                                // Unless this was the last clue, we have a problem if nothing is found
                                KGLog(@"No more answers [%@](%d) in playableAnswers ", answer, iClue);
                                answerIndex = -1;
                            }
                        }
                    }
                }
            }
            [sb addObject:@"]}"];
            if (minClueSize > REVERSE_ROUND_CLUE_MIN_SIZE) {
                [longClueRounds addObject:[sb componentsJoinedByString:@""]];
            } else {
                if (longClueRounds.count < MIN_LONG_CLUES_COUNT && iAnswer >= GAME_SIZE - 1) {
                    // Not enough long clue rounds. Throw the answer away
                    iAnswer--;
                } else {
                    [shortClueRounds addObject:[sb componentsJoinedByString:@""]];
                }
            }
            iAnswer++;
//            if (iAnswer < GAME_SIZE) {
//                [sb addObject:@","];
//            }
        } else {
            // remove the useless answer
            [self removeAnswer:answer];
        }
    }
    // Now combine all rounds into a game, placing long-only clues into designated positions
    int roundsCount = longClueRounds.count + shortClueRounds.count;
    NSMutableArray *allRounds = [[NSMutableArray alloc] initWithCapacity:roundsCount];
    for (int i = 0; i < roundsCount; i++) {
        [allRounds addObject:@""];
    }
    for (int longRoundIndex = 0; longRoundIndex < MIN_LONG_CLUES_COUNT && longClueRounds.count > 0; longRoundIndex++) {
        [allRounds replaceObjectAtIndex:LONG_CLUE_ROUNDS[longRoundIndex] withObject:[longClueRounds objectAtIndex:0]];
        [longClueRounds removeObjectAtIndex:0];
    }
    [longClueRounds addObjectsFromArray:shortClueRounds];
    int allRoundsIndex = 0;
    while (longClueRounds.count > 0) {
        if (![@"" isEqualToString:[allRounds objectAtIndex:allRoundsIndex]]) {
            allRoundsIndex++;
        } else {
            int roundIndex = arc4random() % longClueRounds.count;
            [allRounds replaceObjectAtIndex:allRoundsIndex withObject:[longClueRounds objectAtIndex:roundIndex]];
            [longClueRounds removeObjectAtIndex:roundIndex];
        }
    }
    [sbGames addObject:[allRounds componentsJoinedByString:@","]];
    [sbGames addObject:@"]"];
}

- (void)checkEnoughAnswers:(int)answersCount {
    if (availableClues.count < answersCount) {
        [self initGameState:answersCount * CLUES_SIZE * 5]; // Clean enough clues for 5 more games, while providing some randomness
    }
}

- (void)releaseHistoryClues:(int)cluesCount {
    [self initGameState:cluesCount];
}

- (int)getClueIndexWithSet:(int)setIndex line:(int)lineIndex clue:(int)clueIndex {
    return clueIndex + lineIndex * 100 + setIndex * 10000;
}

- (NSString *)getSetClue:(uint)index {
    uint clueIndex = index % 100;
    uint lineIndex = (index / 100) % 1000;
    uint setIndex = index / 100000;
    NSArray *lines = [lineSets objectAtIndex:setIndex];
    NSArray *line = [lines objectAtIndex:lineIndex];
    NSString *clue = [line objectAtIndex:clueIndex]; // 0 index, reserved for the answer, was already taken into account
    return clue;
}

- (void)removeAnswer:(NSString *)answer {
    [playableAnswers removeObjectIdenticalTo:answer];
    [availableClues removeObjectForKey:answer];
}

@end