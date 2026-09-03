/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.client.dto.v1_0;

import com.liferay.headless.cms.client.function.UnsafeSupplier;
import com.liferay.headless.cms.client.serdes.v1_0.SimilarAssetSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Date;
import java.util.Objects;

/**
 * @author Crescenzo Rega
 * @generated
 */
@Generated("")
public class SimilarAsset implements Cloneable, Serializable {

	public static SimilarAsset toDTO(String json) {
		return SimilarAssetSerDes.toDTO(json);
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setContentType(
		UnsafeSupplier<String, Exception> contentTypeUnsafeSupplier) {

		try {
			contentType = contentTypeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String contentType;

	public Date getDateModified() {
		return dateModified;
	}

	public void setDateModified(Date dateModified) {
		this.dateModified = dateModified;
	}

	public void setDateModified(
		UnsafeSupplier<Date, Exception> dateModifiedUnsafeSupplier) {

		try {
			dateModified = dateModifiedUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Date dateModified;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setId(UnsafeSupplier<Long, Exception> idUnsafeSupplier) {
		try {
			id = idUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long id;

	public String getItemURL() {
		return itemURL;
	}

	public void setItemURL(String itemURL) {
		this.itemURL = itemURL;
	}

	public void setItemURL(
		UnsafeSupplier<String, Exception> itemURLUnsafeSupplier) {

		try {
			itemURL = itemURLUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String itemURL;

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
	public SimilarAsset clone() throws CloneNotSupportedException {
		return (SimilarAsset)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof SimilarAsset)) {
			return false;
		}

		SimilarAsset similarAsset = (SimilarAsset)object;

		return Objects.equals(toString(), similarAsset.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return SimilarAssetSerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:1534178113