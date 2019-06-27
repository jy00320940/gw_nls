package com.mtdcomponents;

import com.tencent.mm.opensdk.modelmsg.SendAuth;

public interface ICallback {
    void OnSuccess( SendAuth.Resp code);
    void OnFailure();
}
