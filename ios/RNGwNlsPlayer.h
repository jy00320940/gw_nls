//
//  RNGwNlsPlayer.h
//  RNGwNls
//
//  Created by LiYang on 2019/8/1.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface RNGwNlsPlayer : NSObject

+(instancetype)sharedInstance;
-(void)setName:(NSString *)name;
-(void)playWithUrl:(NSString *)urlStr;

-(void)stop;
@end

NS_ASSUME_NONNULL_END
