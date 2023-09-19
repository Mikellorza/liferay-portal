/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.model;

import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.LocalizationUtil;

import java.io.Serializable;

import java.util.Date;
import java.util.Locale;

/**
 * @author Lourdes Fernández Besada
 */
public class LockedLayout implements Serializable {

	public LockedLayout(
		long classPK, Date lastAutoSaveDate, String name, long plid,
		String type, String userName) {

		_classPK = classPK;
		_lastAutoSaveDate = lastAutoSaveDate;
		_name = name;
		_plid = plid;
		_type = type;
		_userName = userName;
	}

	public long getClassPK() {
		return _classPK;
	}

	public Date getLastAutoSaveDate() {
		return _lastAutoSaveDate;
	}

	public String getName() {
		return _name;
	}

	public String getName(Locale locale) {
		return LocalizationUtil.getLocalization(
			getName(), LocaleUtil.toLanguageId(locale));
	}

	public long getPlid() {
		return _plid;
	}

	public String getType() {
		return _type;
	}

	public String getUserName() {
		return _userName;
	}

	private final long _classPK;
	private final Date _lastAutoSaveDate;
	private final String _name;
	private final long _plid;
	private final String _type;
	private final String _userName;

}