import { DeviceEventEmitter, NativeModules } from 'react-native'

const RNGwNls = NativeModules.RNGwNls

/////////////////////////////////////////////////////////////////////////
//
// 接口定义
//
/////////////////////////////////////////////////////////////////////////

export interface GwNlsListener {
  onChannelClosed(emsg: string): void
  onTaskFailed(emsg: string): void
  token?: string
  appKey?: string
}

/**
 * 一句话语音接口
 */
export interface RecognizedListener extends GwNlsListener {
  onRecognizedCompleted(resp: any): void
}

/**
 * 实时语音识别接口
 */
export interface TranscriberListener extends GwNlsListener {
  onTranscriptionResultChanged(resp: any): void
  onTranscriptionSentenceEnd(resp: any): void
  onTranscriptionCompleted(resp: any): void
}

/**
 * 录音识别完成
 */
export interface RecorderListener extends GwNlsListener {
  onRecorderResultChanged(resp: any): void
  onRecorderSentenceEnd(resp: any): void
  onRecorderCompleted(resp: any): void
}

var currentListener: GwNlsListener
var recognizedListener: RecognizedListener
var transcriberListener: TranscriberListener
var recorderListener: RecorderListener

/////////////////////////////////////////////////////////////////////////
//
// 添加事件监听
//
/////////////////////////////////////////////////////////////////////////

/**
 * 通道关闭
 */
DeviceEventEmitter.addListener('OnChannelClosed', resp => {
  if (currentListener && currentListener.onChannelClosed) {
    currentListener.onChannelClosed(resp.eMsg)
  }
})

/**
 * 识别失败
 */
DeviceEventEmitter.addListener('OnTaskFailed', resp => {
  if (currentListener && currentListener.onTaskFailed) {
    currentListener.onTaskFailed(resp.eMsg)
  }
})

/**
 * 一句话语音识别完成
 */
DeviceEventEmitter.addListener('OnRecognizedCompleted', resp => {
  // {
  //     "header":{
  //         "namespace":"SpeechRecognizer",
  //         "name":"RecognitionCompleted",
  //         "status":20000000,
  //         "message_id":"f769c5cdc3744e949b18e57829b82d09",
  //         "task_id":"d8060bd8881049fc8fb52cdae8f935a0",
  //         "status_text":
  //         "Gateway:SUCCESS:Success."
  //     },
  //     "payload":{
  //         "result":"你好你好",
  //         "duration":1200
  //     }}
  if (recognizedListener && recognizedListener.onRecognizedCompleted) {
    recognizedListener.onRecognizedCompleted(resp.eMsg)
  }
})

/**
 * 识别中间结果，只有开启相关选项时才会回调
 */
DeviceEventEmitter.addListener('OnTranscriptionResultChanged', resp => {
  if (transcriberListener && transcriberListener.onTranscriptionResultChanged) {
    transcriberListener.onTranscriptionResultChanged(resp.eMsg)
  }
})

/**
 * 当前句子识别结束，得到完整的句子文本
 */
DeviceEventEmitter.addListener('OnTranscriptionSentenceEnd', resp => {
  if (transcriberListener && transcriberListener.onTranscriptionSentenceEnd) {
    transcriberListener.onTranscriptionSentenceEnd(resp.eMsg)
  }
})

/**
 * 实时语音识别结束
 */
DeviceEventEmitter.addListener('OnTranscriptionCompleted', resp => {
  if (transcriberListener && transcriberListener.onTranscriptionCompleted) {
    transcriberListener.onTranscriptionCompleted(resp.eMsg)
  }
})

/**
 * 录音识别:识别中间结果，只有开启相关选项时才会回调
 */
DeviceEventEmitter.addListener('OnRecorderResultChanged', resp => {
  if (recorderListener && recorderListener.onRecorderResultChanged) {
    recorderListener.onRecorderResultChanged(resp.eMsg)
  }
})

/**
 * 录音识别: 当前句子识别结束，得到完整的句子文本
 */
DeviceEventEmitter.addListener('OnRecorderSentenceEnd', resp => {
  if (recorderListener && recorderListener.onRecorderSentenceEnd) {
    recorderListener.onRecorderSentenceEnd(resp.eMsg)
  }
})

/**
 * 录音识别结束
 */
DeviceEventEmitter.addListener('OnRecorderCompleted', resp => {
  if (recorderListener && recorderListener.onRecorderCompleted) {
    recorderListener.onRecorderCompleted(resp.eMsg)
  }
})

/////////////////////////////////////////////////////////////////////////
//
// 抛出功能方法
//
/////////////////////////////////////////////////////////////////////////

/**
 * 开始一句话语音
 * @param token
 * @param appKey
 * @param l
 */
export function startRecongnizerWithToken(
  token: string,
  appKey: string,
  l: RecognizedListener
) {
  currentListener = l
  recognizedListener = l
  RNGwNls.startRecongnizerWithToken(token, appKey)
}

/**
 * 结束一句话语音
 */
export function stopRecognizer() {
  RNGwNls.stopRecognizer()
}

/**
 * 开始语音实时识别
 * @param token
 * @param appKey
 * @param l
 */
export function startTranscribeWithToken(
  token: string,
  appKey: string,
  l: TranscriberListener
) {
  currentListener = l
  transcriberListener = l
  RNGwNls.startTranscribeWithToken(token, appKey)
}

/**
 * 结束语音实时识别
 */
export function stopTranscribe() {
  RNGwNls.stopTranscribe()
}

/**
 * 开始录音
 * @param name 录音文件保存名称
 */
export function startRecordVideo(name: string) {
  RNGwNls.startRecordVideo(name)
}

/**
 * 结束录音
 */
export function stopRecordVideo() {
  RNGwNls.stopRecordVideo()
}

/**
 * 播放录音
 * @param name 录音文件保存名称
 */
export function playVideo(name: string) {
  RNGwNls.playVideo(name)
}

/**
 * 停止播放录音
 */
export function pauseVideo() {
  RNGwNls.pauseVideo()
}

/**
 * 开始识别录音文件
 * @param token
 * @param appKey
 * @param filePath 录音文件路径
 * @param l
 */
export function startTranscribeRecorderWithToken(
  token: string,
  appKey: string,
  filePath: string,
  l: RecorderListener
) {
  currentListener = l
  recorderListener = l
  RNGwNls.startTranscribeRecorderWithToken(token, appKey, filePath)
}

/**
 * 结束识别录音文件
 */
export function stopTranscribeRecorder() {
  RNGwNls.stopTranscribeRecorder()
}

/**
 * 获取录音文件存放目录
 */
export function getVideoFilePath(): string {
  return RNGwNls.getVideoFilePath()
}
