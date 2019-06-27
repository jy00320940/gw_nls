package com.mtdcomponents;

import android.app.Application;

import com.facebook.react.ReactApplication;
import com.chenzhe.rnimageplaceholder.RNCzImagePlaceholderPackage;
import com.mtdcomponents.gwnls.RNGwNlsPackage;
import com.oblador.vectoricons.VectorIconsPackage;
import com.lufinkey.react.eventemitter.RNEventEmitterPackage;
import com.mtdcomponents.Util.Constants;
import com.reactnativecommunity.webview.RNCWebViewPackage;
import com.horcrux.svg.SvgPackage;

import io.realm.react.RealmReactPackage;

import com.reactnativecommunity.asyncstorage.AsyncStoragePackage;
import com.swmansion.gesturehandler.react.RNGestureHandlerPackage;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;

import java.util.Arrays;
import java.util.List;

public class MainApplication extends Application implements ReactApplication {
    private static final WXLoginPackage mCommPackage = new WXLoginPackage();
    private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
            return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
            return Arrays.<ReactPackage>asList(
                    new MainReactPackage(),
                    new RNCzImagePlaceholderPackage(),
                    new VectorIconsPackage(),
                    new RNEventEmitterPackage(),
                    new RNCWebViewPackage(),
                    new SvgPackage(),
                    new RealmReactPackage(),
                    new AsyncStoragePackage(),
                    new RNGestureHandlerPackage(),
                    new PalettePackage(),
                    new WXLoginPackage(),
                    new RNGwNlsPackage()
            );
        }

        @Override
        protected String getJSMainModuleName() {
            return "index";
        }
    };

    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SoLoader.init(this, /* native exopackage */ false);
    }


    /**
     * 获取 reactPackage
     *
     * @return
     */
    public static WXLoginPackage getReactPackage() {
        return mCommPackage;
    }
}
