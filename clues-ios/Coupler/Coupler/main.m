//
//  main.m
//  Coupler
//
//  Created by Andrei on 11/30/13.
//  Copyright (c) 2013 Kindredgames Inc. All rights reserved.
//

#import "WordCouplesGenerator.h"
#import "WorldCitiesGenerator.h"
#import "Tests.h"

int createWordClues(int argc, const char * argv[]);
int createWorldCities(int argc, const char * argv[]);

int main(int argc, const char * argv[])
{

    @autoreleasepool {
        //createWordClues(argc, argv);
        createWorldCities(argc, argv);

        //[Tests runTests];
    }

    return 0;
}


int createWordClues(int argc, const char * argv[]) {

    if (argc < 2) {
        NSLog(@"Usage: Coupler InputFileName [OutputFileName]");
        return 0;
    }

    // Read file
    NSString *inFileName = [NSString stringWithCString:argv[1] encoding:NSASCIIStringEncoding];
    NSString *outFileName = argc > 2 ? [NSString stringWithCString:argv[2] encoding:NSASCIIStringEncoding] : [inFileName stringByAppendingString:@"%d.json"];
    NSString *outLinesFileName = argc > 3 ? [NSString stringWithCString:argv[3] encoding:NSASCIIStringEncoding] : [inFileName stringByAppendingString:@"Lines.txt"];

    WordCouplesGenerator *generator = [[WordCouplesGenerator alloc] init];
    [generator createWordCluesFromFile:inFileName outFile:outFileName outLines:outLinesFileName];
    return 0;
}

int createWorldCities(int argc, const char * argv[]) {

    if (argc < 2) {
        NSLog(@"Usage: Coupler InputFileName [OutputFileName]");
        return 0;
    }

    // Read file
    NSString *inFileName = [NSString stringWithCString:argv[1] encoding:NSASCIIStringEncoding];
    NSString *outFileName = argc > 2 ? [NSString stringWithCString:argv[2] encoding:NSASCIIStringEncoding] : [inFileName stringByAppendingString:@"%d.json"];
    NSString *outLinesFileName = argc > 3 ? [NSString stringWithCString:argv[3] encoding:NSASCIIStringEncoding] : [inFileName stringByAppendingString:@"Lines.txt"];

    WorldCitiesGenerator *generator = [[WorldCitiesGenerator alloc] init];
    [generator createWordCluesFromFile:inFileName outFile:outFileName outLines:outLinesFileName];
    return 0;
}
