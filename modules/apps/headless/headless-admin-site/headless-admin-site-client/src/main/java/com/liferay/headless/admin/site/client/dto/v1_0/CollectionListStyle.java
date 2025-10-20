/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.client.dto.v1_0;

import com.liferay.headless.admin.site.client.function.UnsafeSupplier;
import com.liferay.headless.admin.site.client.serdes.v1_0.CollectionListStyleSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Rubén Pulido
 * @generated
 */
@Generated("")
public abstract class CollectionListStyle implements Cloneable, Serializable {

	public static CollectionListStyle toDTO(String json) {
		return CollectionListStyleSerDes.toDTO(json);
	}

	public CollectionListStyleType getCollectionListStyleType() {
		return collectionListStyleType;
	}

	public String getCollectionListStyleTypeAsString() {
		if (collectionListStyleType == null) {
			return null;
		}

		return collectionListStyleType.toString();
	}

	public void setCollectionListStyleType(
		CollectionListStyleType collectionListStyleType) {

		this.collectionListStyleType = collectionListStyleType;
	}

	public void setCollectionListStyleType(
		UnsafeSupplier<CollectionListStyleType, Exception>
			collectionListStyleTypeUnsafeSupplier) {

		try {
			collectionListStyleType =
				collectionListStyleTypeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected CollectionListStyleType collectionListStyleType;

	@Override
	public CollectionListStyle clone() throws CloneNotSupportedException {
		return (CollectionListStyle)super.clone();
	}

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
		return CollectionListStyleSerDes.toJSON(this);
	}

	public static enum CollectionListStyleType {

		LIST_STYLE("ListStyle"), TEMPLATE("Template");

		public static CollectionListStyleType create(String value) {
			for (CollectionListStyleType collectionListStyleType : values()) {
				if (Objects.equals(collectionListStyleType.getValue(), value) ||
					Objects.equals(collectionListStyleType.name(), value)) {

					return collectionListStyleType;
				}
			}

			return null;
		}

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

}