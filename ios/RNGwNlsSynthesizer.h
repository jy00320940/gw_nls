//
//  RNGwNlsSynthesizer.h
//  RNGwNls
//
//  Created by LiYang on 2019/8/2.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AliyunNlsSdk/NlsSpeechSynthesizerRequest.h"
#import "AliyunNlsSdk/SynthesizerRequestParam.h"
#import "AliyunNlsSdk/AliyunNlsClientAdaptor.h"

#import "RNGwNlsPlayAudio.h"
NS_ASSUME_NONNULL_BEGIN

@interface RNGwNlsSynthesizer : NSObject
@property(nonatomic,strong) NlsClientAdaptor *nlsClient;
@property(nonatomic,strong) NlsSpeechSynthesizerRequest *synthesizerRequest;
@property(nonatomic,strong) SynthesizerRequestParam *requestParam;
@property(nonatomic,strong) RNGwNlsPlayAudio *nlsAudioPlayer;
-(void)startSynthesizerWithToken:(NSString *)token AppKey:(NSString *)appKey;
-(void)setText:(NSString *)text;
-(void)stop;
@end

NS_ASSUME_NONNULL_END
