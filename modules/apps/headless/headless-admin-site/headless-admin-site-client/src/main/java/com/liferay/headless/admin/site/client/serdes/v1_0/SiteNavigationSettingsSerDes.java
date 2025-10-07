/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.client.serdes.v1_0;

import com.liferay.headless.admin.site.client.dto.v1_0.SiteNavigationSettings;
import com.liferay.headless.admin.site.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Rubén Pulido
 * @generated
 */
@Generated("")
public class SiteNavigationSettingsSerDes {

	public static SiteNavigationSettings toDTO(String json) {
		SiteNavigationSettingsJSONParser siteNavigationSettingsJSONParser =
			new SiteNavigationSettingsJSONParser();

		return siteNavigationSettingsJSONParser.parseToDTO(json);
	}

	public static SiteNavigationSettings[] toDTOs(String json) {
		SiteNavigationSettingsJSONParser siteNavigationSettingsJSONParser =
			new SiteNavigationSettingsJSONParser();

		return siteNavigationSettingsJSONParser.parseToDTOs(json);
	}

	public static String toJSON(SiteNavigationSettings siteNavigationSettings) {
		if (siteNavigationSettings == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (siteNavigationSettings.getTest1() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"test1\": ");

			sb.append("\"");

			sb.append(_escape(siteNavigationSettings.getTest1()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		SiteNavigationSettingsJSONParser siteNavigationSettingsJSONParser =
			new SiteNavigationSettingsJSONParser();

		return siteNavigationSettingsJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		SiteNavigationSettings siteNavigationSettings) {

		if (siteNavigationSettings == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (siteNavigationSettings.getTest1() == null) {
			map.put("test1", null);
		}
		else {
			map.put("test1", String.valueOf(siteNavigationSettings.getTest1()));
		}

		return map;
	}

	public static class SiteNavigationSettingsJSONParser
		extends BaseJSONParser<SiteNavigationSettings> {

		@Override
		protected SiteNavigationSettings createDTO() {
			return new SiteNavigationSettings();
		}

		@Override
		protected SiteNavigationSettings[] createDTOArray(int size) {
			return new SiteNavigationSettings[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "test1")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			SiteNavigationSettings siteNavigationSettings,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "test1")) {
				if (jsonParserFieldValue != null) {
					siteNavigationSettings.setTest1(
						(String)jsonParserFieldValue);
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			sb.append(_toJSON(value));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static String _toJSON(Object value) {
		if (value == null) {
			return "null";
		}

		if (value instanceof Map) {
			return _toJSON((Map)value);
		}

		Class<?> clazz = value.getClass();

		if (clazz.isArray()) {
			StringBuilder sb = new StringBuilder("[");

			Object[] values = (Object[])value;

			for (int i = 0; i < values.length; i++) {
				sb.append(_toJSON(values[i]));

				if ((i + 1) < values.length) {
					sb.append(", ");
				}
			}

			sb.append("]");

			return sb.toString();
		}

		if (value instanceof String) {
			return "\"" + _escape(value) + "\"";
		}

		return String.valueOf(value);
	}

}