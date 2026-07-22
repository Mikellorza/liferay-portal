/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.similarity;

import java.util.List;

/**
 * The dimension along which CMS content is compared. Each dimension knows the
 * indexed fields its assets are grouped by and how a resulting cluster is
 * named, so that a new dimension is added by adding a constant here.
 *
 * <p>
 * Only {@link #TEXT} is available. The title and metadata dimensions require
 * their own indexed signature fields, which do not exist yet.
 * </p>
 *
 * @author Mikel Lorza
 */
public enum SimilarityDimension {

	TEXT("textSimilarityBands", "textSimilaritySignature") {

		@Override
		public String getTitle(List<String> titles, String topTitle) {
			return SimilarityClusterTitleUtil.getTitle(titles, topTitle);
		}

	};

	public static SimilarityDimension get(String dimension) {
		if (dimension == null) {
			return TEXT;
		}

		for (SimilarityDimension similarityDimension : values()) {
			if (dimension.equals(similarityDimension.name())) {
				return similarityDimension;
			}
		}

		return null;
	}

	public String getBandField() {
		return _bandField;
	}

	public String getSignatureField() {
		return _signatureField;
	}

	public abstract String getTitle(List<String> titles, String topTitle);

	private SimilarityDimension(String bandField, String signatureField) {
		_bandField = bandField;
		_signatureField = signatureField;
	}

	private final String _bandField;
	private final String _signatureField;

}