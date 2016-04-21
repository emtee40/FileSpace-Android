/**
 * This file is part of FileSpace for Android, an app for managing your server (files, talks...).
 * <p>
 * Copyright (c) 2014-2015 FileSpace for Android contributors (http://mercandalli.com)
 * <p>
 * LICENSE:
 * <p>
 * FileSpace for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 * <p>
 * FileSpace for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * @author Jonathan Mercandalli
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2014-2015 FileSpace for Android contributors (http://mercandalli.com)
 */
package com.mercandalli.android.apps.files.admin.notification.simple;

import com.google.gson.annotations.SerializedName;
import com.mercandalli.android.library.baselibrary.push.PushManager;

public class SendNotificationSimpleRequest {

    @SerializedName("gcmId")
    private String mGcmId;

    @SerializedName("googleApiKey")
    private String mGoogleApiKey;

    @SerializedName(PushManager.PUSH_NOTIFICATION_KEY_MESSAGE)
    private String mPushMessage;

    public SendNotificationSimpleRequest(final String gcmId, final String googleApiKey, final String pushMessage) {
        mGcmId = gcmId;
        mGoogleApiKey = googleApiKey;
        mPushMessage = pushMessage;
    }
}