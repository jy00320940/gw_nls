
#import "RNGwNls.h"

#import <React/RCTBridge.h>
#import <React/RCTConvert.h>
#import <React/RCTLog.h>
#import <React/RCTUIManager.h>
#import <React/RCTUtils.h>

@interface RNGwNls() <DemoVoiceRecorderDelegate>
{
    //录音文件名称
    NSString *_voiceName;
    NSOutputStream *_outputStream;
}
@end

@implementation RNGwNls

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE()

#pragma mark -一句话语音识别-
RCT_EXPORT_METHOD(startRecongnizerWithToken:(NSString *)token AppKey:(NSString *)appkey){
    dispatch_async(dispatch_get_main_queue(), ^{
        if (!_recognizeRequest) {
            _recognizeRequest = [[RNGwNlsRecognizeRequest alloc] initWithToken:token AppKey:appkey Bridge:self.bridge];
        }
        [_recognizeRequest start];
    });
}

RCT_EXPORT_METHOD(stopRecognizer){
    dispatch_async(dispatch_get_main_queue(), ^{
        [_recognizeRequest stop];
    });
}

#pragma mark -实时语音识别-
RCT_EXPORT_METHOD(startTranscribeWithToken:(NSString *)token AppKey:(NSString *)appkey){
    dispatch_async(dispatch_get_main_queue(), ^{
        if (!_transcrinber) {
            _transcrinber = [[RNGwNlsTranscriberRequest alloc] initWithToken:token AppKey:appkey Bridge:self.bridge];
        }
        [_transcrinber start];
    });
}

RCT_EXPORT_METHOD(stopTranscribe){
    dispatch_async(dispatch_get_main_queue(), ^{
        [_transcrinber stop];
    });
}

#pragma mark -实时识别录音文件-
RCT_EXPORT_METHOD(startTranscribeRecorderWithToken:(NSString *)token AppKey:(NSString *)appkey FilePath:(NSString *)filePath ){
    dispatch_async(dispatch_get_main_queue(), ^{
        if (!_transcrinberVoice) {
            _transcrinberVoice = [[RNGwNlsTranscriberRequestVoice alloc] initWithToken:token AppKey:appkey Bridge:self.bridge];
        }
//        NSLog(@"startTranscribeRecorderWithToken----%@",filePath);
        [_transcrinberVoice setFilePath:[self getPathWithFilePath:filePath]];
        [_transcrinberVoice start];
    });
}

RCT_EXPORT_METHOD(stopTranscribeRecorder){
    dispatch_async(dispatch_get_main_queue(), ^{
        [_transcrinberVoice stop];
    });
}

#pragma mark -录音文件-
RCT_EXPORT_METHOD(startRecordVideo:(NSString *)name ){
    dispatch_async(dispatch_get_main_queue(), ^{
        //初始化录音recorder工具
        _voiceRecorder = [[RNGwNlsVoiceRecorder alloc]init];
        _voiceName = name;
        _voiceRecorder.delegate = self;
        [_voiceRecorder start];
    });
}

RCT_EXPORT_METHOD(stopRecordVideo){
    dispatch_async(dispatch_get_main_queue(), ^{
        [_voiceRecorder stop:true];
    });
}

/**
 *5. 录音相关回调
 */
- (void)recorderDidStart {
    NSLog(@"Did start recorder!");
    // 开启文件输出流
    _outputStream = [NSOutputStream outputStreamToFileAtPath:[self getPathWithFilePath:_voiceName] append:YES];
    [_outputStream open];
}

- (void)recorderDidStop {
    NSLog(@"Did stop recorder!");
    [_outputStream close];
    _outputStream = nil;
}

- (void)voiceDidFail:(NSError *)error {
    NSLog(@"Did recorder error!");
    [_outputStream close];
    _outputStream = nil;
}

//5.1 录音数据回调
- (void)voiceRecorded:(NSData *)frame {
    NSLog(@"%@--------%ld",frame,frame.length);
    [self plistSaveWithData:frame];
}

//5.2录制录音时返回音量大小
- (void)voiceVolume:(NSInteger)volume {
    
}

- (void)plistSaveWithData:(NSData *)data {
    [_outputStream write:data.bytes maxLength:data.length];
}

- (NSString *)getPathWithFilePath:(NSString *)filePaht{
    NSString *docsdir = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES).firstObject;
    NSString * dataFilePath = [docsdir stringByAppendingPathComponent:[NSString stringWithFormat:@"%@",filePaht]];
    return dataFilePath;
}

#pragma mark -播放录音文件-
RCT_EXPORT_METHOD(playVideo:(NSString *)name ){
    dispatch_async(dispatch_get_main_queue(), ^{
        _player = [RNGwNlsPlayer sharedInstance];
        [_player setName:name];
        [_player playWithUrl:[self getPathWithFilePath:name]];
    });
}

RCT_EXPORT_METHOD(pauseVideo){
    dispatch_async(dispatch_get_main_queue(), ^{
        [_player stop];
    });
}

#pragma mark -语音合成-
RCT_EXPORT_METHOD(startSynthesizerWithToken:(NSString *)token AppKey:(NSString *)appKey Text:(NSString *)text ){
    dispatch_async(dispatch_get_main_queue(), ^{
        _synthesizer = [[RNGwNlsSynthesizer alloc]init];
        [_synthesizer startSynthesizerWithToken:token AppKey:appKey];
    });
    [_synthesizer setText:text];
}

RCT_EXPORT_METHOD(stopSynthesizer){
    dispatch_async(dispatch_get_main_queue(), ^{
        [_synthesizer stop];
    });
}
@end

