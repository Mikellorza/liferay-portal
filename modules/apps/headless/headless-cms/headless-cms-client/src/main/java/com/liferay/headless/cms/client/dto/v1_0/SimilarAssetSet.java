/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.client.dto.v1_0;

import com.liferay.headless.cms.client.function.UnsafeSupplier;
import com.liferay.headless.cms.client.serdes.v1_0.SimilarAssetSetSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Crescenzo Rega
 * @generated
 */
@Generated("")
public class SimilarAssetSet implements Cloneable, Serializable {

	public static SimilarAssetSet toDTO(String json) {
		return SimilarAssetSetSerDes.toDTO(json);
	}

	public SimilarAsset[] getSimilarAssets() {
		return similarAssets;
	}

	public void setSimilarAssets(SimilarAsset[] similarAssets) {
		this.similarAssets = similarAssets;
	}

	public void setSimilarAssets(
		UnsafeSupplier<SimilarAsset[], Exception> similarAssetsUnsafeSupplier) {

		try {
			similarAssets = similarAssetsUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected SimilarAsset[] similarAssets;

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public void setSize(UnsafeSupplier<Integer, Exception> sizeUnsafeSupplier) {
		try {
			size = sizeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Integer size;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTitle(
		UnsafeSupplier<String, Exception> titleUnsafeSupplier) {

		try {
			title = titleUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String title;

	@Override
	public SimilarAssetSet clone() throws CloneNotSupportedException {
		return (SimilarAssetSet)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof SimilarAssetSet)) {
			return false;
		}

		SimilarAssetSet similarAssetSet = (SimilarAssetSet)object;

		return Objects.equals(toString(), similarAssetSet.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return SimilarAssetSetSerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:1356868600