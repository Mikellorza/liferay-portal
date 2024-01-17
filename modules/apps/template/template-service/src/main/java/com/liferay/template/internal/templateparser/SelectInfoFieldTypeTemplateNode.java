/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.template.internal.templateparser;

import com.liferay.info.field.InfoFieldValue;
import com.liferay.info.type.KeyLocalizedLabelPair;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.templateparser.TemplateNode;
import com.liferay.portal.kernel.theme.ThemeDisplay;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Eudaldo Alonso
 */
public class SelectInfoFieldTypeTemplateNode extends TemplateNode {

	public SelectInfoFieldTypeTemplateNode(
		Map<String, String> attributes, InfoFieldValue<Object> infoFieldValue,
		String name, ThemeDisplay themeDisplay, String type) {

		super(themeDisplay, name, StringPool.BLANK, type, attributes);

		_infoFieldValue = infoFieldValue;
		_themeDisplay = themeDisplay;
	}

	@Override
	public String getData() {
		_log.error(
			"getData() method is deprecated in favor of getLabel(Locale) and " +
				"getKey() for select fields. Please update your template.");

		KeyLocalizedLabelPair keyLocalizedLabelPair =
			_getSelectedKeyLocalizedLabelPair();

		if (keyLocalizedLabelPair != null) {
			return keyLocalizedLabelPair.getLabel(_themeDisplay.getLocale());
		}

		return null;
	}

	public String getKey() {
		KeyLocalizedLabelPair keyLocalizedLabelPair =
			_getSelectedKeyLocalizedLabelPair();

		if (keyLocalizedLabelPair != null) {
			return keyLocalizedLabelPair.getKey();
		}

		return null;
	}

	public String getLabel(Locale locale) {
		KeyLocalizedLabelPair keyLocalizedLabelPair =
			_getSelectedKeyLocalizedLabelPair();

		if (keyLocalizedLabelPair != null) {
			return keyLocalizedLabelPair.getLabel(locale);
		}

		return null;
	}

	private KeyLocalizedLabelPair _getSelectedKeyLocalizedLabelPair() {
		Object value = _infoFieldValue.getValue();

		if (!(value instanceof List)) {
			return null;
		}

		List<KeyLocalizedLabelPair> keyLocalizedLabelPairs =
			(List<KeyLocalizedLabelPair>)value;

		return keyLocalizedLabelPairs.get(0);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SelectInfoFieldTypeTemplateNode.class);

	private final InfoFieldValue<?> _infoFieldValue;
	private final ThemeDisplay _themeDisplay;

}