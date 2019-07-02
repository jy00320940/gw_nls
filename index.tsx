
import { DeviceEventEmitter, NativeModules, } from 'react-native';

const RNGwNls = NativeModules.RNGwNls;

export interface Listener {
    failed(emsg:string):void;
    closed(emsg:string):void;
    completed(resp:any):void;
    token?:string;
    appKey?:string;
}

var listener : Listener;
DeviceEventEmitter.addListener('OnTaskFailed', resp => {
    if (listener && listener.failed){
        listener.failed(resp.eMsg);
    }
});
  
DeviceEventEmitter.addListener('OnChannelClosed', resp => {
    if (listener && listener.closed){
        listener.closed(resp.eMsg);
    }
});
  
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
    if (listener && listener.completed){
        listener.completed(resp.eMsg);
    }
});

export function startRecognizerwithRecorderWithToken(token:string,appKey:string,lis:Listener){
    listener = lis;
    RNGwNls.startRecognizerwithRecorderWithToken(token,appKey);
}

export function stopRecognizerwithRecorder(){
    RNGwNls.stopRecognizerwithRecorder();
}

