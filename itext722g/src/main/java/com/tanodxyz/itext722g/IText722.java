package com.tanodxyz.itext722g;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;

public class IText722 {
    private static Context appContext;
    public static String ANDROID_FONTS_DIR = "system" + File.separatorChar + "fonts";

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static Context getContext() {
        return appContext;
    }

    public static File getCacheDir() {
        return appContext.getCacheDir();
    }

    public static InputStream getResourceStream(String path) throws IOException {
        return getContext().getAssets().open(path);
    }
}
