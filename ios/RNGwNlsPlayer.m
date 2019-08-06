//
//  RNGwNlsPlayer.m
//  RNGwNls
//
//  Created by LiYang on 2019/8/1.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "RNGwNlsPlayer.h"
@interface RNGwNlsPlayer()
{
    AVAudioSession* _audioSession;
    NSString *_name;
}
@property (nonatomic, strong) AVAudioPlayer* staticAudioPlayer;
@end
@implementation RNGwNlsPlayer
-(instancetype)init{
    if (self = [super init]) {
    
        /*
         Adding the above line of code made it so my audio would start even if the app was in the background.
         */
        
        _audioSession = [AVAudioSession sharedInstance];
        [_audioSession setCategory:AVAudioSessionCategoryPlayback withOptions:AVAudioSessionCategoryOptionMixWithOthers error:nil];
        [_audioSession setActive:YES error:nil];
        
    }
    return self;
}
- (NSString *)wavFilePath {
    NSString *docsdir = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES).firstObject;
    NSString * dataFilePath = [docsdir stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.wav",_name]];
    return dataFilePath;
}

- (NSURL *) getAndCreatePlayableFileFromPcmData:(NSString *)filePath
{
    
    NSString *wavFilePath = [self wavFilePath];  //wav文件的路径
    
    NSLog(@"PCM file path : %@",filePath); //pcm文件的路径
    
    FILE *fout;
    
    short NumChannels = 1;       //录音通道数
    short BitsPerSample = 16;    //线性采样位数
    int SamplingRate = 16000;     //录音采样率(Hz)
    int numOfSamples = (int)[[NSData dataWithContentsOfFile:filePath] length];
    
    int ByteRate = NumChannels*BitsPerSample*SamplingRate/8;
    short BlockAlign = NumChannels*BitsPerSample/8;
    int DataSize = NumChannels*numOfSamples*BitsPerSample/8;
    int chunkSize = 16;
    int totalSize = 46 + DataSize;
    short audioFormat = 1;
    
    if((fout = fopen([wavFilePath cStringUsingEncoding:1], "w")) == NULL)
    {
        printf("Error opening out file ");
    }
    
    fwrite("RIFF", sizeof(char), 4,fout);
    fwrite(&totalSize, sizeof(int), 1, fout);
    fwrite("WAVE", sizeof(char), 4, fout);
    fwrite("fmt ", sizeof(char), 4, fout);
    fwrite(&chunkSize, sizeof(int),1,fout);
    fwrite(&audioFormat, sizeof(short), 1, fout);
    fwrite(&NumChannels, sizeof(short),1,fout);
    fwrite(&SamplingRate, sizeof(int), 1, fout);
    fwrite(&ByteRate, sizeof(int), 1, fout);
    fwrite(&BlockAlign, sizeof(short), 1, fout);
    fwrite(&BitsPerSample, sizeof(short), 1, fout);
    fwrite("data", sizeof(char), 4, fout);
    fwrite(&DataSize, sizeof(int), 1, fout);
    
    fclose(fout);
    
    NSMutableData *pamdata = [NSMutableData dataWithContentsOfFile:filePath];
    NSFileHandle *handle;
    handle = [NSFileHandle fileHandleForUpdatingAtPath:wavFilePath];
    [handle seekToEndOfFile];
    [handle writeData:pamdata];
    [handle closeFile];
    
    return [NSURL URLWithString:wavFilePath];
}

+(instancetype)sharedInstance{
    static RNGwNlsPlayer *instance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[self alloc] init];
    });
    return instance;
}

- (void)setName:(NSString *)name{
    _name = name;
}

-(void)playWithUrl:(NSString *)urlStr{
//    NSURL *url = [NSURL URLWithString:[[NSBundle mainBundle] pathForResource:@"awwwwwa" ofType:@"wav"]];
    [self getAndCreatePlayableFileFromPcmData:urlStr];
    NSURL *url = [NSURL fileURLWithPath:[NSString stringWithFormat:@"%@.wav",urlStr]];
   
    NSError *error = nil;
    _staticAudioPlayer = [[AVAudioPlayer alloc] initWithContentsOfURL:url error:&error];
    if (error) {
        NSLog(@"playWithUrl error=%@",error);
        return;
    }
    [_staticAudioPlayer prepareToPlay];
    
    _staticAudioPlayer.volume = 10;
    if (!_staticAudioPlayer.isPlaying) {
        [_staticAudioPlayer play];
    }
}

-(void)stop{
    _staticAudioPlayer.currentTime = 0;
    [_staticAudioPlayer stop];
}
@end
