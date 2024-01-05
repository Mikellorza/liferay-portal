/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.friendly.url.web.internal.display.context;

import com.liferay.friendly.url.configuration.manager.FriendlyURLSeparatorConfigurationManager;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.FriendlyURLResolver;
import com.liferay.portal.kernel.portlet.FriendlyURLResolverRegistryUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author Mikel Lorza
 */
public class FriendlyURLSeparatorCompanyConfigurationDisplayContext {

	public FriendlyURLSeparatorCompanyConfigurationDisplayContext(
		FriendlyURLSeparatorConfigurationManager
			friendlyURLSeparatorConfigurationManager,
		JSONFactory jsonFactory, Language language, ThemeDisplay themeDisplay) {

		_friendlyURLSeparatorConfigurationManager =
			friendlyURLSeparatorConfigurationManager;
		_jsonFactory = jsonFactory;
		_language = language;
		_themeDisplay = themeDisplay;
	}

	public List<FriendlyURLSeparator> getConfigurableFriendlyURLSeparators() {
		List<FriendlyURLSeparator> friendlyURLSeparators = new ArrayList<>();

		JSONArray configuredURLSeparatorsJSONArray =
			_getConfiguredURLSeparatorsJSONArray(_themeDisplay.getCompanyId());

		for (FriendlyURLResolver friendlyURLResolver :
				FriendlyURLResolverRegistryUtil.
					getFriendlyURLResolversAsCollection()) {

			if (!friendlyURLResolver.isURLSeparatorConfigurable() ||
				StringPool.BLANK.equals(friendlyURLResolver.getType())) {

				continue;
			}

			String name = friendlyURLResolver.getType() + "-url-separator";

			friendlyURLSeparators.add(
				new FriendlyURLSeparator(
					name, _language.get(_themeDisplay.getLocale(), name),
					_getURLSeparator(
						friendlyURLResolver.getDefaultURLSeparator(),
						configuredURLSeparatorsJSONArray,
						friendlyURLResolver.getType())));
		}

		Collections.sort(
			friendlyURLSeparators,
			Comparator.comparing(FriendlyURLSeparator::getLabel));

		return friendlyURLSeparators;
	}

	public class FriendlyURLSeparator {

		public FriendlyURLSeparator(String name, String label, String value) {
			_name = name;
			_label = label;
			_value = value;
		}

		public String getLabel() {
			return _label;
		}

		public String getName() {
			return _name;
		}

		public String getValue() {
			return _value;
		}

		private final String _label;
		private final String _name;
		private final String _value;

	}

	private JSONArray _getConfiguredURLSeparatorsJSONArray(long companyId) {
		try {
			String urlSeparators =
				_friendlyURLSeparatorConfigurationManager.getURLSeparators(
					companyId);

			if (!Validator.isBlank(urlSeparators)) {
				return _jsonFactory.createJSONArray(urlSeparators);
			}
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}

		return _jsonFactory.createJSONArray();
	}

	private String _getURLSeparator(
		String defaultURLSeparator, JSONArray configuredURLSeparatorsJSONArray,
		String type) {

		if (JSONUtil.isEmpty(configuredURLSeparatorsJSONArray)) {
			return defaultURLSeparator;
		}

		for (int i = 0; i < configuredURLSeparatorsJSONArray.length(); i++) {
			JSONObject jsonObject =
				configuredURLSeparatorsJSONArray.getJSONObject(i);

			if (Objects.equals(type, jsonObject.get("type"))) {
				return jsonObject.getString("urlSeparator");
			}
		}

		return StringPool.BLANK;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FriendlyURLSeparatorCompanyConfigurationDisplayContext.class.getName());

	private final FriendlyURLSeparatorConfigurationManager
		_friendlyURLSeparatorConfigurationManager;
	private final JSONFactory _jsonFactory;
	private final Language _language;
	private final ThemeDisplay _themeDisplay;

}