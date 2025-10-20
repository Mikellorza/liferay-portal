/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.client.serdes.v1_0;

import com.liferay.headless.admin.site.client.dto.v1_0.CollectionPageElementDefinition;
import com.liferay.headless.admin.site.client.dto.v1_0.CollectionViewport;
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
public class CollectionPageElementDefinitionSerDes {

	public static CollectionPageElementDefinition toDTO(String json) {
		CollectionPageElementDefinitionJSONParser
			collectionPageElementDefinitionJSONParser =
				new CollectionPageElementDefinitionJSONParser();

		return collectionPageElementDefinitionJSONParser.parseToDTO(json);
	}

	public static CollectionPageElementDefinition[] toDTOs(String json) {
		CollectionPageElementDefinitionJSONParser
			collectionPageElementDefinitionJSONParser =
				new CollectionPageElementDefinitionJSONParser();

		return collectionPageElementDefinitionJSONParser.parseToDTOs(json);
	}

	public static String toJSON(
		CollectionPageElementDefinition collectionPageElementDefinition) {

		if (collectionPageElementDefinition == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (collectionPageElementDefinition.getCollectionListStyle() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"collectionListStyle\": ");

			sb.append(
				String.valueOf(
					collectionPageElementDefinition.getCollectionListStyle()));
		}

		if (collectionPageElementDefinition.getCollectionReference() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"collectionReference\": ");

			sb.append(
				String.valueOf(
					collectionPageElementDefinition.getCollectionReference()));
		}

		if (collectionPageElementDefinition.getCollectionViewports() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"collectionViewports\": ");

			sb.append("[");

			for (int i = 0;
				 i < collectionPageElementDefinition.
					 getCollectionViewports().length;
				 i++) {

				sb.append(
					String.valueOf(
						collectionPageElementDefinition.getCollectionViewports()
							[i]));

				if ((i + 1) < collectionPageElementDefinition.
						getCollectionViewports().length) {

					sb.append(", ");
				}
			}

