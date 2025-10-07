/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.client.dto.v1_0;

import com.liferay.headless.admin.site.client.function.UnsafeSupplier;
import com.liferay.headless.admin.site.client.serdes.v1_0.SiteNavigationSettingsSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Rubén Pulido
 * @generated
 */
@Generated("")
public class SiteNavigationSettings implements Cloneable, Serializable {

	public static SiteNavigationSettings toDTO(String json) {
		return SiteNavigationSettingsSerDes.toDTO(json);
	}

	public String getTest1() {
		return test1;
	}

	public void setTest1(String test1) {
		this.test1 = test1;
	}

	public void setTest1(
		UnsafeSupplier<String, Exception> test1UnsafeSupplier) {

		try {
			test1 = test1UnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String test1;

	@Override
	public SiteNavigationSettings clone() throws CloneNotSupportedException {
		return (SiteNavigationSettings)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof SiteNavigationSettings)) {
			return false;
		}

		SiteNavigationSettings siteNavigationSettings =
			(SiteNavigationSettings)object;

		return Objects.equals(toString(), siteNavigationSettings.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return SiteNavigationSettingsSerDes.toJSON(this);
	}

}