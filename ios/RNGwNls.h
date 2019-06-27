
#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

#import "AliyunNlsSdk/NlsSpeechRecognizerRequest.h"
#import "AliyunNlsSdk/RecognizerRequestParam.h"
#import "AliyunNlsSdk/AliyunNlsClientAdaptor.h"

@interface RNGwNls : NSObject <RCTBridgeModule>
@property(nonatomic,strong) NlsClientAdaptor *nlsClient;
@property(nonatomic,strong) NlsSpeechRecognizerRequestwithRecorder *recognizeRequest;
@property(nonatomic,strong) RecognizerRequestParam *recognizeRequestParam;
@end
  
