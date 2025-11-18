/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.client.dto.v1_0;

import com.liferay.headless.admin.site.client.function.UnsafeSupplier;
import com.liferay.headless.admin.site.client.serdes.v1_0.FragmentEditableInlineFragmentValueSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Rubén Pulido
 * @generated
 */
@Generated("")
public class FragmentEditableInlineFragmentValue
	extends FragmentEditableValue implements Cloneable, Serializable {

	public static FragmentEditableInlineFragmentValue toDTO(String json) {
		return FragmentEditableInlineFragmentValueSerDes.toDTO(json);
	}

	public FragmentInlineValue getFragmentInlineValue() {
		return fragmentInlineValue;
	}

	public void setFragmentInlineValue(
		FragmentInlineValue fragmentInlineValue) {

		this.fragmentInlineValue = fragmentInlineValue;
	}

	public void setFragmentInlineValue(
		UnsafeSupplier<FragmentInlineValue, Exception>
			fragmentInlineValueUnsafeSupplier) {

		try {
			fragmentInlineValue = fragmentInlineValueUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected FragmentInlineValue fragmentInlineValue;

	@Override
	public FragmentEditableInlineFragmentValue clone()
		throws CloneNotSupportedException {

		return (FragmentEditableInlineFragmentValue)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof FragmentEditableInlineFragmentValue)) {
			return false;
		}

		FragmentEditableInlineFragmentValue
			fragmentEditableInlineFragmentValue =
				(FragmentEditableInlineFragmentValue)object;

		return Objects.equals(
			toString(), fragmentEditableInlineFragmentValue.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return FragmentEditableInlineFragmentValueSerDes.toJSON(this);
	}

}