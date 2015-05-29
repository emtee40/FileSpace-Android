/**
 * This file is part of Jarvis for Android, an app for managing your server (files, talks...).
 *
 * Copyright (c) 2014-2015 Jarvis for Android contributors (http://mercandalli.com)
 *
 * LICENSE:
 *
 * Jarvis for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Jarvis for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * @author Jonathan Mercandalli
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2014-2015 Jarvis for Android contributors (http://mercandalli.com)
 */
package mercandalli.com.jarvis.model;

import org.json.JSONObject;

import mercandalli.com.jarvis.ui.activity.Application;
import mercandalli.com.jarvis.config.Const;

public abstract class Model {
	
	protected Application app;
	public int viewType = Const.TAB_VIEW_TYPE_NORMAL;
	
	public Model(Application app) {
		this.app = app;
	}
    public Model() {  }

	public abstract JSONObject toJSONObject();

}
