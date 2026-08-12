/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.constants;

/**
 * Shared because the bundle that writes the signature and the bundle that
 * aggregates it are not the same, and a name that only agrees by convention
 * compiles, deploys and returns no clusters at all.
 *
 * @author Mikel Lorza
 */
public class SimilarityConstants {

	public static final String FIELD_NAME_TEXT_BANDS = "textSimilarityBands";

	public static final String FIELD_NAME_TEXT_SIGNATURE =
		"textSimilaritySignature";

}