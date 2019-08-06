//
//  RNGwNlsSynthesizer.m
//  RNGwNls
//
//  Created by LiYang on 2019/8/2.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "RNGwNlsSynthesizer.h"

@interface RNGwNlsSynthesizer()<NlsSpeechSynthesizerDelegate,NLSPlayAudioDelegate>

@end

@implementation RNGwNlsSynthesizer
-(id)init{
    self = [super init];
    if(self){
        //1. 全局参数初始化操作
        //1.1 初始化语音合成客户端
        _nlsClient = [[NlsClientAdaptor alloc]init];
        
        //1.3 初始化合成参数类
        _requestParam = [[SynthesizerRequestParam alloc]init];
        //1.4 设置log级别
        [_nlsClient setLog:NULL logLevel:LOGINFO];
    }
    return self;
}

-(void)startSynthesizerWithToken:(NSString *)token AppKey:(NSString *)appKey{
    
    //2. 创建请求对象和开始语音合成
    if(_synthesizerRequest!= NULL){
        _synthesizerRequest = NULL;
    }
    //2.1 初始化语音播放类
    [_nlsAudioPlayer cleanup];
    _nlsAudioPlayer = [[RNGwNlsPlayAudio alloc]init];
    _nlsAudioPlayer.delegate = self;
    
    //2.2 创建请求对象，设置NlsSpeechSynthesizerRequest回调
    _synthesizerRequest = [_nlsClient createSynthesizerRequest];
    _synthesizerRequest.delegate = self;
    
    //2.4 设置SynthesizerRequestParam请求参数
    [_requestParam setFormat:@"pcm"];
    //Demo 仅提供了pcm格式的合成和播放。pcm格式的语音消耗流量较大，您可以自行实现mp3格式的合成，需要您自行解决mp3解码部分的实现。
    
    //请使用https://help.aliyun.com/document_detail/72153.html 动态生成token
    // <AccessKeyId> <AccessKeySecret> 请使用您的阿里云账户生成 https://ak-console.aliyun.com/
    [_requestParam setToken:token];
    //请使用阿里云语音服务管控台(https://nls-portal.console.aliyun.com/)生成您的appkey
    [_requestParam setAppkey:appKey];
}

-(void)setText:(NSString *)text {
    [_requestParam setText:text];
    //2.5 传入请求参数
    [_synthesizerRequest setSynthesizerParams:_requestParam];
    //2.6 开始语音合成
    [_synthesizerRequest start];
}

-(void)stop{
    [_synthesizerRequest cancel];
    [_nlsAudioPlayer cleanup];
}

/**
 *3. NlsSpeechSynthesizerDelegate接口回调
 */

//3.1 本次请求失败
- (void)OnTaskFailed:(NlsDelegateEvent)event statusCode:(NSString *)statusCode errorMessage:(NSString *)eMsg {
    NSLog(@"OnTaskFailed, statusCode is: %@ error message ：%@",statusCode,eMsg);
}

//3.2 服务端连接关闭
- (void)OnChannelClosed:(NlsDelegateEvent)event statusCode:(NSString *)statusCode errorMessage:(NSString *)eMsg {
    NSLog(@"OnChannelClosed, statusCode is: %@",statusCode);
}

//3.3 回调合成语音数据，通过NlsAudioPlayer工具播放
- (void)OnBinaryDataReceived:(NlsDelegateEvent)event voiceData:(Byte *)data length:(NSInteger)length{
    NSLog(@"Received voice data length %lu", length);
    [_nlsAudioPlayer process:data length:length];
}

//3.4 合成结束
- (void)OnSynthesizerCompleted:(NlsDelegateEvent)event result:(NSString *)result statusCode:(NSString *)statusCode errorMessage:(NSString *)eMsg {
    [_nlsAudioPlayer finishFeed:NO];
}

//3.5 合成开始
- (void)OnSynthesizerStarted:(NlsDelegateEvent)event result:(NSString *)result statusCode:(NSString *)statusCode errorMessage:(NSString *)eMsg {
}


- (void)playDone {
    //播放结束的回调
    //需要在OnSynthesizerCompleted 中调用[_nlsAudioPlayer finishFeed:YES];
}
@end
