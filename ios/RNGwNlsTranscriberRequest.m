//
//  RNGwNlsTranscriberRequest.m
//  RNGwNls
//
//  Created by LiYang on 2019/7/31.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "RNGwNlsTranscriberRequest.h"


@interface RNGwNlsTranscriberRequest() <NlsSpeechTranscriberwithRecorderDelegate,NSStreamDelegate>
{
    Boolean _transcriberStarted;
    NSString *_token;
    NSString *_appkey;
    RCTBridge *_bridge;
}

@end

@implementation RNGwNlsTranscriberRequest

- (instancetype)initWithToken:(NSString *)token AppKey:(NSString *)appkey Bridge:(RCTBridge *)bridge{
    if (self = [super init]) {
        //
        //1. 全局参数初始化操作
        //1.1 初始化识别客户端,将recognizerStarted状态置为false
        _nlsClient = [[NlsClientAdaptor alloc]init];
        [_nlsClient setLog:NULL logLevel:LOGDEBUG];
        
        _transcriberStarted = false;
        //1.3 初始化识别参数类
        _transRequestParam = [[TranscriberRequestParam alloc]init];
        _token = token;
        _appkey = appkey;
        _bridge = bridge;
        
    }
    return self;
}

- (void)stop{
    [_transcriberRequest stop];
    _transcriberStarted= false;
    _transcriberRequest = NULL;
}

- (void)start {
        if (_transcriberStarted) {
            NSLog(@"already started!");
            return;
        }
        //2. 创建请求对象和开始识别
        if(_transcriberRequest!= NULL){
            [_transcriberRequest releaseRequest];
            _transcriberRequest = NULL;
        }
        //2.1 创建请求对象，设置NlsSpeechTranscriberDelegate回调
        _transcriberRequest = [_nlsClient createTranscriberRequestwithRecorder];
        _transcriberRequest.delegate = self;
    
        //2.2 设置TranscriberRequestParam请求参数
        [_transRequestParam setFormat:@"opu"];
        //返回中间识别结果
        [_transRequestParam setEnableIntermediateResult:YES];
        //设置文本规则，如识别'12345',如打开即识别为'12345',如设为NO则识别为'一二三四五'
        [_transRequestParam setEnableInverseTextNormalization:YES];
        //是否在识别结果中添加标点
        [_transRequestParam setEnablePunctuationPrediction:YES];
    
        //请使用https://help.aliyun.com/document_detail/72153.html 动态生成token
        [_transRequestParam setToken:_token];
        //或者采用本Demo的_generateTokeng方法获取token
        // <AccessKeyId> <AccessKeySecret> 请使用您的阿里云账户生成 https://ak-console.aliyun.com/
        //[_transRequestParam setToken:[self _generateToken:@"AccessKeyId" withSecret:@"AccessKeyId"]];
    
        //请使用阿里云语音服务管控台(https://nls-portal.console.aliyun.com/)生成您的appkey
        [_transRequestParam setAppkey:_appkey];
    
        //语音断句检测阈值，一句话之后静音长度超过该值，即本句结束，合法参数范围200～2000(ms)，默认值800ms
        [_transRequestParam setMaxSentenceSilence:800];
    
        //2.3 传入请求参数
        [_transcriberRequest setTranscriberParams:_transRequestParam];
    
        //2.4 启动录音和识别，将transcriberStarted置为true
        [_transcriberRequest start];
        _transcriberStarted = true;
}

//失败
- (void)OnTaskFailed:(NlsDelegateEvent)event statusCode:(NSString *)statusCode errorMessage:(NSString *)eMsg {
    _transcriberStarted= false;
    //        NSLog(@"OnTaskFailed, error message is: %@",eMsg);
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    if (statusCode && eMsg) {
        [dict setObject:statusCode forKey:@"statusCode"];
        [dict setObject:eMsg forKey:@"eMsg"];
    }
    [_bridge.eventDispatcher sendDeviceEventWithName:@"OnTaskFailed" body:dict];
}
//关闭
- (void)OnChannelClosed:(NlsDelegateEvent)event statusCode:(NSString *)statusCode errorMessage:(NSString *)eMsg {
    _transcriberStarted= false;
    //        NSLog(@"OnChannelClosed, statusCode is: %@",statusCode);
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    if (statusCode && eMsg) {
        [dict setObject:statusCode forKey:@"statusCode"];
        [dict setObject:eMsg forKey:@"eMsg"];
    }
    [_bridge.eventDispatcher sendDeviceEventWithName:@"OnChannelClosed" body:eMsg];
}
//结束
- (void)OnSentenceEnd:(NlsDelegateEvent)event result:(NSString *)result statusCode:(NSString *)statusCode errorMessage:(NSString *)eMsg {
    _transcriberStarted= false;
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    if (statusCode && eMsg) {
        [dict setObject:statusCode forKey:@"statusCode"];
        [dict setObject:[self dictionaryWithJsonString:result] forKey:@"eMsg"];
    }
    [_bridge.eventDispatcher sendDeviceEventWithName:@"OnTranscriptionSentenceEnd" body:dict];
}
//完成
- (void)OnTranscriptionCompleted:(NlsDelegateEvent)event statusCode:(NSString *)statusCode errorMessage:(NSString *)eMsg {
    _transcriberStarted= false;
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    if (statusCode && eMsg) {
        [dict setObject:statusCode forKey:@"statusCode"];
        [dict setObject:eMsg forKey:@"eMsg"];
    }
    [_bridge.eventDispatcher sendDeviceEventWithName:@"OnTranscriptionCompleted" body:dict];
}
//中间结果
- (void)OnTranscriptionResultChanged:(NlsDelegateEvent)event result:(NSString *)result statusCode:(NSString *)statusCode errorMessage:(NSString *)eMsg {
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    if (statusCode && eMsg) {
        [dict setObject:statusCode forKey:@"statusCode"];
        [dict setObject:[self dictionaryWithJsonString:result] forKey:@"eMsg"];
    }
    
    [_bridge.eventDispatcher sendDeviceEventWithName:@"OnTranscriptionResultChanged" body:dict];
}

- (void)OnTranscriptionStarted:(NlsDelegateEvent)event statusCode:(NSString *)statusCode errorMessage:(NSString *)eMsg {
    
}

- (void)OnSentenceBegin:(NlsDelegateEvent)event result:(NSString *)result statusCode:(NSString *)statusCode errorMessage:(NSString *)eMsg {
    
}

- (void)OnVoiceData:(NlsDelegateEvent)event data:(NSData *)voiceData length:(NSInteger)length {
    
}


- (void)OnVoiceVolume:(NlsDelegateEvent)event voiceVolume:(NSInteger)voiceVolume {
    
}


- (NSDictionary *)dictionaryWithJsonString:(NSString *)jsonString
{
    if (jsonString == nil) {
        return nil;
    }
    
    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSError *err;
    NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:jsonData
                                                        options:NSJSONReadingMutableContainers
                                                          error:&err];
    if(err)
    {
        NSLog(@"json解析失败：%@",err);
        return nil;
    }
    return dic;
}
@end
