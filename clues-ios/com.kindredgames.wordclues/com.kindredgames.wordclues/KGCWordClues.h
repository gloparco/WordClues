//
// Created by Andrei on 12/11/13.
// Copyright (c) 2013 Kindred Games Inc. All rights reserved.
//


#import <Foundation/Foundation.h>

#import "KGCCacheController.h"

@interface KGCWordClues : NSObject {

@private

    /**
    * A local cache implementation. May be the file or DB-based storage implementation
    */
    NSObject <KGCCacheController> *cache;

    /**
    * Array of arrays with full answer lines. Zero-index is answer word itself, all following elements are clues
    * Same answer can be in multiple lines.
    * Loading order is important and should persists through the application life
    */
    NSMutableArray *lineSets;

    /**
     * Array with indexes in lines, built as setIndex * 100000 + lineIndex * 100 + clueIndex: slllcc
     * An answer gets as many items in the array as there are clues, so answers with more clues have more chances to be played.
     * After generating a game, clues get added to  the array, and appended to the list of played clues.
     * If new sets get added or re-added (user buys new sets or rights to play already played sets)
     * Disadvantage - impossible to synchronize played/playable sets between devices.
     * Also sequences will be different
     * Workaround: random generator may be seeded by user's account hash, then we need to store the current seed after every game generation
      *
      * The set gets generated from lines/lineSets, minus playedClues
     */
    NSHashTable *playedCluesHistory;

    /**
      * Array of pointers on strings (as keys), also used in playableDictionary.
      * Once answer is picked, use playableDictionary to pick clues from answer's list of clues indexes. Also delete 5 in total (answer set) entries for the same answer below or/and above
      * Clues indexes are also used for history
      */
    NSMutableArray *playableAnswers;

    /**
    * Global dictionary of all answers and clues, which can be played. Once less than MIN clues remains, the key-value pair and all entries in playableClues may be removed
    * Can be exported and imported directly? No. For historical purpose
    *
    */
    NSMutableDictionary *availableClues;
}

/**
* Init Word Clues game engine:
* Loads all playable sets - the free and purchased. Purchased sets may be offered for free, but get unlocked only once sufficient games were played.
* If playableClues doesn't exists, it gets created and populated with playable sets
*
*/
- (KGCWordClues *)initWithCache:(NSObject <KGCCacheController> *)cache;

/**
* Generates the required number of games, removes their clues from playableClues, saves them 
*/
- (NSString *)generateGamesJson:(int)gamesCount;
- (NSString *)generateGameJson;
- (void)releaseHistoryClues:(int)cluesCount;

@end