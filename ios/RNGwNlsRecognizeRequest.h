//
//  RNGwNlsRecognizeRequest.h
//  RNGwNls
//
//  Created by LiYang on 2019/7/31.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "AliyunNlsSdk/NlsSpeechRecognizerRequest.h"
#import "AliyunNlsSdk/RecognizerRequestParam.h"
#import "AliyunNlsSdk/AliyunNlsClientAdaptor.h"

#import <React/RCTBridge.h>
#import <React/RCTConvert.h>
#import <React/RCTLog.h>
#import <React/RCTUIManager.h>
#import <React/RCTUtils.h>
NS_ASSUME_NONNULL_BEGIN

@interface RNGwNlsRecognizeRequest : NSObject
@property(nonatomic,strong) NlsClientAdaptor *nlsClient;
@property(nonatomic,strong) NlsSpeechRecognizerRequestwithRecorder *recognizeRequest;
@property(nonatomic,strong) RecognizerRequestParam *recognizeRequestParam;

- (instancetype)initWithToken:(NSString *)token AppKey:(NSString *)appkey Bridge:(RCTBridge *)bridge;
- (void)start;
- (void)stop;
@end

NS_ASSUME_NONNULL_END
