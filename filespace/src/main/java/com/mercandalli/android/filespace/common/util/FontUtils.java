/**
 * This file is part of FileSpace for Android, an app for managing your server (files, talks...).
 * <p/>
 * Copyright (c) 2014-2015 FileSpace for Android contributors (http://mercandalli.com)
 * <p/>
 * LICENSE:
 * <p/>
 * FileSpace for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 * <p/>
 * FileSpace for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * @author Jonathan Mercandalli
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2014-2015 FileSpace for Android contributors (http://mercandalli.com)
 */
package com.mercandalli.android.filespace.common.util;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.TextView;

/**
 * Static Methods used to apply Fonts
 *
 * @author Jonathan
 */
public class FontUtils {
    public static void applyFont(Context context, TextView tv, String police) {
        if (context == null || tv == null || police == null)
            return;
        Typeface font = Typeface.createFromAsset(context.getAssets(), police);
        tv.setTypeface(font);
    }

    public static void applyFont(Context context, Button bt, String police) {
        if (context == null || bt == null || police == null)
            return;
        Typeface font = Typeface.createFromAsset(context.getAssets(), police);
        bt.setTypeface(font);
    }
}