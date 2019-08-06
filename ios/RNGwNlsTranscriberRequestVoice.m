//
//  RNGwNlsTranscriberRequestVoice.m
//  RNGwNls
//
//  Created by LiYang on 2019/7/31.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "RNGwNlsTranscriberRequestVoice.h"

@interface RNGwNlsTranscriberRequestVoice()<NlsSpeechTranscriberDelegate,NSStreamDelegate>
{
    Boolean _transcriberStarted;
    NSString *_token;
    NSString *_appkey;
    NSString *_filePath;
    RCTBridge *_bridge;
}
@property(nonatomic,strong) NSInputStream *inputStream;
@end

@implementation RNGwNlsTranscriberRequestVoice

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

- (void)setFilePath:(NSString *)filePath{
    _filePath = filePath;
}

- (void)stop{
    [_transcriberRequestVoice stop];
    _transcriberStarted= false;
    _transcriberRequestVoice = NULL;
}

- (void)start {
    if (_transcriberStarted) {
        NSLog(@"already started!");
        return;
    }
    //2. 创建请求对象和开始识别
    if(_transcriberRequestVoice!= NULL){
        [_transcriberRequestVoice releaseRequest];
        _transcriberRequestVoice = NULL;
    }
    //2.1 创建请求对象，设置NlsSpeechTranscriberDelegate回调
    _transcriberRequestVoice = [_nlsClient createTranscriberRequest];
    _transcriberRequestVoice.delegate = self;
    
    //2.2 设置TranscriberRequestParam请求参数
    [_transRequestParam setFormat:@"pcm"];
    //返回中间识别结果
    [_transRequestParam setEnableIntermediateResult:YES];
    //设置文本规则，如识别'12345',如打开即识别为'12345',如设为NO则识别为'一二三四五'
    [_transRequestParam setEnableInverseTextNormalization:YES];
    //是否在识别结果中添加标点
    [_transRequestParam setEnablePunctuationPrediction:YES];
    
    //请使用https://help.aliyun.com/document_detail/72153.html 动态生成token
    [_transRequestParam setToken:_token];
    [_transRequestParam setAppkey:_appkey];
    
    //语音断句检测阈值，一句话之后静音长度超过该值，即本句结束，合法参数范围200～2000(ms)，默认值800ms
    [_transRequestParam setMaxSentenceSilence:800];
    
    //2.3 传入请求参数
    [_transcriberRequestVoice setTranscriberParams:_transRequestParam];
    
    [_transcriberRequestVoice start];
    _transcriberStarted = true;
    
    //[_transcriberRequestVoice sendAudio:frame length:(short)frame.length];
    [self setUpStreamForFile:_filePath];
}


- (void)setUpStreamForFile:(NSString *)fullPath {
    NSLog(@"setUpStreamForFile----%@",fullPath);
    // 1、 从数据源中创建和初始化一个NSInputStream实例
    self.inputStream = [[NSInputStream alloc] initWithFileAtPath:fullPath];
    self.inputStream.delegate = self;
    [self.inputStream scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [self.inputStream open];
}

#pragma mark NSStreamDelegate
- (void)stream:(NSStream *)aStream handleEvent:(NSStreamEvent)eventCode{
    /*
     NSStreamEventNone = 0,
     NSStreamEventOpenCompleted = 1UL << 0,
     NSStreamEventHasBytesAvailable = 1UL << 1,
     NSStreamEventHasSpaceAvailable = 1UL << 2,
     NSStreamEventErrorOccurred = 1UL << 3,
     NSStreamEventEndEncountered = 1UL << 4
     */
    _transcriberStarted= false;
    switch (eventCode) {
        case NSStreamEventOpenCompleted:
            NSLog(@"打开流");
            break;
        case NSStreamEventHasBytesAvailable:
        {
            uint8_t buf[640] ={0};
            NSInteger length = [self.inputStream read:buf maxLength:640];
            
            if (length > 0) {
                NSMutableData *data = [NSMutableData data];
                [data appendBytes:buf length:length];
                [_transcriberRequestVoice sendAudio:data length:(short)data.length];
            } else {
                NSLog(@"读取到数据长度<0");
            }
        }
            break;
        case NSStreamEventHasSpaceAvailable:
            NSLog(@"NSStreamEventHasSpaceAvailable");
            break;
        case NSStreamEventErrorOccurred:
            NSLog(@"出错了");
            break;
        case NSStreamEventEndEncountered:
            NSLog(@" 文件读取结束");
            
            //    4、 当没有更多数据可读取时，关闭并销毁流对象。
            // 关闭输入流
            [self.inputStream close];
            [self stop];
            // 从运行循环中移除
            [self.inputStream removeFromRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
            
            break;
            
        default:
            break;
    }
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
    [_bridge.eventDispatcher sendDeviceEventWithName:@"OnRecorderSentenceEnd" body:dict];
}
//完成
- (void)OnTranscriptionCompleted:(NlsDelegateEvent)event statusCode:(NSString *)statusCode errorMessage:(NSString *)eMsg {
    _transcriberStarted= false;
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    if (statusCode && eMsg) {
        [dict setObject:statusCode forKey:@"statusCode"];
        [dict setObject:eMsg forKey:@"eMsg"];
    }
    [_bridge.eventDispatcher sendDeviceEventWithName:@"OnRecorderCompleted" body:dict];
}
//中间结果
- (void)OnTranscriptionResultChanged:(NlsDelegateEvent)event result:(NSString *)result statusCode:(NSString *)statusCode errorMessage:(NSString *)eMsg {
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    if (statusCode && eMsg) {
        [dict setObject:statusCode forKey:@"statusCode"];
        [dict setObject:[self dictionaryWithJsonString:result] forKey:@"eMsg"];
    }
    
    [_bridge.eventDispatcher sendDeviceEventWithName:@"OnRecorderResultChanged" body:dict];
}

- (void)OnTranscriptionStarted:(NlsDelegateEvent)event statusCode:(NSString *)statusCode errorMessage:(NSString *)eMsg {
    
}

- (void)OnSentenceBegin:(NlsDelegateEvent)event result:(NSString *)result statusCode:(NSString *)statusCode errorMessage:(NSString *)eMsg {
    
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
