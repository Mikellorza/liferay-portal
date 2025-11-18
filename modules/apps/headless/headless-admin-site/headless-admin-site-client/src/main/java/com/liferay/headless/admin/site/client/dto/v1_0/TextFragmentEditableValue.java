/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.client.dto.v1_0;

import com.liferay.headless.admin.site.client.function.UnsafeSupplier;
import com.liferay.headless.admin.site.client.serdes.v1_0.TextFragmentEditableValueSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Rubén Pulido
 * @generated
 */
@Generated("")
public class TextFragmentEditableValue implements Cloneable, Serializable {

	public static TextFragmentEditableValue toDTO(String json) {
		return TextFragmentEditableValueSerDes.toDTO(json);
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void setDefaultValue(
		UnsafeSupplier<String, Exception> defaultValueUnsafeSupplier) {

		try {
			defaultValue = defaultValueUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String defaultValue;

	public FragmentEditableValue getFragmentEditableValue() {
		return fragmentEditableValue;
	}

	public void setFragmentEditableValue(
		FragmentEditableValue fragmentEditableValue) {

		this.fragmentEditableValue = fragmentEditableValue;
	}

	public void setFragmentEditableValue(
		UnsafeSupplier<FragmentEditableValue, Exception>
			fragmentEditableValueUnsafeSupplier) {

		try {
			fragmentEditableValue = fragmentEditableValueUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected FragmentEditableValue fragmentEditableValue;

	public Type getType() {
		return type;
	}

	public String getTypeAsString() {
		if (type == null) {
			return null;
		}

		return type.toString();
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setType(UnsafeSupplier<Type, Exception> typeUnsafeSupplier) {
		try {
			type = typeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Type type;

	@Override
	public TextFragmentEditableValue clone() throws CloneNotSupportedException {
		return (TextFragmentEditableValue)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof TextFragmentEditableValue)) {
			return false;
		}

		TextFragmentEditableValue textFragmentEditableValue =
			(TextFragmentEditableValue)object;

		return Objects.equals(toString(), textFragmentEditableValue.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return TextFragmentEditableValueSerDes.toJSON(this);
	}

	public static enum Type {

		INLINE("Inline"), MAPPED("Mapped");

		public static Type create(String value) {
			for (Type type : values()) {
				if (Objects.equals(type.getValue(), value) ||
					Objects.equals(type.name(), value)) {

					return type;
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

		private Type(String value) {
			_value = value;
		}

		private final String _value;

	}

}