package com.project.chatappfirebase;

import android.app.Application;

import com.onesignal.OneSignal;

import java.util.UUID;

public class ChatApplication extends Application {

    private static final String ONESIGNAL_APP_ID = "YOUR APP ID";

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

    }
}
