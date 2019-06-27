
#import "RNGwNls.h"

#import <React/RCTBridge.h>
#import <React/RCTConvert.h>
#import <React/RCTLog.h>
#import <React/RCTUIManager.h>
#import <React/RCTUtils.h>

@interface RNGwNls() <NlsSpeechRecognizerwithRecorderDelegate>
{
    Boolean recognizerStarted;
}
@end

@implementation RNGwNls

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE()

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

- (instancetype)init {
    if (self = [super init]) {
        //1. 全局参数初始化操作
        //1.1 初始化识别客户端,将recognizerStarted状态置为false
        _nlsClient = [[NlsClientAdaptor alloc]init];
        recognizerStarted = false;
        //1.3 初始化识别参数类
        _recognizeRequestParam = [[RecognizerRequestParam alloc]init];
        //1.4 设置log级别
        [_nlsClient setLog:NULL logLevel:LOGDEBUG];
    }
    return self;
}


RCT_EXPORT_METHOD(startRecognizerwithRecorderWithToken:(NSString *)token AppKey:(NSString *)appkey){
    
    if (recognizerStarted) {
        //        NSLog(@"already started!");
        return;
    }
    //2. 创建请求对象和开始识别
    if(_recognizeRequest!= NULL){
        [_recognizeRequest releaseRequest];
        _recognizeRequest = NULL;
    }
    //2.1 创建请求对象，设置NlsSpeechRecognizerDelegate回调
    _recognizeRequest = [_nlsClient createRecognizerRequestwithRecorder];
    _recognizeRequest.delegate = self;
    
    //2.2 设置RecognizerRequestParam请求参数
    [_recognizeRequestParam setFormat:@"opu"];
    //返回中间识别结果
    [_recognizeRequestParam setEnableIntermediateResult:YES];
    //设置文本规则，如识别'12345',如打开即识别为'12345',如设为NO则识别为'一二三四五'
    [_recognizeRequestParam setEnableInverseTextNormalization:YES];
    //是否在识别结果中添加标点
    [_recognizeRequestParam setEnablePunctuationPrediction:NO];
    
    //请使用https://help.aliyun.com/document_detail/72153.html 动态生成token
    [_recognizeRequestParam setToken:token];
    //或者采用本Demo的_generateTokeng方法获取token
    // <AccessKeyId> <AccessKeySecret> 请使用您的阿里云账户生成 https://ak-console.aliyun.com/
    //[_recognizeRequestParam setToken:[self _generateToken:@"AccessKeyId" withSecret:@"AccessKeyId"]];
    
    
    //请使用阿里云语音服务管控台(https://nls-portal.console.aliyun.com/)生成您的appkey
    [_recognizeRequestParam setAppkey:appkey];
    
    //是否开启静音检测
    [_recognizeRequestParam setEnableVoiceDetection:YES];
    //允许的最大开始静音，可选，单位是毫秒，超出后服务端将会发送RecognitionCompleted事件，结束本次识别，需要先设置enable_voice_detection为true
    [_recognizeRequestParam setMaxStartSilence:3000];
    //允许的最大结束静音，可选，单位是毫秒，超出后服务端将会发送RecognitionCompleted事件，结束本次识别，需要先设置enable_voice_detection为true
    [_recognizeRequestParam setMaxEndSilence:800];
    
    //2.3 传入请求参数
    [_recognizeRequest setRecognizeParams:_recognizeRequestParam];
    
    //2.4 启动录音和识别，将recognizerStarted置为true
    [_recognizeRequest start];
    recognizerStarted = true;
}

RCT_EXPORT_METHOD(stopRecognizerwithRecorder){
    //3 结束识别 停止录音，停止识别请求
    [_recognizeRequest stop];
    recognizerStarted = false;
    _recognizeRequest = NULL;
}

/**
 *4. NlsSpeechRecognizerDelegate回调方法
 */
//4.1 识别回调，本次请求失败
-(void)OnTaskFailed:(NlsDelegateEvent)event statusCode:(NSString*)statusCode errorMessage:(NSString*)eMsg{
    recognizerStarted = false;
//        NSLog(@"OnTaskFailed, error message is: %@",eMsg);
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    if (statusCode && eMsg) {
        [dict setObject:statusCode forKey:@"statusCode"];
        [dict setObject:eMsg forKey:@"eMsg"];
    }
    [self.bridge.eventDispatcher sendDeviceEventWithName:@"OnTaskFailed" body:dict];
}

//4.2 识别回调，服务端连接关闭
-(void)OnChannelClosed:(NlsDelegateEvent)event statusCode:(NSString*)statusCode errorMessage:(NSString*)eMsg{
    recognizerStarted = false;
//        NSLog(@"OnChannelClosed, statusCode is: %@",statusCode);
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    if (statusCode && eMsg) {
        [dict setObject:statusCode forKey:@"statusCode"];
        [dict setObject:eMsg forKey:@"eMsg"];
    }
    [self.bridge.eventDispatcher sendDeviceEventWithName:@"OnChannelClosed" body:eMsg];
}

//4.3 识别回调，识别结果结束
-(void)OnRecognizedCompleted:(NlsDelegateEvent)event result:(NSString *)result statusCode:(NSString*)statusCode errorMessage:(NSString*)eMsg{
    recognizerStarted = false;
    //    {"header":{"namespace":"SpeechRecognizer","name":"RecognitionCompleted","status":20000000,"message_id":"f769c5cdc3744e949b18e57829b82d09","task_id":"d8060bd8881049fc8fb52cdae8f935a0","status_text":"Gateway:SUCCESS:Success."},"payload":{"result":"你好你好","duration":1200}}
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    if (statusCode && eMsg) {
        [dict setObject:statusCode forKey:@"statusCode"];
        [dict setObject:[self dictionaryWithJsonString:result] forKey:@"eMsg"];
    }
    [self.bridge.eventDispatcher sendDeviceEventWithName:@"OnRecognizedCompleted" body:dict];
}


//4.4 识别回调，识别中间结果
-(void)OnRecognizedResultChanged:(NlsDelegateEvent)event result:(NSString *)result statusCode:(NSString*)statusCode errorMessage:(NSString*)eMsg{
    //    dispatch_async(dispatch_get_main_queue(), ^{
    //        // UI更新代码
    //        NSLog(@"OnRecognizedResultChanged%@", result);
    //
    //    });
}

- (void)OnVoiceData:(NlsDelegateEvent)event data:(NSData *)voiceData length:(NSInteger)length {
    //    NSLog(@"on VoiceData length :%lu",(unsigned long)[voiceData length]);
}


- (void)OnVoiceVolume:(NlsDelegateEvent)event voiceVolume:(NSInteger)voiceVolume {
    //    NSLog(@"on Voice volume :%lu",(unsigned long)voiceVolume);
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

