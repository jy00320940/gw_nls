//
//  RNGwNlsVoiceRecorder.h
//  RNGwNls
//
//  Created by LiYang on 2019/8/1.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN
/**
 *@discuss DemoVoiceRecorder 各种回调接口
 */
@protocol DemoVoiceRecorderDelegate <NSObject>

/**
 * @discuss Recorder启动回调，在主线程中调用
 */
-(void) recorderDidStart;

/**
 * @discuss Recorde停止回调，在主线程中调用
 */
-(void) recorderDidStop;

/**
 * @discuss Recorder收录到数据，通常涉及VAD及压缩等操作，为了避免阻塞主线，因此将在在AudioQueue的线程中调用，注意线程安全！！！
 */
-(void) voiceRecorded:(NSData*) frame;


/**
 * @discuss Recorder录制录音时返回音量大小
 */
-(void) voiceVolume:(NSInteger)volume;

/**
 *@discussion 录音机无法打开或其他错误的时候会回调
 */
-(void) voiceDidFail:(NSError*)error;
@end

@interface RNGwNlsVoiceRecorder : NSObject
@property(nonatomic,assign) id<DemoVoiceRecorderDelegate> delegate;

@property(nonatomic,readonly) NSUInteger currentVoiceVolume;

/**
 * 开始录音
 */
-(void)start;

/**
 * 停止录音
 */
-(void)stop:(BOOL)shouldNotify;

/**
 * 是否在录音
 */
-(BOOL)isStarted;
@end

NS_ASSUME_NONNULL_END
