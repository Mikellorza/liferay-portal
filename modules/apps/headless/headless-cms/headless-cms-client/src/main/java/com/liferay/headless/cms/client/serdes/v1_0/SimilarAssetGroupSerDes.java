/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.client.serdes.v1_0;

import com.liferay.headless.cms.client.dto.v1_0.SimilarAsset;
import com.liferay.headless.cms.client.dto.v1_0.SimilarAssetGroup;
import com.liferay.headless.cms.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Crescenzo Rega
 * @generated
 */
@Generated("")
public class SimilarAssetGroupSerDes {

	public static SimilarAssetGroup toDTO(String json) {
		SimilarAssetGroupJSONParser similarAssetGroupJSONParser =
			new SimilarAssetGroupJSONParser();

		return similarAssetGroupJSONParser.parseToDTO(json);
	}

	public static SimilarAssetGroup[] toDTOs(String json) {
		SimilarAssetGroupJSONParser similarAssetGroupJSONParser =
			new SimilarAssetGroupJSONParser();

		return similarAssetGroupJSONParser.parseToDTOs(json);
	}

	public static String toJSON(SimilarAssetGroup similarAssetGroup) {
		if (similarAssetGroup == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (similarAssetGroup.getSimilarAssets() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"similarAssets\": ");

			sb.append("[");

			for (int i = 0; i < similarAssetGroup.getSimilarAssets().length;
				 i++) {

				sb.append(
					String.valueOf(similarAssetGroup.getSimilarAssets()[i]));

				if ((i + 1) < similarAssetGroup.getSimilarAssets().length) {
					sb.append(", ");
				}
			}

			sb.append("]");
		}

		if (similarAssetGroup.getSize() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"size\": ");

			sb.append(similarAssetGroup.getSize());
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		SimilarAssetGroupJSONParser similarAssetGroupJSONParser =
			new SimilarAssetGroupJSONParser();

		return similarAssetGroupJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		SimilarAssetGroup similarAssetGroup) {

		if (similarAssetGroup == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (similarAssetGroup.getSimilarAssets() == null) {
			map.put("similarAssets", null);
		}
		else {
			map.put(
				"similarAssets",
				String.valueOf(similarAssetGroup.getSimilarAssets()));
		}

		if (similarAssetGroup.getSize() == null) {
			map.put("size", null);
		}
		else {
			map.put("size", String.valueOf(similarAssetGroup.getSize()));
		}

		return map;
	}

	public static class SimilarAssetGroupJSONParser
		extends BaseJSONParser<SimilarAssetGroup> {

		@Override
		protected SimilarAssetGroup createDTO() {
			return new SimilarAssetGroup();
		}

		@Override
		protected SimilarAssetGroup[] createDTOArray(int size) {
			return new SimilarAssetGroup[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "similarAssets")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "size")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			SimilarAssetGroup similarAssetGroup, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "similarAssets")) {
				if (jsonParserFieldValue != null) {
					Object[] jsonParserFieldValues =
						(Object[])jsonParserFieldValue;

					SimilarAsset[] similarAssetsArray =
						new SimilarAsset[jsonParserFieldValues.length];

					for (int i = 0; i < similarAssetsArray.length; i++) {
						similarAssetsArray[i] = SimilarAssetSerDes.toDTO(
							(String)jsonParserFieldValues[i]);
					}

					similarAssetGroup.setSimilarAssets(similarAssetsArray);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "size")) {
				if (jsonParserFieldValue != null) {
					similarAssetGroup.setSize(
						Integer.valueOf((String)jsonParserFieldValue));
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
// LIFERAY-REST-BUILDER-HASH:1872620494