/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.site.cms.site.initializer.constants.SimilarAssetConstants;

import jakarta.ws.rs.BadRequestException;

import java.util.Arrays;

/**
 * The dimension along which CMS content is compared. Each one knows the field
 * its assets are grouped by, how much evidence a set needs and whether it
 * reads differently in each translation, so a new dimension is a new constant
 * here.
 *
 * @author Mikel Lorza
 */
public enum SimilarAssetDimension {

	METADATA(SimilarAssetConstants.FIELD_NAME_METADATA) {

		/**
		 * A content carries a handful of categories and tags, so the overlap
		 * between two of them is coarse and high: sharing the one tag half the
		 * space carries already scores as similar, and grouping is transitive,
		 * so at three the whole space collapses into one set. Measured over
		 * 3,000 contents with a realistic tag distribution, the largest set
		 * falls from 2,782 assets at three to 150 at twelve, which is where a
		 * set means the metadata is genuinely shared.
		 */
		@Override
		public int getMinSharedSimilarAssets() {
			return 12;
		}

		/**
		 * A category is the same category in every translation, so the whole
		 * space is compared at once and a set can hold content written in
		 * different languages.
		 */
		@Override
		public String getTokenLanguageId(String languageId) {
			return SimilarAssetConstants.TOKEN_LANGUAGE_ID_ALL;
		}

	},
	TEXT(SimilarAssetConstants.FIELD_NAME_TEXT),
	TITLE(SimilarAssetConstants.FIELD_NAME_TITLE);

	/**
	 * @throws BadRequestException if the value names no dimension, so a client
	 *         typo is not answered with an empty result indistinguishable from
	 *         "no duplicates"
	 */
	public static SimilarAssetDimension get(String dimension) {
		if (dimension == null) {
			return TEXT;
		}

		for (SimilarAssetDimension similarAssetDimension : values()) {
			if (dimension.equals(similarAssetDimension.name())) {
				return similarAssetDimension;
			}
		}

		throw new BadRequestException(
			StringBundler.concat(
				"Unknown similar asset dimension \"", dimension,
				"\". Valid values are ", Arrays.toString(values())));
	}

	public String getFieldName() {
		return _fieldName;
	}

	/**
	 * Returns how many tokens two assets have to share before they are
	 * grouped. Three is enough wherever the elements are fine grained enough
	 * that two unrelated assets almost never collide.
	 */
	public int getMinSharedSimilarAssets() {
		return 3;
	}

	/**
	 * Returns the language whose tokens the dimension is read from, which is
	 * the request's language for everything that reads differently in each
	 * translation.
	 */
	public String getTokenLanguageId(String languageId) {
		return languageId;
	}

	private SimilarAssetDimension(String fieldName) {
		_fieldName = fieldName;
	}

	private final String _fieldName;

}