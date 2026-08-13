/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.similarity;

import com.liferay.petra.string.StringBundler;

import jakarta.ws.rs.BadRequestException;

import java.util.Arrays;
import java.util.List;

/**
 * @author Mikel Lorza
 */
public enum SimilarityDimension {

	TEXT("textSimilarityKeys", "textSimilaritySignature") {

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

		throw new BadRequestException(
			StringBundler.concat(
				"Unknown similarity dimension \"", dimension,
				"\". Valid values are ", Arrays.toString(values())));
	}

	public String getSignatureField() {
		return _signatureField;
	}

	public String getSimilarityKeyField() {
		return _similarityKeyField;
	}

	public abstract String getTitle(List<String> titles, String topTitle);

	private SimilarityDimension(
		String similarityKeyField, String signatureField) {

		_similarityKeyField = similarityKeyField;
		_signatureField = signatureField;
	}

	private final String _signatureField;
	private final String _similarityKeyField;

}