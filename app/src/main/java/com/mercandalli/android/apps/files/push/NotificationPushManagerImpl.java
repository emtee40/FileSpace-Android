package com.mercandalli.android.apps.files.push;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.mercandalli.android.apps.files.BuildConfig;
import com.mercandalli.android.apps.files.R;
import com.mercandalli.android.apps.files.main.Config;
import com.mercandalli.android.apps.files.main.Constants;
import com.mercandalli.android.library.baselibrary.network.NetworkUtils;
import com.mercandalli.android.library.baselibrary.push.PushManager;

import static com.mercandalli.android.library.baselibrary.device.DeviceUtils.getDeviceBuilder;
import static com.mercandalli.android.library.baselibrary.network.NetworkUtils.isNetworkAvailable;

/**
 * Manage the notification push: id...
 */
/* package */
class NotificationPushManagerImpl implements NotificationPushManager, PushManager.OnGcmMessageListener {

    private static final String GCM_SENDER = "807253530972";

    private final Context mAppContext;
    private final PushManager mPushManager;

    /* package */ NotificationPushManagerImpl(final Application application) {
        mAppContext = application.getApplicationContext();
        mPushManager = PushManager.getInstance(mAppContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        if (!isNetworkAvailable(mAppContext)) {
            return;
        }
        mPushManager.addOnGcmMessageListener(this);
        mPushManager.initialize(
                BuildConfig.DEBUG,
                getDeviceBuilder(mAppContext),
                GCM_SENDER,
                Constants.URL_DOMAIN,
                Config.ROUTE_DEVICE_ADD);
    }

    @Override
    public void reset() {
        mPushManager.removeOnGcmMessageListener(this);
    }

    @Override
    public void onGcmMessageReceived(
            final @Nullable String from,
            final @NonNull Bundle data,
            final @PushManager.PushType String type,
            final @Nullable String message,
            final @Nullable String title,
            final @Nullable String actionData) {
        switch (type) {
            case PushManager.PUSH_NOTIFICATION_TYPE_MESSAGE_ONLY:
                if (message == null) {
                    break;
                }
                NetworkUtils.sendNotification(
                        mAppContext,
                        message,
                        TextUtils.isEmpty(title) ? mAppContext.getString(R.string.app_name) : title,
                        "com.mercandalli.android.apps.files",
                        R.drawable.ic_notification_cloud,
                        ContextCompat.getColor(mAppContext, R.color.accent),
                        1);
                break;
            case PushManager.PUSH_NOTIFICATION_TYPE_OPEN_PLAY_STORE:
                if (actionData == null) {
                    break;
                }
                NetworkUtils.openPlayStore(mAppContext, actionData);
                break;
            case PushManager.PUSH_NOTIFICATION_TYPE_OPEN_URL:
                if (actionData == null) {
                    break;
                }
                NetworkUtils.openUrl(mAppContext, actionData);
                break;
        }
    }
}