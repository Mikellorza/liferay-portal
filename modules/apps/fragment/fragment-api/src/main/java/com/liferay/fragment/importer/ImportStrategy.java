/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.importer;

/**
 * @author Mikel Lorza
 */
public enum ImportStrategy {

	DO_NOT_IMPORT("do_not_import"), DO_NOT_OVERRIDE("do_not_override"),
	KEEP_BOTH("keep_both"), OVERRIDE("override");

	private ImportStrategy(String value) {
		_value = value;
	}

	private final String _value;

}