
#import "RNGwNlsTranscriberRequestVoice.h"
#import "RNGwNlsTranscriberRequest.h"
#import "RNGwNlsRecognizeRequest.h"
#import "RNGwNlsVoiceRecorder.h"
#import "RNGwNlsPlayer.h"
#import "RNGwNlsSynthesizer.h"

@interface RNGwNls : NSObject <RCTBridgeModule>
//录音文件解析文字
@property(nonatomic,strong) RNGwNlsTranscriberRequestVoice *transcrinberVoice;
//实时录音
@property(nonatomic,strong) RNGwNlsTranscriberRequest *transcrinber;
//一句话语音
@property(nonatomic,strong) RNGwNlsRecognizeRequest *recognizeRequest;
//录音
@property(nonatomic,strong) RNGwNlsVoiceRecorder *voiceRecorder;
//播放录音
@property(nonatomic,strong) RNGwNlsPlayer *player;
//语音合成
@property(nonatomic,strong) RNGwNlsSynthesizer *synthesizer;
@end
  
