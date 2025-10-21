/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.dto.v1_0;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;

import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;
import com.liferay.portal.vulcan.util.ObjectMapperUtil;

import jakarta.annotation.Generated;

import jakarta.validation.Valid;

import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Rubén Pulido
 * @generated
 */
@Generated("")
@GraphQLName(
	description = "The page collection's list style.",
	value = "CollectionListStyle"
)
@JsonFilter("Liferay.Vulcan")
@JsonSubTypes(
	{
		@JsonSubTypes.Type(name = "ListStyle", value = ListStyle.class),
		@JsonSubTypes.Type(name = "Template", value = TemplateListStyle.class)
	}
)
@JsonTypeInfo(
	include = JsonTypeInfo.As.PROPERTY, property = "collectionListStyleType",
	use = JsonTypeInfo.Id.NAME, visible = true
)
@XmlRootElement(name = "CollectionListStyle")
public abstract class CollectionListStyle implements Serializable {

	public static CollectionListStyle toDTO(String json) {
		return ObjectMapperUtil.readValue(CollectionListStyle.class, json);
	}

	public static CollectionListStyle unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(
			CollectionListStyle.class, json);
	}

	@io.swagger.v3.oas.annotations.media.Schema(
		description = "The collection's list style (ListStyle, Template)."
	)
	@JsonGetter("collectionListStyleType")
	@Valid
	public CollectionListStyleType getCollectionListStyleType() {
		if (_collectionListStyleTypeSupplier != null) {
			collectionListStyleType = _collectionListStyleTypeSupplier.get();

			_collectionListStyleTypeSupplier = null;
		}

		return collectionListStyleType;
	}

	@JsonIgnore
	public String getCollectionListStyleTypeAsString() {
		CollectionListStyleType collectionListStyleType =
			getCollectionListStyleType();

		if (collectionListStyleType == null) {
			return null;
		}

		return collectionListStyleType.toString();
	}

	public void setCollectionListStyleType(
		CollectionListStyleType collectionListStyleType) {

		this.collectionListStyleType = collectionListStyleType;

		_collectionListStyleTypeSupplier = null;
	}

	@JsonIgnore
	public void setCollectionListStyleType(
		UnsafeSupplier<CollectionListStyleType, Exception>
			collectionListStyleTypeUnsafeSupplier) {

		_collectionListStyleTypeSupplier = () -> {
			try {
				return collectionListStyleTypeUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(
		description = "The collection's list style (ListStyle, Template)."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected CollectionListStyleType collectionListStyleType;

	@JsonIgnore
	private Supplier<CollectionListStyleType> _collectionListStyleTypeSupplier;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof CollectionListStyle)) {
			return false;
		}

		CollectionListStyle collectionListStyle = (CollectionListStyle)object;

		return Objects.equals(toString(), collectionListStyle.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		StringBundler sb = new StringBundler();

		sb.append("{");

		CollectionListStyleType collectionListStyleType =
			getCollectionListStyleType();

		if (collectionListStyleType != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"collectionListStyleType\": ");

			sb.append("\"");

			sb.append(collectionListStyleType);

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	@io.swagger.v3.oas.annotations.media.Schema(
		accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY,
		defaultValue = "com.liferay.headless.admin.site.dto.v1_0.CollectionListStyle",
		name = "x-class-name"
	)
	public String xClassName;

	@GraphQLName("CollectionListStyleType")
	public static enum CollectionListStyleType {

		LIST_STYLE("ListStyle"), TEMPLATE("Template");

		@JsonCreator
		public static CollectionListStyleType create(String value) {
			if ((value == null) || value.equals("")) {
				return null;
			}

			for (CollectionListStyleType collectionListStyleType : values()) {
				if (Objects.equals(collectionListStyleType.getValue(), value)) {
					return collectionListStyleType;
				}
			}

			throw new IllegalArgumentException("Invalid enum value: " + value);
		}

		@JsonValue
		public String getValue() {
			return _value;
		}

		@Override
		public String toString() {
			return _value;
		}

		private CollectionListStyleType(String value) {
			_value = value;
		}

		private final String _value;

	}

	private static String _escape(Object object) {
		return StringUtil.replace(
			String.valueOf(object), _JSON_ESCAPE_STRINGS[0],
			_JSON_ESCAPE_STRINGS[1]);
	}

	private static boolean _isArray(Object value) {
		if (value == null) {
			return false;
		}

		Class<?> clazz = value.getClass();

		return clazz.isArray();
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
			sb.append(_escape(entry.getKey()));
			sb.append("\": ");

			Object value = entry.getValue();

			if (_isArray(value)) {
				sb.append("[");

				Object[] valueArray = (Object[])value;

				for (int i = 0; i < valueArray.length; i++) {
					if (valueArray[i] instanceof Map) {
						sb.append(_toJSON((Map<String, ?>)valueArray[i]));
					}
					else if (valueArray[i] instanceof String) {
						sb.append("\"");
						sb.append(valueArray[i]);
						sb.append("\"");
					}
					else {
						sb.append(valueArray[i]);
					}

					if ((i + 1) < valueArray.length) {
						sb.append(", ");
					}
				}

				sb.append("]");
			}
			else if (value instanceof Map) {
				sb.append(_toJSON((Map<String, ?>)value));
			}
			else if (value instanceof String) {
				sb.append("\"");
				sb.append(_escape(value));
				sb.append("\"");
			}
			else {
				sb.append(value);
			}

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static final String[][] _JSON_ESCAPE_STRINGS = {
		{"\\", "\"", "\b", "\f", "\n", "\r", "\t"},
		{"\\\\", "\\\"", "\\b", "\\f", "\\n", "\\r", "\\t"}
	};

	private Map<String, Serializable> _extendedProperties;

}