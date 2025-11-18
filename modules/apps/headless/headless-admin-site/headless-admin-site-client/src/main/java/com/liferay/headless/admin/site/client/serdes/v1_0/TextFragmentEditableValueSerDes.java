/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.client.serdes.v1_0;

import com.liferay.headless.admin.site.client.dto.v1_0.TextFragmentEditableValue;
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
public class TextFragmentEditableValueSerDes {

	public static TextFragmentEditableValue toDTO(String json) {
		TextFragmentEditableValueJSONParser
			textFragmentEditableValueJSONParser =
				new TextFragmentEditableValueJSONParser();

		return textFragmentEditableValueJSONParser.parseToDTO(json);
	}

	public static TextFragmentEditableValue[] toDTOs(String json) {
		TextFragmentEditableValueJSONParser
			textFragmentEditableValueJSONParser =
				new TextFragmentEditableValueJSONParser();

		return textFragmentEditableValueJSONParser.parseToDTOs(json);
	}

	public static String toJSON(
		TextFragmentEditableValue textFragmentEditableValue) {

		if (textFragmentEditableValue == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (textFragmentEditableValue.getDefaultValue() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"defaultValue\": ");

			sb.append("\"");

			sb.append(_escape(textFragmentEditableValue.getDefaultValue()));

			sb.append("\"");
		}

		if (textFragmentEditableValue.getFragmentEditableValue() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"fragmentEditableValue\": ");

			sb.append(
				String.valueOf(
					textFragmentEditableValue.getFragmentEditableValue()));
		}

		if (textFragmentEditableValue.getType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"type\": ");

			sb.append("\"");
			sb.append(textFragmentEditableValue.getType());
			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		TextFragmentEditableValueJSONParser
			textFragmentEditableValueJSONParser =
				new TextFragmentEditableValueJSONParser();

		return textFragmentEditableValueJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		TextFragmentEditableValue textFragmentEditableValue) {

		if (textFragmentEditableValue == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (textFragmentEditableValue.getDefaultValue() == null) {
			map.put("defaultValue", null);
		}
		else {
			map.put(
				"defaultValue",
				String.valueOf(textFragmentEditableValue.getDefaultValue()));
		}

		if (textFragmentEditableValue.getFragmentEditableValue() == null) {
			map.put("fragmentEditableValue", null);
		}
		else {
			map.put(
				"fragmentEditableValue",
				String.valueOf(
					textFragmentEditableValue.getFragmentEditableValue()));
		}

		if (textFragmentEditableValue.getType() == null) {
			map.put("type", null);
		}
		else {
			map.put(
				"type", String.valueOf(textFragmentEditableValue.getType()));
		}

		return map;
	}

	public static class TextFragmentEditableValueJSONParser
		extends BaseJSONParser<TextFragmentEditableValue> {

		@Override
		protected TextFragmentEditableValue createDTO() {
			return new TextFragmentEditableValue();
		}

		@Override
		protected TextFragmentEditableValue[] createDTOArray(int size) {
			return new TextFragmentEditableValue[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "defaultValue")) {
				return false;
			}
			else if (Objects.equals(
						jsonParserFieldName, "fragmentEditableValue")) {

				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "type")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			TextFragmentEditableValue textFragmentEditableValue,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "defaultValue")) {
				if (jsonParserFieldValue != null) {
					textFragmentEditableValue.setDefaultValue(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "fragmentEditableValue")) {

				if (jsonParserFieldValue != null) {
					textFragmentEditableValue.setFragmentEditableValue(
						FragmentEditableValueSerDes.toDTO(
							(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "type")) {
				if (jsonParserFieldValue != null) {
					textFragmentEditableValue.setType(
						TextFragmentEditableValue.Type.create(
							(String)jsonParserFieldValue));
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