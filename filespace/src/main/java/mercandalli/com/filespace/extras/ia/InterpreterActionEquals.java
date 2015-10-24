/**
 * This file is part of Jarvis for Android, an app for managing your server (files, talks...).
 * <p/>
 * Copyright (c) 2014-2015 Jarvis for Android contributors (http://mercandalli.com)
 * <p/>
 * LICENSE:
 * <p/>
 * Jarvis for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 * <p/>
 * Jarvis for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * @author Jonathan Mercandalli
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2014-2015 Jarvis for Android contributors (http://mercandalli.com)
 */
package mercandalli.com.filespace.extras.ia;

import android.content.Context;

import mercandalli.com.filespace.extras.ia.action.ENUM_Action;

/**
 * Created by Jonathan on 19/04/2015.
 */
public class InterpreterActionEquals extends Interpreter {

    public InterpreterActionEquals(Context context, Resource resource) {
        super(context, resource);
    }

    @Override
    public InterpreterResult interpret(String input) {
        String output = null;

        if (input.equals("recherche") || input.equals("recherche google") || input.equals("google") || input.equals("ouvre google"))
            output = ENUM_Action.WEB_SEARCH.action.action(mContext, "http://www.google.com/");

        return new InterpreterResult(output);
    }

}