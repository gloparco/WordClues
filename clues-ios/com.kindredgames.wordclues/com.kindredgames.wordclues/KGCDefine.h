// Enables iOS logs in Debug mode only
#ifdef DEBUG_BUILD
#define KGLog( s, ... ) NSLog( @"<%@:(%d)> %@", [[NSString stringWithUTF8String:__FILE__] lastPathComponent], __LINE__, [NSString stringWithFormat:(s), ##__VA_ARGS__] )
#else
#define KGLog( s, ... )
#endif

// Errors should be reported in all modes
#define LogError( s, ...) NSLog( @"<%@:(%d)>ERROR %@", [[NSString stringWithUTF8String:__FILE__] lastPathComponent], __LINE__, [NSString stringWithFormat:(s), ##__VA_ARGS__] )