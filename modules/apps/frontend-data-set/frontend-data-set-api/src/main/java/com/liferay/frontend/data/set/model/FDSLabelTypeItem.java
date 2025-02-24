/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2025-06
 */

package com.liferay.frontend.data.set.model;

import java.util.HashMap;

/**
 * @author Mikel Lorza
 */
public class FDSLabelTypeItem extends HashMap<String, Object> {

	public FDSLabelTypeItem(String displayType, String value) {
		setDisplayType(displayType);
		setValue(value);
	}

	public void setDisplayType(String displayType) {
		put("displayType", displayType);
	}

	public void setValue(String value) {
		put("value", value);
	}

}