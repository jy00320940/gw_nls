package com.mtdcomponents;

import com.tencent.mm.opensdk.modelmsg.SendAuth;

import java.util.ArrayList;

/**
 * @author Jcking
 * @time 2017/7/13 16:00
 */
public class PayLoop implements ICallback {

//    private ArrayList<ICallback> mList;

    private static volatile PayLoop sSingleton = null;

//    private PayLoop() {
//        mList = new ArrayList<>();
//    }
    ICallback callback;
    public static PayLoop getIntstance() {
        if (sSingleton == null) {
            synchronized (PayLoop.class) {
                if (sSingleton == null)
                    sSingleton = new PayLoop();
            }
        }
        return sSingleton;
    }

    public void add(ICallback callback) {
//        mList.add(callback);
        this.callback = callback;
    }

//    public void remove(ICallback callback) {
//        mList.remove(callback);
//    }
//
//    public void clear() {
//        mList.clear();
//    }

    @Override
    public void OnSuccess( SendAuth.Resp code) {
//        for (ICallback callback : mList)
            callback.OnSuccess(code);
//        clear();
    }

    @Override
    public void OnFailure() {
        callback = null;
//        for (ICallback callback : mList)
//            callback.OnFailure();
//        clear();
    }
}