			sb.append("]");
		}

		if (collectionPageElementDefinition.getDisplayAllItems() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"displayAllItems\": ");

			sb.append(collectionPageElementDefinition.getDisplayAllItems());
		}

		if (collectionPageElementDefinition.getDisplayAllPages() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"displayAllPages\": ");

			sb.append(collectionPageElementDefinition.getDisplayAllPages());
		}

		if (collectionPageElementDefinition.getEmptyCollectionConfig() !=
				null) {

			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"emptyCollectionConfig\": ");

			sb.append(
				String.valueOf(
					collectionPageElementDefinition.
						getEmptyCollectionConfig()));
		}

		if (collectionPageElementDefinition.getHidden() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"hidden\": ");

			sb.append(collectionPageElementDefinition.getHidden());
		}

		if (collectionPageElementDefinition.getName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"name\": ");

			sb.append("\"");

			sb.append(_escape(collectionPageElementDefinition.getName()));

			sb.append("\"");
		}

		if (collectionPageElementDefinition.getNumberOfItems() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"numberOfItems\": ");

			sb.append(collectionPageElementDefinition.getNumberOfItems());
		}

		if (collectionPageElementDefinition.getNumberOfItemsPerPage() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"numberOfItemsPerPage\": ");

			sb.append(
				collectionPageElementDefinition.getNumberOfItemsPerPage());
		}

		if (collectionPageElementDefinition.getNumberOfPages() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"numberOfPages\": ");

			sb.append(collectionPageElementDefinition.getNumberOfPages());
		}

		if (collectionPageElementDefinition.getPaginationType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"paginationType\": ");

			sb.append("\"");

			sb.append(collectionPageElementDefinition.getPaginationType());

			sb.append("\"");
		}

		if (collectionPageElementDefinition.getType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"type\": ");

			sb.append("\"");

			sb.append(collectionPageElementDefinition.getType());

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		CollectionPageElementDefinitionJSONParser
			collectionPageElementDefinitionJSONParser =
				new CollectionPageElementDefinitionJSONParser();

		return collectionPageElementDefinitionJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		CollectionPageElementDefinition collectionPageElementDefinition) {

		if (collectionPageElementDefinition == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (collectionPageElementDefinition.getCollectionListStyle() == null) {
			map.put("collectionListStyle", null);
		}
		else {
			map.put(
				"collectionListStyle",
				String.valueOf(
					collectionPageElementDefinition.getCollectionListStyle()));
		}

		if (collectionPageElementDefinition.getCollectionReference() == null) {
			map.put("collectionReference", null);
		}
		else {
			map.put(
				"collectionReference",
				String.valueOf(
					collectionPageElementDefinition.getCollectionReference()));
		}

		if (collectionPageElementDefinition.getCollectionViewports() == null) {
			map.put("collectionViewports", null);
		}
		else {
			map.put(
				"collectionViewports",
				String.valueOf(
					collectionPageElementDefinition.getCollectionViewports()));
		}

		if (collectionPageElementDefinition.getDisplayAllItems() == null) {
			map.put("displayAllItems", null);
		}
		else {
			map.put(
				"displayAllItems",
				String.valueOf(
					collectionPageElementDefinition.getDisplayAllItems()));
		}

		if (collectionPageElementDefinition.getDisplayAllPages() == null) {
			map.put("displayAllPages", null);
		}
		else {
			map.put(
				"displayAllPages",
				String.valueOf(
					collectionPageElementDefinition.getDisplayAllPages()));
		}

		if (collectionPageElementDefinition.getEmptyCollectionConfig() ==
				null) {

			map.put("emptyCollectionConfig", null);
		}
		else {
			map.put(
				"emptyCollectionConfig",
				String.valueOf(
					collectionPageElementDefinition.
						getEmptyCollectionConfig()));
		}

		if (collectionPageElementDefinition.getHidden() == null) {
			map.put("hidden", null);
		}
		else {
			map.put(
				"hidden",
				String.valueOf(collectionPageElementDefinition.getHidden()));
		}

		if (collectionPageElementDefinition.getName() == null) {
			map.put("name", null);
		}
		else {
			map.put(
				"name",
				String.valueOf(collectionPageElementDefinition.getName()));
		}

		if (collectionPageElementDefinition.getNumberOfItems() == null) {
			map.put("numberOfItems", null);
		}
		else {
			map.put(
				"numberOfItems",
				String.valueOf(
					collectionPageElementDefinition.getNumberOfItems()));
		}

		if (collectionPageElementDefinition.getNumberOfItemsPerPage() == null) {
			map.put("numberOfItemsPerPage", null);
		}
		else {
			map.put(
				"numberOfItemsPerPage",
				String.valueOf(
					collectionPageElementDefinition.getNumberOfItemsPerPage()));
		}

		if (collectionPageElementDefinition.getNumberOfPages() == null) {
			map.put("numberOfPages", null);
		}
		else {
			map.put(
				"numberOfPages",
				String.valueOf(
					collectionPageElementDefinition.getNumberOfPages()));
		}

		if (collectionPageElementDefinition.getPaginationType() == null) {
			map.put("paginationType", null);
		}
		else {
			map.put(
				"paginationType",
				String.valueOf(
					collectionPageElementDefinition.getPaginationType()));
		}

		if (collectionPageElementDefinition.getType() == null) {
			map.put("type", null);
		}
		else {
			map.put(
				"type",
				String.valueOf(collectionPageElementDefinition.getType()));
		}

		return map;
	}

	public static class CollectionPageElementDefinitionJSONParser
		extends BaseJSONParser<CollectionPageElementDefinition> {

		@Override
		protected CollectionPageElementDefinition createDTO() {
			return new CollectionPageElementDefinition();
		}

		@Override
		protected CollectionPageElementDefinition[] createDTOArray(int size) {
			return new CollectionPageElementDefinition[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "collectionListStyle")) {
				return false;
			}
			else if (Objects.equals(
						jsonParserFieldName, "collectionReference")) {

				return false;
			}
			else if (Objects.equals(
						jsonParserFieldName, "collectionViewports")) {

				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "displayAllItems")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "displayAllPages")) {
				return false;
			}
			else if (Objects.equals(
						jsonParserFieldName, "emptyCollectionConfig")) {

				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "hidden")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "name")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "numberOfItems")) {
				return false;
			}
			else if (Objects.equals(
						jsonParserFieldName, "numberOfItemsPerPage")) {

				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "numberOfPages")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "paginationType")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "type")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			CollectionPageElementDefinition collectionPageElementDefinition,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "collectionListStyle")) {
				if (jsonParserFieldValue != null) {
					collectionPageElementDefinition.setCollectionListStyle(
						CollectionListStyleSerDes.toDTO(
							(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "collectionReference")) {

				if (jsonParserFieldValue != null) {
					collectionPageElementDefinition.setCollectionReference(
						CollectionReferenceSerDes.toDTO(
							(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "collectionViewports")) {

				if (jsonParserFieldValue != null) {
					Object[] jsonParserFieldValues =
						(Object[])jsonParserFieldValue;

					CollectionViewport[] collectionViewportsArray =
						new CollectionViewport[jsonParserFieldValues.length];

					for (int i = 0; i < collectionViewportsArray.length; i++) {
						collectionViewportsArray[i] =
							CollectionViewportSerDes.toDTO(
								(String)jsonParserFieldValues[i]);
					}

					collectionPageElementDefinition.setCollectionViewports(
						collectionViewportsArray);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "displayAllItems")) {
				if (jsonParserFieldValue != null) {
					collectionPageElementDefinition.setDisplayAllItems(
						(Boolean)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "displayAllPages")) {
				if (jsonParserFieldValue != null) {
					collectionPageElementDefinition.setDisplayAllPages(
						(Boolean)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "emptyCollectionConfig")) {

				if (jsonParserFieldValue != null) {
					collectionPageElementDefinition.setEmptyCollectionConfig(
						EmptyCollectionConfigSerDes.toDTO(
							(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "hidden")) {
				if (jsonParserFieldValue != null) {
					collectionPageElementDefinition.setHidden(
						(Boolean)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "name")) {
				if (jsonParserFieldValue != null) {
					collectionPageElementDefinition.setName(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "numberOfItems")) {
				if (jsonParserFieldValue != null) {
					collectionPageElementDefinition.setNumberOfItems(
						Integer.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "numberOfItemsPerPage")) {

				if (jsonParserFieldValue != null) {
					collectionPageElementDefinition.setNumberOfItemsPerPage(
						Integer.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "numberOfPages")) {
				if (jsonParserFieldValue != null) {
					collectionPageElementDefinition.setNumberOfPages(
						Integer.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "paginationType")) {
				if (jsonParserFieldValue != null) {
					collectionPageElementDefinition.setPaginationType(
						CollectionPageElementDefinition.PaginationType.create(
							(String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "type")) {
				if (jsonParserFieldValue != null) {
					collectionPageElementDefinition.setType(
						CollectionPageElementDefinition.Type.create(
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